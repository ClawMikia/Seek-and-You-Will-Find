package com.christopher.bibleverse.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.christopher.bibleverse.data.local.Reminder
import java.util.Calendar

/**
 * Schedules recurring daily alarms that fire the morning verse notification.
 * Each reminder gets its own one-shot exact alarm + self-rescheduling receiver
 * so it keeps firing every day even under Doze, without using the drift-prone
 * inexact repeating API.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: Reminder) {
        val pendingIntent = buildPendingIntent(reminder.id)
        val triggerAt = nextTriggerMillis(reminder.hour, reminder.minute)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancel(reminderId: Long) {
        alarmManager.cancel(buildPendingIntent(reminderId))
    }

    private fun buildPendingIntent(reminderId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_DAILY_VERSE
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BASE + (reminderId % REQUEST_CODE_RANGE).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (trigger.before(now)) {
            trigger.add(Calendar.DAY_OF_YEAR, 1)
        }
        return trigger.timeInMillis
    }

    companion object {
        const val ACTION_DAILY_VERSE = "com.christopher.bibleverse.action.DAILY_VERSE"
        const val EXTRA_REMINDER_ID = "com.christopher.bibleverse.extra.REMINDER_ID"
        private const val REQUEST_CODE_BASE = 1001
        private const val REQUEST_CODE_RANGE = 1_000_000
    }
}
