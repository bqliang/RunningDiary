package com.bqliang.running.diary.ui.course.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Course(
    val videoUrl: String,
    val imageUrl: String,
    val name: String,
    val levelString: String,
    val distanceInMinute: Int,
    val caloriesInKcal: Int,
    val shortDescription: String,
    val fitnessActions : List<FitnessAction>,
    val description: String = "",
): Parcelable

