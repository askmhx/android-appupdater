package com.iosxc.android.updater.utils

import android.content.Context
import android.net.ConnectivityManager

/**
 * Created by Crazz on 2017/3/28.
 */

object NetWorkUtils {
    fun getNetworkStatus(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mNetworkInfo = manager.activeNetworkInfo
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable
        }
        return false
    }
}
