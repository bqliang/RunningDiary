package com.bqliang.running.diary.ui.body

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import cn.leancloud.LCUser
import com.bqliang.running.diary.database.dao.BodyDao
import com.bqliang.running.diary.databinding.FragmentBodyListBinding
import com.bqliang.running.diary.utils.collectLifecycleFlow
import com.bqliang.running.diary.utils.navController
import com.bqliang.running.diary.utils.shortId
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BodyListFragment : Fragment() {

    private var _binding: FragmentBodyListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BodyListViewModel by viewModels()

    @Inject
    lateinit var bodyDao: BodyDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBodyListBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        val bodyAdapter = BodyAdapter() { body ->
            navController.navigate(
                BodyListFragmentDirections.actionBodyListFragmentToAddBodyFragment(
                    body.id
                )
            )
        }

        viewModel.allBody(LCUser.currentUser().shortId).collectLifecycleFlow(this) { bodies ->
            bodyAdapter.submitList(bodies)
        }

        binding.bodyRecyclerView.adapter = bodyAdapter

        val bodyItemTouchHelperCallback = BodyItemTouchHelperCallback(bodyAdapter, requireContext()) { body ->
            lifecycleScope.launch {
                bodyDao.deleteBody(body)
            }
        }
        ItemTouchHelper(bodyItemTouchHelperCallback).attachToRecyclerView(binding.bodyRecyclerView)

        binding.fab.setOnClickListener { fab ->
            navController.navigate(
                BodyListFragmentDirections.actionBodyListFragmentToAddBodyFragment()
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}