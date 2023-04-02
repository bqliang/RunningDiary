package com.bqliang.running.diary.ui.base

import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BaseAlertDialogBuilder : MaterialAlertDialogBuilder {
    constructor(context: Context) : super(context)
    constructor(context: Context, overrideThemeResId: Int) : super(context, overrideThemeResId)

    override fun create(): AlertDialog {
        return super.create().also { dialog ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dialog.window?.apply {
                    addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                    attributes.blurBehindRadius = 64
                }
            }
        }
    }
}