package com.iosxc.android.updater

import com.iosxc.android.updater.bean.VersionModel

interface VersionModelParser {

    fun parse(rsp: String): VersionModel

}
