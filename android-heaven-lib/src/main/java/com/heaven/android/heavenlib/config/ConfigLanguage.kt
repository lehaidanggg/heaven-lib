package com.heaven.android.heavenlib.config

import androidx.appcompat.app.AppCompatActivity
import com.heaven.android.heavenlib.datas.models.AppLanguage

interface IClickDoneLanguage {
    fun onDoneLanguage() {}
}

data class ConfigLanguage(
    val bgItemLanguage: Int,
    val languages: List<AppLanguage>,
)