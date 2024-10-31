package com.heaven.android.heavenlib.datas

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.ktx.Firebase
import com.heaven.android.heavenlib.datas.models.AppVersionModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.heaven.android.heavenlib.config.HeavenEnv
import com.heaven.android.heavenlib.datas.models.AdConfigModel
import com.heaven.android.heavenlib.utils.Logger

interface IRemoteCFListener {
    fun onRMCFLoadCompleted(error: String?)
}

object FBConfig {
    private var _adsConfig = MutableLiveData(AdConfigModel.defaultEnableAds())
    private var _appVersion = MutableLiveData(AppVersionModel.getDefaultWhenHasErr())


    private const val TAG = "FBConfig"
    private const val APP_VERSION = "config_app_version"
    private const val KEY_AD_CONFIG = "ads_configs"
    private const val TIME_OUT = 10000L

    fun config() {
        Firebase.remoteConfig.setConfigSettingsAsync(remoteConfigSettings {
            /*
            * DEBUG: 0s
            * RELEASE: 3600s
            * */
            minimumFetchIntervalInSeconds = if (HeavenEnv.buildConfig.isDebug) 0 else 3600
        })
    }

    fun getAdsConfig(): AdConfigModel {
        return _adsConfig.value ?: AdConfigModel.defaultEnableAds()
    }

    fun getAppVersionConfig(): AppVersionModel {
        return _appVersion.value ?: AppVersionModel.getDefaultWhenHasErr()
    }

    private suspend fun fetchAdsConfig(): AdConfigModel? = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            // Fetch remote config data
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val configMap = AdConfigModel.fromJson(
                            remoteConfig.getString(KEY_AD_CONFIG)
                        )
                        Logger.log(TAG, "fetchAdsConfig: ${remoteConfig.getString(KEY_AD_CONFIG)}")
                        continuation.resume(configMap)
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(null)
                    Logger.log(TAG, "fetchAdsConfig: ${exception.message}")
                }
        }
    }

    private suspend fun fetchUpdate(): AppVersionModel? = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            // Fetch remote config data
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val configMap = AppVersionModel.fromJson(
                            remoteConfig.getString(APP_VERSION)
                        )
                        Logger.log(TAG, "fetchUpdate: $configMap")
                        continuation.resume(configMap)
                    }
                }
                .addOnFailureListener { exception ->
                    continuation.resume(null)
                    Logger.log(TAG, "fetchUpdate: ERR:  ${exception.message}")
                }
        }
    }

    fun getFirebaseConfig(
        listener: IRemoteCFListener
    ) = CoroutineScope(Dispatchers.IO).launch {
        try {
            withTimeout(TIME_OUT) {
                val adsConfigModel = async { fetchAdsConfig() }.await()
                val versionModel = async { fetchUpdate() }.await()

                if (adsConfigModel == null || versionModel == null) {
                    return@withTimeout
                }
                _adsConfig.postValue(adsConfigModel)
                _appVersion.postValue(versionModel)
                listener.onRMCFLoadCompleted(null)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e("FETCH_RCF", "TIME OUT: ${e.message}")
            listener.onRMCFLoadCompleted(e.message)
        } catch (e: Exception) {
            Log.e("FETCH_RCF", "ERR WHEN FETCH RCF: ${e.message}")
            listener.onRMCFLoadCompleted(e.message)
        }
    }
}