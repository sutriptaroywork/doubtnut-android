package com.doubtnut.core.base

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable

open class CoreViewModel(val compositeDisposable: CompositeDisposable) : ViewModel() {

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}