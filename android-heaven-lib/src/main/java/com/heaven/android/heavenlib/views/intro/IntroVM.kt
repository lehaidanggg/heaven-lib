package com.heaven.android.heavenlib.views.intro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

class IntroVM : ViewModel() {
    private var _indexIntro = MutableLiveData(0);
    val indexIntro: LiveData<Int> = _indexIntro


    fun onChangePage(index: Int) {
        _indexIntro.value = index
    }


    companion object {
        const val TAG = "IntroVM"
        //
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return IntroVM() as T
            }
        }
    }

}