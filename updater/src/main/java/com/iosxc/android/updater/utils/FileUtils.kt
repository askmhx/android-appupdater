package com.iosxc.android.updater.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider

import java.io.File

/**
 * Created by Crazz on 2017/3/9.
 */

object FileUtils {

    fun getApkFilePath(context: Context, downLoadUrl: String): String {
        val externalFile = context.getExternalFilesDir(null)
        val filePath = externalFile!!.absolutePath
        val fileName: String
        if (downLoadUrl.endsWith(".apk")) {
            val index = downLoadUrl.lastIndexOf("/")
            if (index != -1) {
                fileName = downLoadUrl.substring(index)
            } else {
                fileName = context.packageName + ".apk"
            }
        } else {
            fileName = context.packageName + ".apk"
        }

        val file = File(filePath, fileName)
        return file.absolutePath
    }

    fun openApkFile(context: Context, outputFile: File): Intent {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Intent.ACTION_VIEW
        val uri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider.appupdatefileprovider",
                outputFile
            )
        } else {
            uri = Uri.fromFile(outputFile)
        }

        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        return intent
    }
}
