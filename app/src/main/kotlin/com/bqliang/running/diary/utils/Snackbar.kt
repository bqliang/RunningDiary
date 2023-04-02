package com.bqliang.running.diary.utils

import android.view.View
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar

/**
 * Show snackbar
 *
 * @param text The text to show.
 * @param actionText The text of the action item.
 * @param duration How long to display the message. Either [Snackbar.LENGTH_SHORT] or [Snackbar.LENGTH_LONG]
 * @param block The action to be taken when the user clicks the action item.
 */
fun View.showSnackbar(text: String, actionText: String? = null, duration: Int = Snackbar.LENGTH_SHORT, block: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(this, text, duration)
    if (actionText != null && block != null) {
        snackbar.setAction(actionText) {
            block()
        }
    }
    snackbar.show()
}


/**
 * Show snackbar
 *
 * @param textResId The resource id of the string resource to use.
 * @param actionText The text of the action item.
 * @param duration How long to display the message. Either [Snackbar.LENGTH_SHORT] or [Snackbar.LENGTH_LONG]
 * @param block The action to be taken when the user clicks the action item.
 */
fun View.showSnackbar(@StringRes textResId: Int, actionText: Int? = null, duration: Int = Snackbar.LENGTH_SHORT, block: (() -> Unit)? = null) {
    val snackbar = Snackbar.make(this, textResId, duration)
    if (actionText != null && block != null) {
        snackbar.setAction(actionText) {
            block()
        }
    }
    snackbar.show()
}