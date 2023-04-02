package com.bqliang.running.diary.ui.course

import android.os.Build
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
import android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.addCallback
import com.bqliang.running.diary.databinding.ActivityVideoBinding
import com.bqliang.running.diary.ui.base.BaseActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class VideoActivity : BaseActivity(), Player.Listener {

    companion object {
        const val VIDEO_URI = "video_uri"
    }

    private lateinit var binding: ActivityVideoBinding
    private lateinit var player: ExoPlayer
    private val videoUriSample =
        "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            window.insetsController?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }

        // 拦截返回
        onBackPressedDispatcher.addCallback(this) {
            if (player.isPlaying) {
                MaterialAlertDialogBuilder(this@VideoActivity)
                    .setMessage("视频正在播放，确定要退出吗？")
                    .setNegativeButton("确定") { _, _ ->
                        if (!isFinishing) {
                            finish()
                        }
                    }
                    .setPositiveButton("取消", null)
                    .create()
                    .show()
            } else {
                if (!isFinishing) {
                    finish()
                }
            }
        }

        binding = ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player = ExoPlayer.Builder(this).build()
        player.addListener(this)
        binding.playerView.player = player

        val videoUri = intent.getStringExtra(VIDEO_URI) ?: videoUriSample
        val mediaItem = MediaItem.fromUri(videoUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }


    override fun onStop() {
        super.onStop()
        player.pause()
    }


    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }


    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_ENDED) {
            finish()
        }
    }
}