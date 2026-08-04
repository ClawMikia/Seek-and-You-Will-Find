package com.christopher.bibleverse.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.christopher.bibleverse.data.local.AlarmPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmPreferences: AlarmPreferences

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val pendingResult = goAsync()
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
