package com.iosxc.android.updater.utils

import android.content.Context
import android.widget.Toast

/**
 * Created by Crazz on 2017/3/28.
 */

object ToastUtils {

    fun show(context: Context?, msgId: Int) {
        if (context != null)
            show(context, context.resources.getString(msgId))
    }

    fun show(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}
