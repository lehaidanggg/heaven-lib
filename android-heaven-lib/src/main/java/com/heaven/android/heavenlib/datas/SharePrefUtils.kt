package com.heaven.android.heavenlib.datas

import android.content.Context
import android.content.SharedPreferences

object HeavenSharePref {
    private const val NAME = "HeavenSharedPref"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    private val IS_FIRST_RUN_PREF = Pair("is_first_run", false)
    private val LANGUAGE_CODE = Pair("language_code", "en")
    private val IS_FIRST_INSTALL = Pair("is_first_install", true)
    private val STATUS_ORGANIC = Pair("status_organic", true)
    private val TRACK_STATUS_ORGANIC = Pair("track_status_organic", false)
    private val TRACK_STATUS_NON_ORGANIC = Pair("track_status_non_organic", false)

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var firstRun: Boolean
        get() = preferences.getBoolean(IS_FIRST_RUN_PREF.first, IS_FIRST_RUN_PREF.second)
        set(value) = preferences.edit {
            it.putBoolean(IS_FIRST_RUN_PREF.first, value)
        }

    var languageCode: String
        get() = preferences.getString(LANGUAGE_CODE.first, LANGUAGE_CODE.second)
            ?: LANGUAGE_CODE.second
        set(value) = preferences.edit {
            it.putString(LANGUAGE_CODE.first, value)
        }

    var isFirstInstall: Boolean
        get() = preferences.getBoolean(IS_FIRST_INSTALL.first, IS_FIRST_INSTALL.second)
        set(value) {
            preferences.edit {
                it.putBoolean(IS_FIRST_INSTALL.first, value)
            }
        }

    val isShowFullAds: Boolean
        get() = !preferences.getBoolean(STATUS_ORGANIC.first, STATUS_ORGANIC.second)


    var statusOrganic: Boolean
        get() = preferences.getBoolean(STATUS_ORGANIC.first, STATUS_ORGANIC.second)
        set(value) {
            preferences.edit {
                it.putBoolean(STATUS_ORGANIC.first, value)
            }
        }

    var trackStatusOrganic: Boolean
        get() = preferences.getBoolean(TRACK_STATUS_ORGANIC.first, TRACK_STATUS_ORGANIC.second)
        set(value) {
            preferences.edit {
                it.putBoolean(TRACK_STATUS_ORGANIC.first, value)
            }
        }

    var trackNonOrganic: Boolean
        get() = preferences.getBoolean(
            TRACK_STATUS_NON_ORGANIC.first,
            TRACK_STATUS_NON_ORGANIC.second
        )
        set(value) {
            preferences.edit {
                it.putBoolean(TRACK_STATUS_NON_ORGANIC.first, value)
            }
        }
}