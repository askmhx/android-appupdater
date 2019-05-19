package com.iosxc.android.updater.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment

/**
 * Created by Crazz on 2017/3/10.
 */

abstract class AbstractFragment : DialogFragment() {

    protected abstract val layout: Int

    protected abstract fun setContent(view: View, contentId: Int)

    protected abstract fun initView(view: View)

    protected abstract fun bindUpdateListener(view: View, updateId: Int)

    protected abstract fun bindCancelListener(view: View, cancelId: Int)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }
}
