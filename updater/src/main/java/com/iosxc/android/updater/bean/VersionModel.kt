package com.iosxc.android.updater.bean

import org.json.JSONException
import org.json.JSONObject

import java.io.Serializable

/**
 * Created by Crazz on 2017/3/8.
 */

class VersionModel : Serializable {

    /**
     * versionCode : 1
     * versionName : 1.0.0
     * content : 更新描述
     * minSupport : 1
     * url : 文件下载地址
     */

    var versionCode: Int = 0
    var versionName: String? = null
    var content: String? = null
    var minSupport: Int = 0
    var url: String? = null

    @Throws(JSONException::class)
    fun parse(json: String): VersionModel {
        val `object` = JSONObject(json)
        versionCode = `object`.getInt("versionCode")
        versionName = `object`.getString("versionName")
        content = `object`.getString("content")
        url = `object`.getString("url")
        minSupport = `object`.optInt("minSupport")
        return this
    }
}
