package com.iosxc.android.updater.base

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * Created by Crazz on 2017/3/10.
 */

abstract class AbstractUpdateActivity : AppCompatActivity() {
    protected abstract val updateDialogFragment: Fragment
    protected abstract val downLoadDialogFragment: Fragment
}
