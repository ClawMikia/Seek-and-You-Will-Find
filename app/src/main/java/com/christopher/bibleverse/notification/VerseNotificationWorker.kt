package com.christopher.bibleverse.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.christopher.bibleverse.di.verseRepositoryEntryPoint
import kotlinx.coroutines.flow.first

class VerseNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = applicationContext.verseRepositoryEntryPoint()
        val favorite = repository.observeFavorite().first()
        val notificationId = inputData.getInt(
            EXTRA_NOTIFICATION_ID,
            VerseNotificationHelper.NOTIFICATION_BASE_ID
        )
        VerseNotificationHelper(applicationContext)
            .showDailyVerseNotification(favorite, notificationId)
        return Result.success()
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
