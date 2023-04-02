package com.bqliang.running.diary.ui.about

import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.bqliang.running.diary.BuildConfig
import com.bqliang.running.diary.R
import com.bqliang.running.diary.RunningDiaryApp
import com.bqliang.running.diary.utils.setRepeatClickListener
import com.drakeet.about.*
import kotlinx.coroutines.launch


class AboutActivity : AbsAboutActivity() {

    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {
        val appIcon = packageManager.getApplicationIcon(packageName)
        icon.setImageDrawable(appIcon)
        slogan.text = "跑步日记，记录健康与美好"
        version.text = BuildConfig.VERSION_NAME

        icon.setRepeatClickListener {
            lifecycleScope.launch {
                RunningDiaryApp.logoutAndRestartApp()
            }
        }
    }

    override fun onItemsCreated(items: MutableList<Any>) {
        items.apply {
            add(Category("介绍与帮助"))
            add(
                Card(
                    """
                跑步，一项简单又充满乐趣的运动。我们为您打造了一款全方位的跑步应用，让您的运动更加科学、有趣。

                使用我们的应用，您可以记录您的跑步轨迹，了解您的运动状态，让您更好的管理和规划您的运动。同时，我们为您提供了周、月、年的目标进度，让您时刻掌握自己的运动进程。

                除此之外，我们还为您提供了海量的运动课程和资讯，让您随时随地学习运动知识，不断提升自己。还有，我们还提供了管理体重的功能，帮助您更好的掌控自己的身体健康。

                这是一款充满爱心和用心的应用，我们一直在努力为您提供更好的服务和体验。希望您能够喜欢我们的应用，享受到我们的服务带来的快乐和健康！
            """.trimIndent()
                )
            )


            add(Category("Developers"))
            add(
                Contributor(
                    R.drawable.dev_avatar,
                    "bqliang",
                    "开发者 & 设计师",
                    "https://github.com/bqliang"
                )
            )



            add(Category("Open Source Licenses"))
            add(
                License(
                    "about-page",
                    "drakeet",
                    License.APACHE_2,
                    "https://github.com/drakeet/about-page"
                )
            )
            add(
                License(
                    "PermissionX",
                    "guolindev",
                    License.APACHE_2,
                    "https://github.com/guolindev/PermissionX"
                )
            )
            add(
                License(
                    "Lottie Android",
                    "airbnb",
                    License.APACHE_2,
                    "https://github.com/airbnb/lottie-android"
                )
            )
            add(
                License(
                    "ExoPlayer",
                    "google",
                    License.APACHE_2,
                    "https://github.com/google/ExoPlayer"
                )
            )
            add(
                License(
                    "MMKV",
                    "Tencent",
                    License.APACHE_2,
                    "https://github.com/Tencent/MMKV"
                )
            )
            add(
                License(
                    "Glide",
                    "bumptech",
                    License.APACHE_2,
                    "https://github.com/bumptech/glide"
                )
            )
            add(
                License(
                    "Material Components for Android",
                    "google",
                    License.APACHE_2,
                    "https://github.com/material-components/material-components-android"
                )
            )
            add(
                License(
                    "Timber",
                    "JakeWharton",
                    License.APACHE_2,
                    "https://github.com/JakeWharton/timber"
                )
            )
            add(
                License(
                    "Kotlin",
                    "JetBrains",
                    License.APACHE_2,
                    "https://github.com/JetBrains/kotlin"
                )
            )
            add(
                License(
                    "Markwon",
                    "noties",
                    License.APACHE_2,
                    "https://github.com/noties/Markwon"
                )
            )
            add(
                License(
                    "AppIntro",
                    "cortinico",
                    License.APACHE_2,
                    "https://github.com/AppIntro/AppIntro"
                )
            )
        }
    }
}