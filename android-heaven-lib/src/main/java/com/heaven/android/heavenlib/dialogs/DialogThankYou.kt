package com.heaven.android.heavenlib.dialogs

import android.view.LayoutInflater
import android.view.ViewGroup
import com.heaven.android.heavenlib.base.dialog.BaseDialogFragment
import com.heaven.android.heavenlib.databinding.DialogThanksBinding

class DialogThankYou : BaseDialogFragment<DialogThanksBinding>() {

    override fun makeBinding(inflater: LayoutInflater, container: ViewGroup?): DialogThanksBinding {
        return DialogThanksBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        super.setupView()
        binding.btnOke.setOnClickListener {
            dismiss()
        }
    }
}