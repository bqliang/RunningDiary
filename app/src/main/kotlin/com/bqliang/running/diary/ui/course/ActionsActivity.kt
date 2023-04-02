package com.bqliang.running.diary.ui.course

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.bqliang.running.diary.databinding.ActivityActionsBinding
import com.bqliang.running.diary.ui.base.BaseActivity
import com.bqliang.running.diary.ui.course.model.FitnessAction
import com.bqliang.running.diary.utils.collectLifecycleFlow
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

class ActionsActivity : BaseActivity() {

    companion object {
        const val FITNESS_ACTIONS = "fitness_actions"
        const val CURRENT_ACTION_INDEX = "current_action_index"
    }

    private val viewModel: ActionsViewModel by viewModels()
    private lateinit var binding: ActivityActionsBinding
    private lateinit var player: Player

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 已经在 xml 布局里设置了 edgeToEdge = true
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        setLightStatusBar()

        binding = ActivityActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.fitnessActionList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(FITNESS_ACTIONS, FitnessAction::class.java)!!
        } else {
            @Suppress("deprecation")
            intent.getParcelableArrayListExtra(FITNESS_ACTIONS)!!
        }

        if (viewModel.actionIndex.value < 0) {
            viewModel.actionIndex.value = intent.getIntExtra(CURRENT_ACTION_INDEX, 0)
        }


        player = ExoPlayer.Builder(this).build()
        player.setVideoSurfaceView(binding.surfaceView)
        player.repeatMode = Player.REPEAT_MODE_ONE
        val mediaItems = viewModel.fitnessActionList.map { MediaItem.fromUri(it.videoUrl) }
        player.setMediaItems(mediaItems, viewModel.actionIndex.value, 0)
        player.prepare()
        player.play()

        viewModel.actionIndex.collectLifecycleFlow(this) { index ->
            player.seekTo(index, 0)
            binding.tvActionName.text = viewModel.fitnessActionList[index].name
            binding.tvActionDesc.text = viewModel.fitnessActionList[index].description
            binding.tvPage.text = "${index + 1}/${viewModel.fitnessActionList.size}"

            if (index == 0) {
                binding.previousBtn.visibility = View.INVISIBLE
            } else {
                binding.previousBtn.visibility = View.VISIBLE
            }

            if (index == viewModel.fitnessActionList.size - 1) {
                binding.nextBtn.visibility = View.INVISIBLE
            } else {
                binding.nextBtn.visibility = View.VISIBLE
            }
        }

        binding.previousBtn.setOnClickListener {
            viewModel.minusIndex()
        }

        binding.nextBtn.setOnClickListener {
            viewModel.addIndex()
        }
    }


    override fun onStop() {
        super.onStop()
        player.pause()
    }


    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}