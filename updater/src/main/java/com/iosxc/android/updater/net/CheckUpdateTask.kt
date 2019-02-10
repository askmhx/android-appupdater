package com.iosxc.android.updater.net

import android.content.Context
import com.iosxc.android.updater.VersionModelParser
import com.iosxc.android.updater.bean.VersionModel
import com.iosxc.android.updater.log.L
import com.iosxc.android.updater.utils.PackageUtils
import org.json.JSONException

import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

/**
 * Created by Crazz on 2017/3/8.
 */

class CheckUpdateTask(
    private val mContext: Context,
    private val mCheckUpdateUrl: String,
    private val mIsPost: Boolean?,
    private val mPostParams: Map<String, String>?,
    private val mConverter: VersionModelParser?,
    private val mCallBack: Callback
) : Thread() {

    override fun run() {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(mCheckUpdateUrl)
            if (mCheckUpdateUrl.startsWith("https://")) {
                TrustAllCertificates.install()
            }

            connection = url.openConnection() as HttpURLConnection
            if (mIsPost!!) {
                val mStringBuilder = StringBuilder("")
                if (mPostParams != null) {
                    val set = mPostParams.entries
                    val iterator = set.iterator()
                    while (iterator.hasNext()) {
                        val mEntry = iterator.next() as Map.Entry<*, *>
                        mStringBuilder.append(mEntry.key)
                        mStringBuilder.append("=")
                        mStringBuilder.append(mEntry.value)
                        if (iterator.hasNext()) {
                            mStringBuilder.append("&")
                        }
                    }
                }
                val urlParameters = mStringBuilder.toString()
                val postData = urlParameters.toByteArray(Charset.forName("UTF-8"))
                val postDataLength = postData.size
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded ;charset=utf-8")
                connection.setRequestProperty("Content-Length", Integer.toString(postDataLength))
                connection.doOutput = true
                connection.doInput = true
                connection.useCaches = false
                val wr = DataOutputStream(connection.outputStream)
                wr.write(postData)
                wr.flush()
            }

            val bis = BufferedInputStream(connection.inputStream)
            val data = read(bis)
            bis.close()
            L.d(TAG, "result:$data")
            try {
                var model = VersionModel()
                if (mConverter != null) {
                    model = mConverter.parse(data)
                    model.versionCode = 3
                } else {
                    model.parse(data)
                }
                mCallBack.callBack(model, hasNewVersion(PackageUtils.getVersionCode(mContext), model.versionCode))
            } catch (e: JSONException) {
                e.printStackTrace()
                mCallBack.callBack(null, false)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            mCallBack.callBack(null, false)
        } finally {
            connection?.disconnect()
        }
    }

    private fun hasNewVersion(old: Int, n: Int): Boolean {
        return old < n
    }

    interface Callback {
        fun callBack(model: VersionModel?, hasNewVersion: Boolean)
    }

    companion object {

        private val TAG = "CheckUpdateTask"

        @Throws(IOException::class)
        private fun read(bis: InputStream): String {
            return bis.bufferedReader().use { it.readText() }
        }
    }
}
