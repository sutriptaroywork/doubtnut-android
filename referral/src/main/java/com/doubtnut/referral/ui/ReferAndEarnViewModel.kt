package com.doubtnut.referral.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.base.CoreViewModel
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.utils.NetworkApiUtils
import com.doubtnut.referral.data.entity.ReferAndEarnLandingPageResponse
import com.doubtnut.referral.data.remote.ReferralRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReferAndEarnViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val referralRepository: ReferralRepository
) :
    CoreViewModel(compositeDisposable) {

    private val _mutableLiveDataReferAndEarn =
        MutableLiveData<Outcome<ReferAndEarnLandingPageResponse>>()
    val liveDataReferAndEarn: LiveData<Outcome<ReferAndEarnLandingPageResponse>> get() = _mutableLiveDataReferAndEarn

    private val _mutableLiveDataReferAndEarnFAQ =
        MutableLiveData<Outcome<ReferAndEarnLandingPageResponse>>()
    val liveDataReferAndEarnFAQ: LiveData<Outcome<ReferAndEarnLandingPageResponse>> get() = _mutableLiveDataReferAndEarnFAQ

    fun getReferAndEarnData() {
        viewModelScope.launch {
            try {
                _mutableLiveDataReferAndEarn.value = Outcome.loading(true)
                val response = referralRepository.getReferAndEarnLandingPageResponse()
                _mutableLiveDataReferAndEarn.value = Outcome.loading(false)
                _mutableLiveDataReferAndEarn.postValue(Outcome.success(response))
            } catch (e: Exception) {
                e.printStackTrace()
                _mutableLiveDataReferAndEarn.value = Outcome.loading(false)
                NetworkApiUtils.onApiError(_mutableLiveDataReferAndEarn, e)
            }
        }
    }

    fun getReferAndEarnFAQData() {
        viewModelScope.launch {
            try {
                _mutableLiveDataReferAndEarnFAQ.value = Outcome.loading(true)
                val response = referralRepository.getReferAndEarnFAQData()
                _mutableLiveDataReferAndEarnFAQ.value = Outcome.loading(false)
                _mutableLiveDataReferAndEarnFAQ.postValue(Outcome.success(response))
            } catch (e: Exception) {
                e.printStackTrace()
                NetworkApiUtils.onApiError(_mutableLiveDataReferAndEarnFAQ, e)
            }
        }
    }
}