package com.bqliang.running.diary.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionMediator
import com.permissionx.guolindev.PermissionX


fun Fragment.requestPermission(allGranted: () -> Unit) {
    ignoreBatteryOptimization(this.requireContext())
    PermissionX.init(this)
        .requestPermission(allGranted)
}


fun FragmentActivity.requestPermission(allGranted: () -> Unit) {
    ignoreBatteryOptimization(this)
    PermissionX.init(this)
        .requestPermission(allGranted)
}


private fun PermissionMediator.requestPermission(allGranted: () -> Unit) {
    val list = mutableListOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,

        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        list.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    permissions(list)
        .onExplainRequestReason { scope, deniedList, beforeRequest ->
            // 监听那些被用户拒绝，而又可以再次去申请的权限
            scope.showRequestReasonDialog(deniedList, "需要您同意以下权限才能正常使用本功能", "确定", "取消")
        }
        .explainReasonBeforeRequest()
        .onForwardToSettings { scope, deniedList ->
            // 监听那些被用户永久拒绝的权限
            val message = "您需要在设置中手动开启以下权限"
            scope.showForwardToSettingsDialog(deniedList, message, "确定", "取消")
        }
        .request { allGranted, grantedList, deniedList ->
            if (allGranted) {
                // 用户已经同意所有申请的权限
                allGranted()
            } else {
                // 用户没有同意所有申请的权限
            }
        }
}


@SuppressLint("BatteryLife")
private fun ignoreBatteryOptimization(context: Context) {
    val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
    if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(intent)
    }
}