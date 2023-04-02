package com.bqliang.running.diary.ui.document

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.bqliang.running.diary.BuildConfig
import com.bqliang.running.diary.R
import com.bqliang.running.diary.ui.base.SimplePreference
import com.bqliang.running.diary.utils.getInstallPackageName
import com.bqliang.running.diary.utils.showToast

class HelpAndSupportFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = preferenceManager.context
        val screen = preferenceManager.createPreferenceScreen(context)

        val trackDriftQuestion = SimplePreference(context).apply {
            key = "track_drift_question"
            title = "教程"
            setIcon(R.drawable.round_article_24)
        }

        val faqCategory = PreferenceCategory(context)
        faqCategory.key = "faq_category"
        faqCategory.title = "常见问题"
        screen.addPreference(faqCategory)
        faqCategory.addPreference(trackDriftQuestion)

        val sendEmailPreference = SimplePreference(context).apply {
            key = "send_email"
            title = "发送邮件"
            setIcon(R.drawable.round_email_24)
        }
        sendEmailPreference.setOnPreferenceClickListener {
            feedback()
            true
        }

        val contactUsCategory = PreferenceCategory(context)
        contactUsCategory.key = "contact_us_category"
        contactUsCategory.title = "联系我们"
        screen.addPreference(contactUsCategory)
        contactUsCategory.addPreference(sendEmailPreference)

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


    private fun feedback() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("bqliang@outlook.com"))
            putExtra(Intent.EXTRA_TITLE, "[建议反馈] 邮件")
            putExtra(Intent.EXTRA_SUBJECT, "[建议反馈] 跑步日记")
            putExtra(Intent.EXTRA_TEXT, """
                ------------------
                app 名称: ${getString(R.string.app_name)}
                app 版本: ${BuildConfig.VERSION_NAME}
                设备 Android 版本: ${Build.VERSION.SDK_INT}
                设备型号: ${Build.MODEL}
                安装来源: ${requireContext().getInstallPackageName()}
                ------------------
                
                请勿删改以上内容，否则邮件将被自动删除。
                在此处输入您的建议或反馈: 
            """.trimIndent())
        }

        try {
            startActivity(Intent.createChooser(intent, "选择一个邮件客户端"))
        } catch (e: ActivityNotFoundException) {
            "没有找到邮件客户端".showToast()
        }
    }
}