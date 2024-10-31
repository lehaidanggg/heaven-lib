package com.heaven.android.heavenlib.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.heaven.android.heavenlib.datas.FBConfig
import com.heaven.android.heavenlib.datas.HeavenSharePref
import com.heaven.android.heavenlib.dialogs.LoadingDialog
import com.heaven.android.heavenlib.dialogs.ResumeLoadingDialog
import java.util.Date


class AppOpenManager private constructor() : ActivityLifecycleCallbacks, LifecycleObserver {
    private var appResumeAd: AppOpenAd? = null
    private var loadCallback: AppOpenAd.AppOpenAdLoadCallback? = null
    private var fullScreenContentCallback: FullScreenContentCallback? = null
    private var appResumeAdId: String? = null
    private var currentActivity: Activity? = null
    private var myApplication: Application? = null
    private var appResumeLoadTime: Long = 0
    var isInitialized = false // on  - off ad resume on app
    private var isAppResumeEnabled = true
    var isInterstitialShowing = false
    private var isShowingAdResume = false
    private var enableScreenContentCallback =
        false // default =  true when use splash & false after show splash
    private var disableAdResumeByClickAction = false
    private val disabledAppOpenList: MutableList<Class<*>>
    private var splashActivity: Class<*>? = null


    /**
     * Init AppOpenManager
     *
     * @param application
     */
    fun init(application: Application?, appOpenAdId: String?) {
        isInitialized = true
        disableAdResumeByClickAction = false
        myApplication = application
        myApplication!!.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        appResumeAdId = appOpenAdId
    }

    fun setEnableScreenContentCallback(enableScreenContentCallback: Boolean) {
        this.enableScreenContentCallback = enableScreenContentCallback
    }

    /**
     * Call disable ad resume when click a button, auto enable ad resume in next start
     */
    fun disableAdResumeByClickAction() {
        disableAdResumeByClickAction = true
    }

    fun setDisableAdResumeByClickAction(disableAdResumeByClickAction: Boolean) {
        this.disableAdResumeByClickAction = disableAdResumeByClickAction
    }

    val isShowingAd: Boolean
        /**
         * Check app open ads is showing
         *
         * @return
         */
        get() = Companion.isShowingAd

    /**
     * Disable app open app on specific activity
     *
     * @param activityClass
     */
    fun disableAppResumeWithActivity(activityClass: Class<*>) {
        Log.d(TAG, "disableAppResumeWithActivity: " + activityClass.name)
        disabledAppOpenList.add(activityClass)
    }

    fun enableAppResumeWithActivity(activityClass: Class<*>) {
        Log.d(TAG, "enableAppResumeWithActivity: " + activityClass.name)
        disabledAppOpenList.remove(activityClass)
    }

    fun disableAppResume() {
        isAppResumeEnabled = false
    }

    fun enableAppResume() {
        isAppResumeEnabled = true
    }

    fun setFullScreenContentCallback(callback: FullScreenContentCallback?) {
        fullScreenContentCallback = callback
    }

    fun removeFullScreenContentCallback() {
        fullScreenContentCallback = null
    }

    private val adRequest: AdRequest
        /**
         * Creates and returns ad request.
         */
        private get() = AdRequest.Builder().build()

