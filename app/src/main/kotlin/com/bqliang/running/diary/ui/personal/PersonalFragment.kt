package com.bqliang.running.diary.ui.personal

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import cn.leancloud.LCFile
import cn.leancloud.LCUser
import com.bqliang.running.diary.R
import com.bqliang.running.diary.databinding.EditTextBinding
import com.bqliang.running.diary.databinding.FragmentPersonalBinding
import com.bqliang.running.diary.ui.base.BaseAlertDialogBuilder
import com.bqliang.running.diary.ui.main.MainViewModel
import com.bqliang.running.diary.utils.*
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.style.PictureWindowAnimationStyle
import com.permissionx.guolindev.PermissionX
import com.yalantis.ucrop.UCrop
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.*


class PersonalFragment : Fragment() {

    private var _binding: FragmentPersonalBinding? = null
    private val binding get() = _binding!!
    private val activityViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentPersonalBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = activityViewModel
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()

        binding.shortId.text = LCUser.currentUser().shortId

        activityViewModel.birth.collectLifecycleFlow(this) { birth ->
            val birthFormatStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(birth)
            binding.birthdate.text = birthFormatStr
        }

        activityViewModel.avatarUrl.collectLifecycleFlow(this) { url ->
            Glide.with(this).load(url).into(binding.avatar)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun initListener() {

        binding.avatar.setOnClickListener {
            selectAvatar()
        }

        binding.itemUsername.setOnClickListener {
            showEditTextDialog("修改用户名", LCUser.currentUser().username) { username ->
                LCUser.currentUser().apply {
                    this.username = username
                    update("用户名")
                }
                activityViewModel.username.value = username
            }
        }

        binding.itemSex.setOnClickListener {
            showSexPickerDialog()
        }

        binding.itemBirthdate.setOnClickListener {
            showBirthdayPickerDialog(LCUser.currentUser().birth) { ms ->
                LCUser.currentUser().apply {
                    birth = ms
                    update("出生日期")
                }
                activityViewModel.birth.value = ms
            }
        }

        binding.itemSignature.setOnClickListener {
            showEditTextDialog("修改个性签名", LCUser.currentUser().signature) { signature ->
                LCUser.currentUser().apply {
                    this.signature = signature
                    update("个性签名")
                }
                activityViewModel.signature.value = signature
            }
        }

        binding.itemEmail.setOnClickListener {
            showEditTextDialog("修改邮箱", LCUser.currentUser().email) { email ->
                LCUser.currentUser().apply {
                    this.email = email
                    update("邮箱")
                }
                activityViewModel.email.value = email
            }
        }

        binding.itemShortId.setOnLongClickListener {
            // 将 LcUser 的 short id 复制到系统剪贴板
            val shortId = LCUser.currentUser().shortId
            val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("shortId", shortId)
            clipboardManager.setPrimaryClip(clipData)
            "UUID 已复制到剪贴板".showToast()
            true
        }

        binding.itemGoalWeek.setOnClickListener {
            showEditTextDialog("修改周目标", activityViewModel.goalWeek.value.toString(),"公里") { text ->
                val goal: Int
                try {
                    goal = text.toInt()
                } catch (e: Exception) {
                    "请输入合法整数".showToast()
                    return@showEditTextDialog
                }
                LCUser.currentUser().apply {
                    this.weekGoal = goal
                    update("周目标")
                }
                activityViewModel.goalWeek.value = goal
            }
        }

        binding.itemGoalMonth.setOnClickListener {
            showEditTextDialog("修改月目标", activityViewModel.goalMonth.value.toString(),"公里") { text ->
                val goal: Int
                try {
                    goal = text.toInt()
                } catch (e: Exception) {
                    "请输入合法整数".showToast()
                    return@showEditTextDialog
                }
                LCUser.currentUser().apply {
                    this.monthGoal = goal
                    update("月目标")
                }
                activityViewModel.goalMonth.value = goal
            }
        }

        binding.itemGoalYear.setOnClickListener {
            showEditTextDialog("修改年目标", activityViewModel.goalYear.value.toString(),"公里") { text ->
                val goal: Int
                try {
                    goal = text.toInt()
                } catch (e: Exception) {
                    "请输入合法整数".showToast()
                    return@showEditTextDialog
                }
                LCUser.currentUser().apply {
                    this.yearGoal = goal
                    update("年目标")
                }
                activityViewModel.goalYear.value = goal
            }
        }
    }


    private fun showEditTextDialog(title: String, curValue: String, suffixText: String? = null, onBlock: (String) -> Unit) {
        var mEditTextBinding: EditTextBinding? =
            EditTextBinding.inflate(layoutInflater, null, false)
        val editTextBinding: EditTextBinding = mEditTextBinding!!
        editTextBinding.editText.setText(curValue)
        editTextBinding.textInputLayout.suffixText = suffixText

        BaseAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(editTextBinding.root)
            .setPositiveButton("确定") { _, _ ->
                onBlock(editTextBinding.editText.text.toString())
                mEditTextBinding = null
            }
            .setNegativeButton("取消") { _, _ ->
                mEditTextBinding = null
            }
            .create()
            .show()
    }


    private fun showSexPickerDialog() {
        val choices = arrayOf("男", "女")
        val selectIndex = choices.indexOf(LCUser.currentUser().sex)
        BaseAlertDialogBuilder(requireContext())
            .setTitle("请选择您的性别")
            .setPositiveButton("确定") { dialog, _ ->
                val checkedItemPosition = (dialog as AlertDialog).listView.checkedItemPosition
                if (checkedItemPosition != AdapterView.INVALID_POSITION) {
                    LCUser.currentUser().apply {
                        sex = choices[checkedItemPosition]
                        update("性别")
                    }
                    activityViewModel.sex.value = choices[checkedItemPosition]
                }
            }
            .setNegativeButton("取消", null)
            .setSingleChoiceItems(choices, selectIndex, null)
            .create()
            .show()
    }


    private fun showBirthdayPickerDialog(selectionInUtcMs: Long, onDateSelected: (Long) -> Unit) {
        val selectionInLocalMs = selectionInUtcMs + TimeZone.getDefault().rawOffset
        val dialog = MaterialDatePicker.Builder.datePicker()
            .setTitleText("请选择您的出生日期")
            .setSelection(selectionInLocalMs)
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.now())
                    .build()
            )
            .build()

