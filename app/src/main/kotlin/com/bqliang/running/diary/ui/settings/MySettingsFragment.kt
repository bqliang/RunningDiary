package com.bqliang.running.diary.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import cn.leancloud.LCUser
import com.bqliang.running.diary.NavGraphDirections
import com.bqliang.running.diary.R
import com.bqliang.running.diary.databinding.DialogRunGoalBinding
import com.bqliang.running.diary.ui.base.BaseAlertDialogBuilder
import com.bqliang.running.diary.utils.*
import com.tencent.mmkv.MMKV
import rikka.preference.SimpleMenuPreference

class MySettingsFragment : PreferenceFragmentCompat() {

    enum class RunGoalType {
        WEEK, MONTH, YEAR
    }

    private val user: LCUser by lazy { LCUser.currentUser() }
    private val runGoalWeek by lazy { findPreference<Preference>(getString(R.string.pref_run_goal_week)) }
    private val runGoalMonth by lazy { findPreference<Preference>(getString(R.string.pref_run_goal_month)) }
    private val runGoalYear by lazy { findPreference<Preference>(getString(R.string.pref_run_goal_year)) }
    private val logOutPref by lazy { findPreference<Preference>(getString(R.string.pref_log_out)) }
    private val rotateMapPref by lazy { findPreference<Preference>(getString(R.string.pref_rotate_map_follow_orientation)) }
    private val mapMatchPref by lazy { findPreference<Preference>(getString(R.string.pref_is_need_map_match)) }
    private val darkModePref by lazy { findPreference<SimpleMenuPreference>(getString(R.string.pref_dark_mode)) }
    private val mmkv by lazy { MMKV.defaultMMKV() }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentDarkMode = mmkv.getString(getString(R.string.pref_dark_mode), "system")
        val darkModeIndex = darkModePref!!.entryValues.indexOf(currentDarkMode)
        darkModePref?.setValueIndex(darkModeIndex)

        val recyclerview = view.findViewById<RecyclerView>(androidx.preference.R.id.recycler_view)
        recyclerview.clipToPadding = false

        ViewCompat.setOnApplyWindowInsetsListener(recyclerview) { view, windowInsets ->
            val bottomPadding = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            } else {
                @Suppress("DEPRECATION")
                windowInsets.systemWindowInsetBottom
            }
            view.updatePadding(
                bottom = bottomPadding
            )
            WindowInsetsCompat.CONSUMED
        }


        runGoalWeek?.summary = "${user.weekGoal} 公里"
        runGoalWeek?.setOnPreferenceClickListener { pref ->
            showRunGoalPickerDialog(RunGoalType.WEEK, user.weekGoal) { goal ->
                user.setWeekGoal(goal) {
                    pref.summary = "$goal 公里"
                }
            }
            true
        }


        runGoalMonth?.summary = "${user.monthGoal} 公里"
        runGoalMonth?.setOnPreferenceClickListener { pref ->
            showRunGoalPickerDialog(RunGoalType.MONTH, user.monthGoal) { goal ->
                user.setMonthGoal(goal) {
                    pref.summary = "$goal 公里"
                }
            }
            true
        }


        runGoalYear?.summary = "${user.yearGoal} 公里"
        runGoalYear?.setOnPreferenceClickListener { pref ->
            showRunGoalPickerDialog(RunGoalType.YEAR, user.yearGoal) { goal ->
                user.setYearGoal(goal) {
                    pref.summary = "$goal 公里"
                }
            }
            true
        }


        rotateMapPref?.setOnPreferenceChangeListener { preference, newValue ->
            mmkv.putBoolean(
                getString(R.string.pref_rotate_map_follow_orientation),
                newValue as Boolean
            )
            true
        }


        mapMatchPref?.setOnPreferenceChangeListener { preference, newValue ->
            mmkv.putBoolean(
                getString(R.string.pref_is_need_map_match),
                newValue as Boolean
            )
            true
        }


        darkModePref?.setOnPreferenceChangeListener { preference, newValue ->
            mmkv.putString(
                getString(R.string.pref_dark_mode),
                newValue as String
            )
            val newDarkMode = when (newValue) {
                "system" -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                "off" -> AppCompatDelegate.MODE_NIGHT_NO
                "on" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
            val curDarkMode = AppCompatDelegate.getDefaultNightMode()
            if (curDarkMode != newDarkMode) {
                val msg = when (newDarkMode) {
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "跟随系统"
                    AppCompatDelegate.MODE_NIGHT_NO -> "关闭"
                    AppCompatDelegate.MODE_NIGHT_YES -> "开启"
                    else -> "跟随系统"
                }
                msg.showToast()
                AppCompatDelegate.setDefaultNightMode(newDarkMode)
            }
            activity?.recreate()
            true
        }


        logOutPref?.setOnPreferenceClickListener {
            BaseAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.round_logout_24)
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定") { _, _ ->
                    LCUser.logOut()
                    navController.navigate(NavGraphDirections.actionGlobalNavGraphAuthentication())
                }
                .setNegativeButton("取消", null)
                .create()
                .show()
            true
        }
    }


    private fun showRunGoalPickerDialog(
        goalType: RunGoalType,
        curValue: Int,
        onConfirm: (Int) -> Unit
    ) {
        val binding = DialogRunGoalBinding.inflate(layoutInflater, null, false)

        binding.runGoalEditText.setText(curValue.toString())
        binding.runGoalTextInputLayout.hint = when (goalType) {
            RunGoalType.WEEK -> "周目标"
            RunGoalType.MONTH -> "月目标"
            RunGoalType.YEAR -> "年目标"
        }


        val dialog = BaseAlertDialogBuilder(requireContext())
            .setTitle("设置${binding.runGoalTextInputLayout.hint}")
            .setIcon(R.drawable.round_sports_score_24)
            .setView(binding.root)
            .setPositiveButton("确定") { _, _ ->
                onConfirm(1)
            }
            .setPositiveButton("确定") { _, _ ->
                val goal = binding.runGoalEditText.text.toString().toIntOrNull()
                if (goal == null || goal == 0) {
                    "请输入正确的目标".showToast()
                } else {
                    onConfirm(goal)
                }
            }
            .setNegativeButton("取消", null)
            .create()

        dialog.setOnShowListener {
            val imm = getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(binding.runGoalEditText, InputMethodManager.SHOW_IMPLICIT)
        }

        dialog.show()
    }
}