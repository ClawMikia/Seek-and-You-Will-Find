package com.christopher.bibleverse.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.christopher.bibleverse.BibleVerseApplication
import com.christopher.bibleverse.R
import com.christopher.bibleverse.data.model.VerseDetail
import com.christopher.bibleverse.ui.main.MainActivity

class VerseNotificationHelper(private val context: Context) {

    fun showDailyVerseNotification(favorite: VerseDetail?, notificationId: Int = NOTIFICATION_BASE_ID) {
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val collapsed = RemoteViews(context.packageName, R.layout.notification_verse_small).apply {
            applyContent(this, favorite, expanded = false)
        }
        val expanded = RemoteViews(context.packageName, R.layout.notification_verse_expanded).apply {
            applyContent(this, favorite, expanded = true)
        }

        val notification = NotificationCompat.Builder(context, BibleVerseApplication.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_cross)
            .setContentTitle(context.getString(R.string.notification_title))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsed)
            .setCustomBigContentView(expanded)
            // Full rich content is theologically neutral devotional text, so it is
            // safe to reveal on the lock screen exactly as-is (no public redaction needed).
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun applyContent(views: RemoteViews, favorite: VerseDetail?, expanded: Boolean) {
        if (favorite == null) {
            views.setTextViewText(R.id.tvNotifVerseText, context.getString(R.string.home_empty_subtitle))
            views.setTextViewText(R.id.tvNotifReference, context.getString(R.string.home_empty_title))
            if (expanded) {
                views.setViewVisibility(R.id.tvNotifTranslation, android.view.View.GONE)
            }
        } else {
            views.setTextViewText(R.id.tvNotifVerseText, "\u201C${favorite.text}\u201D")
            views.setTextViewText(R.id.tvNotifReference, favorite.reference)
            if (expanded) {
                views.setViewVisibility(R.id.tvNotifTranslation, android.view.View.VISIBLE)
                views.setTextViewText(R.id.tvNotifTranslation, favorite.translationName)
            }
        }
    }

    companion object {
        const val NOTIFICATION_BASE_ID = 2001

        fun notificationIdFor(reminderId: Long): Int =
            NOTIFICATION_BASE_ID + (reminderId % 1_000_000L).toInt()
    }
}
