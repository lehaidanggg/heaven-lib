package com.heaven.android.heavenlib.views.intro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.heaven.android.heavenlib.R
import com.heaven.android.heavenlib.base.activity.BaseActivity
import com.heaven.android.heavenlib.config.HeavenEnv
import com.heaven.android.heavenlib.databinding.ActivityIntroBinding
import com.heaven.android.heavenlib.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IntroActivity : BaseActivity<ActivityIntroBinding>() {
    private val configIntro = HeavenEnv.configIntro

    private val viewmodel by viewModels<IntroVM> {
        IntroVM.Factory
    }

    private lateinit var adapter: IntroAdapter
    private var job: Job? = null

    private val onChangePager = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            viewmodel.onChangePage(position)
        }
    }

    override fun makeBinding(layoutInflater: LayoutInflater): ActivityIntroBinding {
        return ActivityIntroBinding.inflate(layoutInflater)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        //
        adapter = IntroAdapter(
            configIntro.intros.toMutableList(),
            object : IOnIntroAdapterListener {
                override fun onClickNextPage(currentPos: Int) {
                    if (currentPos == configIntro.intros.size - 1) {
                        Toast.makeText(this@IntroActivity, "Last intro", Toast.LENGTH_SHORT).show()
                    } else {
                        viewmodel.onChangePage(currentPos + 1)
                    }
                }
            }
        )
        binding.vPager.adapter = adapter
        binding.vPager.registerOnPageChangeCallback(onChangePager)
    }

    override fun observeUI() {
        super.observeUI()
        viewmodel.indexIntro.observe(this@IntroActivity) { index ->
            binding.vPager.setCurrentItem(index, true)
        }
    }

    private fun startDelay() {
        Logger.log("asdasd", "start delay")
        cancelDelay()

        job = CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
        }
    }

    private fun cancelDelay() {
        Logger.log("asdasd", "cancel delay")
        job?.cancel()
        job = null
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.vPager.unregisterOnPageChangeCallback(onChangePager)
        cancelDelay()
    }
}