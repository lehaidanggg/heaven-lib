package com.heaven.android.heavenlib.datas.models

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import com.heaven.android.heavenlib.utils.Logger
import com.heaven.android.heavenlib.utils.PackageInfo

data class AdsConfigModel(
    @SerializedName("version_reviewing")
    val version_reviewing: String = "",
    @SerializedName("enable_all_ads")
    var enable_all_ads: Boolean = true,
    @SerializedName("enable_rating")
    var enable_rating: Boolean = true,
    @SerializedName("enable_inter_splash")
    val enable_inter_splash: Boolean = true,
    @SerializedName("enable_open_resume")
    val enable_open_resume: Boolean = true,
    @SerializedName("enable_native_language")
    val enable_native_language: Boolean = true,
    @SerializedName("enable_native_language_selected")
    val enable_native_language_selected: Boolean = true,
    @SerializedName("enable_native_language_setting")
    val enable_native_language_setting: Boolean = true,
) {
    companion object {
        fun fromJson(strData: String): AdsConfigModel {
            try {
                val adConfigModel = Gson().fromJson(strData, AdsConfigModel::class.java)

                Logger.log(
                    "ADS CONFIG MODEL:",
                    "fromJson: version name: ${PackageInfo.VERSION_NAME} --- version reviewing: ${adConfigModel.version_reviewing}"
                )
                if (checkVersionReviewing(adConfigModel.version_reviewing)) {
                    return adConfigModel.copy(
                        enable_all_ads = false,
                        enable_rating = false
                    )
                }
                return adConfigModel
            } catch (e: JsonSyntaxException) {
                Logger.log("CONVERT JSON", "JsonSyntaxException: ${e.message}")
                return defaultEnableAds()
            } catch (e: Exception) {
                Logger.log("CONVERT JSON", "OTHER EXCEPTION: ${e.message}")
                return defaultEnableAds()
            }
        }

        private fun checkVersionReviewing(versionReviewing: String): Boolean {
            return convertStringToInt(PackageInfo.VERSION_NAME) == convertStringToInt(
                versionReviewing
            )
        }

        open fun defaultEnableAds(): AdsConfigModel = AdsConfigModel()

        private fun convertStringToInt(str: String): Int {
            return try {
                val strVersion = str.split(".")
                if (strVersion.size < 2) "${strVersion[0]}0".toInt()
                else "${strVersion[0]}${strVersion[1]}".toInt()
            } catch (e: Exception) {
                Logger.log("convertStringToInt", "convertStringToInt: err convert string to int")
                convertStringToInt(PackageInfo.VERSION_NAME)
            }
        }
    }
}