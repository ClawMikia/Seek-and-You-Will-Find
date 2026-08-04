package com.christopher.bibleverse.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.alarmDataStore by preferencesDataStore(name = "alarm_prefs")

data class Reminder(
    val id: Long,
    val hour: Int,
    val minute: Int
)

/**
 * The app supports any number of recurring daily reminders. Each reminder has
 * a unique id, hour and minute; ids are persisted here so reminders survive
 * process death, reboots, and can be re-read whenever the user changes them.
 */
class AlarmPreferences(private val context: Context) {

    private object Keys {
        val REMINDER_IDS = stringSetPreferencesKey("reminder_ids")
        val NEXT_ID = longPreferencesKey("next_reminder_id")
        fun hour(id: Long) = intPreferencesKey("reminder_${id}_hour")
        fun minute(id: Long) = intPreferencesKey("reminder_${id}_minute")
    }

    val remindersFlow: Flow<List<Reminder>> = context.alarmDataStore.data.map { prefs ->
        (prefs[Keys.REMINDER_IDS] ?: emptySet())
            .mapNotNull { it.toLongOrNull() }
            .mapNotNull { id ->
                val hour = prefs[Keys.hour(id)] ?: return@mapNotNull null
                val minute = prefs[Keys.minute(id)] ?: return@mapNotNull null
                Reminder(id, hour, minute)
            }
            .sortedWith(compareBy<Reminder> { it.hour }.thenBy { it.minute })
    }

    suspend fun addReminder(hour: Int, minute: Int): Reminder {
        var result: Reminder? = null
        context.alarmDataStore.edit { prefs ->
            val id = prefs[Keys.NEXT_ID] ?: 1L
            prefs[Keys.NEXT_ID] = id + 1
            prefs[Keys.REMINDER_IDS] = (prefs[Keys.REMINDER_IDS] ?: emptySet()) + id.toString()
            prefs[Keys.hour(id)] = hour
            prefs[Keys.minute(id)] = minute
            result = Reminder(id, hour, minute)
        }
        return checkNotNull(result) { "Reminder could not be persisted" }
    }

    suspend fun removeReminder(id: Long) {
        context.alarmDataStore.edit { prefs ->
            val ids = prefs[Keys.REMINDER_IDS] ?: emptySet()
            prefs[Keys.REMINDER_IDS] = ids - id.toString()
            prefs.remove(Keys.hour(id))
            prefs.remove(Keys.minute(id))
        }
    }

    suspend fun updateReminder(id: Long, hour: Int, minute: Int) {
        context.alarmDataStore.edit { prefs ->
            prefs[Keys.hour(id)] = hour
            prefs[Keys.minute(id)] = minute
        }
    }

    suspend fun currentReminders(): List<Reminder> = remindersFlow.first()
}
