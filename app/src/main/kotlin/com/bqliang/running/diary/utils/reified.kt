package com.bqliang.running.diary.utils

import android.app.Activity
import android.content.Context
import android.content.Intent


inline fun <reified T : Activity> Context.startActivity(noinline block: (Intent.() -> Unit)? = null) {
    val intent = Intent(this, T::class.java)
    if (block != null) {
        intent.block()
    }
    startActivity(intent)
}