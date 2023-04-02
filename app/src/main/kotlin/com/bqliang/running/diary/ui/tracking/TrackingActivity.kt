package com.bqliang.running.diary.ui.tracking

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Rational
import android.util.Size
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.lifecycle.lifecycleScope
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions
import com.bqliang.running.diary.R
import com.bqliang.running.diary.databinding.ActivityTrackingBinding
import com.bqliang.running.diary.services.TrackingService
import com.bqliang.running.diary.services.TrackingService.Status
import com.bqliang.running.diary.ui.base.BaseActivity
import com.bqliang.running.diary.ui.base.BaseAlertDialogBuilder
import com.bqliang.running.diary.ui.detail.DetailActivity
import com.bqliang.running.diary.utils.collectLifecycleFlow
import com.bqliang.running.diary.utils.showToast
import com.bqliang.running.diary.utils.startActivity
import com.bqliang.running.diary.utils.toGdLatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tencent.mmkv.MMKV
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt

private const val ACTION_PIP_CONTROL = "com.bqliang.running.diary.ACTION_PIP_CONTROL"

private const val EXTRA_CONTROL_TYPE = "control_type"

private const val CONTROL_TYPE_ZOOM_IN = 1
private const val CONTROL_TYPE_ZOOM_OUT = 2
private const val CONTROL_TYPE_PAUSE_OR_RESUME = 3

private const val REQUEST_ZOOM_IN = 4
private const val REQUEST_ZOOM_OUT = 5
private const val REQUEST_PAUSE_OR_RESUME = 6

@AndroidEntryPoint
class TrackingActivity : BaseActivity(), LocationSource {

    private lateinit var binding: ActivityTrackingBinding

    private lateinit var aMap: AMap
    private var currentLapPolyline: Polyline? = null

    private val viewModel: TrackingViewModel by viewModels()

    private var onLocationChangedListener: LocationSource.OnLocationChangedListener? = null

    private var pipDataUpdateJob: Job? = null


    private lateinit var serviceBinder: TrackingService.TrackingBinder

