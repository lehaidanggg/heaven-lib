package com.heaven.android.heavenlib.utils

import android.util.Log
import com.heaven.android.heavenlib.config.HeavenEnv
import com.heaven.android.heavenlib.datas.models.AppVersionModel
import com.heaven.android.heavenlib.datas.models.StatusForceUpdate

object Utils {
    fun isNeedUpdateAppVersions(
        version1: String,
        versionRemote: AppVersionModel?
    ): StatusForceUpdate {

        if (versionRemote == null) {
            return StatusForceUpdate.NONE
        }
        val ver1 = convertStringToInt(version1)
        val ver2 = convertStringToInt(versionRemote.lastVersion)
        //
        if (ver2 - ver1 > 0) {
            if (versionRemote.isForce) return StatusForceUpdate.ONLY_UPDATE
            if (versionRemote.isDisplay) return StatusForceUpdate.HAS_NO_THANKS
        }
        return StatusForceUpdate.NONE
    }

    private fun convertStringToInt(str: String): Int {
        return try {
            val strVersion = str.split(".")
            if (strVersion.size < 2) "${strVersion[0]}0".toInt()
            else "${strVersion[0]}${strVersion[1]}".toInt()
        } catch (e: Exception) {
            Log.e("TAG", "convertStringToInt: err convert string to int")
            convertStringToInt(HeavenEnv.buildConfig.versionName)
        }

    }
}