        dialog.addOnPositiveButtonClickListener { selectionInLocalMs ->
            val selectionInUtcMs = selectionInLocalMs - TimeZone.getDefault().rawOffset
            onDateSelected(selectionInUtcMs)
        }

        dialog.show(childFragmentManager, "birthday_picker")
    }


    private fun selectAvatar() {
        val pictureWindowAnimationStyle =
            PictureWindowAnimationStyle(R.anim.slide_in_right, R.anim.slide_out_left)

        requestSelectPicturePermission {
            PictureSelector.create(this)
                .openGallery(SelectMimeType.TYPE_IMAGE)
                .setImageEngine(GlideEngine.instance)
                .isDisplayCamera(false)
                .setCropEngine { fragment, sourceUri, destinationUri, dataSource, requestCode ->
                    UCrop.of(sourceUri, destinationUri, dataSource)
                        .withAspectRatio(1F, 1F)
                        .withMaxResultSize(200, 200)
                        .withOptions(UCrop.Options().apply {
                            isDarkStatusBarBlack(true)
                            setCompressionFormat(Bitmap.CompressFormat.JPEG)
                            setCompressionQuality(100)
                        })
                        .start(fragment.requireActivity(), fragment, requestCode)
                }
                .setSelectionMode(SelectModeConfig.SINGLE)
                .isDirectReturnSingle(true)
                .isGif(false)
                .forResult(object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: ArrayList<LocalMedia>) {
                        saveAvatar(result[0])
                    }

                    override fun onCancel() {
                    }
                })
        }
    }


    private fun requestSelectPicturePermission(onGranted: () -> Unit) {
        val permissionList = mutableListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissionList.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionList.add(Manifest.permission.READ_MEDIA_IMAGES)
        }

        PermissionX.init(this)
            .permissions(permissionList)
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(deniedList, "请在设置中打开以下权限", "确定", "取消")
            }
            .onExplainRequestReason { scope, deniedList, beforeRequest ->
                scope.showRequestReasonDialog(deniedList, "请在设置中打开以下权限", "确定", "取消")
            }
            .explainReasonBeforeRequest()
            .request { allGranted, _, _ ->
                if (allGranted) {
                    onGranted()
                }
            }
    }


    private fun saveAvatar(media: LocalMedia) {
        val file = LCFile.withAbsoluteLocalPath(media.fileName, media.availablePath)
        file.saveInBackground().subscribe(object : Observer<LCFile> {
            override fun onSubscribe(d: Disposable) {
            }

            override fun onNext(file: LCFile) {
                LCUser.currentUser().apply {
                    avatar = file
                    update(tag = "头像") {
                        activityViewModel.avatarUrl.value = file.url
                    }
                }
            }

            override fun onError(e: Throwable) {
                "上传头像失败".showToast()
                e.printStackTrace()
            }

            override fun onComplete() {
            }
        })
    }
}