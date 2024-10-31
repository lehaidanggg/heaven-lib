package com.heaven.android.heavenlib.datas.models

import com.heaven.android.heavenlib.R

class AppLanguage(
    var name: String,
    var code: String,
    var flag: Int
) {

    companion object {
        val appSupportedLanguages: ArrayList<AppLanguage>
            get() {
                val appLanguages = ArrayList<AppLanguage>()
                appLanguages.add(AppLanguage("India", "hi", R.drawable.ic_hi))
                appLanguages.add(AppLanguage("Chinese", "zh", R.drawable.ic_zh))
                appLanguages.add(AppLanguage("French", "fr", R.drawable.ic_fr))
                appLanguages.add(AppLanguage("Germany", "de", R.drawable.ic_de))
                appLanguages.add(AppLanguage("Indonesian", "id", R.drawable.ic_id))
                appLanguages.add(AppLanguage("English", "en", R.drawable.ic_en))

                return appLanguages
            }
    }
}
