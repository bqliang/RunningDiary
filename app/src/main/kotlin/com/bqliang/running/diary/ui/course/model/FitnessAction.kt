package com.bqliang.running.diary.ui.course.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class FitnessAction(
    val name: String,
    val countAndDurationStr: String,
    val videoUrl: String,
    val description: String,
) : Parcelable
