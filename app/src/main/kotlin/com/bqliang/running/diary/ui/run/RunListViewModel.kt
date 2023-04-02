package com.bqliang.running.diary.ui.list

import androidx.lifecycle.ViewModel
import com.bqliang.running.diary.database.dao.RunDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RunListViewModel @Inject constructor (private val runDao: RunDao): ViewModel() {



    fun allRuns(entityName: String) = runDao.getAllByEntityName(entityName)

}