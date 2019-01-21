package com.iosxc.android.updater.net

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.iosxc.android.updater.R
import com.iosxc.android.updater.utils.FileUtils

import java.io.File

/**
 * Created by Crazz on 2017/3/8.
 */

class DownLoadService : Service() {
    private var notificationIcon: Int = 0
    private var filePath: String? = null
    private var isBackground = false
    private var mDownLoadTask: DownLoadTask? = null
    private var mProgressListener: DownLoadTask.ProgressListener? = null
    private var mNotificationManager: NotificationManager? = null
    private var mBuilder: NotificationCompat.Builder? = null

    private val mDownLoadBinder = DownLoadBinder()

    override fun onCreate() {
        super.onCreate()
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    }

    fun startDownLoad(url: String) {
        filePath = FileUtils.getApkFilePath(applicationContext, url)
        mDownLoadTask = DownLoadTask(filePath!!, url, object : DownLoadTask.ProgressListener {
            override fun done() {
                mNotificationManager!!.cancel(NOTIFICATION_ID)
                if (isBackground) {
                    //download finish . start to install app
                    startActivity(FileUtils.openApkFile(applicationContext, File(filePath!!)))
                } else {
                    if (mProgressListener != null) {
                        mProgressListener!!.done()
                    }
                }
            }

            override fun update(bytesRead: Long, contentLength: Long) {
                if (isBackground) {
                    var currentProgress = (bytesRead * 100 / contentLength).toInt()
                    if (currentProgress < 1) {
                        currentProgress = 1
                    }
                    notification(currentProgress)
                    return
                }

                if (mProgressListener != null) {
                    mProgressListener!!.update(bytesRead, contentLength)
                }
            }

            override fun onError() {
                if (mProgressListener != null) {
                    mProgressListener!!.onError()
                }
                cancelNotification()
            }
        })
        mDownLoadTask!!.start()
    }

    fun setBackground(background: Boolean) {
        isBackground = background
    }

    override fun onBind(intent: Intent): IBinder? {
        return mDownLoadBinder
    }

    fun cancel() {
        if (mDownLoadTask != null) {
            mDownLoadTask!!.interrupt()
            mDownLoadTask = null
        }
    }

    fun setNotificationIcon(notificationIcon: Int) {
        this.notificationIcon = notificationIcon
    }

    inner class DownLoadBinder : Binder() {
        val service: DownLoadService
            get() = this@DownLoadService
    }

    fun registerProgressListener(progressListener: DownLoadTask.ProgressListener) {
        mProgressListener = progressListener
    }

    fun showNotification(current: Int) {
        mBuilder = NotificationCompat.Builder(this)
        mBuilder!!.setContentTitle(resources.getString(R.string.update_lib_file_download))
            .setContentText(resources.getString(R.string.update_lib_file_downloading))
            .setSmallIcon(if (notificationIcon == 0) R.drawable.ic_launcher else notificationIcon)
        mBuilder!!.setProgress(100, current, false)
        mNotificationManager!!.notify(NOTIFICATION_ID, mBuilder!!.build())
    }

    private fun notification(current: Int) {
        if (mBuilder == null) {
            showNotification(current)
            return
        }
        mBuilder!!.setProgress(100, current, false)
        mNotificationManager!!.notify(NOTIFICATION_ID, mBuilder!!.build())
    }

    private fun cancelNotification() {
        mNotificationManager!!.cancel(NOTIFICATION_ID)
    }

    companion object {

        private val NOTIFICATION_ID = 0
    }
}
