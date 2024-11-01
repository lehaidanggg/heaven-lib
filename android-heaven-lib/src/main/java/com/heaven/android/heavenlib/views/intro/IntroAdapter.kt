package com.heaven.android.heavenlib.views.intro

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.nativead.NativeAdView
import com.heaven.android.heavenlib.R
import com.heaven.android.heavenlib.databinding.ItemIntroNormalBinding
import com.heaven.android.heavenlib.datas.HeavenSharePref
import com.heaven.android.heavenlib.datas.models.AppIntro

interface IOnIntroAdapterListener {
    fun onClickNextPage(currentPos: Int)
    fun onClickSkipAd(currentPos: Int) {}
    fun onAutoSkipAd() {}
}

class IntroAdapter(
    private var items: MutableList<AppIntro>,
    private val listener: IOnIntroAdapterListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_NORMAL = 1
        const val VIEW_TYPE_FULL_AD = 2
    }

    inner class NormalIntroViewHolder(
        private val itemView: View,
        private val listener: IOnIntroAdapterListener
    ) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.findViewById<TextView>(R.id.tvNext).setOnClickListener {
                listener.onClickNextPage(adapterPosition)
            }
        }

        fun bind(data: AppIntro) {
            val vContext = itemView.context

            val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
            val tvDescription = itemView.findViewById<TextView>(R.id.tvDescription)
            val tvNext = itemView.findViewById<TextView>(R.id.tvNext)
            val imgIntro = itemView.findViewById<ImageView>(R.id.imgIntro)
            val icIndex = itemView.findViewById<ImageView>(R.id.ic_index)
            val frAd = itemView.findViewById<FrameLayout>(R.id.frAd)

            tvTitle.text = vContext.getString(data.title)
            tvDescription.text = vContext.getString(data.description)
            tvNext.text = vContext.getString(data.titleNext)

            imgIntro.setImageResource(data.image)
            icIndex.setImageResource(data.icIndex)


//            data.nativeAd?.let {
//                val adView = getViewNativeNormalAd(vContext)
//                if (frAd.childCount > 0) {
//                    frAd.removeAllViews()
//                }
//                frAd.addView(adView)
//                NativeAdsManager.getInstance().displayNativeAdsHasMedia(it, adView)
//            }
        }

        private fun getViewNativeNormalAd(context: Context): NativeAdView {
            if (HeavenSharePref.isShowFullAds) {
                return LayoutInflater.from(context)
                    .inflate(R.layout.native_ads_media_fullad, null) as NativeAdView
            }
            return LayoutInflater.from(context)
                .inflate(R.layout.native_ads_media_normal, null) as NativeAdView
        }
    }

//    override fun getItemViewType(position: Int): Int {
//        return if (items[position].typeAd == TypeAdIntro.FULL_AD) {
//            VIEW_TYPE_FULL_AD
//        } else {
//            VIEW_TYPE_NORMAL
//        }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_intro_normal, parent, false)
        return NormalIntroViewHolder(view, listener)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NormalIntroViewHolder).bind(items[position])
    }

}