    private fun wasLoadTimeLessThanNHoursAgo(loadTime: Long, numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    private fun isAdAvailable(): Boolean {
        val loadTime = appResumeLoadTime
        val wasLoadTimeLessThanNHoursAgo = wasLoadTimeLessThanNHoursAgo(loadTime, 4)
        Log.d(
            TAG,
            "isAdAvailable: $wasLoadTimeLessThanNHoursAgo"
        )
        return appResumeAd != null && wasLoadTimeLessThanNHoursAgo
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
        Log.d(TAG, "onActivityStarted: $currentActivity")
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
        Log.d(TAG, "onActivityResumed: $currentActivity")
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (activity.javaClass.name != AdActivity::class.java.name) {
            currentActivity = null
        }
        Log.d(TAG, "onActivityDestroyed: null")
    }

    private var dialog: ResumeLoadingDialog? = null
    fun loadAdResume() {
        try {
            dismissDialogLoading()
            dismissDialogLoading()
            dialog = currentActivity?.let { ResumeLoadingDialog(it) }
            dialog?.show()
            dialog?.window?.setGravity(Gravity.CENTER)
            dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            dialog?.setCanceledOnTouchOutside(false)
        } catch (e: Exception) {
            if (fullScreenContentCallback != null && enableScreenContentCallback) {
                fullScreenContentCallback!!.onAdDismissedFullScreenContent()
            }
            return
        }

        loadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appResumeAd = ad
                appResumeAd!!.onPaidEventListener =
                    OnPaidEventListener { adValue: AdValue? -> }
                appResumeLoadTime = Date().time
                showResumeAds()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                fullScreenContentCallbackNew.onAdFailedToShowFullScreenContent(loadAdError)
                dismissDialogLoading()
            }
        }
        val request = adRequest
        AppOpenAd.load(
            myApplication!!,
            appResumeAdId!!,
            request,
            loadCallback as AppOpenAd.AppOpenAdLoadCallback
        )
    }

    private fun showAdIfAvailable(activity: Activity) {

        if (HeavenSharePref.isShowFullAds) {
            loadAdResume()
            return
        }

//        if (MainApplication.consentInformation?.canRequestAds() == false) {
//            return
//        }
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            Log.d(TAG, "The app open ad is already showing.")
            return
        }

        if (
            !FBConfig.getAdsConfig().enable_all_ads ||
            !FBConfig.getAdsConfig().enable_open_resume
        ) {
            return
        }
        // If the app open ad is not available yet, invoke the callback then load the ad.
        if (!isAdAvailable()) {
            Log.d(TAG, "The app open ad is not ready yet.")
            loadAdResume()
            return
        }

        appResumeAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdDismissedFullScreenContent() {
                // Called when full screen content is dismissed.
                // Set the reference to null so isAdAvailable() returns false.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                appResumeAd = null
                isShowingAdResume = false
                loadAdResume()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when fullscreen content failed to show.
                // Set the reference to null so isAdAvailable() returns false.
                Log.d(TAG, adError.message)
                appResumeAd = null
                isShowingAdResume = false
                loadAdResume()
            }

            override fun onAdShowedFullScreenContent() {
                // Called when fullscreen content is shown.
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }
        isShowingAdResume = true
        appResumeAd?.show(activity)

    }

    private fun showResumeAds() {

        if (appResumeAd == null || currentActivity == null) {
            return
        }
        isShowingAdResume = true
        if (appResumeAd != null) {
            appResumeAd!!.fullScreenContentCallback = fullScreenContentCallbackNew
            Companion.isShowingAd = true
            appResumeAd!!.show(currentActivity!!)
        }
    }

    var fullScreenContentCallbackNew: FullScreenContentCallback =
        object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // Set the reference to null so isAdAvailable() returns false.
                Log.e(TAG, "onAdDismissedFullScreenContent: ")
                Companion.isShowingAd = false
                isShowingAdResume = false
                dismissDialogLoading()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "onAdFailedToShowFullScreenContent: " + adError.message)
                Companion.isShowingAd = false
                isShowingAdResume = false
                dismissDialogLoading()
            }

            override fun onAdShowedFullScreenContent() {
                Log.e(TAG, "onAdShowedFullScreenContent: ")
            }

            override fun onAdClicked() {
                super.onAdClicked()
                if (currentActivity != null) {
//                    FirebaseUtil.logClickAdsEvent(currentActivity, appResumeAdId)
                    if (fullScreenContentCallback != null) {
                        fullScreenContentCallback!!.onAdClicked()
                    }
                }
            }

            override fun onAdImpression() {
                super.onAdImpression()
                if (currentActivity != null) {
                    if (fullScreenContentCallback != null) {
                        fullScreenContentCallback!!.onAdImpression()
                    }
                }
            }
        }

    /**
     * Constructor
     */
    init {
        disabledAppOpenList = ArrayList()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onResume() {
        if (!isAppResumeEnabled) {
            Log.d(TAG, "onResume: app resume is disabled")
            return
        }
        if (isInterstitialShowing) {
            Log.d(TAG, "onResume: interstitial is showing")
            return
        }
        if (disableAdResumeByClickAction) {
            Log.d(TAG, "onResume:ad resume disable ad by action")
            disableAdResumeByClickAction = false
            return
        }
        for (activity in disabledAppOpenList) {
            if (activity.name == currentActivity!!.javaClass.name) {
                Log.d(TAG, "onStart: activity is disabled")
                return
            }
        }
        if (isShowingAdResume) return
        Log.d(TAG, "onStart: show resume ads :" + currentActivity!!.javaClass.name)
        showAdIfAvailable(currentActivity!!)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        Log.d(TAG, "onStop: app stop")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        Log.d(TAG, "onPause")
    }

    private fun dismissDialogLoading() {
        if (dialog != null) {
            dialog!!.dismiss()
        }
    }
//
//    fun showAppOpenSplash(context: Context?, adCallback: AdCallback) {
//        if (splashAd == null) {
//            adCallback.onNextAction()
//            adCallback.onAdFailedToLoad(null)
//        } else {
//            try {
//                dialog = null
//                dialog = currentActivity?.let { ResumeLoadingDialog2(it) }
//                (dialog as ResumeLoadingDialog2).show()
//            } catch (e: Exception) {
//            }
//            /*this.dismissDialogLoading();
//            if (this.dialog == null) {
//                try {
//                    LoadingAdsDialog dialog = new LoadingAdsDialog(context);
//                    dialog.setCancelable(false);
//                    this.dialog = dialog;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            try {
//                if (this.dialog != null) {
//                    this.dialog.show();
//                }
//            } catch (Exception e) {}*/Handler().postDelayed({
//                if (splashAd != null) {
//                    splashAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
//                        override fun onAdDismissedFullScreenContent() {
//                            adCallback.onNextAction()
//                            adCallback.onAdClosed()
//                            splashAd = null
//                            Companion.isShowingAd = false
//                            isShowLoadingSplash = false
//                            if (dialog != null && !currentActivity!!.isDestroyed) {
//                                try {
//                                    dialog!!.dismiss()
//                                } catch (e: Exception) {
//                                    e.printStackTrace()
//                                }
//                            }
//                        }
//
//                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
//                            isShowLoadingSplash = true
//                            // adCallback.onNextAction();
//                            adCallback.onAdFailedToShow(adError)
//                            Companion.isShowingAd = false
//                            dismissDialogLoading()
//                        }
//
//                        override fun onAdShowedFullScreenContent() {
//                            adCallback.onAdImpression()
//                            Companion.isShowingAd = true
//                            splashAd = null
//                        }
//
//                        override fun onAdClicked() {
//                            super.onAdClicked()
//                            adCallback.onAdClicked()
//                        }
//                    }
//                    splashAd!!.show(currentActivity!!)
//                }
//            }, 800L)
//        }
//    }
//
//    fun loadOpenAppAdSplash(
//        context: Context,
//        idResumeSplash: String?,
//        timeDelay: Long,
//        timeOut: Long,
//        isShowAdIfReady: Boolean,
//        adCallback: AdCallback
//    ) {
//        splashAdId = idResumeSplash
//        if (!isNetworkConnected(context)) {
//            Handler().postDelayed({
//                adCallback.onAdFailedToLoad(null)
//                adCallback.onNextAction()
//            }, timeDelay)
//        } else {
//            if (AppPurchase.getInstance().isPurchased(context)) {
//                adCallback.onNextAction()
//            } else {
//                val currentTimeMillis = System.currentTimeMillis()
//                val timeOutRunnable = Runnable {
//                    Log.d("AppOpenManager", "getAdSplash time out")
//                    adCallback.onNextAction()
//                    Companion.isShowingAd = false
//                }
//                val handler = Handler()
//                handler.postDelayed(timeOutRunnable, timeOut)
//                val adRequest = adRequest
//                val adUnitId = splashAdId
//                val appOpenAdLoadCallback: AppOpenAdLoadCallback =
//                    object : AppOpenAdLoadCallback() {
//                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                            super.onAdFailedToLoad(loadAdError)
//                            handler.removeCallbacks(timeOutRunnable)
//                            adCallback.onAdFailedToLoad(null)
//                            adCallback.onNextAction()
//                        }
//
//                        override fun onAdLoaded(appOpenAd: AppOpenAd) {
//                            super.onAdLoaded(appOpenAd)
//                            handler.removeCallbacks(timeOutRunnable)
//                            splashAd = appOpenAd
//                            splashAd!!.onPaidEventListener =
//                                OnPaidEventListener { adValue: AdValue? -> }
//                            appOpenAd.onPaidEventListener =
//                                OnPaidEventListener { adValue: AdValue? ->
//                                    FirebaseUtil.logPaidAdImpression(
//                                        myApplication!!.applicationContext,
//                                        adValue,
//                                        appOpenAd.adUnitId,
//                                        AdType.APP_OPEN
//                                    )
//                                }
//                            if (isShowAdIfReady) {
//                                var elapsedTime = System.currentTimeMillis() - currentTimeMillis
//                                if (elapsedTime >= timeDelay) {
//                                    elapsedTime = 0L
//                                }
//                                val handler1 = Handler()
//                                val showAppOpenSplashRunnable = Runnable {
//                                    showAppOpenSplash(
//                                        context,
//                                        adCallback
//                                    )
//                                }
//                                handler1.postDelayed(showAppOpenSplashRunnable, elapsedTime)
//                            } else {
//                                adCallback.onAdSplashReady()
//                            }
//                        }
//                    }
//                AppOpenAd.load(
//                    context,
//                    adUnitId!!,
//                    adRequest,
//                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
//                    appOpenAdLoadCallback
//                )
//            }
//        }
//    }
//
//    fun loadOpenAppAdSplashFloor(
//        context: Context,
//        listIDResume: java.util.ArrayList<String>?,
//        isShowAdIfReady: Boolean,
//        adCallback: AdCallback
//    ) {
//        if (!isNetworkConnected(context)) {
//            Handler().postDelayed({
//                adCallback.onAdFailedToLoad(null)
//                adCallback.onNextAction()
//            }, 3000)
//        } else {
//            if (listIDResume == null) {
//                adCallback.onAdFailedToLoad(null)
//                adCallback.onNextAction()
//                return
//            }
//            if (listIDResume.size > 0) {
//                Log.e("AppOpenManager", "load ID :" + listIDResume[0])
//            }
//            if (listIDResume.size < 1) {
//                adCallback.onAdFailedToLoad(null)
//                adCallback.onNextAction()
//            }
//            if (AppPurchase.getInstance().isPurchased(context) || listIDResume.size < 1) {
//                adCallback.onNextAction()
//            } else {
//                val adRequest = adRequest
//                val appOpenAdLoadCallback: AppOpenAdLoadCallback =
//                    object : AppOpenAdLoadCallback() {
//                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                            super.onAdFailedToLoad(loadAdError)
//                            // adCallback.onAdFailedToLoad(loadAdError);
//                            listIDResume.removeAt(0)
//                            if (listIDResume.size == 0) {
//                                adCallback.onAdFailedToLoad(null)
//                                adCallback.onNextAction()
//                            } else {
//                                loadOpenAppAdSplashFloor(
//                                    context,
//                                    listIDResume,
//                                    isShowAdIfReady,
//                                    adCallback
//                                )
//                            }
//                        }
//
//                        override fun onAdLoaded(appOpenAd: AppOpenAd) {
//                            super.onAdLoaded(appOpenAd)
//                            splashAd = appOpenAd
//                            splashAd!!.onPaidEventListener =
//                                OnPaidEventListener { adValue: AdValue? ->
//                                    FirebaseUtil.logPaidAdImpression(
//                                        myApplication!!.applicationContext,
//                                        adValue,
//                                        appOpenAd.adUnitId,
//                                        AdType.APP_OPEN
//                                    )
//                                }
//                            if (isShowAdIfReady) {
//                                showAppOpenSplash(context, adCallback)
//                            } else {
//                                adCallback.onAdSplashReady()
//                            }
//                        }
//                    }
//                AppOpenAd.load(
//                    context,
//                    listIDResume[0],
//                    adRequest,
//                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
//                    appOpenAdLoadCallback
//                )
//            }
//        }
//    }
//
//    fun onCheckShowSplashWhenFail(
//        activity: AppCompatActivity,
//        callback: AdCallback,
//        timeDelay: Int
//    ) {
//        Handler(activity.mainLooper).postDelayed({
//            if (splashAd != null && !Companion.isShowingAd) {
//                Log.e("AppOpenManager", "show ad splash when show fail in background")
//                instance!!.showAppOpenSplash(activity, callback)
//            }
//        }, timeDelay.toLong())
//    }

    private fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    companion object {
        private const val TAG = "AppOpenManager"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: AppOpenManager? = null
        private var isShowingAd = false
        private const val TIMEOUT_MSG = 11

        @get:Synchronized
        val instance: AppOpenManager?
            get() {
                if (INSTANCE == null) {
                    INSTANCE = AppOpenManager()
                }
                return INSTANCE
            }
    }
}
