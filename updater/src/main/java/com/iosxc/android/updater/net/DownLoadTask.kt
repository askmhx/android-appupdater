package com.iosxc.android.updater.net

import com.iosxc.android.updater.log.L

import java.io.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by Crazz on 2017/3/8.
 */

class DownLoadTask(
    private val mFilePath: String,
    private val mDownLoadUrl: String,
    private val mProgressListener: ProgressListener
) : Thread() {
    private var bis: InputStream? = null

    override fun run() {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(mDownLoadUrl)
            if (mDownLoadUrl.startsWith("https://")) {
                TrustAllCertificates.install()
            }
            connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Connection", "Keep-Alive")
            connection.setRequestProperty("Keep-Alive", "header")
            bis = connection.inputStream
            val count = connection.contentLength
            if (count <= 0) {
                L.e(TAG, "file length must > 0")
                return
            }
            if (bis == null) {
                L.e(TAG, "InputStream not be null")
                return
            }
            writeToFile(bis!!, count, mFilePath)
        } catch (e: Exception) {
            e.printStackTrace()
            mProgressListener.onError()
        } finally {
            if (bis != null) {
                try {
                    bis!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            connection?.disconnect()
        }
    }

    @Throws(IOException::class)
    private fun writeToFile(bis: InputStream, count: Int, filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
        var length = 0.toLong()
        val buffer = ByteArray(1024)
        var len: Int
        val fileOut = file.outputStream()
        while (true) {
            len = bis.read(buffer)
            length += len
            mProgressListener.update(length, count.toLong())
            if (len <= 0)
                break
            fileOut.write(buffer, 0, len)
        }
        mProgressListener.done()
    }


    interface ProgressListener {
        fun done()

        fun update(bytesRead: Long, contentLength: Long)

        fun onError()
    }

    companion object {

        private val TAG = "DownLoadTask"
    }
}
