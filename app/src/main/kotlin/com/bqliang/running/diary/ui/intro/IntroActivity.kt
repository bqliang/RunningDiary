package com.bqliang.running.diary.ui.intro

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bqliang.running.diary.R
import com.bqliang.running.diary.ui.main.MainActivity
import com.bqliang.running.diary.utils.startActivity
import com.github.appintro.AppIntro2
import com.github.appintro.AppIntroFragment
import com.tencent.mmkv.MMKV
import rikka.insets.WindowInsetsHelper
import rikka.layoutinflater.view.LayoutInflaterFactory


/**
 * appintro_intro_layout2.xml: https://github.com/AppIntro/AppIntro/blob/7e38a4ded21a8897e55d2608f261030022d2fea9/appintro/src/main/res/layout/appintro_intro_layout2.xml
 * appintro_fragment_intro.xml: https://github.com/AppIntro/AppIntro/blob/7e38a4ded21a8897e55d2608f261030022d2fea9/appintro/src/main/res/layout/appintro_fragment_intro.xml
 *
 */
class IntroActivity : AppIntro2() {

    companion object {
        const val FIRST_START = "first_start"
    }

    override val layoutId: Int
        get() = R.layout.custom_appintro_intro_layout2


    override fun onCreate(savedInstanceState: Bundle?) {
        // rikkaX insert need
        layoutInflater.factory2 =
            LayoutInflaterFactory(delegate).addOnViewCreatedListener(WindowInsetsHelper.LISTENER)
        super.onCreate(savedInstanceState)

        showStatusBar(true)
        setStatusBarColor(Color.TRANSPARENT)
        setNavBarColor(Color.TRANSPARENT)

        // 颜色过渡
        isColorTransitionsEnabled = true
        // 指示器
        isIndicatorEnabled = true
        //  Switch from Dotted Indicator to Progress Indicator
        // setProgressIndicator()
        // 触感反馈
        isVibrate = true
        vibrateDuration = 10
        // 左右前进后退，不允许跳过
        isWizardMode = true

        addSlide(
            AppIntroFragment.createInstance(
                title = "欢迎使用跑步日记！",
                description = "记录你的运动轨迹、管理身高体重、参加各种运动课程、获取最新的运动资讯以及统计跑步信息。让我们一起打造健康的生活方式！",
                backgroundColorRes = R.color.intro_app_slide_1_background,
                imageDrawable = R.drawable.fitness_tracker
            )
        )

        addSlide(
            AppIntroFragment.createInstance(
                title = "记录你的运动轨迹",
                description = "记录你的运动轨迹？没问题，我们的App可以让你轻松实现！从此，你可以在地图上看到自己奔跑的轨迹，感受自己的成长与进步。你会发现，每一步都比前一步更加强大、更加优秀！",
                backgroundColorRes = R.color.intro_app_slide_2_background,
                imageDrawable = R.drawable.destination
            )
        )

        addSlide(
            AppIntroFragment.createInstance(
                title = "跑步信息统计",
                description = "统计你的跑步信息，包括步数、距离、时间、卡路里等数据。通过这些数据，你可以更好地了解自己的运动情况和进步，让你的运动更加有动力。",
                backgroundColorRes = R.color.intro_app_slide_3_background,
                imageDrawable = R.drawable.visual_data
            )
        )

        addSlide(
            AppIntroFragment.createInstance(
                title = "身高体重管理",
                description = "身高体重管理？别担心，我们的App可以帮你轻松管理！通过我们的App，你可以记录自己的身高体重数据，并根据数据获取健康建议和运动计划。因此，你会变得更加健康、更加自信、更加精力充沛！",
                backgroundColorRes = R.color.intro_app_slide_4_background,
                imageDrawable = R.drawable.stock_prices
            )
        )

        addSlide(
            AppIntroFragment.createInstance(
                title = "丰富运动课程",
                description = "我们的 app 提供各种运动课程，包括跑步、瑜伽、健身等。专业教练制定，让你可以在家里轻松学习各种运动技能，达到健身效果。",
                backgroundColorRes = R.color.intro_app_slide_5_background,
                imageDrawable = R.drawable.personal_trainer
            )
        )

        addSlide(
            AppIntroFragment.createInstance(
                title = "运动资讯",
                description = "提供最新的运动资讯，包括健康饮食、运动知识等。我们的资讯由专业的健身人士撰写，让你可以了解最新的健身知识和热门话题。",
                backgroundColorRes = R.color.intro_app_slide_6_background,
                imageDrawable = R.drawable.browsing_online
            )
        )
    }


    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        val mmkv = MMKV.defaultMMKV()
        mmkv.putBoolean(FIRST_START, false)

        startActivity<MainActivity>() {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    }
}