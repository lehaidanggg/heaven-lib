package com.heaven.android.heavenlib.datas

import android.app.Activity
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.heaven.android.heavenlib.utils.PackageInfo

interface IUmpListener {
    fun requestConsentCompleted(err: String?)
}

object UMPUtils {
    private lateinit var consentInformation: ConsentInformation



    fun requestConsent(activity: Activity, listener: IUmpListener) {
        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId("61AFC09657EC976743A413E0ECC4CD30")
            .build()

        val paramsBuilder = ConsentRequestParameters.Builder()
        if (PackageInfo.IS_DEBUG_MODE){
            paramsBuilder.setConsentDebugSettings(debugSettings)
        }
        val params = paramsBuilder.build()


        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    activity
                ) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        // Consent gathering failed.
                        listener.requestConsentCompleted(loadAndShowError.message)
                    }
                    // Consent has been gathered.
                    listener.requestConsentCompleted(null)
                }
            },
            { requestConsentError ->
                listener.requestConsentCompleted(requestConsentError.message)
            })
    }
}