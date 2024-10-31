package com.heaven.android.heavenlib.views.splash

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.gms.ads.MobileAds
import com.heaven.android.heavenlib.ads.InterstitialAdManager
import com.heaven.android.heavenlib.ads.NativeAdManagerV2
import com.heaven.android.heavenlib.base.activity.BaseActivity
import com.heaven.android.heavenlib.config.HeavenEnv
import com.heaven.android.heavenlib.databinding.ActivitySplashBinding
import com.heaven.android.heavenlib.datas.FBConfig
import com.heaven.android.heavenlib.datas.HeavenSharePref
import com.heaven.android.heavenlib.datas.IRemoteCFListener
import com.heaven.android.heavenlib.datas.IUmpListener
import com.heaven.android.heavenlib.datas.UMPUtils
import com.heaven.android.heavenlib.datas.models.StatusForceUpdate
import com.heaven.android.heavenlib.utils.Logger
import com.heaven.android.heavenlib.utils.Utils.isNeedUpdateAppVersions
import com.heaven.android.heavenlib.views.intro.IntroActivity
import com.heaven.android.heavenlib.views.language.LanguageActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    private val interAds = InterstitialAdManager.getInstance()
    private var isMobileAdsInitializeCalled = AtomicBoolean(false)

    override fun makeBinding(layoutInflater: LayoutInflater): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(layoutInflater)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        //setup view
        with(binding) {
            imgSplash.setImageResource(HeavenEnv.configSplash.imageSplash)
            tvSplash.text = getString(HeavenEnv.configSplash.tileSplash)
        }
        // request consent
        requestConsent()
    }

    private fun requestConsent() {
        UMPUtils.requestConsent(
            this,
            object : IUmpListener {
                override fun requestConsentCompleted(err: String?) {
                    Logger.log("requestConsent", "$err")
                    initMobileAds()
                }
            })
    }

    private fun fetchRMCF() {
        FBConfig.getFirebaseConfig(object : IRemoteCFListener {
            override fun onRMCFLoadCompleted(error: String?) {
                Logger.log("fetchRMCF", "Error RMCF: $error")
                checkUpdate()
            }
        })
    }

    private fun initMobileAds() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }
        val backgroundThread = CoroutineScope(Dispatchers.IO)
        backgroundThread.launch {
            MobileAds.initialize(this@SplashActivity) {
                runOnUiThread {
                    fetchRMCF()
                }
            }
        }
    }

    private fun checkUpdate() {
        val data = FBConfig.getAppVersionConfig()
        when (isNeedUpdateAppVersions(
            HeavenEnv.buildConfig.versionName,
            data
        )) {
            StatusForceUpdate.NONE -> {
                runOnUiThread {
                    showAdsInter()
                }
            }

            StatusForceUpdate.HAS_NO_THANKS -> {
                Logger.log("TAG", "checkUpdate===: HAS_NO_THANKS")
                showDialogForceUpdate(false,
                    onClickUpdate = {
                        runOnUiThread {
                            onClickUpdateDialog()
                        }
                    },
                    onClickNoThanks = {
                        runOnUiThread {
                            onClickNoThankDialog()
                        }
                    })
            }

            StatusForceUpdate.ONLY_UPDATE -> {
                Logger.log("TAG", "checkUpdate===: ONLY_UPDATE")
                runOnUiThread {
                    showDialogForceUpdate(true,
                        onClickUpdate = {
                            onClickUpdateDialog()
                        },
                        onClickNoThanks = {
                            onClickNoThankDialog()
                        })
                }
            }
        }
    }

    private fun showAdsInter() {
        preloadAdLanguageOrIntro()
        interAds.loadInterAds(
            this,
            adUnitId = "",
            enable = FBConfig.getAdsConfig().enable_inter_splash,
            isSplashAd = true,
            onSuccessAds = {
                checkScreen()
            },
            onLoadErr = { error ->
                Logger.log("showAdsInter", error)
                checkScreen()
            }
        )
    }

    private fun checkScreen() {
//        if (HeavenSharePref.isFirstInstall) {
        val intent = Intent(this@SplashActivity, LanguageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
//        } else {
//            val intent = Intent(this@SplashActivity, IntroActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//            finish()
//        }
    }

    private fun onClickNoThankDialog() {
        checkScreen()
        dismissDialogForceUpdate()
    }

    private fun onClickUpdateDialog() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${HeavenEnv.buildConfig.applicationId}")
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${HeavenEnv.buildConfig.applicationId}")
                )
            )
        }
        finish()
    }

    private fun preloadAdLanguageOrIntro() {
        if (HeavenSharePref.isFirstInstall) {
            NativeAdManagerV2.getInstance()
                .loadAd(
                    this,
                    "ca-app-pub-2857599586325936/3047367211",
                    FBConfig.getAdsConfig().enable_native_language
                )
            NativeAdManagerV2.getInstance()
                .loadAd(
                    this,
                    "ca-app-pub-2857599586325936/1676006411",
                    FBConfig.getAdsConfig().enable_native_language_selected
                )
        }
    }
}