package com.heaven.android.heavenlib.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.heaven.android.heavenlib.base.dialog.BaseDialogFragment
import com.heaven.android.heavenlib.databinding.DialogForceUpdateBinding

interface IClickForeUpdate {
    fun onClickUpdate()
    fun onClickNoThanks()
}

class DialogForceUpdate(
    private val onlyUpdate: Boolean,
    private val listener: IClickForeUpdate
) : BaseDialogFragment<DialogForceUpdateBinding>() {

    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogForceUpdateBinding {
        return DialogForceUpdateBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        super.setupView()
        binding.tvNoThanks.visibility = if (onlyUpdate) View.GONE else View.VISIBLE
        binding.tvNoThanks.setOnClickListener {
            listener.onClickNoThanks()
        }
        //
        binding.tvUpdate.setOnClickListener {
            listener.onClickUpdate()
        }
    }
}