package com.bqliang.running.diary.ui.knowledge

import android.content.ComponentName
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.bqliang.running.diary.R
import com.bqliang.running.diary.ui.base.SimplePreference
import rikka.insets.*

class KnowledgeFragment : PreferenceFragmentCompat() {

    companion object {
        const val BASE_URL =
            "https://tips-p01-drcn.dbankcdn.cn/hwtips/topic/health_help_all/zh-CN/SF-10190178_f6186.html?v=100100190022&cid=11080&channel=04#SF-10190178_f6186__"
        const val STEP_RATE_URL = "${BASE_URL}06"
        const val STRIDE_URL = "${BASE_URL}07"
        const val TOUCHDOWN_TIME_URL = "${BASE_URL}09"
        const val FREE_TIME_URL = "${BASE_URL}10"
        const val TOUCHDOWN_RATIO_URL = "${BASE_URL}11"
        const val LANDING_IMPACT_URL = "${BASE_URL}19"
        const val LEFT_AND_RIGHT_TOUCHDOWN_BALANCE_URL = "${BASE_URL}16"
        const val VERTICAL_AMPLITUDE_URL = "${BASE_URL}15"
        const val VERTICAL_STRIDE_RATIO_URL = "${BASE_URL}17"
        const val TOUCHDOWN_PEAK_URL = "${BASE_URL}14"
        const val SHOCK_LOAD_RATE_URL = "${BASE_URL}18"
        const val EVERSION_URL = "${BASE_URL}12"
        const val SWING_ANGLE_URL = "${BASE_URL}13"
        const val LANDING_METHOD_URL = "${BASE_URL}01"
        const val PACE_URL = "${BASE_URL}04"
    }

    private var customTabsSession: CustomTabsSession? = null
    private var customTabsServiceBound = false

    private val customTabsServiceConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            customTabsServiceBound = true
            client.warmup(0)
            customTabsSession = client.newSession(null)
            customTabsSession?.mayLaunchUrl(
                Uri.parse("https://tips-p01-drcn.dbankcdn.cn/hwtips/topic/health_help_all/zh-CN/SF-10190178_f6186.html"),
                null,
                null
            )
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            customTabsSession = null
        }
    }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        val stepRate = SimplePreference(context).apply {
            key = "step_rate"
            title = "步频"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(STEP_RATE_URL)
                true
            }
        }

        val stride = SimplePreference(context).apply {
            key = "stride"
            title = "步幅"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(STRIDE_URL)
                true
            }
        }

        val touchdownTime = SimplePreference(context).apply {
            key = "touchdown_time"
            title = "触地时间"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(TOUCHDOWN_TIME_URL)
                true
            }
        }

        val freeTime = SimplePreference(context).apply {
            key = "free_time"
            title = "腾空时间"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(FREE_TIME_URL)
                true
            }
        }

        val touchdownRatio = SimplePreference(context).apply {
            key = "touchdown_ratio"
            title = "触地腾空比"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(TOUCHDOWN_RATIO_URL)
                true
            }
        }

        val landingImpact = SimplePreference(context).apply {
            key = "landing_impact"
            title = "着地冲击"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(LANDING_IMPACT_URL)
                true
            }
        }

        val leftAndRightTouchdownBalance = SimplePreference(context).apply {
            key = "left_and_right_touchdown_balance"
            title = "左右触地平衡"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(LEFT_AND_RIGHT_TOUCHDOWN_BALANCE_URL)
                true
            }
        }

        val verticalAmplitude = SimplePreference(context).apply {
            key = "vertical_amplitude"
            title = "垂直振幅"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(VERTICAL_AMPLITUDE_URL)
                true
            }
        }

        val verticalStrideRatio = SimplePreference(context).apply {
            key = "vertical_stride_ratio"
            title = "垂直步幅比"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(VERTICAL_STRIDE_RATIO_URL)
                true
            }
        }

        val touchdownPeak = SimplePreference(context).apply {
            key = "touchdown_peak"
            title = "触地峰值"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(TOUCHDOWN_PEAK_URL)
                true
            }
        }

        val shockLoadRate = SimplePreference(context).apply {
            key = "shock_load_rate"
            title = "冲击负载率"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(SHOCK_LOAD_RATE_URL)
                true
            }
        }

        val eversion = SimplePreference(context).apply {
            key = "eversion"
            title = "外翻幅度"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(EVERSION_URL)
                true
            }
        }

        val swingAngle = SimplePreference(context).apply {
            key = "swing_angle"
            title = "摆动角度"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(SWING_ANGLE_URL)
                true
            }
        }

        val landingMethod = SimplePreference(context).apply {
            key = "landing_method"
            title = "着地方式"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(LANDING_METHOD_URL)
                true
            }
        }

        val pace = SimplePreference(context).apply {
            key = "pace"
            title = "配速"
            setIcon(R.drawable.round_article_24)
            setOnPreferenceClickListener {
                openWebPageInCustomTab(PACE_URL)
                true
            }
        }


        screen.apply {
            addPreference(stepRate)
            addPreference(stride)
            addPreference(touchdownTime)
            addPreference(freeTime)
            addPreference(touchdownRatio)
            addPreference(landingImpact)
            addPreference(leftAndRightTouchdownBalance)
            addPreference(verticalAmplitude)
            addPreference(verticalStrideRatio)
            addPreference(touchdownPeak)
            addPreference(shockLoadRate)
            addPreference(eversion)
            addPreference(swingAngle)
            addPreference(landingMethod)
            addPreference(pace)
        }

        preferenceScreen = screen
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerview = view.findViewById<RecyclerView>(androidx.preference.R.id.recycler_view)
        recyclerview.clipToPadding = false

        ViewCompat.setOnApplyWindowInsetsListener(recyclerview) { view, windowInsets ->
            val bottomPadding = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            } else {
                @Suppress("DEPRECATION")
                windowInsets.systemWindowInsetBottom
            }
            view.updatePadding(
                bottom = bottomPadding
            )
            WindowInsetsCompat.CONSUMED
        }
    }


    override fun onStart() {
        super.onStart()
        val packageName = CustomTabsClient.getPackageName(requireContext(), null)
        if (packageName != null) {
            CustomTabsClient.bindCustomTabsService(
                requireContext(),
                packageName,
                customTabsServiceConnection
            )
        }
    }


    override fun onStop() {
        super.onStop()
        if (customTabsServiceBound) {
            requireContext().unbindService(customTabsServiceConnection)
            customTabsServiceBound = false
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        customTabsSession = null
    }


    private fun openWebPageInCustomTab(url: String) {

        val customTabsIntentBuilder = CustomTabsIntent.Builder()
            .setShowTitle(true)
//            .setCloseButtonIcon(
//                BitmapFactory.decodeResource(resources, R.drawable.round_arrow_back_24)
//            )
            .setStartAnimations(requireContext(), R.anim.slide_in_right, R.anim.slide_out_left)
            .setExitAnimations(requireContext(), R.anim.slide_in_left, R.anim.slide_out_right)

        customTabsSession?.also {
            customTabsIntentBuilder.setSession(it)
        }

        customTabsIntentBuilder.build()
            .launchUrl(requireContext(), Uri.parse(url))

    }
}