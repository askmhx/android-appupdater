package com.iosxc.android.updater.dialog

import android.graphics.Point
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iosxc.android.updater.R
import com.iosxc.android.updater.base.AbstractUpdateActivity
import com.iosxc.android.updater.bean.VersionModel
import com.iosxc.android.updater.common.Constant
import com.iosxc.android.updater.utils.PackageUtils

class UpdateActivity : AbstractUpdateActivity(), DownLoadDialog.OnFragmentOperation {

    private var notificationIcon: Int = 0
    protected var mModel: VersionModel? = null
    protected var mToastMsg: String? = null
    protected var mIsShowToast: Boolean = false
    protected var mIsShowBackgroundDownload: Boolean = false

    protected override val updateDialogFragment: Fragment
        get() = UpdateDialog.newInstance(mModel!!, mToastMsg!!, mIsShowToast)

    protected override val downLoadDialogFragment: Fragment
        get() = DownLoadDialog.newInstance(
            mModel!!.url!!,
            notificationIcon,
            PackageUtils.getVersionCode(getApplicationContext()) < mModel!!.minSupport,
            mIsShowBackgroundDownload
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)
        getWindow().setLayout(calcWidth(), ViewGroup.LayoutParams.WRAP_CONTENT)
        setFinishOnTouchOutside(false)
        notificationIcon = getIntent().getIntExtra(Constant.NOTIFICATION_ICON, 0)
        mModel = getIntent().getSerializableExtra(Constant.MODEL) as VersionModel
        mToastMsg = getIntent().getStringExtra(Constant.TOAST_MSG)
        mIsShowToast = getIntent().getBooleanExtra(Constant.IS_SHOW_TOAST_MSG, true)
        mIsShowBackgroundDownload = getIntent().getBooleanExtra(Constant.IS_SHOW_BACKGROUND_DOWNLOAD, true)
        if (mModel == null) {
            finish()
            return
        }

        showUpdateDialog()
    }

    private fun calcWidth(): Int {
        if (getResources().getBoolean(R.bool.au_is_tablet)) {
            return getResources().getDimensionPixelSize(R.dimen.au_dialog_max_width)
        } else {
            val wm = getWindow().getWindowManager()
            val display = wm.getDefaultDisplay()
            val size = Point()
            display.getSize(size)
            val windowWidth = size.x

            val windowHorizontalPadding = getResources().getDimensionPixelSize(R.dimen.au_dialog_horizontal_margin)
            return windowWidth - windowHorizontalPadding * 2
        }
    }

    private fun showUpdateDialog() {
        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.container, updateDialogFragment)
            .commit()
    }

    fun showDownLoadProgress() {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.container, downLoadDialogFragment)
            .commit()
    }

    override fun onBackPressed() {
        if (PackageUtils.getVersionCode(getApplicationContext()) < mModel!!.minSupport) {
            return
        }
        super.onBackPressed()
    }

    override fun onFailed() {
        showUpdateDialog()
    }
}
