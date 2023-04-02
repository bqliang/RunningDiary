package com.bqliang.running.diary.ui.detail

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import com.bqliang.running.diary.databinding.ActivityDetailBinding
import com.bqliang.running.diary.ui.base.BaseActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailActivity : BaseActivity() {

    companion object {
        const val EXTRA_RUN_ID = "run_id"
    }

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        intent.getLongExtra(EXTRA_RUN_ID, -1).also {
            viewModel.setRunId(it)
        }

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val myFragmentStateAdapter = MyFragmentStateAdapter(this)
        binding.viewPage2.adapter = myFragmentStateAdapter
        // 禁止滑动
        binding.viewPage2.isUserInputEnabled = false
        TabLayoutMediator(binding.tabLayout, binding.viewPage2) { tab, position ->
            when (position) {
                0 -> tab.text = "轨迹"
                1 -> tab.text = "图表"
                2 -> tab.text = "日记"
            }
        }.attach()
    }
}