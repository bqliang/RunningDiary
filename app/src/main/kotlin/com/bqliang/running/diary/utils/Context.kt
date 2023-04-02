package com.bqliang.running.diary.utils

import android.content.Context
import android.graphics.Point
import android.os.Build

fun Context.getScreenCenterInPixels(): Point {
    val displayMetrics = resources.displayMetrics
    return Point(displayMetrics.widthPixels / 2, displayMetrics.heightPixels / 2)
}

fun Context.getInstallPackageName() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    packageManager.getInstallSourceInfo(packageName).installingPackageName
} else {
    @Suppress("deprecation")
    packageManager.getInstallerPackageName(packageName)
}