package com.doubtnut.referral.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.base.CoreViewModel
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.referral.data.entity.ReferralInfoResponse
import com.doubtnut.referral.data.remote.ReferralRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class ReferralHomeFragmentVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val referralRepository: ReferralRepository
) : CoreViewModel(compositeDisposable) {

    private val _referralInfoResponse: MutableLiveData<Outcome<ReferralInfoResponse>> =
        MutableLiveData()
    val referralInfoResponse: LiveData<Outcome<ReferralInfoResponse>>
        get() = _referralInfoResponse

    fun getReferralInfo(
        page: Int,
    ) {
        _referralInfoResponse.value = Outcome.loading(true)
        viewModelScope.launch {
            referralRepository.getReferralInfo(
                page = page
            )
                .catch {
                    it.printStackTrace()
                    _referralInfoResponse.value = Outcome.loading(false)
                    _referralInfoResponse.value = if (it is HttpException) {
                        when (it.response()?.code()) {
                            HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(
                                it.message ?: ""
                            )
                            HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(it)
                            else -> Outcome.Failure(it)
                        }
                    } else {
                        Outcome.Failure(it)
                    }
                }
                .collect { res ->
                    _referralInfoResponse.value = Outcome.loading(false)
                    _referralInfoResponse.value = Outcome.success(res)
                }
        }
    }
}