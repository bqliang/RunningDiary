package com.bqliang.running.diary.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.setRepeatClickListener(threshold: Int = 5, onClickAction: () -> Unit) {
    val clickTimeout = 1000L // 1 second in milliseconds
    val clickTimes = LongArray(size = threshold)
    var clickIndex = 0

    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        clickTimes[clickIndex] = currentTime

        if (clickIndex == clickTimes.lastIndex) {
            val timeDiff = currentTime - clickTimes[0]
            if (timeDiff <= threshold * clickTimeout) {
                onClickAction()
            }
        }

        clickIndex = (clickIndex + 1) % threshold
    }
}