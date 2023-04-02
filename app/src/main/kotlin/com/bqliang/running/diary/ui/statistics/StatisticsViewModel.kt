package com.bqliang.running.diary.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.leancloud.LCUser
import com.bqliang.running.diary.database.dao.RunDao
import com.bqliang.running.diary.utils.monthGoal
import com.bqliang.running.diary.utils.shortId
import com.bqliang.running.diary.utils.weekGoal
import com.bqliang.running.diary.utils.yearGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.*
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject


@HiltViewModel
class StatisticsViewModel @Inject constructor(runDao: RunDao) : ViewModel() {

    private val entityName by lazy { LCUser.currentUser().shortId }

    private val _weekDistanceGoalInKm = MutableStateFlow(LCUser.currentUser().weekGoal)
    val weekDistanceGoalInKm = _weekDistanceGoalInKm.asStateFlow()
    private val _monthDistanceGoalInKm = MutableStateFlow(LCUser.currentUser().monthGoal)
    val monthDistanceGoalInKm = _monthDistanceGoalInKm.asStateFlow()
    private val _yearDistanceGoalInKm = MutableStateFlow(LCUser.currentUser().yearGoal)
    val yearDistanceGoalInKm = _yearDistanceGoalInKm.asStateFlow()


    val distanceInKmThisWeek =
        runDao.getTotalDistance(entityName, getStartTimestampFor("week"))
            .map { distanceInMeterThisWeek ->
                (distanceInMeterThisWeek ?: 0.0) / 1000
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val distanceInKmThisMonth =
        runDao.getTotalDistance(entityName, getStartTimestampFor("month"))
            .map { distanceInMeterThisMonth ->
                (distanceInMeterThisMonth ?: 0.0) / 1000
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val distanceInKmThisYear =
        runDao.getTotalDistance(entityName, getStartTimestampFor("year"))
            .map { distanceInMeterThisYear ->
                (distanceInMeterThisYear ?: 0.0) / 1000
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalDistanceInKm = runDao.getTotalDistance(entityName, 0)
        .map { totalDistanceInM ->
            (totalDistanceInM ?: 0.0) / 1000
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val runCount = runDao.getRunCount(entityName)
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val totalSteps = runDao.getTotalSteps(entityName)
        .map { steps ->
            steps ?: 0
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val totalDurationInHour =
        runDao.getDurationInMillisSeconds(entityName)
            .map { totalDurationInMs ->
                val ms = totalDurationInMs ?: 0L
                ms / 1000.00 / 60.00 / 60.00
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, 0.00)

    val totalCaloriesInKcal = runDao.getTotalCaloriesInKcal(entityName)
        .map { totalCalories ->
            totalCalories ?: 0
        }.stateIn(viewModelScope, SharingStarted.Lazily, 0)


    private fun getStartTimestampFor(selection: String): Long {
        val now = LocalDate.now(ZoneOffset.UTC)
        val selectedDate: LocalDate = when (selection) {
            "week" -> now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            "month" -> now.withDayOfMonth(1)
            "year" -> now.withDayOfYear(1)
            else -> throw IllegalArgumentException("Invalid selection: $selection")
        }
        val selectedDateTime =
            LocalDateTime.of(selectedDate, LocalTime.MIDNIGHT).atZone(ZoneOffset.UTC)
        return selectedDateTime.toInstant().toEpochMilli()
    }
}