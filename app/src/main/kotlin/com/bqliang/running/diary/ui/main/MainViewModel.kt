package com.bqliang.running.diary.ui.main

import androidx.lifecycle.ViewModel
import cn.leancloud.LCUser
import com.bqliang.running.diary.utils.*
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel:ViewModel() {

    val entityName = MutableStateFlow("")
    val username = MutableStateFlow("")
    val email = MutableStateFlow("")
    val avatarUrl = MutableStateFlow("")
    val birth = MutableStateFlow(0L)
    val sex = MutableStateFlow("")
    val signature = MutableStateFlow("这个人很神秘，什么都没有写")
    val goalWeek = MutableStateFlow(5)
    val goalMonth = MutableStateFlow(20)
    val goalYear = MutableStateFlow(200)


    fun setUser(user: LCUser) {
        username.value = user.username
        email.value = user.email
        avatarUrl.value = user.avatar.url
        birth.value = user.birth
        sex.value = user.sex
        signature.value = user.signature
        goalWeek.value = user.weekGoal
        goalMonth.value = user.monthGoal
        goalYear.value = user.yearGoal
    }
}