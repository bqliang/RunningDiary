package com.bqliang.running.diary.ui.authentication

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bqliang.running.diary.R
import com.bqliang.running.diary.databinding.FragmentRegisterBinding
import com.bqliang.running.diary.ui.base.BaseAlertDialogBuilder
import com.bqliang.running.diary.utils.collectLifecycleFlow
import com.bqliang.running.diary.utils.hideKeyboard
import com.bqliang.running.diary.utils.navController
import com.bqliang.running.diary.utils.showSnackbar
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import rikka.html.text.toHtml

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    companion object {
        const val SIGN_UP_SUCCESSFUL_EMAIL: String = "SIGN_UP_SUCCESSFUL_EMAIL"
    }

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            duration = 300L
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            duration = 300L
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        binding.checkBox.text = getString(
            R.string.read_and_agree,
            "<a href=\"https://www.baidu.com\">服务协议</a>"
        ).toHtml()
        binding.checkBox.movementMethod = LinkMovementMethod.getInstance()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backToLoginBtn.setOnClickListener {
            navController.popBackStack()
        }

        binding.registerBtn.setOnClickListener { btn ->
            btn.hideKeyboard()
            checkAndRegister()
        }

        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAndRegister()
            }
            false
        }

        viewModel.error.collectLifecycleFlow(this) { lcException ->
            binding.root.showSnackbar(lcException.localizedMessage ?: "注册失败")
        }

        viewModel.signUpSuccess.collectLifecycleFlow(this) { signUpSuccess ->
            if (signUpSuccess) {
                BaseAlertDialogBuilder(requireContext())
                    .setTitle("注册成功")
                    .setMessage("登录前请前往邮箱 ${viewModel.email.value} 验证您的账号。 (验证链接 48 小时内有效)")
                    .setCancelable(false)
                    .setPositiveButton("好的") { _, _ ->
                        val savedStateHandle =
                            navController.previousBackStackEntry!!.savedStateHandle
                        savedStateHandle[SIGN_UP_SUCCESSFUL_EMAIL] = viewModel.email.value
                        navController.popBackStack()
                    }
                    .create()
                    .show()
            }
        }
    }


    private fun checkAndRegister() {
        if (viewModel.agreePrivacyPolicy.value) {
            viewModel.signUp(viewModel.username.value, viewModel.email.value, viewModel.password.value)
        } else {
            binding.root.showSnackbar("请先同意隐私政策")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}