package com.heaven.android.heavenlib.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.heaven.android.heavenlib.datas.FBConfig
import com.heaven.android.heavenlib.datas.HeavenSharePref
import com.heaven.android.heavenlib.utils.Logger
import com.heaven.android.heavenlib.utils.PackageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class InterstitialAdManager {
    private var mInterstitialAd: InterstitialAd? = null
    private var durationShowInterAds: Date? = null

    //
    companion object {
        val TAG = InterstitialAdManager::class.simpleName.toString()
        var isShowingInterAds = false

        @Volatile
        private var instance: InterstitialAdManager? = null

        fun getInstance(): InterstitialAdManager {
            return instance ?: synchronized(this) {
                instance ?: InterstitialAdManager().also { instance = it }
            }
        }
    }


    fun loadInterAds(
        activity: Activity,
        adUnitId: String,
        enable: Boolean,
        isSplashAd: Boolean = false,
        onSuccessAds: () -> Unit,
        onLoadErr: (String) -> Unit
    ) {
        if (HeavenSharePref.isShowFullAds) {
            makeRequestLoadAd(
                activity = activity,
                adUnitId = adUnitId,
                isSplashAd = isSplashAd,
                onSuccessAds = onSuccessAds,
                onLoadErr = onLoadErr
            )
            return
        }
        if (!FBConfig.getInstance().getAdsConfig().enable_all_ads) {
            onLoadErr("Disable all ad from RMCF")
            return
        }

        if (!enable || isShowingInterAds || !checkDurationShowAds()) {
            onLoadErr("Disable ad from RMCF")
            return
        }

        if (isShowingInterAds) return

        makeRequestLoadAd(
            activity = activity,
            adUnitId = adUnitId,
            isSplashAd = isSplashAd,
            onSuccessAds = onSuccessAds,
            onLoadErr = onLoadErr
        )
    }

    fun showInterAds(activity: Activity, onDoneAds: () -> Unit) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                    Log.d(TAG, "Ad was clicked.")
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    Log.d(TAG, "Ad dismissed fullscreen content.")
                    isShowingInterAds = false
                    mInterstitialAd = null
                    showInterAds(activity, onDoneAds)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Called when ad fails to show.
                    Log.e(TAG, "Ad failed to show fullscreen content.")
                    isShowingInterAds = false
                    mInterstitialAd = null
                    showInterAds(activity, onDoneAds)
                }

                override fun onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    Log.d(TAG, "Ad recorded an impression.")
                }

                override fun onAdShowedFullScreenContent() {
                    isShowingInterAds = true
                    // Called when ad is shown.
                    Log.d(TAG, "Ad showed fullscreen content.")
                }
            }

            mInterstitialAd!!.show(activity)
        } else {
            onDoneAds()
        }
    }

    private fun makeRequestLoadAd(
        activity: Activity,
        adUnitId: String,
        isSplashAd: Boolean = false,
        onSuccessAds: () -> Unit,
        onLoadErr: (String) -> Unit
    ) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(activity, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Load ads err: $adError")
                mInterstitialAd = null
                onLoadErr("Ad failed to load")
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded! ${interstitialAd.responseInfo}")
                val loadedAdapterResponseInfo = interstitialAd.responseInfo.loadedAdapterResponseInfo
                val adSourceInstanceName = loadedAdapterResponseInfo?.adSourceInstanceName

                val isTest = adSourceInstanceName?.lowercase()?.contains("test") == true
                if (isTest && isSplashAd) {
                    HeavenSharePref.statusOrganic = true
                }
                interstitialAd.onPaidEventListener = OnPaidEventListener { adValue ->
                    val valueMicros = adValue.valueMicros
                    val zeroLong: Long = 0

                    val isTestAd: Boolean = valueMicros == zeroLong
                    if (isTestAd && isSplashAd) {
                        HeavenSharePref.statusOrganic = true
                    }
                }
                mInterstitialAd = interstitialAd
                durationShowInterAds = Date()
                showInterAds(activity, onSuccessAds)
            }
        })
    }

    private fun checkDurationShowAds(): Boolean {
        return if (durationShowInterAds == null) {
            true
        } else {
            val time = if (!HeavenSharePref.isShowFullAds) 30 else 0
            calculateDifferenceInMinutes(durationShowInterAds!!, Date()) > time
        }
    }

    private fun calculateDifferenceInMinutes(startDate: Date, endDate: Date): Long {
        val diffInMillis: Long = endDate.time - startDate.time
        return TimeUnit.MILLISECONDS.toSeconds(diffInMillis)
    }

}

class InterstitialAdManagerV2 {
    companion object {
        suspend fun makeRequestInterAD(
            context: Context,
            adUnitId: String
        ): InterstitialAd? = withContext(Dispatchers.Main) {
            suspendCoroutine { continuation ->
                val adRequest = AdRequest.Builder().build()
                InterstitialAd.load(
                    context,
                    adUnitId,
                    adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            if (PackageInfo.IS_DEBUG_MODE) {
                                Logger.log("LOAD_INTER", "LOAD FAILED: ${adError.message}")
                            }
                            continuation.resume(null)
                        }

                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            if (PackageInfo.IS_DEBUG_MODE){
                                Logger.log("LOAD_INTER", "LOAD SUCCESS!")
                            }
                            continuation.resume(interstitialAd)
                        }
                    })
            }
        }
    }
}
