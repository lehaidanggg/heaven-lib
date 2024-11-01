package com.heaven.android.heavenlib.views.language

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.heaven.android.heavenlib.R
import com.heaven.android.heavenlib.base.activity.BaseActivity
import com.heaven.android.heavenlib.config.HeavenEnv
import com.heaven.android.heavenlib.databinding.ActivityLanguageBinding
import com.heaven.android.heavenlib.datas.HeavenSharePref
import com.heaven.android.heavenlib.datas.models.AppLanguage
import com.heaven.android.heavenlib.views.intro.IntroActivity

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    private val configLanguage = HeavenEnv.configLanguage
    private lateinit var adapter: LanguageAdapter
    private var languageCode = ""

    override fun makeBinding(layoutInflater: LayoutInflater): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(layoutInflater)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        //
        binding.btnDone.alpha = 0.5F
        binding.btnDone.setOnClickListener {
            onClickDone()
        }
        //
        adapter = LanguageAdapter(
            configLanguage.languages.toMutableList(),
            object : IClickLanguage {
                override fun onClickLanguage(language: AppLanguage, position: Int) {
                    languageCode = language.code
                    binding.btnDone.alpha = 1F
                }
            }
        )

        binding.rcv.layoutManager = LinearLayoutManager(this)
        binding.rcv.adapter = adapter
    }

    private fun onClickDone() {
        if (languageCode.isNotEmpty()) {
            HeavenSharePref.languageCode = languageCode
            if (HeavenSharePref.isFirstInstall) {
                Intent(this, IntroActivity::class.java).also { startActivity(it) }
                finish()
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
            } else {
                finish()
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }
    }

}
