package com.bqliang.running.diary.ui.base

import android.content.Context
import android.graphics.Color
import androidx.preference.Preference
import com.bqliang.running.diary.R
import com.google.android.material.color.MaterialColors

class SimplePreference(context: Context) : Preference(context) {

    private val iconTintColor by lazy {
        MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondaryVariant, Color.BLACK)
    }

    init {
        layoutResource = R.layout.preference_material_item
    }

    override fun setIcon(iconResId: Int) {
        super.setIcon(iconResId)
        icon?.setTint(iconTintColor)
    }

}