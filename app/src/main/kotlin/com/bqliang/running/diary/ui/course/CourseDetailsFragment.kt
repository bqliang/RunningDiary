package com.bqliang.running.diary.ui.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bqliang.running.diary.databinding.FragmentCourseDetailsBinding
import com.bqliang.running.diary.utils.startActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority

class CourseDetailsFragment: Fragment() {

    private var _binding: FragmentCourseDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: CourseDetailsFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentCourseDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val course = args.course
        Glide.with(this)
            .load(course.imageUrl)
            .priority(Priority.HIGH)
            .into(binding.imageView)
        binding.tvShortDesc.text = course.shortDescription
        binding.tvLevel.text = course.levelString
        binding.tvDuration.text = "${course.distanceInMinute} 分钟"
        binding.tvCalories.text = "${course.caloriesInKcal} 千卡"

        val fitnessActionAdapter = FitnessActionAdapter(course.fitnessActions) { index->
            requireContext().startActivity<ActionsActivity>(){
                putParcelableArrayListExtra(ActionsActivity.FITNESS_ACTIONS, ArrayList(course.fitnessActions))
                putExtra(ActionsActivity.CURRENT_ACTION_INDEX, index)
            }
        }

        binding.recyclerView.adapter = fitnessActionAdapter

        binding.button.setOnClickListener {
           requireContext().startActivity<VideoActivity> {
                putExtra(VideoActivity.VIDEO_URI, course.videoUrl)
           }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}