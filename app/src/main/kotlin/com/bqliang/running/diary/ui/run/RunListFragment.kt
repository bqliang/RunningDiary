package com.bqliang.running.diary.ui.run

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.leancloud.LCUser
import com.bqliang.running.diary.databinding.FragmentRunListBinding
import com.bqliang.running.diary.ui.list.RunAdapter
import com.bqliang.running.diary.ui.list.RunListViewModel
import com.bqliang.running.diary.ui.main.MainViewModel
import com.bqliang.running.diary.utils.*
import com.google.android.material.divider.MaterialDividerItemDecoration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunListFragment : Fragment() {

    private var _binding: FragmentRunListBinding? = null
    private val binding: FragmentRunListBinding get() = _binding!!

    private lateinit var recyclerView: RecyclerView

    private val viewModel: RunListViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user: LCUser? = LCUser.currentUser()
        if (user == null) {
            navController.navigate(RunListFragmentDirections.actionRunListFragmentToNavGraphAuthentication())
        } else {
            activityViewModel.setUser(user)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRunListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabRun.setOnClickListener {
            requestPermission {
                navController.navigate(RunListFragmentDirections.actionRunListFragmentToTrackingActivity())
            }
        }

        recyclerView = binding.recyclerview

        val adapter = RunAdapter { run ->
            navController.navigate(
                RunListFragmentDirections.actionRunListFragmentToDetailActivity(
                    run.id
                )
            )
        }
        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.dividerInsetStart = dpToPx(130)
//        recyclerView.addItemDecoration(divider)
        recyclerView.adapter = adapter
        viewModel.allRuns(LCUser.currentUser().shortId).collectLifecycleFlow(this) { runs ->
            adapter.submitList(runs)

            if (runs.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.emptyText.visibility = View.VISIBLE
                if (!binding.emptyView.isAnimating) {
                    binding.emptyView.playAnimation()
                }
            } else {
                binding.emptyView.visibility = View.GONE
                binding.emptyText.visibility = View.GONE
                if (binding.emptyView.isAnimating) {
                    binding.emptyView.cancelAnimation()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}