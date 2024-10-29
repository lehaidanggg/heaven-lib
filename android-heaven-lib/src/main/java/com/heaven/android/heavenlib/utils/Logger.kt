package com.heaven.android.heavenlib.utils

import android.util.Log

object Logger {
    fun log(tag: String, message: String) {
//        if (BuildConfig.DEBUG) {
        Log.e(
            tag,
            "----------------------------------------------------------------\n"
                                            + message +
                 "\n----------------------------------------------------------------"
        )
//        }
    }
}