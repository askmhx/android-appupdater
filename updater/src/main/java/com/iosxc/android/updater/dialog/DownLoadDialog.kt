package com.iosxc.android.updater.dialog

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.iosxc.android.updater.R
import com.iosxc.android.updater.common.Constant
import com.iosxc.android.updater.log.L
import com.iosxc.android.updater.net.DownLoadService
import com.iosxc.android.updater.net.DownLoadTask
import com.iosxc.android.updater.utils.FileUtils
import com.iosxc.android.updater.utils.ToastUtils

import java.io.File

/**
 * Created by Crazz on 2017/3/9.
 */

class DownLoadDialog : DialogFragment(), View.OnClickListener {
    private var mDownloadUrl: String? = null
    private var notificationIcon: Int = 0
    private var currentProgress: Int = 0
    private var mBtnCancel: Button? = null
    private var mBtnBackground: Button? = null
    private var mTvTitle: TextView? = null
    private var mProgressBar: ProgressBar? = null
    private var mDownLoadService: DownLoadService? = null
    private var mMustUpdate: Boolean = false
    private var mIsShowBackgroundDownload: Boolean = false
    private var mOnFragmentOperation: OnFragmentOperation? = null

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as DownLoadService.DownLoadBinder
            mDownLoadService = binder.service
            mDownLoadService!!.registerProgressListener(mProgressListener)
            mDownLoadService!!.startDownLoad(mDownloadUrl!!)
            mDownLoadService!!.setNotificationIcon(notificationIcon)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mDownLoadService = null
        }
    }

    private val mProgressListener = object : DownLoadTask.ProgressListener {
        override fun done() {
            mHandler.sendEmptyMessage(DONE)
        }

        override fun update(bytesRead: Long, contentLength: Long) {
            currentProgress = (bytesRead * 100 / contentLength).toInt()
            if (currentProgress < 1) {
                currentProgress = 1
            }
            L.d(TAG, "$bytesRead,$contentLength;current=$currentProgress")
            val message = mHandler.obtainMessage()
            message.what = LOADING
            message.arg1 = currentProgress
            val bundle = Bundle()
            bundle.putLong("bytesRead", bytesRead)
            bundle.putLong("contentLength", contentLength)
            message.data = bundle
            message.sendToTarget()
        }

        override fun onError() {
            mHandler.sendEmptyMessage(ERROR)
        }
    }
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                LOADING -> {
                    mProgressBar!!.progress = msg.arg1
                    val bundle = msg.data
                    val bytesRead = bundle.getLong("bytesRead")
                    val contentLength = bundle.getLong("contentLength")
                    if (getActivity() != null)
                        mTvTitle!!.setText(
                            String.format(
                                getResources().getString(R.string.update_lib_file_download_format),
                                Formatter.formatFileSize(getActivity()!!.getApplication(), bytesRead),
                                Formatter.formatFileSize(getActivity()!!.getApplication(), contentLength)
                            )
                        )
                }
                DONE -> if (getActivity() != null) {
                    getActivity()!!.startActivity(
                        FileUtils.openApkFile(
                            getActivity()!!,
                            File(FileUtils.getApkFilePath(getActivity()!!, mDownloadUrl!!))
                        )
                    )
                    getActivity()!!.finish()
                    ToastUtils.show(getActivity(), R.string.update_lib_download_finish)
                }
                ERROR -> {
                    if (getActivity() != null)
                        ToastUtils.show(getActivity(), R.string.update_lib_download_failed)
                    if (!mMustUpdate) {
                        dismiss()
                        if (getActivity() != null)
                            getActivity()!!.finish()
                    } else {
                        dismiss()
                        if (mOnFragmentOperation != null) {
                            mOnFragmentOperation!!.onFailed()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_download, container, false)
        mDownloadUrl = getArguments()!!.getString(Constant.URL)
        notificationIcon = getArguments()!!.getInt(Constant.NOTIFICATION_ICON)
        mMustUpdate = getArguments()!!.getBoolean(Constant.MUST_UPDATE)
        if (mMustUpdate) {
            setCancelable(false)
        }
        mIsShowBackgroundDownload = getArguments()!!.getBoolean(Constant.IS_SHOW_BACKGROUND_DOWNLOAD)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mTvTitle = view.findViewById<View>(R.id.title) as TextView
        mBtnCancel = view.findViewById(R.id.btnCancel) as Button
        mBtnCancel!!.setOnClickListener(this)
        mBtnBackground = view.findViewById(R.id.btnBackground) as Button
        mBtnBackground!!.setOnClickListener(this)
        mProgressBar = view.findViewById(R.id.progressBar) as ProgressBar
        mProgressBar!!.max = 100

        val intent = Intent(getActivity(), DownLoadService::class.java)
        getActivity()!!.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)

        if (mMustUpdate) {
            (view.findViewById(R.id.downLayout) as LinearLayout).setVisibility(View.GONE)
        }
        if (!mIsShowBackgroundDownload) {
            mBtnBackground!!.visibility = View.GONE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentOperation) {
            mOnFragmentOperation = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mOnFragmentOperation = null
    }

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.btnCancel) {
            doCancel()
        } else if (id == R.id.btnBackground) {
            doBackground()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        getActivity()!!.unbindService(mConnection)
    }

    private fun doCancel() {
        mDownLoadService!!.cancel()
        getActivity()!!.finish()
        ToastUtils.show(getActivity(), R.string.update_lib_download_cancel)
    }

    private fun doBackground() {
        mDownLoadService!!.setBackground(true)
        mDownLoadService!!.showNotification(currentProgress)
        if (getActivity() != null) {
            ToastUtils.show(getActivity(), R.string.update_lib_download_in_background)
            getActivity()!!.finish()
        }
    }

    interface OnFragmentOperation {
        fun onFailed()
    }

    companion object {

        private val TAG = "DownLoadDialog"

        fun newInstance(downLoadUrl: String, notificationIcon: Int): DownLoadDialog {
            val args = Bundle()
            args.putString(Constant.URL, downLoadUrl)
            args.putInt(Constant.NOTIFICATION_ICON, notificationIcon)
            val fragment = DownLoadDialog()
            fragment.setArguments(args)
            return fragment
        }

        fun newInstance(
            downLoadUrl: String,
            notificationIcon: Int,
            mustUpdate: Boolean,
            isShowBackgroundDownload: Boolean
        ): DownLoadDialog {
            val args = Bundle()
            args.putString(Constant.URL, downLoadUrl)
            args.putInt(Constant.NOTIFICATION_ICON, notificationIcon)
            args.putBoolean(Constant.MUST_UPDATE, mustUpdate)
            args.putBoolean(Constant.IS_SHOW_BACKGROUND_DOWNLOAD, isShowBackgroundDownload)
            val fragment = DownLoadDialog()
            fragment.setArguments(args)
            return fragment
        }

        private val LOADING = 1000
        private val DONE = 1001
        private val ERROR = 1002
    }
}
