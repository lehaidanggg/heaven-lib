package com.heaven.android.heavenlib.datas.models

import com.heaven.android.heavenlib.R

class AppLanguage(
    var name: String,
    var code: String,
    var isSelected: Boolean,
    var flag: Int
) {

    companion object {
        val appSupportedLanguages: ArrayList<AppLanguage>
            get() {
                val appLanguages = ArrayList<AppLanguage>()
                appLanguages.add(AppLanguage("India", "hi", false, R.drawable.ic_hi))
                appLanguages.add(AppLanguage("Chinese", "zh", false, R.drawable.ic_zh))
                appLanguages.add(AppLanguage("French", "fr", false, R.drawable.ic_fr))
                appLanguages.add(AppLanguage("Germany", "de", false, R.drawable.ic_de))
                appLanguages.add(AppLanguage("Indonesian", "id", false, R.drawable.ic_id))
                appLanguages.add(AppLanguage("English", "en", false, R.drawable.ic_en))

                return appLanguages
            }
    }
}