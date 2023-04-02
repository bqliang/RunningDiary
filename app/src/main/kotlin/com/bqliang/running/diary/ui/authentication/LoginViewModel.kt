package com.bqliang.running.diary.ui.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.leancloud.LCException
import cn.leancloud.LCUser
import cn.leancloud.types.LCNull
import com.bqliang.running.diary.utils.showToast
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    private val _loginSuccess = MutableSharedFlow<Boolean>()
    val loginSuccess = _loginSuccess.asSharedFlow()

    private val _sendResetPasswordEmailSuccess = MutableSharedFlow<Boolean>()
    val sendResetPasswordEmailSuccess = _sendResetPasswordEmailSuccess.asSharedFlow()

    private val _error = MutableSharedFlow<LCException>()
    val error = _error.asSharedFlow()

    val agreePrivacyPolicy = MutableStateFlow(false)

    fun loginByEmail(email: String, password: String) {
        LCUser.loginByEmail(email, password).subscribe(
            object : Observer<LCUser> {
                override fun onSubscribe(disposable: Disposable) {
                }

                override fun onNext(lcUser: LCUser) {
                    viewModelScope.launch {
                        _loginSuccess.emit(true)
                    }
                }

                override fun onError(throwable: Throwable) {
                    val lcException = throwable as LCException
                    lcException.printStackTrace()
                    viewModelScope.launch {
                        _error.emit(lcException)
                    }
                }

                override fun onComplete() {  }
            }
        )
    }


    fun verifyEmail(email: String) {
        LCUser.requestEmailVerifyInBackground(email).subscribe(
            object : Observer<LCNull> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                    val lcException = e as LCException
                    lcException.printStackTrace()
                    viewModelScope.launch {
                        _error.emit(lcException)
                    }
                }

                override fun onComplete() {
                }

                override fun onNext(lcNull: LCNull) {
                    "已发送验证邮件".showToast()
                }

            }
        )
    }

    fun retrievePassword(email: String) {
        LCUser.requestPasswordResetInBackground(email).subscribe(
            object : Observer<LCNull> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                    val lcException = e as LCException
                    lcException.printStackTrace()
                    lcException.localizedMessage?.showToast()
                }

                override fun onComplete() {
                }

                override fun onNext(lcNull: LCNull) {
                    "已发送重置密码邮件".showToast()
                    viewModelScope.launch {
                        _sendResetPasswordEmailSuccess.emit(true)
                    }
                }
            }
        )
    }
}