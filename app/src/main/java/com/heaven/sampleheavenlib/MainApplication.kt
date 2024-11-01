package com.heaven.sampleheavenlib

import android.content.Intent
import com.heaven.android.heavenlib.HeavenApplication
import com.heaven.android.heavenlib.config.ConfigBuildInfo
import com.heaven.android.heavenlib.config.ConfigIntro
import com.heaven.android.heavenlib.config.ConfigLanguage
import com.heaven.android.heavenlib.config.ConfigSplash
import com.heaven.android.heavenlib.config.HeavenEnv
import com.heaven.android.heavenlib.config.IClickDoneLanguage
import com.heaven.android.heavenlib.datas.HeavenSharePref
import com.heaven.android.heavenlib.datas.models.AppIntro
import com.heaven.android.heavenlib.datas.models.AppLanguage

class MainApplication : HeavenApplication() {

    override fun initOtherFunc() {
        HeavenEnv.init(
            buildConfig = ConfigBuildInfo(
                applicationId = BuildConfig.APPLICATION_ID,
                isDebug = BuildConfig.DEBUG,
                versionCode = BuildConfig.VERSION_CODE,
                versionName = BuildConfig.VERSION_NAME
            ), configSplash = ConfigSplash(
                imageSplash = R.drawable.ic_launcher_background,
                tileSplash = R.string.app_name,
                adUnitSplash = ""
            ), configLanguage = ConfigLanguage(
                bgItemLanguage = R.drawable.bg_language,
                languages = AppLanguage.appSupportedLanguages,
            ), configIntro = ConfigIntro(
                getListContentIntro(),
                MainActivity::class.java
            )

        )
    }

    private fun getListContentIntro(): List<AppIntro> {
        val lsPages = ArrayList<AppIntro>()
        lsPages.add(
            AppIntro(
                R.string.title_intro1,
                R.string.des_intro1,
                R.drawable.img_intro1,
                R.drawable.ic_index_1,
                R.string.title_next,
                ""
            )
        )
        lsPages.add(
            AppIntro(
                R.string.title_intro2,
                R.string.des_intro2,
                R.drawable.img_intro2,
                R.drawable.ic_index_2,
                R.string.title_next,
                ""
            )
        )
        lsPages.add(
            AppIntro(
                R.string.title_intro3,
                R.string.des_intro3,
                R.drawable.img_intro3,
                R.drawable.ic_index_3,
                R.string.title_next,
                ""
            )
        )
        lsPages.add(
            AppIntro(
                R.string.title_intro4,
                R.string.des_intro4,
                R.drawable.img_intro4,
                R.drawable.ic_index_4,
                R.string.title_start,
                ""
            )
        )

        return lsPages
    }
}