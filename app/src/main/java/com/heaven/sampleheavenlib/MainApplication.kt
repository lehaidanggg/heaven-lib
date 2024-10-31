package com.heaven.sampleheavenlib

import com.heaven.android.heavenlib.HeavenApplication
import com.heaven.android.heavenlib.config.ConfigBuildInfo
import com.heaven.android.heavenlib.config.ConfigLanguage
import com.heaven.android.heavenlib.config.ConfigSplash
import com.heaven.android.heavenlib.config.HeavenEnv
import com.heaven.android.heavenlib.datas.models.AppLanguage

class MainApplication : HeavenApplication() {

    override fun initOtherFunc() {
        HeavenEnv.init(
            buildConfig = ConfigBuildInfo(
                applicationId = BuildConfig.APPLICATION_ID,
                isDebug = BuildConfig.DEBUG,
                versionCode = BuildConfig.VERSION_CODE,
                versionName = BuildConfig.VERSION_NAME
            ),
            configSplash = ConfigSplash(
                imageSplash = R.drawable.ic_launcher_background,
                tileSplash = R.string.app_name
            ),
            configLanguage = ConfigLanguage(
                bgItemLanguage = R.drawable.bg_language,
                languages = AppLanguage.appSupportedLanguages
            )
        )

    }

}