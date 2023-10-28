package com.doubtnut.referral.ui

import com.doubtnut.core.base.CoreViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ReferralActivityVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
) : CoreViewModel(compositeDisposable) {

    private val _title = MutableStateFlow<String?>(null)
    val title: StateFlow<String?>
        get() = _title

    private val _mobile = MutableStateFlow<String?>(null)
    val mobile: StateFlow<String?>
        get() = _mobile

    fun updateTitle(title: String?) {
        _title.value = title
    }

    fun updateMobile(mobile: String?) {
        _mobile.value = mobile
    }

}