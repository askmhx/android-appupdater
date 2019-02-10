package com.iosxc.android.updater.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

/**
 * Created by Crazz on 2017/3/8.
 */

object PackageUtils {
    fun getVersionCode(context: Context): Int {
        val manager = context.packageManager
        try {
            val info = manager.getPackageInfo(context.packageName, 0) ?: return 0
            return info.versionCode
        } catch (e: Exception) {
            return 0
        }

    }

    fun getVersionName(context: Context): String {
        val manager = context.packageManager
        try {
            val info = manager.getPackageInfo(context.packageName, 0) ?: return "1.0"
            return info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            return "1.0"
        }

    }
}
