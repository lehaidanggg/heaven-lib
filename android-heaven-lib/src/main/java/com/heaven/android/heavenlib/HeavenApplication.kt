package com.heaven.android.heavenlib

import android.app.Application
import com.heaven.android.heavenlib.datas.FBConfig
import com.heaven.android.heavenlib.datas.HeavenSharePref
import java.lang.RuntimeException

abstract class HeavenApplication : Application() {

    /*
    * - setup ảnh, title splash
    * - language: default 7
    * - intro: 3 ảnh intro, 3 title intro.
    *
    * - rating:
    * - share:
    * - term of use, privacy
    *
    * - firebase: enable ad, version app
    *
    * - ad
    *
    * - biến môi trường: Build config
    *
    * */

    // init someone here
    override fun onCreate() {
        super.onCreate()
        //
        initData()
        initOtherFunc()
        initFireBase()
    }

    abstract fun initOtherFunc()

    private fun initData() {
        HeavenSharePref.init(applicationContext)
    }

    private fun initFireBase() {
        FBConfig.config()
    }

}