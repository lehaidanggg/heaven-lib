package com.heaven.android.heavenlib.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, languageCode: String, recreate: Boolean = true) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources: Resources = context.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        if (recreate) (context as Activity).recreate()
    }
}

class MyContextWrapper(base: Context?) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context?, language: String): ContextWrapper {
            var mContext = context
            val config = mContext?.resources?.configuration
            var sysLocale: Locale? = null
            sysLocale = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                config?.let { getSystemLocale(it) }
            } else {
                config?.let { getSystemLocaleLegacy(it) }
            }
            if (language != "" && sysLocale?.language != language) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                config?.let { setSystemLocale(it, locale) }
            }
            mContext = config?.let { mContext?.createConfigurationContext(it) }
            return MyContextWrapper(mContext)
        }

        private fun getSystemLocaleLegacy(config: Configuration): Locale {
            return config.locale
        }

        fun getSystemLocale(config: Configuration): Locale {
            return config.locales[0]
        }

        private fun setSystemLocaleLegacy(config: Configuration, locale: Locale?) {
            config.locale = locale
        }

        fun setSystemLocale(config: Configuration, locale: Locale?) {
            config.setLocale(locale)
        }
    }
}