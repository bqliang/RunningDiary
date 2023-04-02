package com.bqliang.running.diary.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import cn.leancloud.LCUser
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.baidu.trace.api.track.*
import com.baidu.trace.model.CoordType
import com.baidu.trace.model.Point
import com.baidu.trace.model.ProcessOption
import com.baidu.trace.model.TransportMode
import com.bqliang.running.diary.BAIDU_TRACE_SERVICE_ID
import com.bqliang.running.diary.R
import com.bqliang.running.diary.RunningDiaryApp
import com.bqliang.running.diary.ui.tracking.TrackingActivity
import com.bqliang.running.diary.utils.collectLifecycleFlow
import com.bqliang.running.diary.utils.shortId
import com.bqliang.running.diary.utils.toBdPoint
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*


/**
 *
 * @property binder 用于绑定服务
 * @property timerJob 计时器的协程
 * @property notification 前台通知
 * @property notificationId 通知的 id
 * @property aMapLocationClient 高德定位客户端
 * @property sensorManager 传感器管理器
 * @property stepSensor 步数传感器
 * @property hasRecordedFirstStep 是否已记录第一步
 * @property firstStepInCurrentLap 计步器每次启动记录的第一个数据
 */
class TrackingService : LifecycleService(), SensorEventListener, AMapLocationListener {

    enum class Status {
        NOT_STARTED, STARTED, PAUSED
    }

