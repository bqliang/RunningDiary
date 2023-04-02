package com.bqliang.running.diary.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bqliang.running.diary.databinding.FragmentStatisticsBinding
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
        returnTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListener()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun initListener() {
        val str = "%.2f/%d (%d%%)"

        binding.progressViewWeek.setOnProgressChangeListener {
            binding.progressViewWeek.labelText = str.format(
                it,
                viewModel.weekDistanceGoalInKm.value,
                (it / viewModel.weekDistanceGoalInKm.value * 100).toInt()
            )
        }

        binding.progressViewMonth.setOnProgressChangeListener {
            binding.progressViewMonth.labelText = str.format(
                it,
                viewModel.monthDistanceGoalInKm.value,
                (it / viewModel.monthDistanceGoalInKm.value * 100).toInt()
            )
        }

        binding.progressViewYear.setOnProgressChangeListener {
            binding.progressViewYear.labelText = str.format(
                it,
                viewModel.yearDistanceGoalInKm.value,
                (it / viewModel.yearDistanceGoalInKm.value * 100).toInt()
            )
        }
    }

}