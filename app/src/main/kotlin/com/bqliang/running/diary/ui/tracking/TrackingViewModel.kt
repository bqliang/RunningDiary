package com.bqliang.running.diary.ui.tracking

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amap.api.maps.AMap
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Polyline
import com.bqliang.running.diary.database.entity.Path
import com.bqliang.running.diary.database.entity.Point
import com.bqliang.running.diary.database.entity.Run
import com.bqliang.running.diary.logic.Repository
import com.bqliang.running.diary.services.TrackingService
import com.bqliang.running.diary.ui.base.MyMapView
import com.bqliang.running.diary.utils.showToast
import com.bqliang.running.diary.utils.toGdLatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds

/**
 *
 * @property curLapAMapLatLng 当前圈轨迹点
 * @property dimension 地图维度
 * @property mapType 地图类型
 */
@HiltViewModel
class TrackingViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    private var _cameraPosition: CameraPosition? = null
    val cameraPosition: CameraPosition?
        get() = _cameraPosition

    val previousLapPolylineList = mutableListOf<Polyline>()

    val totalDistanceInMeter =
        TrackingService.previousLapsDistanceInMeter.combine(TrackingService.curLapDistanceInMeter) { previous, current ->
            previous + current
        }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val paceInMinSecPerKm =
        TrackingService.runDurationInMilliseconds.combine(totalDistanceInMeter) { milliseconds, meters ->

            // 避免分母为 0
            if (meters == 0.0) {
                0.0
            } else {
                val paceInSecondsPerKm = (milliseconds / meters).toInt()
                (paceInSecondsPerKm / 60) + (paceInSecondsPerKm % 60) / 60.00
            }
        }
            .sample(2.seconds.inWholeMilliseconds)
            .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    @OptIn(FlowPreview::class)
    val paceInMinSecPerKmStr =
        TrackingService.runDurationInMilliseconds.combine(totalDistanceInMeter) { milliseconds, meters ->

            // 避免分母为 0
            if (meters == 0.0) return@combine "--"

            val paceInSecondsPerKm = (milliseconds / meters).toInt()
            val min = paceInSecondsPerKm / 60
            val sec = paceInSecondsPerKm % 60
            String.format("%02d'%02d''", min, sec)
        }
            .sample(2.seconds.inWholeMilliseconds)
            .stateIn(viewModelScope, SharingStarted.Lazily, "00'00''")

    val caloriesInKcal = totalDistanceInMeter.combine(paceInMinSecPerKm) { distanceInMeter, paceInMinSecPerKm ->
        val coefficient = 0.0175 + 0.78 * Math.E.pow(-0.17 * paceInMinSecPerKm)
        (TrackingService.weightInKg * coefficient * distanceInMeter / 1000).toInt()
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val altitudeInStr = TrackingService.latestAMapLocation.map { aMapLocation ->
        if (aMapLocation == null) {
            "--"
        } else {
            String.format("%.2f", aMapLocation.altitude)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, "--")

    val accuracyStr = TrackingService.latestAMapLocation.map { aMapLocation ->
        if (aMapLocation == null) {
            "--"
        } else {
            String.format("%.2f", aMapLocation.accuracy)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, "--")

    val cadenceInStr = TrackingService.stepCount.map { stepCount ->
        val minutes = TrackingService.runDurationInMilliseconds.value / 1000 / 60
        if (minutes == 0L) return@map "--"
        (stepCount / minutes).toString()
    }.stateIn(viewModelScope, SharingStarted.Lazily, "--")


    private val _runId = MutableSharedFlow<Long?>()
    val runId = _runId.asSharedFlow()

    val curLapAMapLatLng: StateFlow<List<LatLng>> =
        TrackingService.curLapBdTrackPoints.map { curLapBdTrackPoints ->
            curLapBdTrackPoints.map { it.toGdLatLng() }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _dimension = MutableStateFlow(MyMapView.Dimension.TWO)
    val dimension = _dimension.asStateFlow()

    private val _mapType = MutableStateFlow(AMap.MAP_TYPE_NORMAL)
    val mapType = _mapType.asStateFlow()


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


    fun switchMapDimension() {
        _dimension.value = when (dimension.value) {
            MyMapView.Dimension.TWO -> MyMapView.Dimension.THREE
            MyMapView.Dimension.THREE -> MyMapView.Dimension.TWO
        }

        when (dimension.value) {
            MyMapView.Dimension.TWO -> "2D模式"
            MyMapView.Dimension.THREE -> "3D模式"
        }.showToast()
    }


    /**
     * 保存地图位置
     *
     * @param cameraPosition 地图位置
     */
    fun saveCameraPosition(cameraPosition: CameraPosition) {
        _cameraPosition = cameraPosition
    }


    fun insertRun(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            val run = Run(
                entityName = TrackingService.entityName,
                startTime = TrackingService.serviceStartTime,
                durationInMilliseconds = TrackingService.runDurationInMilliseconds.value,
                distanceInMeter = TrackingService.previousLapsDistanceInMeter.value,
                caloriesInKcal = caloriesInKcal.value,
                stepCount = TrackingService.stepCount.value,
                img = bitmap
            )

            val id = repository.insertRun(run)

            flowOf(id)
                .map { runId ->
                    val paths = mutableListOf<Path>()
                    val path = Path(runId = runId)
                    repeat(TrackingService.previousLapsBdTrackPoints.value.size) {
                        paths.add(path)
                    }
                    repository.insertPaths(paths)
                }
                .map { pathIds ->
                    val allPoints =
                        TrackingService.previousLapsBdTrackPoints.value.flatMapIndexed { index, pathPoints ->
                            pathPoints.map { point ->
                                Point(
                                    pathId = pathIds[index],
                                    latitude = point.location.latitude,
                                    longitude = point.location.longitude,
                                    altitude = point.height,
                                    speed = point.speed,
                                    locateTime = point.locTime * 1000,
                                    direction = point.direction,
                                    accuracy = point.radius
                                )
                            }
                        }
                    repository.insertPoint(allPoints)
                }.flowOn(Dispatchers.IO)
                .onEach {
                    _runId.emit(id)
                }.launchIn(this)
        }
    }
}