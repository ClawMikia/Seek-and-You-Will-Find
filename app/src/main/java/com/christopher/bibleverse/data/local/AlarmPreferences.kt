package com.christopher.bibleverse.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.alarmDataStore by preferencesDataStore(name = "alarm_prefs")

data class AlarmState(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)

/**
 * The app supports exactly one recurring daily alarm. Its time is
 * persisted here so it survives process death, reboots, and can be
 * re-read whenever the user wants to change it.
 */
class AlarmPreferences(private val context: Context) {

    private object Keys {
        val ENABLED = booleanPreferencesKey("alarm_enabled")
        val HOUR = intPreferencesKey("alarm_hour")
        val MINUTE = intPreferencesKey("alarm_minute")
    }

    val alarmStateFlow: Flow<AlarmState> = context.alarmDataStore.data.map { prefs ->
        AlarmState(
            enabled = prefs[Keys.ENABLED] ?: false,
            hour = prefs[Keys.HOUR] ?: 7,
            minute = prefs[Keys.MINUTE] ?: 0
        )
    }

    suspend fun setAlarm(hour: Int, minute: Int) {
        context.alarmDataStore.edit { prefs ->
            prefs[Keys.ENABLED] = true
            prefs[Keys.HOUR] = hour
            prefs[Keys.MINUTE] = minute
        }
    }

    suspend fun clearAlarm() {
        context.alarmDataStore.edit { prefs ->
            prefs[Keys.ENABLED] = false
        }
    }

    suspend fun currentState(): AlarmState = alarmStateFlow.first()
}
