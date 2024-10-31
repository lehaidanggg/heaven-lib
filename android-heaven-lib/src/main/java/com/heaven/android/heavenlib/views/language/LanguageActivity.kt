package com.heaven.android.heavenlib.views.language

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.heaven.android.heavenlib.R
import com.heaven.android.heavenlib.base.activity.BaseActivity
import com.heaven.android.heavenlib.config.HeavenEnv
import com.heaven.android.heavenlib.databinding.ActivityLanguageBinding
import com.heaven.android.heavenlib.datas.HeavenSharePref
import com.heaven.android.heavenlib.datas.models.AppLanguage
import com.heaven.android.heavenlib.utils.Logger

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    private val languages = HeavenEnv.configLanguage.languages
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
        adapter = LanguageAdapter(languages.toMutableList(), object : IClickLanguage {
            override fun onClickLanguage(language: AppLanguage, position: Int) {
                languageCode = language.code
                binding.btnDone.alpha = 1F
            }
        })

        binding.rcv.layoutManager = LinearLayoutManager(this)
        binding.rcv.adapter = adapter
    }

    private fun onClickDone() {
        if (languageCode.isNotEmpty()) {
            HeavenSharePref.languageCode = languageCode
        }
    }


}
