package com.christopher.bibleverse.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.christopher.bibleverse.R
import com.christopher.bibleverse.data.model.VerseDetail
import com.christopher.bibleverse.di.verseRepositoryEntryPoint
import com.christopher.bibleverse.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BibleVerseWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            val favorite = context.verseRepositoryEntryPoint().observeFavorite().first()
            appWidgetIds.forEach { widgetId ->
                pushUpdate(context, appWidgetManager, widgetId, favorite)
            }
        }
    }

    companion object {
        fun pushUpdate(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
            favorite: VerseDetail?
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_bible_verse)

            if (favorite == null) {
                views.setTextViewText(R.id.tvWidgetVerseText, context.getString(R.string.widget_no_verse))
                views.setTextViewText(R.id.tvWidgetReference, context.getString(R.string.app_name))
            } else {
                views.setTextViewText(R.id.tvWidgetVerseText, "\u201C${favorite.text}\u201D")
                views.setTextViewText(R.id.tvWidgetReference, favorite.reference)
            }

            val openAppIntent = PendingIntent.getActivity(
                context,
                widgetId,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, openAppIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }

        /** Call after saving a new favorite so every placed widget refreshes immediately. */
        fun requestUpdateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, BibleVerseWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                val intent = Intent(context, BibleVerseWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
