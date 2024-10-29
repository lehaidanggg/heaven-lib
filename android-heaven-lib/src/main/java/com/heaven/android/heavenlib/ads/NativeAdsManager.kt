package com.heaven.android.heavenlib.ads

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.heaven.android.heavenlib.R
import com.heaven.android.heavenlib.datas.FBConfig
import com.heaven.android.heavenlib.datas.HeavenSharePref
import com.heaven.android.heavenlib.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


interface IOnLoadNativeCallback {
    fun onAdLoaded(nativeAd: NativeAd)
    fun onAdFailedToLoad(errorCode: String)
}

class NativeAdsManager {
    private val configAd = FBConfig.getInstance().getAdsConfig()
    private val optional = NativeAdOptions.Builder().build()

    companion object {
        const val TAG = "NativeAdsManager"

        @Volatile
        private var instance: NativeAdsManager? = null

        fun getInstance(): NativeAdsManager {
            return instance ?: synchronized(this) {
                instance ?: NativeAdsManager().also { instance = it }
            }
        }
    }

    private fun makeRequestAd(
        activity: Context,
        adUnitId: String,
        callback: IOnLoadNativeCallback
    ) {
        AdLoader.Builder(activity, adUnitId)
            .forNativeAd { ad ->
                Log.d(TAG, "onAdLoaded: Ad loaded successfully")
                val onPaid = OnPaidEventListener { adValue ->

                    val valueMicros = adValue.valueMicros
                    val zeroLong: Long = 0

                    val isTestAd: Boolean = valueMicros == zeroLong
                    if (isTestAd) {
                        HeavenSharePref.statusOrganic = true
                    }

                    val loadedAdapterResponseInfo = ad.responseInfo?.loadedAdapterResponseInfo
                    val adSourceInstanceName = loadedAdapterResponseInfo?.adSourceInstanceName
                    if (adSourceInstanceName?.lowercase()?.contains("test") == true) {
                        HeavenSharePref.statusOrganic = true
                    }
                }
                ad.setOnPaidEventListener(onPaid)
                callback.onAdLoaded(ad)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(errorCode: LoadAdError) {
                    Logger.log(
                        TAG,
                        "onAdFailedToLoad: Ad failed to load with error code $errorCode"
                    )
                    callback.onAdFailedToLoad("$errorCode")
                }
            })
            .withNativeAdOptions(optional)
            .build()
            .loadAd(AdRequest.Builder().build())
    }

    fun loadAd(
        activity: Context,
        adUnitId: String,
        enableAd: Boolean,
        callback: IOnLoadNativeCallback
    ) {
        if (HeavenSharePref.isShowFullAds) {
            makeRequestAd(
                activity = activity,
                adUnitId = adUnitId,
                callback = callback
            )
            return
        }


        if (!configAd.enable_all_ads) {
            callback.onAdFailedToLoad("googleMobileAdsConsentManager: enable all ads: FALSE")
            return
        }

        if (!enableAd) {
            callback.onAdFailedToLoad("googleMobileAdsConsentManager: enable this ads: FALSE")
            return
        }

        makeRequestAd(
            activity = activity,
            adUnitId = adUnitId,
            callback = callback
        )

    }

    fun displayNativeAdsHasMedia(
        ad: NativeAd,
        adView: NativeAdView,
    ) {
        val headlineView = adView.findViewById<TextView>(R.id.tvHeaderAds)
        val bodyView = adView.findViewById<TextView>(R.id.tvContentAds)
        val mediaView = adView.findViewById<MediaView>(R.id.mediaViewAds)
        val vCallToAction = adView.findViewById<View>(R.id.vCallToActionView)
        val btnCallToAction = adView.findViewById<Button>(R.id.btnCallToActionView)
        val iconView = adView.findViewById<ImageView>(R.id.imgAds)

        if (ad.headline == null) {
            headlineView.visibility = View.GONE
        } else {
            headlineView.visibility = View.VISIBLE
            headlineView.text = ad.headline
            adView.headlineView = headlineView
        }

        if (ad.body == null) {
            bodyView.visibility = View.GONE
        } else {
            bodyView.visibility = View.VISIBLE
            bodyView.text = ad.body
            adView.bodyView = bodyView
        }

        if (mediaView != null) {
            mediaView.visibility = View.VISIBLE
            mediaView.mediaContent = ad.mediaContent
            adView.mediaView = mediaView
        }

        if (ad.callToAction == null) {
            btnCallToAction.visibility = View.GONE
        } else {
            btnCallToAction.visibility = View.VISIBLE
            btnCallToAction.text = ad.callToAction
            adView.callToActionView = vCallToAction
        }

        if (ad.icon == null) {
            iconView.visibility = View.GONE
        } else {
            iconView.visibility = View.VISIBLE
            iconView.setImageDrawable(
                ad.icon!!.drawable
            )
            adView.iconView = iconView
        }

        adView.setNativeAd(ad)
    }
}


