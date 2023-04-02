package com.bqliang.running.diary.ui.body

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import cn.leancloud.LCUser
import com.bqliang.running.diary.databinding.DialogFloatPickerBinding
import com.bqliang.running.diary.databinding.FragmentAddBodyBinding
import com.bqliang.running.diary.utils.collectLifecycleFlow
import com.bqliang.running.diary.utils.navController
import com.bqliang.running.diary.utils.shortId
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class AddBodyFragment : Fragment() {

    private var _binding: FragmentAddBodyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddBodyViewModel by viewModels()
    private val args: AddBodyFragmentArgs by navArgs()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBodyBinding.inflate(inflater, container, false)

        lifecycleScope.launch(Dispatchers.IO) {
            if (args.bodyId < 0) {
                binding.fabSave.text = "保存"
                val latestBody = viewModel.getLatestBody(LCUser.currentUser().shortId)
                if (latestBody != null) {
                    viewModel.heightInCm.value = latestBody.heightInCm
                    viewModel.weightInKg.value = latestBody.weightInKg
                }
                viewModel.dateInMs.value = getTodayMs()
            } else {
                binding.fabSave.text = "更新"
                val curBody = viewModel.getBodyById(args.bodyId)
                viewModel.oldBody = curBody
                viewModel.heightInCm.value = curBody.heightInCm
                viewModel.weightInKg.value = curBody.weightInKg
                viewModel.dateInMs.value = curBody.dateInMillisSeconds
            }
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.successful.collectLifecycleFlow(this) {
            navController.popBackStack()
        }

        binding.itemDate.setOnClickListener {
            showDatePickerDialog(viewModel.dateInMs.value) { utcMs ->
                viewModel.dateInMs.value = utcMs
            }
        }

        binding.itemHeight.setOnClickListener {
            showFloatDialog("身高", viewModel.heightInCm.value, 120, 200, "cm") { value ->
                viewModel.heightInCm.value = value
            }
        }

        binding.itemWeight.setOnClickListener {
            showFloatDialog("体重", viewModel.weightInKg.value, 36, 125, "kg") { value ->
                viewModel.weightInKg.value = value
            }
        }

        binding.fabSave.setOnClickListener {
            if (args.bodyId < 0) {
                viewModel.insertBody()
            } else {
                viewModel.updateBody()
            }
        }
    }


    private fun showFloatDialog(
        title: String,
        currentValue: Float,
        min: Int,
        max: Int,
        unitText: String,
        block: (Float) -> Unit
    ) {
        val binding = DialogFloatPickerBinding.inflate(layoutInflater, null, false).apply {
            integer.minValue = min
            integer.maxValue = max
            decimal.minValue = 0
            decimal.maxValue = 9
            integer.value = currentValue.toInt()
            decimal.minValue = ((currentValue - currentValue.toInt()) * 10).toInt()
            suffix.text = unitText
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(binding.root)
            .setPositiveButton("确定") { _, _ ->
                val integer = binding.integer.value
                val decimal = binding.decimal.value
                val value = "$integer.$decimal".toFloat()
                block(value)
            }
            .setNegativeButton("取消", null)
            .create()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dialog.window?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                attributes.blurBehindRadius = 64
            }
        }

        dialog.show()
    }


    private fun showDatePickerDialog(defaultSelectionInUtcMs: Long, onSelect:(Long) -> Unit) {
        val defaultSelectionInLocalMs = defaultSelectionInUtcMs + TimeZone.getDefault().rawOffset

        val calendarConstraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(System.currentTimeMillis() + TimeZone.getDefault().rawOffset))
            .build()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a date")
            .setSelection(defaultSelectionInLocalMs)
            .setCalendarConstraints(calendarConstraints)
            .build()

        // 设置动画
        datePicker.enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        datePicker.returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        datePicker.addOnPositiveButtonClickListener { localMs ->
            val utcMs = localMs - TimeZone.getDefault().rawOffset
            onSelect(utcMs)
        }

        datePicker.show(childFragmentManager, "datePicker")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun getTodayMs(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}