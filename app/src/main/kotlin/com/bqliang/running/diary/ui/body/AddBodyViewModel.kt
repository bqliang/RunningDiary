package com.bqliang.running.diary.ui.body

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.leancloud.LCUser
import com.bqliang.running.diary.database.dao.BodyDao
import com.bqliang.running.diary.database.entity.Body
import com.bqliang.running.diary.utils.shortId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBodyViewModel @Inject constructor(private val bodyDao: BodyDao) : ViewModel() {

    private val _successful: MutableSharedFlow<Boolean> = MutableSharedFlow()
    val successful = _successful.asSharedFlow()

    var oldBody: Body? = null

    val dateInMs = MutableStateFlow(-1L)
    val heightInCm = MutableStateFlow(168.0f)
    val weightInKg = MutableStateFlow(58.0f)

    private val bmi = heightInCm.combine(weightInKg) { cm, kg ->
        val heightInM = cm / 100
        kg / (heightInM * heightInM)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 23.0f)

    val bmiString = bmi.map { bmi ->
        val desc = when (bmi) {
            in 0.0..18.5 -> "过轻"
            in 18.5..24.0 -> "正常"
            in 24.0..28.0 -> "过重"
            in 28.0..32.0 -> "肥胖"
            else -> "非常肥胖"
        }
        "BMI: %.1f  $desc".format(bmi)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "--")


    fun insertBody() {
        viewModelScope.launch(Dispatchers.IO) {
            val newBody = Body(
                entityName = LCUser.currentUser().shortId,
                heightInCm = heightInCm.value,
                weightInKg = weightInKg.value,
                dateInMillisSeconds = dateInMs.value
            )
            bodyDao.insertBody(newBody)
            _successful.emit(true)
        }
    }


    fun updateBody() {
        viewModelScope.launch(Dispatchers.IO) {
            val newBody = oldBody!!.copy(
                heightInCm = heightInCm.value,
                weightInKg = weightInKg.value,
                dateInMillisSeconds = dateInMs.value
            )
            newBody.id = oldBody!!.id
            bodyDao.updateBody(newBody)
            _successful.emit(true)
        }
    }


    fun getBodyById(id: Long) = bodyDao.getBodyById(id)


    fun getLatestBody(entityName: String) = bodyDao.getLatestBody(entityName)
}