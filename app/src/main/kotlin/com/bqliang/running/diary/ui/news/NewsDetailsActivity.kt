package com.bqliang.running.diary.ui.news

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import com.bqliang.running.diary.databinding.ActivityNewsDetailsBinding
import com.bqliang.running.diary.ui.base.BaseActivity
import com.bqliang.running.diary.utils.showToast
import com.bumptech.glide.Glide
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import javax.inject.Inject


@AndroidEntryPoint
class NewsDetailsActivity : BaseActivity() {

    companion object {
        const val NEWS_TITLE = "news_title"
        const val NEWS_IMAGE_URL = "news_image_url"
        const val NEWS_MD_CONTENT = "news_md_content"
    }

    @Inject
    lateinit var markwon: Markwon
    private lateinit var binding: ActivityNewsDetailsBinding
    private var isLike = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = if (isNightModeActive()) {
            Color.parseColor("#B3000000")
        } else {
            Color.parseColor("#B3FFFFFF")
        }


        binding = ActivityNewsDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra(NEWS_TITLE)
        val newsMdContent = intent.getStringExtra(NEWS_MD_CONTENT) ?: ""
        val newsImageUrl = intent.getStringExtra(NEWS_IMAGE_URL) ?: ""

        binding.toolbar.setNavigationOnClickListener { finish() }
        markwon.setMarkdown(binding.tvContent, newsMdContent)
        Glide.with(this).load(newsImageUrl).into(binding.ivNewsImage)
        binding.toolbar.title = title

        binding.fab.setOnClickListener {
            isLike = !isLike
            binding.fab.imageTintList = if (isLike) {
                "点赞成功".showToast()
                ColorStateList.valueOf(Color.RED)
            } else {
                "取消点赞".showToast()
                ColorStateList.valueOf(
                    MaterialColors.getColor(
                        applicationContext,
                        com.google.android.material.R.attr.colorControlNormal,
                        Color.BLACK
                    )
                )
            }
        }
    }
}