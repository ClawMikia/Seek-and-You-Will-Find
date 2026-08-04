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
        VerseNotificationHelper(applicationContext).showDailyVerseNotification(favorite)
        return Result.success()
    }
}
