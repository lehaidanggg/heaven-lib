package com.heaven.android.heavenlib

import android.app.Application
import com.heaven.android.heavenlib.datas.HeavenSharePref
import com.heaven.android.heavenlib.utils.PackageInfo

abstract class HeavenApplication : Application() {

    abstract fun initOther()
    abstract var isDebugMode: Boolean

    // init someone here
    override fun onCreate() {
        super.onCreate()
        //
        initData()
        initOther()
    }

    fun initFirebase() {}
    fun initAppFlyer() {}
    private fun initData() {
        PackageInfo.getPackageInfo(applicationContext)
        PackageInfo.setDebugMode(isDebugMode)
        //
        HeavenSharePref.init(applicationContext)
    }

}