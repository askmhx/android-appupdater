package com.iosxc.android.updater.dialog

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.iosxc.android.updater.R
import com.iosxc.android.updater.base.AbstractFragment
import com.iosxc.android.updater.bean.VersionModel
import com.iosxc.android.updater.common.Constant
import com.iosxc.android.updater.utils.NetWorkUtils
import com.iosxc.android.updater.utils.PackageUtils
import com.iosxc.android.updater.utils.PublicFunctionUtils
import com.iosxc.android.updater.utils.ToastUtils

/**
 * Created by Crazz on 2017/3/8.
 */

class UpdateDialog : AbstractFragment(), View.OnClickListener {

    private var mActivity: UpdateActivity? = null
    protected var mModel: VersionModel? = null
    protected var mToastMsg: String? = null
    protected var mIsShowToast: Boolean = false

    private val content: String
        get() {
            val sb = StringBuilder()
            sb.append(getActivity()!!.getResources().getString(R.string.update_lib_version_code))
                .append(mModel!!.versionName)
                .append("\n")
                .append("\n")
                .append(getActivity()!!.getResources().getString(R.string.update_lib_update_content))
                .append("\n")
                .append(mModel!!.content!!.replace("#".toRegex(), "\\\n"))
            return sb.toString()
        }

    protected override val layout: Int
        get() = R.layout.fragment_update

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mModel = getArguments()!!.getSerializable(Constant.MODEL) as VersionModel
        mToastMsg = getArguments()!!.getString(Constant.TOAST_MSG)
        mIsShowToast = getArguments()!!.getBoolean(Constant.IS_SHOW_TOAST_MSG)
        closeIfNoNewVersionUpdate()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContent(view, R.id.tvContent)
    }

    private fun closeIfNoNewVersionUpdate() {
        if (mModel!!.versionCode <= PackageUtils.getVersionCode(getActivity()!!.getApplicationContext())) {
            isLatest()
            getActivity()!!.finish()
        }
    }

    private fun isLatest() {
        if (mIsShowToast) {
            ToastUtils.show(
                getActivity()!!,
                if (TextUtils.isEmpty(mToastMsg)) getResources().getString(R.string.update_lib_default_toast) else mToastMsg!!
            )
        }
    }

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.btnCancel) {
            onCancel()
        } else if (id == R.id.btnUpdate) {
            onUpdate()
        }
    }

    protected fun onCancel() {
        getActivity()!!.finish()
    }

    protected fun onUpdate() {
        if (!NetWorkUtils.getNetworkStatus(mActivity!!.getApplicationContext())) {
            return
        }
        mActivity!!.showDownLoadProgress()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is UpdateActivity) {
            mActivity = context
        }
    }

    override fun setContent(view: View, contentId: Int) {
        val tvContext = view.findViewById<View>(contentId) as TextView
        tvContext.text = content
    }

    protected fun initIfMustUpdate(view: View, id: Int) {
        if (PackageUtils.getVersionCode(mActivity!!.getApplicationContext()) < mModel!!.minSupport) {
            view.findViewById<View>(id).visibility = View.GONE
            PublicFunctionUtils.setLastCheckTime(getActivity()!!.getApplicationContext(), 0)
        }
    }

    override fun initView(view: View) {
        bindUpdateListener(view, R.id.btnUpdate)
        bindCancelListener(view, R.id.btnCancel)
        initIfMustUpdate(view, R.id.btnCancel)
    }

    override fun bindUpdateListener(view: View, updateId: Int) {
        view.findViewById<View>(updateId).setOnClickListener { onUpdate() }
    }

    override fun bindCancelListener(view: View, cancelId: Int) {
        view.findViewById<View>(cancelId).setOnClickListener { onCancel() }
    }

    companion object {

        fun newInstance(model: VersionModel?, toastMsg: String?, isShowToast: Boolean): UpdateDialog {
            val args = Bundle()
            args.putSerializable(Constant.MODEL, model)
            args.putString(Constant.TOAST_MSG, toastMsg)
            args.putBoolean(Constant.IS_SHOW_TOAST_MSG, isShowToast)
            val fragment = UpdateDialog()
            fragment.setArguments(args)
            return fragment
        }
    }
}
