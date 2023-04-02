package com.bqliang.running.diary.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bqliang.running.diary.BuildConfig
import com.bqliang.running.diary.R
import com.bqliang.running.diary.databinding.ActivityMainBinding
import com.bqliang.running.diary.ui.base.BaseActivity
import com.bqliang.running.diary.ui.body.BodyListFragmentDirections
import com.bqliang.running.diary.ui.course.CourseDetailsFragmentArgs
import com.bqliang.running.diary.ui.intro.IntroActivity
import com.bqliang.running.diary.utils.collectLifecycleFlow
import com.bqliang.running.diary.utils.startActivity
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.distribute.Distribute
import com.tencent.mmkv.MMKV
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var drawerHeaderUsername: MaterialTextView
    private lateinit var drawerHeaderEmail: MaterialTextView
    private lateinit var drawerHeaderAvatar: ShapeableImageView


    override fun onCreate(savedInstanceState: Bundle?) {

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        super.onCreate(savedInstanceState)

        val firstStart = MMKV.defaultMMKV().getBoolean(IntroActivity.FIRST_START, true)
        if (firstStart) {
            startActivity<IntroActivity>() {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val drawerHeader = binding.navigationView.getHeaderView(0)
        drawerHeaderUsername = drawerHeader.findViewById(R.id.headline)
        drawerHeaderEmail = drawerHeader.findViewById(R.id.subhead)
        drawerHeaderAvatar = drawerHeader.findViewById(R.id.avatar)

        viewModel.email.collectLifecycleFlow(this) { email ->
            drawerHeaderEmail.text = email
        }

        viewModel.username.collectLifecycleFlow(this) { username ->
            drawerHeaderUsername.text = username
        }

        viewModel.avatarUrl.collectLifecycleFlow(this) { url ->
            Glide.with(this).load(url).into(drawerHeaderAvatar)
        }

        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.run_list_fragment,
                R.id.body_list_fragment,
                R.id.news_fragment,
                R.id.course_list_fragment
            ), binding.drawerLayout
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        binding.navigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { controller, destination, bundle ->

            if (destination.id == R.id.course_details_fragment && bundle != null) {
                binding.toolbar.title = CourseDetailsFragmentArgs.fromBundle(bundle).course.name
            }

            val hideToolbar = destination.parent?.id == R.id.nav_graph_authentication
            if (hideToolbar) {
                binding.toolbar.visibility = android.view.View.INVISIBLE
            } else {
                binding.toolbar.visibility = android.view.View.VISIBLE
            }

            if (destination.id == R.id.body_list_fragment) {
                // add menus on toolbar
                binding.toolbar.inflateMenu(R.menu.menu_body_list)
            } else {
                // remove menus on toolbar
                binding.toolbar.menu.clear()
            }
        }


        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.menu_weight_chart) {
                navController.navigate(BodyListFragmentDirections.actionBodyListFragmentToWeightChartActivity())
            }
            true
        }


        AppCenter.start(
            application, BuildConfig.APP_CENTER_SECRET,
            Analytics::class.java, Crashes::class.java, Distribute::class.java
        )
    }


    @SuppressLint("RtlHardcoded")
    override fun onResume() {
        super.onResume()
        // 因为 drawer 配合 navigation 打开 activity 时不会自动关闭 drawer，所以需要在 onResume 时 close drawer
        binding.drawerLayout.closeDrawer(
            /* gravity = */ Gravity.LEFT,
            /* animate = */ false
        )
    }
}