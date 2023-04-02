package com.bqliang.running.diary.ui.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bqliang.running.diary.logic.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    val username = MutableStateFlow("")
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    val agreePrivacyPolicy = MutableStateFlow(false)

    private val _signUpSuccess = MutableSharedFlow<Boolean>()
    val signUpSuccess = _signUpSuccess.asSharedFlow()

    private val _error = MutableSharedFlow<Throwable>()
    val error = _error.asSharedFlow()

    fun signUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            repository.register(username, email, password)
                .flowOn(Dispatchers.IO)
                .catch { throwable ->
                    _error.emit(throwable)
                }
                .launchIn(viewModelScope)
        }
    }
}