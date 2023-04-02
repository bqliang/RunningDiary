package com.bqliang.running.diary.utils

import android.widget.Toast
import com.bqliang.running.diary.RunningDiaryApp

/**
 * Show toast
 *
 * @param duration Toast.LENGTH_SHORT or Toast.LENGTH_LONG
 */
fun String.showToast(duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(RunningDiaryApp.context, this, duration).show()
}


/**
 * Show toast
 *
 * @param duration Toast.LENGTH_SHORT or Toast.LENGTH_LONG
 */
fun Int.showToast(duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(RunningDiaryApp.context, this, duration).show()
}