package com.heaven.android.heavenlib.config

import androidx.appcompat.app.AppCompatActivity
import com.heaven.android.heavenlib.datas.models.AppIntro

data class ConfigIntro(
    val intros: List<AppIntro>,
    val nextScreen: Class<out AppCompatActivity>
)
