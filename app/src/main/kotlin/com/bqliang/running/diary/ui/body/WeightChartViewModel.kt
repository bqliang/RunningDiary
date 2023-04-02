package com.bqliang.running.diary.ui.body

import androidx.lifecycle.ViewModel
import cn.leancloud.LCUser
import com.bqliang.running.diary.logic.Repository
import com.bqliang.running.diary.utils.shortId
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WeightChartViewModel @Inject constructor(private val repository: Repository):ViewModel() {

    private val bodyList = repository.getAllBodyByEntityName(LCUser.currentUser().shortId)


}