package com.iosxc.android.updater.log

import android.util.Log
import com.iosxc.android.updater.BuildConfig

/**
 * Created by Crazz on 2017/3/8.
 */

object L {
    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg)
        }
    }
}
