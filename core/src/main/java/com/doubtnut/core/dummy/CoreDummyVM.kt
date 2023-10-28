package com.doubtnut.core.dummy

import com.doubtnut.core.base.CoreViewModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class CoreDummyVM @Inject constructor(compositeDisposable: CompositeDisposable) :
    CoreViewModel(compositeDisposable)