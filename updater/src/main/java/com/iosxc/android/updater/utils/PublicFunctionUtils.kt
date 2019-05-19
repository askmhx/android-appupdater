package com.iosxc.android.updater.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Crazz on 2017/3/8.
 */

object PublicFunctionUtils {
    private val NAME = "app_update"

    fun getLastCheckTime(context: Context): Long {
        val preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        return preferences.getLong("update_time", 0)
    }

    fun setLastCheckTime(context: Context, time: Long) {
        val preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        preferences.edit().putLong("update_time", time).apply()
    }
}
