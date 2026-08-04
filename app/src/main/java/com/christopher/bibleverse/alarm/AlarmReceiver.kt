package com.christopher.bibleverse.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.christopher.bibleverse.data.local.AlarmPreferences
import com.christopher.bibleverse.notification.VerseNotificationWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receives the daily alarm fire. Shows (or refreshes) the morning
 * notification via a short-lived WorkManager job, then immediately
 * re-arms tomorrow's alarm — since exact alarms are one-shot.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmPreferences: AlarmPreferences

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        // Kick off the notification work immediately.
        val workRequest = OneTimeWorkRequestBuilder<VerseNotificationWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val state = alarmPreferences.currentState()
                if (state.enabled) {
                    AlarmScheduler(context).schedule(state.hour, state.minute)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
