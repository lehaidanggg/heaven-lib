package com.heaven.android.heavenlib.dialogs

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.heaven.android.heavenlib.R
import com.heaven.android.heavenlib.base.dialog.BaseDialogFragment
import com.heaven.android.heavenlib.config.HeavenEnv
import com.heaven.android.heavenlib.databinding.DialogRatingBinding
import com.heaven.android.heavenlib.utils.setOnSingleClickListener

interface IRateClickListener {
    fun onRateLessFourStart()
    fun onRateClick(isNeedFinish: Boolean)
}

class DialogRating(
    private val listener: IRateClickListener?
) : BaseDialogFragment<DialogRatingBinding>() {

    override fun makeBinding(inflater: LayoutInflater, container: ViewGroup?): DialogRatingBinding {
        return DialogRatingBinding.inflate(inflater, container, false)
    }

    var indexStar = 4
    var isSelected = false
    private var isFromMenu: Boolean = false

    override fun setupView() {
        isCancelable = false

        val listStars = arrayOf(
            binding.imgStar1,
            binding.imgStar2,
            binding.imgStar3,
            binding.imgStar4,
            binding.img5Star,
        )

        listStars.forEachIndexed { index, view ->
            view.setOnSingleClickListener {
                if (indexStar == index && isSelected) {
                    return@setOnSingleClickListener
                }
                indexStar = index
                isSelected = true

                binding.tvTitle.text = getString(R.string.thanks_for_rating)
                binding.tvDescription.text = genDescription(index)
                checkStarRate(index)

                val isSelected = BooleanArray(listStars.size)
                if (!isSelected[index]) {
                    if (view.scaleX == 1f && view.scaleY == 1f) {
                        view.startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.zoom_out
                            )
                        )
                    } else {
                        view.startAnimation(
                            AnimationUtils.loadAnimation(
                                requireContext(),
                                R.anim.zoom_in
                            )
                        )
                    }
                }
            }
        }

        binding.btnNotNow.setOnSingleClickListener {
            dismiss()
        }

        binding.btnRate.setOnSingleClickListener {
            rateUS(indexStar + 1)
            listener?.onRateClick(!isFromMenu)
        }
    }

    private fun checkStarRate(index: Int) {
        val starImages = listOf(
            R.drawable.ic_star_filled,
            R.drawable.ic_star_none
        )

        val statusIcons = listOf(
            R.drawable.ic_rating_smile_2,
            R.drawable.ic_rating_smile_3,
            R.drawable.ic_rating_smile_4,
            R.drawable.ic_rating_smile_5,
            R.drawable.ic_rating_smile_6
        )

        val starImageViews = listOf(
            binding.imgStar1,
            binding.imgStar2,
            binding.imgStar3,
            binding.imgStar4,
            binding.img5Star
        )

        for (i in 0..4) {
            starImageViews[i].setImageResource(starImages[if (i <= index) 0 else 1])
        }
        binding.imgStatus.setImageResource(statusIcons[index])
    }

    private fun rateUS(i: Int) {
        if (i == 5 || i == 4) {
            navigateToStore()
            dismiss()
        } else {
            dismiss()
            listener?.onRateLessFourStart()
        }
    }

    private fun navigateToStore() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${HeavenEnv.buildConfig.applicationId}")
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${HeavenEnv.buildConfig.applicationId}")
                )
            )
        }
    }

    private fun genDescription(index: Int): String {
        return when (index) {
            0 -> getString(R.string.give_us_a_quick_rating)
            1 -> getString(R.string.we_are_working_hard)
            3 -> getString(R.string.we_are_working_hard)
            4 -> getString(R.string.that_great_to_hear)
            5 -> getString(R.string.that_great_to_hear)
            else -> getString(R.string.we_are_working_hard)

        }
    }
}