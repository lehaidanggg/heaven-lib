package com.heaven.android.heavenlib.datas.models

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.heaven.android.heavenlib.config.HeavenEnv

data class AppVersionModel(
    @SerializedName("isDisplay")
    val isDisplay: Boolean,
    @SerializedName("isForce")
    val isForce: Boolean,
    @SerializedName("latest_version")
    val lastVersion: String
) {
    companion object {
        fun fromJson(strJson: String): AppVersionModel {
            val gson = Gson()
            return gson.fromJson(strJson, AppVersionModel::class.java)
        }

        fun getDefaultWhenHasErr(): AppVersionModel {
            return AppVersionModel(
                isDisplay = true,
                isForce = true,
                lastVersion = HeavenEnv.buildConfig.versionName
            )
        }

    }
}