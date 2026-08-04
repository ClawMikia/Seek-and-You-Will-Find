package com.christopher.bibleverse.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    fun formatSavedDate(epochMillis: Long): String {
        val formatter = SimpleDateFormat("MMMM d, yyyy \u2022 h:mm a", Locale.getDefault())
        return formatter.format(Date(epochMillis))
    }

    fun formatHourMinute(hour: Int, minute: Int): String {
        val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
        calendar.set(java.util.Calendar.MINUTE, minute)
        return formatter.format(calendar.time)
    }
}
