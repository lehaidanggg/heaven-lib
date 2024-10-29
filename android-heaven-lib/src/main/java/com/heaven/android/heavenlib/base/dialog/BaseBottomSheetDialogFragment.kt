package com.heaven.android.heavenlib.base.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialogFragment<B : ViewBinding> : BottomSheetDialogFragment() {
    lateinit var binding: B

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = makeBinding(inflater, container)
        setupView()
        return binding.root
    }

    abstract fun makeBinding(inflater: LayoutInflater, container: ViewGroup?): B

    open fun setupView() {}
}