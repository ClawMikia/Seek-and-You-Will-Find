package com.christopher.bibleverse

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import com.christopher.bibleverse.alarm.AlarmSoundProvider
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BibleVerseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = Uri.parse(AlarmSoundProvider.SOUND_URI)
            val manager = getSystemService(NotificationManager::class.java)

            // Channels are immutable once created. If the channel already exists
            // without the bundled alarm sound (created by an older build), recreate
            // it once so the new sound takes effect.
            val prefs = getSharedPreferences("channel_prefs", MODE_PRIVATE)
            if (!prefs.getBoolean(PREF_SOUND_CONFIGURED, false)) {
                manager?.deleteNotificationChannel(CHANNEL_ID)
                prefs.edit().putBoolean(PREF_SOUND_CONFIGURED, true).apply()
            }

            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_desc)
                enableLights(true)
                lightColor = getColor(R.color.gold)
                setSound(
                    soundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "daily_verse_channel"
        private const val PREF_SOUND_CONFIGURED = "channel_sound_configured"
    }
}
