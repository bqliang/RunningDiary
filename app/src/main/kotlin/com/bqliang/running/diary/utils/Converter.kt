@file:JvmName("Converter")

package com.bqliang.running.diary.utils

import android.content.res.Resources
import java.text.SimpleDateFormat
import java.util.*


fun millisecondsHmsFormat(milliSeconds: Long): String {
    val hours = milliSeconds / 1000 / 60 / 60
    val minutes = milliSeconds / 1000 / 60 % 60
    val seconds = milliSeconds / 1000 % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private val dateTimeFormatter by lazy { SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA) }
private val dateFormatter by lazy { SimpleDateFormat("yyyy/MM/dd", Locale.CHINA) }

fun millsSecondsDateTimeFormat(milliSeconds: Long) = dateTimeFormatter.format(milliSeconds)


fun paceInMillisSecondsPerKmFormat(pace: Long) :String {
    val minutes = pace / 1000 / 60
    val seconds = pace / 1000 % 60
    return String.format("%02d'%02d''", minutes, seconds)
}


fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}


fun millisSecondsDateFormat(millisSeconds: Long) = dateFormatter.format(millisSeconds)


fun utcMsToLocaleMs(utcMs: Long): Long {
    val calendar = Calendar.getInstance()
    val offset = calendar.timeZone.rawOffset
    return utcMs + offset
}


fun localeMsToUtcMs(localeMs: Long): Long {
    val calendar = Calendar.getInstance()
    val offset = calendar.timeZone.rawOffset
    return localeMs - offset
}