    private val trackingServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.d("Tracking Service connected")
            serviceBinder = service as TrackingService.TrackingBinder
        }

        /**
         * 是在服务端崩溃或被杀死的时候被调用，绑定服务时并不会触发
         *
         */
        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    private val pipBroadcastReceiver = object : BroadcastReceiver() {
        // Called when an item is clicked.
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || intent.action != ACTION_PIP_CONTROL) return

            val type = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)
            when (type) {
                CONTROL_TYPE_ZOOM_IN -> aMap.animateCamera(CameraUpdateFactory.zoomIn())
                CONTROL_TYPE_ZOOM_OUT -> aMap.animateCamera(CameraUpdateFactory.zoomOut())
                CONTROL_TYPE_PAUSE_OR_RESUME -> {
                    serviceBinder.startOrPauseTracking()
                    updatePictureInPictureParams(TrackingService.status.value == Status.STARTED)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.TRANSPARENT

        binding = ActivityTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.data = TrackingService.Companion

        // 初始化地图
        initMap(savedInstanceState)

        val intent = Intent(this, TrackingService::class.java)
        // 启动 TrackingService
        startForegroundService(intent)

        // 初始化画中画
        initPip()

        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.trackingBottomSheetCardCollapsed.root.visibility = View.VISIBLE
                        binding.trackingBottomSheetCardExpanded.root.visibility = View.INVISIBLE
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.trackingBottomSheetCardCollapsed.root.visibility = View.INVISIBLE
                        binding.trackingBottomSheetCardExpanded.root.visibility = View.VISIBLE
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        binding.trackingBottomSheetCardCollapsed.root.visibility = View.INVISIBLE
                        binding.trackingBottomSheetCardExpanded.root.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        }
        BottomSheetBehavior.from(binding.standardBottomSheetContainer)
            .addBottomSheetCallback(bottomSheetCallback)


        binding.layerSwitchBtn.setOnClickListener {
            viewModel.switchMapType()
        }

        binding.switchDimensionBtn.setOnClickListener {
            viewModel.switchMapDimension()
        }


        binding.zoomBtns.addOnButtonCheckedListener { group, checkedId, _ ->
            group.clearChecked()
            val cameraUpdate = when (checkedId) {
                R.id.zoomInBtn -> CameraUpdateFactory.zoomIn()
                R.id.zoomOutBtn -> CameraUpdateFactory.zoomOut()
                else -> return@addOnButtonCheckedListener
            }
            aMap.animateCamera(cameraUpdate)
        }

        // 暂停
        binding.trackingBottomSheetCardExpanded.pauseBtn.setOnClickListener {
            serviceBinder.startOrPauseTracking()
            currentLapPolyline?.also {
                viewModel.previousLapPolylineList.add(it)
            }
            currentLapPolyline = null
        }

        // 点击结束运动按钮
        binding.trackingBottomSheetCardExpanded.stopBtn.setOnClickListener {
            "长按结束运动".showToast()
        }

        // 长按结束运动按钮
        binding.trackingBottomSheetCardExpanded.stopBtn.setOnLongClickListener {
            val warningMsg = when {
                TrackingService.previousLapsDistanceInMeter.value < 800 -> "距离过短"
                // TODO TrackingService.runDurationInMilliseconds.value < 8.minutes.inWholeMilliseconds -> "时间过短"
                else -> null
            }?.let { warning ->
                String.format("本次运动%s，将不会保存运动数据。是否结束运动？", warning)
            }

            if (warningMsg != null) {
                BaseAlertDialogBuilder(this)
                    .setCancelable(false)
                    .setMessage(warningMsg)
                    .setPositiveButton("继续运动") { _, _ ->
                        serviceBinder.startOrPauseTracking()
                    }
                    .setNegativeButton("结束运动") { _, _ ->
                        serviceBinder.stopTracking()
                        finish()
                    }
                    .create()
                    .show()
                return@setOnLongClickListener true
            } else {
                endRunAndSaveToDB()
            }
            return@setOnLongClickListener true
        }

        // 继续运动按钮
        binding.trackingBottomSheetCardExpanded.startBtn.setOnClickListener {
            serviceBinder.startOrPauseTracking()
        }


        onBackPressedDispatcher.addCallback {
            "退出请点击暂停按钮，再结束运动".showToast()
        }


        subscribeToObservers()

        registerReceiver(pipBroadcastReceiver, IntentFilter(ACTION_PIP_CONTROL))
    }


    override fun onStart() {
        super.onStart()
        Intent(this, TrackingService::class.java).also {
            bindService(it, trackingServiceConnection, 0)
        }
    }


    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }


    override fun onPause() {
        super.onPause()
        if (!isInPictureInPictureMode) {
            binding.mapView.onPause()
        }
    }


    override fun onStop() {
        super.onStop()
        unbindService(trackingServiceConnection)
    }


    override fun onDestroy() {
        super.onDestroy()
        viewModel.saveCameraPosition(aMap.cameraPosition)
        binding.mapView.onDestroy()
        serviceBinder.stopTracking()
        try {
            unbindService(trackingServiceConnection)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        unregisterReceiver(pipBroadcastReceiver)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }


    override fun activate(onLocationChangedListener: LocationSource.OnLocationChangedListener) {
        this@TrackingActivity.onLocationChangedListener = onLocationChangedListener
    }

    override fun deactivate() {
        onLocationChangedListener = null
    }


    private fun initMap(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        aMap = binding.mapView.map

        aMap.uiSettings.apply {
            // 不显示缩放按钮
            isZoomControlsEnabled = false
            // 显示罗盘
            isCompassEnabled = true
            // 隐藏比例尺
            isScaleControlsEnabled = false
            // 禁用倾斜手势
            isTiltGesturesEnabled = false
            // 隐藏定位按钮
            isMyLocationButtonEnabled = false
        }

        if (viewModel.cameraPosition != null) {
            aMap.moveCamera(CameraUpdateFactory.newCameraPosition(viewModel.cameraPosition))
        }

        viewModel.previousLapPolylineList.forEach { polyline ->
            val polylineOptions = getEmptyPolylineOptions().addAll(polyline.points)
            aMap.addPolyline(polylineOptions)
        }

        val rotateMap = MMKV.defaultMMKV().getBoolean(getString(R.string.pref_rotate_map_follow_orientation), false)
        val myLocationType = if (rotateMap) {
            MyLocationStyle.LOCATION_TYPE_MAP_ROTATE
        } else {
            MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE
        }

        aMap.myLocationStyle = MyLocationStyle().apply {
            myLocationType(myLocationType)
            strokeColor(Color.TRANSPARENT)
            radiusFillColor(Color.TRANSPARENT)
            strokeWidth(0f)
        }

        aMap.setLocationSource(this)
        aMap.isMyLocationEnabled = true
    }


    private fun subscribeToObservers() {

        viewModel.mapType.collectLifecycleFlow(this) { type ->
            if (type == AMap.MAP_TYPE_NIGHT) {
                setDarkStatusBar()
            } else {
                setLightStatusBar()
            }
        }

        // 有新的定位点则更新地图中心
        TrackingService.latestAMapLocation.collectLifecycleFlow(this) { aMapLocation ->
            if (TrackingService.status.value == Status.STARTED) {
                onLocationChangedListener?.onLocationChanged(aMapLocation)
            }
        }

        // 绘制当前圈的轨迹
        viewModel.curLapAMapLatLng.collectLifecycleFlow(this) { latLngs ->
            if (latLngs.size < 2 || TrackingService.status.value != Status.STARTED) return@collectLifecycleFlow
            if (currentLapPolyline == null) {
                val polylineOptions = getEmptyPolylineOptions().addAll(latLngs)
                currentLapPolyline = aMap.addPolyline(polylineOptions)
            } else {
                currentLapPolyline!!.points = latLngs
            }
        }

        // 已保存数据，跳转到详情页
        viewModel.runId.collectLifecycleFlow(this) { runId ->
            if (runId != null) {
                startActivity<DetailActivity>() {
                    putExtra(DetailActivity.EXTRA_RUN_ID, runId)
                }
                finish()
            }
        }
    }


    private fun initPip() {
        val isSupportPip =
            packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

        if (isSupportPip) {
            updatePictureInPictureParams(TrackingService.status.value == Status.STARTED)
            binding.pipBtn.visibility = View.VISIBLE
            binding.pipBtn.setOnClickListener {
                enterPictureInPictureMode(updatePictureInPictureParams(TrackingService.status.value == Status.STARTED))
            }
        }
    }


    /**
     * Updates the parameters of the picture-in-picture mode for this activity based on the current
     * [isTracking] state of the stopwatch.
     */
    private fun updatePictureInPictureParams(isTracking: Boolean): PictureInPictureParams {
        val mapRect = Rect()
        binding.mapView.getGlobalVisibleRect(mapRect)
        val aspectRatio = Rational(3, 4)
        val builder = PictureInPictureParams.Builder()
            .setActions(
                listOf(
                    // "ZOOM IN" action.
                    createRemoteAction(
                        R.drawable.round_add_24,
                        "ZOOM IN",
                        REQUEST_ZOOM_IN,
                        CONTROL_TYPE_ZOOM_IN
                    ),
                    if (isTracking) {
                        // "Pause" action when the stopwatch is already started.
                        createRemoteAction(
                            R.drawable.round_pause_24,
                            "PAUSE",
                            REQUEST_PAUSE_OR_RESUME,
                            CONTROL_TYPE_PAUSE_OR_RESUME
                        )
                    } else {
                        // "Start" action when the stopwatch is not started.
                        createRemoteAction(
                            R.drawable.round_play_arrow_24,
                            "START",
                            REQUEST_PAUSE_OR_RESUME,
                            CONTROL_TYPE_PAUSE_OR_RESUME
                        )
                    },
                    createRemoteAction(
                        R.drawable.round_remove_24,
                        "ZOOM OUT",
                        REQUEST_ZOOM_OUT,
                        CONTROL_TYPE_ZOOM_OUT
                    )
                )
            )
            // Set the aspect ratio of the picture-in-picture mode.
            .setAspectRatio(aspectRatio)
            // Specify the portion of the screen that turns into the picture-in-picture mode.
            // This makes the transition animation smoother.
            .setSourceRectHint(mapRect)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Turn the screen into the picture-in-picture mode if it's hidden by the "Home" button.
            builder.setAutoEnterEnabled(true)
                // Disables the seamless resize. The seamless resize works great for videos where the
                // content can be arbitrarily scaled, but you can disable this for non-video content so
                // that the picture-in-picture mode is resized with a cross fade animation.
                .setSeamlessResizeEnabled(false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            builder.setTitle("运动中")
        }
        val params = builder.build()
        setPictureInPictureParams(params)
        return params
    }


    /**
     * Creates a [RemoteAction]. It is used as an action icon on the overlay of the
     * picture-in-picture mode.
     */
    private fun createRemoteAction(
        @DrawableRes iconResId: Int,
        /* @StringRes titleResId: Int, */ title: String,
        requestCode: Int,
        controlType: Int
    ): RemoteAction = RemoteAction(
        /* icon = */ Icon.createWithResource(this, iconResId),
        /* title = */ title,
        /* contentDescription = */ title,
        /* intent = */ PendingIntent.getBroadcast(
            this,
            requestCode,
            Intent(ACTION_PIP_CONTROL).putExtra(EXTRA_CONTROL_TYPE, controlType),
            PendingIntent.FLAG_IMMUTABLE
        )
    )


    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            // 关闭指南针
            aMap.uiSettings.isCompassEnabled = false
            binding.fullScreenUi.visibility = View.GONE
            binding.standardBottomSheetContainer.visibility = View.GONE
            binding.pipUi.visibility = View.VISIBLE
            pipDataUpdateJob = lifecycleScope.launch {
                while (isActive) {
                    delay(2000)
                    binding.pipDistance.text =
                        String.format("%.2f KM", viewModel.totalDistanceInMeter.value / 1000)
                }
            }
        } else {
            pipDataUpdateJob?.cancel()
            pipDataUpdateJob = null
            aMap.uiSettings.isCompassEnabled = true
            binding.fullScreenUi.visibility = View.VISIBLE
            binding.standardBottomSheetContainer.visibility = View.VISIBLE
            binding.pipUi.visibility = View.GONE
        }
    }


    private fun getEmptyPolylineOptions(): PolylineOptions {
        return PolylineOptions()
            .color(Color.parseColor("#00DEAE"))
            .width(25f)
    }


    /**
     * 缩放地图到整个轨迹
     * @return Size 轨迹的宽高
     *
     */
    private fun zoomToWholeTrack() = flow {

        val bounds = LatLngBounds.Builder()

        TrackingService.previousLapsBdTrackPoints.value
            .flatten()
            .map { trackPoints ->
                trackPoints.toGdLatLng()
            }.forEach {
                bounds.include(it)
            }

        val aspectRatio = 64.00 / 114.00
        val width = binding.mapView.width
        val height = (binding.mapView.width * aspectRatio).roundToInt()

        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(
            /* Bounds */ bounds.build(),
            /* 宽 */ width,
            /* 高 */ height,
            /* padding */ 80
        )

        val size = suspendCoroutine { continuation ->
            aMap.stopAnimation()
            aMap.animateCamera(
                cameraUpdate,
                object : AMap.CancelableCallback {

                    override fun onFinish() {
                        val size = Size(width, height)
                        continuation.resume(size)
                    }

                    override fun onCancel() {}
                })
        }

        emit(size)
    }


    private suspend fun getMapScreen(bounds: Size) = flow {

        val bitmap = suspendCoroutine { continuation ->
            aMap.getMapScreenShot(object : AMap.OnMapScreenShotListener {
                override fun onMapScreenShot(screenShot: Bitmap) {
                    // 从中间截取轨迹部分
                    val y = screenShot.height / 2 - bounds.height / 2
                    val clipBitmap =
                        Bitmap.createBitmap(screenShot, 0, y, screenShot.width, bounds.height)
                    val matrix = Matrix()

                    // 缩放大小
                    val scale = 350f / clipBitmap.width
                    matrix.setScale(scale, scale)
                    val bitmap = Bitmap.createBitmap(
                        clipBitmap,
                        0,
                        0,
                        clipBitmap.width,
                        clipBitmap.height,
                        matrix,
                        true
                    )
                    continuation.resume(bitmap)
                }

                override fun onMapScreenShot(bitmap: Bitmap, status: Int) {

                }
            })
        }

        emit(bitmap)
    }


    private fun endRunAndSaveToDB() {
        // 禁用手势
        aMap.uiSettings.isScrollGesturesEnabled = false
        // 隐藏 bottom sheet
        binding.standardBottomSheetContainer.visibility = View.GONE

        zoomToWholeTrack()
            .flatMapConcat { size ->
                getMapScreen(size)
            }.onEach { bitmap ->
                viewModel.insertRun(bitmap)
            }.launchIn(lifecycleScope)
    }
}