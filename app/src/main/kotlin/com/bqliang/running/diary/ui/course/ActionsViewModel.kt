package com.bqliang.running.diary.ui.course

import androidx.lifecycle.ViewModel
import com.bqliang.running.diary.ui.course.model.FitnessAction
import kotlinx.coroutines.flow.MutableStateFlow

class ActionsViewModel: ViewModel() {

    lateinit var fitnessActionList: ArrayList<FitnessAction>
    val actionIndex = MutableStateFlow(-1)


    fun addIndex() {
        actionIndex.value = (actionIndex.value + 1).coerceAtMost(fitnessActionList.size - 1)
    }

    fun minusIndex() {
        actionIndex.value = (actionIndex.value - 1).coerceAtLeast(0)
    }
}