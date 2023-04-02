package com.bqliang.running.diary.ui.detail

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amap.api.maps.AMap
import com.amap.api.maps.model.PolylineOptions
import com.bqliang.running.diary.logic.Repository
import com.bqliang.running.diary.utils.gpx
import com.bqliang.running.diary.utils.isoFormatMillisSeconds
import com.bqliang.running.diary.utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.OutputStream
import javax.inject.Inject

/**
 *
 * @property repository
 * @property mapType 地图类型 (标准/卫星/夜间)
 * @see [switchMapType]
 * @property runId Run ID
 * @see [setRunId]
 * @property run Run
 * @property pathsWithPoints 路径点
 * @property allPoints 所有点
 * @property polylineOptions
 * @property firstLocation 起点
 * @property lastLocation 终点
 * @property paceString 配速字符串
 *
 *
 */
@HiltViewModel
@OptIn(FlowPreview::class)
class DetailViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private val _mapType = MutableStateFlow(AMap.MAP_TYPE_NORMAL)
    val mapType = _mapType.asStateFlow()

    private val runId = MutableStateFlow<Long?>(null)

    val run = runId.flatMapConcat { runId ->
        if (runId != null) {
            repository.getRunById(runId)
        } else {
            emptyFlow()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val allPoints = run.flatMapConcat { run ->
        if (run != null) {
            repository.getPointsByRunId(run.id)
        } else {
            emptyFlow()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val pathsWithPoints = allPoints.map { allPoints ->
        allPoints?.groupBy {
            it.pathId
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val polylineOptions = pathsWithPoints.map { pathsWithPoints ->
        pathsWithPoints
            ?.values
            ?.map { points ->
                points.map { point ->
                    point.toGdLatLng()
                }
            }
            ?.map { latLngs ->
                PolylineOptions()
                    .width(20f)
                    .color(Color.GREEN)
                    .addAll(latLngs)
            }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val firstLocation = allPoints.map { allPoints ->
        allPoints
            ?.minByOrNull { it.locateTime }
            ?.toGdLatLng()
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val lastLocation = allPoints.map { allPoints ->
        allPoints
            ?.maxByOrNull { it.locateTime }
            ?.toGdLatLng()
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)


    val paceString = run.map { run ->
        if (run != null) {
            val paceInSecondsPerKM = (run.durationInMilliseconds / run.distanceInMeter).toInt()
            String.format(
                "%02d'%02d''",
                paceInSecondsPerKM / 60,
                paceInSecondsPerKM % 60
            )
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)


    val cadence = run.map { run ->
        if (run != null) {
            val durationInMin = run.durationInMilliseconds / 1000 / 60
            (run.stepCount / durationInMin).toInt()
        } else {
            0
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val speedInKmPerHour = run.map { run ->
        if (run != null) {
            val distanceInKm = run.distanceInMeter / 1000
            val durationInMin = run.durationInMilliseconds / 1000 / 60
            (distanceInKm / durationInMin) * 60
        } else {
            0.0
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)


    /**
     * Set Run ID
     *
     * @param runId Run ID
     */
    fun setRunId(runId: Long) {
        this.runId.value = runId
    }


    /**
     * 切换地图类型 [标准地图/卫星地图/夜间地图]
     * @see [AMap.MAP_TYPE_NORMAL]
     * @see [AMap.MAP_TYPE_SATELLITE]
     * @see [AMap.MAP_TYPE_NIGHT]
     *
     */
    fun switchMapType() {
        _mapType.value = when (mapType.value) {
            AMap.MAP_TYPE_NORMAL -> AMap.MAP_TYPE_SATELLITE
            AMap.MAP_TYPE_SATELLITE -> AMap.MAP_TYPE_NIGHT
            AMap.MAP_TYPE_NIGHT -> AMap.MAP_TYPE_NORMAL
            else -> AMap.MAP_TYPE_NORMAL
        }

        when (mapType.value) {
            AMap.MAP_TYPE_NORMAL -> "标准地图"
            AMap.MAP_TYPE_SATELLITE -> "卫星地图"
            AMap.MAP_TYPE_NIGHT -> "夜间地图"
            else -> "标准地图"
        }.showToast()
    }


    fun exportGpxFile(outputStream: OutputStream) {
        gpx {
            metadata {
                "time" to isoFormatMillisSeconds(run.value?.startTime ?: 0L)
            }

            trk {
                type("Running")

                extension {
                    "totalDistanceInMeter" to (run.value?.distanceInMeter ?: 0)
                    "totalDurationInMilliseconds" to (run.value?.durationInMilliseconds ?: 0)
                }

                pathsWithPoints.value!!.forEach { (path, points) ->
                    trkseg {
                        points.forEach { points ->
                            trkpt(
                                lat = points.latitude,
                                lon = points.longitude,
                                ele = points.altitude,
                                time = points.locateTime
                            )
                        }
                    }
                }
            }
        }.toStream(outputStream)

        "导出成功".showToast()
    }


    fun updateRunNote(runNote: String) {
        viewModelScope.launch {
            repository.updateRunNoteById(runNote, runId.value ?: 0L)
        }
    }
}