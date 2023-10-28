package com.doubtnutapp.dummy

import com.doubtnutapp.base.BaseViewModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@Deprecated("use CoreDummyVM and CoreDummyModule")
class DummyViewModel @Inject constructor(compositeDisposable: CompositeDisposable) :
    BaseViewModel(compositeDisposable)