    /**
     * TrackingService 伴生对象
     *
     * @property status TrackingService 状态
     * @property runDurationInMilliseconds 运动时长（单位：毫秒）
     * @property runDurationInHMSFormat 运动时长字符串，格式：(hh:)mm:ss
     * @property curLapBdTrackPoints 当前圈 aMapLocation 定位点集合
     * @property previousLapsBdTrackPoints 跑步路径 aMapLocation 定位点集合（不包括当前圈）
     * @property stepCount 步数
     * @property instantaneousSpeed 瞬时速度
     * @property pace 配速
     * @property caloriesBurned 消耗卡路里
     * @property _curLapDistanceInMeter 单圈路程（单位：米）
     * @property _previousLapsDistanceInMeter 前面圈总路程（单位：米）
     * @property totalDistanceInKm 总路程（单位：千米）
     *
     */
    companion object {
        const val FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID =
            "foreground_service_notification_channel_id"
        const val FOREGROUND_SERVICE_NOTIFICATION_ID = 1001
        var entityName = ""
        var weightInKg = 68f

        private val _status = MutableStateFlow(Status.NOT_STARTED)
        val status = _status.asStateFlow()

        private val _runDurationInMilliseconds = MutableStateFlow(0L)
        val runDurationInMilliseconds = _runDurationInMilliseconds.asStateFlow()
        val runDurationInHMSFormat: Flow<String> = runDurationInMilliseconds.map { milliseconds ->
            val hours = milliseconds / 1000 / 60 / 60
            val minutes = milliseconds / 1000 / 60 % 60
            val seconds = milliseconds / 1000 % 60

            if (hours > 0L) String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else String.format("%02d:%02d", minutes, seconds)
        }

        private val _stepCount = MutableStateFlow(0)
        val stepCount = _stepCount.asStateFlow()

        private val _latestAMapLocation = MutableStateFlow<AMapLocation?>(null)
        val latestAMapLocation = _latestAMapLocation.asSharedFlow()

        private val _previousLapsBdTrackPoints = MutableStateFlow(emptyList<List<TrackPoint>>())
        val previousLapsBdTrackPoints = _previousLapsBdTrackPoints.asStateFlow()
        private val _curLapBdTrackPoints = MutableStateFlow(emptyList<TrackPoint>())
        val curLapBdTrackPoints = _curLapBdTrackPoints.asStateFlow()

        private val _previousLapsDistanceInMeter = MutableStateFlow(0.0)
        val previousLapsDistanceInMeter = _previousLapsDistanceInMeter.asStateFlow()
        private val _curLapDistanceInMeter = MutableStateFlow(0.0)
        val curLapDistanceInMeter = _curLapDistanceInMeter.asStateFlow()

        private val _instantaneousSpeed = MutableStateFlow(0f)
        val instantaneousSpeed = _instantaneousSpeed.asStateFlow()

        private val _pace = MutableStateFlow(0.0)
        val pace = _pace.asStateFlow()

        private val _caloriesBurned = MutableStateFlow(0f)
        val caloriesBurned = _caloriesBurned.asStateFlow()

        var serviceStartTime = -1L
        var currentLapStartTime = System.currentTimeMillis()
    }


    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(this)
    }
    private val binder = TrackingBinder()
    private var timerJob: Job? = null
    private var updateNotificationJob: Job? = null
    private lateinit var notification: Notification
    private lateinit var aMapLocationClient: AMapLocationClient
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var hasRecordedFirstStep = false
    private var firstStepInCurrentLap = 0
    private var isNeedMapMatch = false


    inner class TrackingBinder : Binder() {

        fun startOrPauseTracking() {
            if (status.value == Status.STARTED) {
                pause()
            } else if (status.value == Status.PAUSED) {
                start()
            }
        }

        fun stopTracking() {
            _status.value = Status.NOT_STARTED
            // 取消前台通知
            stopForeground(STOP_FOREGROUND_REMOVE)
            // 停止服务
            stopSelf()
            // 重置数据
            resetData()
        }
    }


    private fun start() {
        _status.value = Status.STARTED
        // 开始计时
        timerJob = startTimer()
        // 更新前台通知
        updateNotificationJob = updateNotification()
        // 注册传感器监听器（计步器）
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        // 开始定位
        aMapLocationClient.startLocation()
    }


    private fun pause() {
        _status.value = Status.PAUSED
        // 停止定位
        aMapLocationClient.stopLocation()
        // 暂停计时器
        timerJob?.cancel()
        // 暂停更新前台通知
        updateNotificationJob?.cancel()

        // 累加更新数据
        if (curLapBdTrackPoints.value.size >= 2) {
            _previousLapsBdTrackPoints.value += listOf(curLapBdTrackPoints.value)
            _previousLapsDistanceInMeter.value += curLapDistanceInMeter.value
        }
        _curLapBdTrackPoints.value = emptyList()
        _curLapDistanceInMeter.value = 0.0

        // 暂停计步器
        sensorManager.unregisterListener(this@TrackingService)
        hasRecordedFirstStep = false

        // 将瞬时速度置为 0
        _instantaneousSpeed.value = 0f
    }


    private fun resetData() {
        _runDurationInMilliseconds.value = 0L
        _curLapDistanceInMeter.value = 0.0
        _previousLapsDistanceInMeter.value = 0.0
        _stepCount.value = 0
        _instantaneousSpeed.value = 0f
        _caloriesBurned.value = 0f
        _pace.value = 0.0
        _curLapBdTrackPoints.value = emptyList()
        _previousLapsBdTrackPoints.value = emptyList()
    }


    override fun onCreate() {
        Timber.d("Tracking Service created")
        super.onCreate()
        initAMapLocationClient()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        entityName = LCUser.currentUser().shortId
        isNeedMapMatch = MMKV.defaultMMKV().getBoolean(getString(R.string.pref_is_need_map_match), false)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("Tracking Service onStartCommand")
        if (status.value == Status.NOT_STARTED) {
            serviceStartTime = System.currentTimeMillis()
            makeForeground()
            start()
        }

        return super.onStartCommand(intent, flags, startId)
    }


    /**
     * 多次绑定时，只会调用一次 onBind 方法
     */
    override fun onBind(intent: Intent): IBinder {
        Timber.d("Tracking Service onBind")
        super.onBind(intent)
        return binder
    }


    /**
     * 当所有客户端都断开连接时，会调用此方法。例如：有多个客户端绑定了 service，那么只有最后一个客户端断开连接时，才会调用此方法
     *
     * @return 指示在所有客户端断开连接后是否应自动重新创建服务。
     * - true 意味着一旦客户端再次绑定到该服务，该服务就会自动重新创建。
     * - false (default) 表示不会自动重新创建服务，并在所有客户端断开连接时终止服务  (即使有客户端再次绑定到该服务)
     *
     * [管理绑定服务的生命周期](https://developer.android.com/guide/components/bound-services#Lifecycle)
     */
    override fun onUnbind(intent: Intent?): Boolean {
        Timber.d("Tracking Service onUnbind")
        return super.onUnbind(intent)
    }


    override fun onDestroy() {
        Timber.d("Tracking Service destroyed")
        super.onDestroy()
        // 取消注册传感器监听器
        sensorManager.unregisterListener(this)
        // 销毁定位客户端
        aMapLocationClient.onDestroy()
    }


    /**
     * 将服务提升至前台
     *
     */
    @SuppressLint("MissingPermission")
    private fun makeForeground() {

        val channel = NotificationChannelCompat.Builder(
            FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_LOW
        )
            .setName("前台通知")
            .setShowBadge(false) // 是否允许这个渠道下的通知显示角标，默认会显示角标
            .setDescription("用于显示前台通知")
            .build()

        notificationManager.createNotificationChannel(channel)

        val intent = Intent(this, TrackingActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        notification = NotificationCompat.Builder(this, FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE) // 立刻启动前台通知
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH).setOngoing(true) // 通知不可清除
            .setContentTitle(getString(R.string.app_name)).setContentText("00:00:00")
            .setContentIntent(pendingIntent).setSmallIcon(R.drawable.ic_launcher_foreground).build()

        startForeground(FOREGROUND_SERVICE_NOTIFICATION_ID, notification)
    }


    /**
     * 初始化高德定位客户端 [AMapLocationClient]
     *
     */
    private fun initAMapLocationClient() {
        aMapLocationClient = AMapLocationClient(RunningDiaryApp.context)
        aMapLocationClient.setLocationListener(this)

        val option = AMapLocationClientOption().apply {
            locationPurpose = AMapLocationClientOption.AMapLocationPurpose.Sport
            isNeedAddress = true
            isSensorEnable = true
            isMockEnable = true
        }
        aMapLocationClient.setLocationOption(option)
    }


    /**
     * 获取百度鹰眼历史轨迹请求 [HistoryTrackRequest]
     *
     */
    private fun getHistoryTrackRequest(): HistoryTrackRequest {
        val processOption = ProcessOption().apply {
            transportMode = TransportMode.driving
            // 抽稀
            isNeedVacuate = false
            // 抽稀力度（取值范围[0,5]，0 为不抽稀，默认 1）
            // vacuateStrength = 1
            // 去噪力度（取值范围[0,5]，0 为不去噪， 默认 1）
            denoiseStrength = 3
            // 绑路（默认不绑路） isNeedMapMatch
            isNeedMapMatch = this@TrackingService.isNeedMapMatch
        }

        return HistoryTrackRequest().apply {
            coordTypeOutput = CoordType.gcj02
            serviceId = BAIDU_TRACE_SERVICE_ID
            entityName = TrackingService.entityName
            startTime = (currentLapStartTime / 1000) - 1
            endTime = System.currentTimeMillis() / 1000
            // 纠偏
            isProcessed = true
            setProcessOption(processOption)
            pageIndex = 1
            pageSize = 4888
        }
    }


    private fun startTimer() = lifecycleScope.launch {
        currentLapStartTime = System.currentTimeMillis()
        val timberStartTime = currentLapStartTime - runDurationInMilliseconds.value
        while (isActive) {
            _runDurationInMilliseconds.value = System.currentTimeMillis() - timberStartTime
            delay(1000)
        }
    }


    /**
     * Update notification
     * @return Job
     *
     */
    @SuppressLint("MissingPermission")
    private fun updateNotification() =
        collectLifecycleFlow(runDurationInHMSFormat) { runDurationInHMSFormat ->
            notification = NotificationCompat.Builder(this, notification)
                .setContentText(runDurationInHMSFormat).build()

            notificationManager.notify(FOREGROUND_SERVICE_NOTIFICATION_ID, notification)
        }


    /**
     * 将轨迹点 [Point] 上传到百度鹰眼
     *
     * @param entityName 轨迹所属的 entity
     * @param point 轨迹点
     * @param tag 请求标识
     */
    private fun uploadBdPoint(entityName: String, point: Point, tag: Int? = null) {
        val request = AddPointRequest()
        request.point = point
        request.entityName = entityName
        request.serviceId = BAIDU_TRACE_SERVICE_ID

        if (tag != null) {
            request.tag = tag
        }

        RunningDiaryApp.lbsTraceClient.addPoint(request, OnTrackListenerImpl)
    }


    /**
     * 当传感器的值发生变化时，系统会调用这个方法。在这个方法中可以读取传感器的值，并对其进行处理
     *
     * @param event 包含传感器的值、类型和精度等信息
     */
    override fun onSensorChanged(event: SensorEvent) {
        val latestStepCount = event.values[0].toInt()
        Timber.i("latestStepCount: $latestStepCount")
        if (!hasRecordedFirstStep) {
            hasRecordedFirstStep = true
            firstStepInCurrentLap = latestStepCount - stepCount.value
        } else {
            _stepCount.value = latestStepCount - firstStepInCurrentLap
        }
        Timber.i("stepCount: ${stepCount.value}")
    }


    /**
     * 当传感器的精度发生变化时，系统会调用这个方法。在这个方法中可以读取传感器的类型、精度等信息，并对其进行处理
     *
     * @param sensor 包含传感器的类型和精度等信息
     * @param accuracy 传感器的精度
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}


    /**
     * 定位回调监听，当定位完成后调用此方法
     *
     * @param aMapLocation 定位结果信息
     */
    override fun onLocationChanged(aMapLocation: AMapLocation?) {
        if (aMapLocation == null) return

        if (aMapLocation.errorCode == 0) {
            // 返回的 aMapLocation 有可能是缓存定位，所以重新赋值 time
            aMapLocation.time = System.currentTimeMillis()

            // 更新数据
            if (status.value == Status.STARTED) {
                _latestAMapLocation.value = aMapLocation
                _instantaneousSpeed.value = aMapLocation.speed

                // 上传轨迹点
                uploadBdPoint(entityName = entityName, point = aMapLocation.toBdPoint())
                // 查询历史纠偏轨迹
                RunningDiaryApp.lbsTraceClient.queryHistoryTrack(
                    getHistoryTrackRequest(),
                    OnTrackListenerImpl
                )
            }

            Timber.i("[定位成功] ${aMapLocation.toStr(1)}")
        } else {
            Timber.e("[定位异常] Error code: ${aMapLocation.errorCode}, Error info: ${aMapLocation.errorInfo}.")
        }
    }


    /**
     * 百度鹰眼轨迹监听器
     *
     */
    private object OnTrackListenerImpl : OnTrackListener() {

        /**
         * 添加轨迹点回调
         *
         * @param addPointResponse 添加轨迹点响应
         */
        override fun onAddPointCallback(addPointResponse: AddPointResponse) {
            super.onAddPointCallback(addPointResponse)
            with(addPointResponse) {
                if (status == 0) {
                    Timber.v("上传轨迹点成功")
                } else {
                    Timber.e("上传轨迹点异常 Status: $status, Message: $message, Tag: $tag")
                }
            }
        }


        /**
         * 查询历史轨迹回调
         *
         * @param response 历史轨迹响应
         */
        override fun onHistoryTrackCallback(response: HistoryTrackResponse) {
            super.onHistoryTrackCallback(response)
            if (response.status == 0) {
                // 更新数据
                if (status.value == Status.STARTED) {
                    // 防止用户在两秒内迅速点击暂停、开始按钮，导致旧的轨迹被添加到新的一圈上
                    _curLapBdTrackPoints.value =
                        response.trackPoints.filter { it.locTime > ((currentLapStartTime / 1000) - 3) }
                    _curLapDistanceInMeter.value = response.distance
                }

                Timber.d("[查询历史轨迹] Msg: ${response.message}, Distance: ${response.distance}, Original point size: ${response.trackPoints.size}, Filter point size: ${curLapBdTrackPoints.value.size}")
            } else {
                Timber.e("查询历史轨迹异常 Status: ${response.status}, Message: ${response.message}, Tag: ${response.tag}")
            }
        }
    }
}