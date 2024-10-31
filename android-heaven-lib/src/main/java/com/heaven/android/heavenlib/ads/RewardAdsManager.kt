package com.heaven.android.heavenlib.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.heaven.android.heavenlib.datas.FBConfig
import com.heaven.android.heavenlib.datas.HeavenSharePref

class RewardAdsManager {
    private var rewardedAd: RewardedAd? = null

    companion object {
        var isShowAdsReward = false

        @Volatile
        private var instance: RewardAdsManager? = null

        fun getInstance(): RewardAdsManager {
            return instance ?: synchronized(this) {
                instance ?: RewardAdsManager().also {
                    instance = it
                }
            }
        }
    }

    private fun makeRequestAd(
        context: Context,
        adUnitID: String,
        onLoadFailure: () -> Unit,
        onAdsClose: () -> Unit,
    ) {
        RewardedAd.load(
            context,
            adUnitID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(errorCode: LoadAdError) {
                    isShowAdsReward = false
                    AppOpenManager.instance?.enableAppResume()
                    onLoadFailure()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    isShowAdsReward = true
                    rewardedAd = ad
                    showAd(context, onAdsClose)
                }
            }
        )
    }

    fun loadAd(
        context: Context,
        adUnitID: String,
        enable: Boolean,
        onLoadFailure: () -> Unit,
        onAdsClose: () -> Unit,
    ) {
        if (HeavenSharePref.isShowFullAds) {
            makeRequestAd(
                context = context,
                adUnitID = adUnitID,
                onLoadFailure = onLoadFailure,
                onAdsClose = onAdsClose
            )
            return
        }

        if (!enable || !FBConfig.getAdsConfig().enable_all_ads) {
            onLoadFailure()
            return
        }

//        if (MainApplication.consentInformation?.canRequestAds() == false) {
//            onLoadFailure()
//            return
//        }

        if (isShowAdsReward) return
        AppOpenManager.instance?.disableAppResume()
        makeRequestAd(
            context = context,
            adUnitID = adUnitID,
            onLoadFailure = onLoadFailure,
            onAdsClose = onAdsClose
        )
    }

    fun showAd(context: Context, onAdsClose: () -> Unit) {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                isShowAdsReward = false
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                super.onAdFailedToShowFullScreenContent(adError)
                Log.e("TAG", "onAdFailedToShowFullScreenContent: err $adError")
                rewardedAd = null
                isShowAdsReward = false
                onAdsClose()
            }

            override fun onAdShowedFullScreenContent() {
                isShowAdsReward = true
                rewardedAd = null
            }
        }
        rewardedAd?.show(context as Activity) {
            isShowAdsReward = true
            onAdsClose()
        }
    }
}