package com.heaven.android.heavenlib.ads

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.heaven.android.heavenlib.datas.FBConfig
import com.heaven.android.heavenlib.datas.HeavenSharePref


interface IBannerAds {
    fun onBannerLoadSuccess() {}
    fun onBannerLoadErr() {}
    fun onBannerClose() {}
    fun onBannerOpened() {}
    fun onBannerClick() {}
}

class BannerAds(
    private val context: Context,
    private val onBannerListener: IBannerAds,
) {

    private var adView: AdView? = null
    private var isShowing: Boolean = false

    companion object {
        const val TAG = "BannerAds"
        const val KEY_BUNDLE_BANNER = "collapsible"
        const val VALUE_BUNDLE_BANNER = "bottom"
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun loadBanner(idAd: String, enable: Boolean, parent: ViewGroup) {

        if (HeavenSharePref.isShowFullAds) {
            makeRequestBanner(idAd = idAd, parent = parent)
            return
        }
        if (isShowing){
            onBannerListener.onBannerLoadErr()
            return
        }

        if (!enable || !FBConfig.getInstance().getAdsConfig().enable_all_ads) {
            onBannerListener.onBannerLoadErr()
            return
        }

//        if (MainApplication.consentInformation?.canRequestAds() == false) {
//            onBannerListener.onBannerLoadErr()
//            return
//        }
        makeRequestBanner(idAd = idAd, parent = parent)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun makeRequestBanner(
        idAd: String,
        parent: ViewGroup
    ) {
        adView = AdView(context)
        adView.let {
            it?.setAdSize(getAdsBanner(context, parent))
            it?.adUnitId = idAd
            it?.adListener = listenerAdBanner(parent)
            val extras = Bundle()
            extras.putString(KEY_BUNDLE_BANNER, VALUE_BUNDLE_BANNER)
            val adRequest = AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()

            it?.loadAd(adRequest)
        }
    }

    private fun listenerAdBanner(parent: ViewGroup): AdListener {
        return object : AdListener() {
            override fun onAdClicked() {
                onBannerListener.onBannerClick()
            }

            override fun onAdClosed() {
                onBannerListener.onBannerClose()
                isShowing = false
                Log.e(TAG, "onAdClosed: ")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "onAdFailedToLoad: $adError")
                parent.visibility = View.GONE
                onBannerListener.onBannerLoadErr()
            }

            override fun onAdImpression() {}

            override fun onAdLoaded() {
                parent.removeAllViews()
                parent.addView(adView)
                onBannerListener.onBannerLoadSuccess()
            }

            override fun onAdOpened() {
                Log.e(TAG, "onAdOpened: Banner opened")
                isShowing = true
                onBannerListener.onBannerOpened()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getAdsBanner(context: Context, parent: ViewGroup): AdSize {
        val outMetrics = DisplayMetrics()
        context.display?.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = parent.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)

    }
}