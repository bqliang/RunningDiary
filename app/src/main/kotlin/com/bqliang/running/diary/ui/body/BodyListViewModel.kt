package com.bqliang.running.diary.ui.body

import androidx.lifecycle.ViewModel
import com.bqliang.running.diary.logic.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BodyListViewModel @Inject constructor(private val repository: Repository): ViewModel() {

    fun allBody(entityName: String) = repository.getAllBodyByEntityName(entityName)

}