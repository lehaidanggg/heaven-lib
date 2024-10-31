package com.heaven.android.heavenlib.datas.models

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.heaven.android.heavenlib.config.HeavenEnv
import com.heaven.android.heavenlib.utils.Logger
import java.lang.RuntimeException
import java.lang.reflect.Type


data class AdConfigModel(
    val version_reviewing: String = "",
    var enable_all_ads: Boolean = true,
    var enable_rating: Boolean = true,
    val enable_inter_splash: Boolean = true,
    val enable_open_resume: Boolean = true,
    val enable_native_language: Boolean = true,
    val enable_native_language_selected: Boolean = true,
    val enable_native_language_setting: Boolean = true,
    val enable_native_intro1: Boolean = true,
    val enable_native_intro2: Boolean = true,
    val enable_native_intro3: Boolean = true,
    val enable_native_intro1_2nd: Boolean = true,
    val enable_native_intro2_2nd: Boolean = true,
    val enable_native_intro3_2nd: Boolean = true,
    val customFields: MutableMap<String, Any?> = mutableMapOf()
) {

    fun addField(field: Map<String, Any?>) {
        customFields.putAll(field)
    }

    fun addCustomField(fields: List<Map<String, Any?>>) {
        for (fieldMap in fields) {
            customFields.putAll(fieldMap)
        }
    }

    fun getCustomField(key: String): Any {
        return customFields[key] ?: throw RuntimeException("Unknown field AdConfigModel")
    }

    companion object {

        fun fromJson(json: String): AdConfigModel {
            val gson = GsonBuilder()
                .registerTypeAdapter(AdConfigModel::class.java, DynamicModelDeserializer())
                .create()

            return gson.fromJson(json, AdConfigModel::class.java)
        }

        fun defaultEnableAds(): AdConfigModel {
            return AdConfigModel()
        }

        fun checkVersionReviewing(versionReviewing: String): Boolean {
            return convertStringToInt(HeavenEnv.buildConfig.versionName) == convertStringToInt(
                versionReviewing
            )
        }

        private fun convertStringToInt(str: String): Int {
            return try {
                val strVersion = str.split(".")
                if (strVersion.size < 2) "${strVersion[0]}0".toInt()
                else "${strVersion[0]}${strVersion[1]}".toInt()
            } catch (e: Exception) {
                Logger.log("convertStringToInt", "convertStringToInt: err convert string to int")
                convertStringToInt(HeavenEnv.buildConfig.versionName)
            }
        }

    }

}

class DynamicModelDeserializer : JsonDeserializer<AdConfigModel> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AdConfigModel {
        val jsonObject = json.asJsonObject

        // Sử dụng Gson để deserialize các field mặc định
        val gson = Gson()
        val model = gson.fromJson(jsonObject, AdConfigModel::class.java)

        // Lấy tất cả các field đã biết của DynamicModel
        val knownFields = AdConfigModel::class.java.declaredFields.map { it.name }.toSet()

        // Duyệt qua các field trong JSON để thêm các trường không biết vào customFields
        for ((key, value) in jsonObject.entrySet()) {
            if (key !in knownFields) {
                model.customFields[key] = when {
                    value.isJsonPrimitive -> {
                        val primitive = value.asJsonPrimitive
                        when {
                            primitive.isBoolean -> primitive.asBoolean
                            primitive.isNumber -> primitive.asNumber
                            primitive.isString -> primitive.asString
                            else -> null
                        }
                    }

                    value.isJsonObject -> value.asJsonObject
                    value.isJsonArray -> value.asJsonArray
                    else -> null
                }
            }
        }
        return model
    }
}
