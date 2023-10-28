package com.doubtnutapp.dnr.viewmodel

import com.doubtnutapp.base.BaseViewModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class DnrActivityViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable)
