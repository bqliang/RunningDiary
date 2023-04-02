package com.bqliang.running.diary.ui.authentication

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bqliang.running.diary.R
import com.bqliang.running.diary.databinding.DialogRetrievePasswordBinding
import com.bqliang.running.diary.databinding.FragmentLoginBinding
import com.bqliang.running.diary.ui.base.BaseAlertDialogBuilder
import com.bqliang.running.diary.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.transition.MaterialSharedAxis
import rikka.html.text.toHtml

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()
    private var retrievePasswordDialog: BottomSheetDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        savedStateHandle.getStateFlow(RegisterFragment.SIGN_UP_SUCCESSFUL_EMAIL, "")
            .collectLifecycleFlow(this) { sign_up_successful_email ->
                viewModel.email.value = sign_up_successful_email
            }
    }


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.checkBox.text = getString(
            R.string.read_and_agree,
            "<a href=\"https://www.baidu.com\">服务协议</a>"
        ).toHtml()
        binding.checkBox.movementMethod = LinkMovementMethod.getInstance()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginBtn.setOnClickListener { btn ->
            btn.hideKeyboard()
            checkAndLogin()
        }

        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAndLogin()
            }

            /**
             * 对于 actionDone、actionNext 和 actionPrevious，系统都自己进行了部分处理。
             * - actionDone：隐藏输入法
             * - actionNext：跳到下一个EditText
             * - actionPrevious：跳到上一个EditText
             * 系统会首先判断用户实现的方法 ict.onEditorActionListener.onEditorAction(this, actionCode, null) 的返回值，一旦返回 true，会立即 return，因此系统的处理被直接跳过。
             * 如果想自己实现部分功能，然后其他的基本操作由系统完成，那就在实现方法 onEditorAction（）返回 false
             */
            false
        }

        binding.registerBtn.setOnClickListener {
            exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
                duration = 300L
            }
            reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
                duration = 300L
            }
            navController.navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }

        binding.forgetPasswordBtn.setOnClickListener {
            showRetrievePasswordDialog()
        }

        collectLifecycleFlow(viewModel.loginSuccess) { loginSuccess ->
            if (loginSuccess) {
                navController.navigate(LoginFragmentDirections.actionLoginFragmentToRunListFragment())
            }
        }

        collectLifecycleFlow(viewModel.sendResetPasswordEmailSuccess) { sendResetPasswordEmailSuccess ->
            if (sendResetPasswordEmailSuccess) {
                retrievePasswordDialog?.dismiss()
            }
        }


        collectLifecycleFlow(viewModel.error) { lcException ->
            when (lcException.code) {
                EMAIL_NOT_VERIFIED -> {
                    BaseAlertDialogBuilder(requireContext())
                        .setCancelable(false)
                        .setTitle("邮箱未验证")
                        .setMessage("您的邮箱 ${viewModel.email.value} 尚未验证，您可以点击下方按钮，我们随后将向您发送一封验证邮件。\n请注意，验证链接有效时间为 48 小时。")
                        .setPositiveButton("立刻验证") { _, _ ->
                            viewModel.verifyEmail(viewModel.email.value)
                        }
                        .setNegativeButton("取消") { _, _ ->
                        }
                        .create()
                        .show()
                }
                else -> binding.root.showSnackbar(lcException.localizedMessage ?: "登录失败")
            }
        }
    }


    private fun checkAndLogin() {
        if (viewModel.agreePrivacyPolicy.value) {
            viewModel.loginByEmail(viewModel.email.value, viewModel.password.value)
        } else {
            binding.root.showSnackbar("请先同意隐私政策")
        }
    }


    private fun showRetrievePasswordDialog() {
        retrievePasswordDialog = BottomSheetDialog(requireContext())
        retrievePasswordDialog?.dismissWithAnimation = true
        retrievePasswordDialog?.setOnDismissListener {
            retrievePasswordDialog = null
        }

        val binding = DialogRetrievePasswordBinding.inflate(layoutInflater, null, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        binding.resetBtn.setOnClickListener { btn ->
            btn.hideKeyboard()
            viewModel.retrievePassword(viewModel.email.value)
        }

        retrievePasswordDialog?.setContentView(binding.root)
        retrievePasswordDialog?.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}