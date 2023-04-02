package com.bqliang.running.diary.utils

import cn.leancloud.LCFile
import cn.leancloud.LCObject
import cn.leancloud.LCUser
import com.bqliang.running.diary.DEFAULT_BIRTH
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

val LCUser.shortId: String
    get() = getString("shortId")


var LCUser.avatar: LCFile
    set(value) = put("avatar", value)
    get() = getLCFile("avatar")


var LCUser.birth: Long
    set(value) = put("birth", value)
    get() {
        val birth = getLong("birth")
        return if (birth == 0L) DEFAULT_BIRTH else birth
    }


var LCUser.sex: String
    set(value) = put("sex", value)
    get() = getString("sex")


var LCUser.signature: String
    set(value) = put("signature", value)
    get() = getString("signature")


var LCUser.weekGoal: Int
    set(value) = put("week_goal", value)
    get()  = getInt("week_goal")

fun LCUser.setWeekGoal(weekGoal: Int, onSuccess: (() -> Unit)?) {
    this.weekGoal = weekGoal
    update("周目标", onSuccess)
}


var LCUser.monthGoal: Int
    set(value) = put("month_goal", value)
    get()  = getInt("month_goal")

fun LCUser.setMonthGoal(monthGoal: Int, onSuccess: (() -> Unit)?) {
    this.monthGoal = monthGoal
    update("月目标", onSuccess)
}


var LCUser.yearGoal: Int
    set(value) = put("year_goal", value)
    get()= getInt("year_goal")

fun LCUser.setYearGoal(yearGoal: Int, onSuccess: (() -> Unit)?) {
    this.yearGoal = yearGoal
    update("年目标", onSuccess)
}


fun LCUser.update(tag: String = "", onSuccess: (() -> Unit)? = null) {
    saveInBackground().subscribe(object : Observer<LCObject> {
        override fun onSubscribe(disposable: Disposable) {
        }

        override fun onNext(lcObject: LCObject) {
            "更新${tag}成功".showToast()
            onSuccess?.invoke()
        }


        override fun onError(e: Throwable) {
            "更新${tag}失败: ${e.localizedMessage}".showToast()
            e.printStackTrace()
        }

        override fun onComplete() {
        }
    })
}