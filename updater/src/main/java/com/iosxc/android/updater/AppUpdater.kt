package com.iosxc.android.updater

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import com.iosxc.android.updater.bean.VersionModel
import com.iosxc.android.updater.common.Constant
import com.iosxc.android.updater.dialog.UpdateActivity
import com.iosxc.android.updater.net.CheckUpdateTask
import com.iosxc.android.updater.utils.NetWorkUtils
import com.iosxc.android.updater.utils.PublicFunctionUtils
import com.iosxc.android.updater.utils.ToastUtils

/**
 * Created by caik on 2017/3/8.
 */

class AppUpdater private constructor() {

    private var mContext: Context? = null
    private var mUrl: String? = null
    private var mToastMsg: String? = null
    private var mCallback: CheckUpdateTask.Callback? = null
    private var mNotificationIcon: Int = 0
    private var mTime: Long = 0
    private var mIsShowToast = true
    private var mIsShowNetworkErrorToast = true
    private var mIsShowBackgroundDownload = true
    private var mIsPost = false
    private var mPostParams: Map<String, String>? = null
    private var mParser: VersionModelParser? = null
    private var mCls: Class<out FragmentActivity>? = null

    private val mInnerCallBack = object : CheckUpdateTask.Callback {
        override fun callBack(model: VersionModel?, hasNewVersion: Boolean) {
            if (model == null) {
                mHandler.post {
                    if (mIsShowToast) {
                        ToastUtils.show(
                            mContext!!,
                            if (TextUtils.isEmpty(mToastMsg))
                                mContext!!.resources.getString(R.string.update_lib_default_toast)
                            else
                                mToastMsg!!
                        )
                    }
                }
                return
            }
            //记录本次更新时间
            PublicFunctionUtils.setLastCheckTime(mContext!!, System.currentTimeMillis())
            if (mCallback != null) {
                mCallback!!.callBack(model, hasNewVersion)
            }
            if (hasNewVersion || mIsShowToast) {
                start2Activity(mContext!!, model)
            }
        }

    }
    private val mHandler = Handler(Looper.getMainLooper())

    fun start() {
        if (!NetWorkUtils.getNetworkStatus(mContext!!)) {
            if (mIsShowNetworkErrorToast) {
                ToastUtils.show(mContext!!, R.string.update_lib_network_not_available)
            }
            return
        }
        if (TextUtils.isEmpty(mUrl)) {
            throw RuntimeException("url not be null")
        }

        if (checkUpdateTime(mTime)) {
            return
        }
        CheckUpdateTask(mContext!!, mUrl!!,mIsPost,  mPostParams, mParser, mInnerCallBack).start()
    }

    private fun checkUpdateTime(time: Long): Boolean {
        val lastCheckUpdateTime = PublicFunctionUtils.getLastCheckTime(mContext!!)
        val currentTime = System.currentTimeMillis()
        return if (currentTime - lastCheckUpdateTime > time) {
            false
        } else true
    }

    private fun start2Activity(context: Context?, model: VersionModel?) {
        try {
            val intent = Intent(context, if (mCls == null) UpdateActivity::class.java else mCls)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(Constant.MODEL, model)
            intent.putExtra(Constant.TOAST_MSG, mToastMsg)
            intent.putExtra(Constant.NOTIFICATION_ICON, mNotificationIcon)
            intent.putExtra(Constant.IS_SHOW_TOAST_MSG, mIsShowToast)
            intent.putExtra(Constant.IS_SHOW_BACKGROUND_DOWNLOAD, mIsShowBackgroundDownload)
            context!!.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    class Builder(context: Context) {
        private val wrapper = AppUpdater()

        init {
            wrapper.mContext = context
        }

        fun setUrl(url: String): Builder {
            wrapper.mUrl = url
            return this
        }

        fun setTime(time: Long): Builder {
            wrapper.mTime = time
            return this
        }

        fun setNotificationIcon(notificationIcon: Int): Builder {
            wrapper.mNotificationIcon = notificationIcon
            return this
        }

        fun setCustomsActivity(cls: Class<out FragmentActivity>): Builder {
            wrapper.mCls = cls
            return this
        }

        fun setCallback(callback: CheckUpdateTask.Callback): Builder {
            wrapper.mCallback = callback
            return this
        }

        fun setToastMsg(toastMsg: String): Builder {
            wrapper.mToastMsg = toastMsg
            return this
        }

        fun setIsShowToast(isShowToast: Boolean): Builder {
            wrapper.mIsShowToast = isShowToast
            return this
        }

        fun setIsShowNetworkErrorToast(isShowNetworkErrorToast: Boolean): Builder {
            wrapper.mIsShowNetworkErrorToast = isShowNetworkErrorToast
            return this
        }

        fun setIsShowBackgroundDownload(isShowBackgroundDownload: Boolean): Builder {
            wrapper.mIsShowBackgroundDownload = isShowBackgroundDownload
            return this
        }

        fun setIsPost(isPost: Boolean): Builder {
            wrapper.mIsPost = isPost
            return this
        }

        fun setPostParams(postParams: Map<String, String>): Builder {
            wrapper.mPostParams = postParams
            return this
        }

        fun setVesionModelParser(parser: VersionModelParser): Builder {
            wrapper.mParser = parser
            return this
        }

        fun build(): AppUpdater {
            return wrapper
        }
    }

    companion object {

        private val TAG = "AppUpdater"
    }
}
