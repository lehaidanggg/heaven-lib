package com.heaven.android.heavenlib.datas

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.heaven.android.heavenlib.config.HeavenEnv


class AppsFlyerConversionListenerImpl : AppsFlyerConversionListener {
    private val TAG = "AppsFlyerConversionListenerImpl"
    private lateinit var source: String
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    fun init(source: String, context: Context) {
        this.source = source
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    @SuppressLint("LongLogTag")
    override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
        Log.e(TAG, "onConversionDataSuccess: $data")
        if (data == null) {
            firebaseAnalytics.logEvent("conversion_success_no_data", null)
            return
        }
        val status: String = data["af_status"].toString().lowercase()
        if (!HeavenSharePref.isShowFullAds) {
            val isNonOrganic: Boolean = (status == "non_organic")
            if (isNonOrganic) {
                HeavenSharePref.statusOrganic = false
                if (!HeavenSharePref.trackNonOrganic) {
                    firebaseAnalytics.logEvent("non_organic_${source}", null)
                    HeavenSharePref.trackNonOrganic = true
                }
            }
        }
        firebaseAnalytics.logEvent("conversion_success_has_data", null)
    }

    @SuppressLint("LongLogTag")
    override fun onConversionDataFail(error: String?) {
        Log.e(TAG, "error onAttributionFailure :  $error")
        if (HeavenEnv.buildConfig.isDebug) {
            HeavenSharePref.statusOrganic = false
        }
        firebaseAnalytics.logEvent("conversion_failed_${source}", null)
    }

    @SuppressLint("LongLogTag")
    override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
        data?.map {
            Log.e(TAG, "onAppOpen_attribute: ${it.key} = ${it.value}")
        }
    }

    @SuppressLint("LongLogTag")
    override fun onAttributionFailure(error: String?) {
        Log.e(TAG, "error onAttributionFailure :  $error")
    }
}