class NativeAdManagerV2 {
    private var _instanceNatives = MutableLiveData(mutableMapOf<String, NativeAd?>())

    companion object {
        const val TAG = "NativeAdsManager"

        @Volatile
        private var instance: NativeAdManagerV2? = null

        fun getInstance(): NativeAdManagerV2 {
            return instance ?: synchronized(this) {
                instance ?: NativeAdManagerV2().also { instance = it }
            }
        }
    }

    fun loadAd(
        context: Context,
        adUnitId: String,
        enableAd: Boolean
    ) = CoroutineScope(Dispatchers.IO).launch {
        val currentMap = _instanceNatives.value ?: mutableMapOf()
        if (HeavenSharePref.isShowFullAds) {
            val nativeAd = makeRequestAd(context, adUnitId)
            currentMap[adUnitId] = nativeAd
            _instanceNatives.postValue(currentMap)
            return@launch
        }

        if (!FBConfig.getInstance().getAdsConfig().enable_all_ads || !enableAd) {
            currentMap[adUnitId] = null
            _instanceNatives.postValue(currentMap)
            return@launch
        }

        val nativeAd = makeRequestAd(context, adUnitId)
        currentMap[adUnitId] = nativeAd
        _instanceNatives.postValue(currentMap)
    }

    fun displayAd(adUnitId: String, parentView: FrameLayout, adView: NativeAdView) {
        val currentMap = _instanceNatives.value ?: mutableMapOf()
        val currentAd = currentMap[adUnitId]
        if (currentAd == null) {
            parentView.visibility = View.GONE
            return
        }
        parentView.visibility = View.VISIBLE
        if (parentView.childCount > 0) {
            parentView.removeAllViews()
        }
        parentView.addView(adView)
        bindAdToView(currentAd, adView)
    }

    private fun bindAdToView(
        ad: NativeAd,
        adView: NativeAdView,
    ) {
        val headlineView = adView.findViewById<TextView>(R.id.tvHeaderAds)
        val bodyView = adView.findViewById<TextView>(R.id.tvContentAds)
        val mediaView = adView.findViewById<MediaView>(R.id.mediaViewAds)
        val vCallToAction = adView.findViewById<View>(R.id.vCallToActionView)
        val btnCallToAction = adView.findViewById<Button>(R.id.btnCallToActionView)
        val iconView = adView.findViewById<ImageView>(R.id.imgAds)

        if (ad.headline == null) {
            headlineView.visibility = View.GONE
        } else {
            headlineView.visibility = View.VISIBLE
            headlineView.text = ad.headline
            adView.headlineView = headlineView
        }

        if (ad.body == null) {
            bodyView.visibility = View.GONE
        } else {
            bodyView.visibility = View.VISIBLE
            bodyView.text = ad.body
            adView.bodyView = bodyView
        }

        if (mediaView != null) {
            mediaView.visibility = View.VISIBLE
            mediaView.mediaContent = ad.mediaContent
            adView.mediaView = mediaView
        }

        if (ad.callToAction == null) {
            btnCallToAction.visibility = View.GONE
        } else {
            btnCallToAction.visibility = View.VISIBLE
            btnCallToAction.text = ad.callToAction
            adView.callToActionView = vCallToAction
        }

        if (ad.icon == null) {
            iconView.visibility = View.GONE
        } else {
            iconView.visibility = View.VISIBLE
            iconView.setImageDrawable(
                ad.icon!!.drawable
            )
            adView.iconView = iconView
        }

        adView.setNativeAd(ad)
    }

    private suspend fun makeRequestAd(
        context: Context,
        adUnitId: String
    ): NativeAd? = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            AdLoader.Builder(context.applicationContext, adUnitId)
                .forNativeAd { ad ->
                    Logger.log(TAG, "onAdLoaded: Ad loaded successfully")
                    continuation.resume(ad)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(errorCode: LoadAdError) {
                        Logger.log(
                            TAG,
                            "onAdFailedToLoad: Ad failed to load with error code $errorCode"
                        )
                        continuation.resume(null)
                    }
                })
                .withNativeAdOptions(NativeAdOptions.Builder().build())
                .build()
                .loadAd(AdRequest.Builder().build())
        }
    }
}