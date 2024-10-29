package com.heaven.android.heavenlib.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager

object PackageInfo {
    var APPLICATION_ID: String = ""
    var VERSION_CODE: Int = 0
    var VERSION_NAME: String = ""
    var IS_DEBUG_MODE: Boolean = false

    fun setDebugMode(isDebug: Boolean) {
        IS_DEBUG_MODE = isDebug
    }

    @SuppressLint("NewApi")
    fun getPackageInfo(context: Context) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName ?: ""
            val versionCode = packageInfo.longVersionCode.toInt()
            VERSION_NAME = versionName
            VERSION_CODE = versionCode
            APPLICATION_ID = packageInfo.packageName

        } catch (e: PackageManager.NameNotFoundException) {
            Logger.log("PackageInfo", "getPackageInfo: ${e.message}")
            VERSION_CODE = 0
            VERSION_NAME = ""
        }
    }
}