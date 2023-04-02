package com.bqliang.running.diary

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import cn.leancloud.LCLogger
import cn.leancloud.LeanCloud
import com.amap.api.maps.MapsInitializer
import com.baidu.trace.LBSTraceClient
import com.google.android.material.color.DynamicColors
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


@SuppressLint("StaticFieldLeak")
@HiltAndroidApp
class RunningDiaryApp : Application() {

    /**
     *
     * @property context Application Context
     * @property lbsTraceClient 百度鹰眼轨迹服务客户端
     */
    companion object {
        lateinit var context: Context
        lateinit var lbsTraceClient: LBSTraceClient
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext

        // enable material dynamic color
        if (DynamicColors.isDynamicColorAvailable()) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }

        // initialize Timber
        Timber.plant(Timber.DebugTree())

        // initialize mmkv
        MMKV.initialize(applicationContext)

        // enable leanCloud sdk debug log
        if (BuildConfig.DEBUG) LeanCloud.setLogLevel(LCLogger.Level.DEBUG)

        // initialize LeanCloud
        LeanCloud.initialize(
            /* context = */ applicationContext,
            /* appId = */ LEANCLOUD_APP_ID,
            /* appKey = */ BuildConfig.LEANCLOUD_APP_KEY,
            /* serverURL = */ LEANCLOUD_APP_HOST
        )

        MapsInitializer.updatePrivacyShow(
            /* Context */ this,
            /* 隐私权政策是否包含高德开平隐私权政策 */ true,
            /* 隐私权政策是否弹窗展示告知用户 */ true
        )
        MapsInitializer.updatePrivacyAgree(
            /* Context */ this,
            /* 隐私权政策是否取得用户同意 */ true
        )

        // 同意隐私合规政策
        LBSTraceClient.setAgreePrivacy(applicationContext, true)
        // 初始化百度轨迹服务客户端
        try {
            lbsTraceClient = LBSTraceClient(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}