package com.christopher.bibleverse.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.christopher.bibleverse.data.local.AlarmPreferences
import com.christopher.bibleverse.notification.VerseNotificationHelper
import com.christopher.bibleverse.notification.VerseNotificationWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receives a daily alarm fire for one specific reminder. Shows (or refreshes)
 * the morning notification via a short-lived WorkManager job, then immediately
 * re-arms that reminder's alarm for the next day — since exact alarms are
 * one-shot.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmPreferences: AlarmPreferences

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(AlarmScheduler.EXTRA_REMINDER_ID, -1L)
        if (reminderId < 0) return

        val pendingResult = goAsync()

        // Kick off the notification work immediately with a per-reminder id.
        val workRequest = OneTimeWorkRequestBuilder<VerseNotificationWorker>()
            .setInputData(
                workDataOf(
                    VerseNotificationWorker.EXTRA_NOTIFICATION_ID to
                        VerseNotificationHelper.notificationIdFor(reminderId)
                )
            )
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = alarmPreferences.currentReminders()
                    .firstOrNull { it.id == reminderId }
                if (reminder != null) {
                    AlarmScheduler(context).schedule(reminder)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
