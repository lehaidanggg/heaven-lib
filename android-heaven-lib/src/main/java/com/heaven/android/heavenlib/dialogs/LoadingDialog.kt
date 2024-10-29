package com.heaven.android.heavenlib.dialogs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.heaven.android.heavenlib.base.dialog.BaseDialogFragment
import com.heaven.android.heavenlib.databinding.DialogLoadingBinding

class LoadingDialog : BaseDialogFragment<DialogLoadingBinding>() {
    override fun makeBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogLoadingBinding {
        return DialogLoadingBinding.inflate(inflater, container, false)
    }

    override fun setupView() {
        super.setupView()
    }
}