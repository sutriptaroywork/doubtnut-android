package com.doubtnutapp.qrpayment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.repository.QrPaymentRepository
import com.doubtnutapp.domain.payment.entities.PaymentStartBody
import com.doubtnutapp.domain.payment.entities.Taxation
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class QrPaymentViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val qrPaymentRepository: QrPaymentRepository
) : BaseViewModel(compositeDisposable) {

    private val _qrPaymentData
            : MutableLiveData<Outcome<Taxation>> = MutableLiveData()

    val qrPaymentData: LiveData<Outcome<Taxation>>
        get() = _qrPaymentData

    fun startLoading() {
        _qrPaymentData.value = Outcome.loading(true)
    }

    fun stopLoading() {
        _qrPaymentData.value = Outcome.loading(false)
    }

    fun fetchQrPaymentInitialData(paymentStartBody: PaymentStartBody) {
        startLoading()
        viewModelScope.launch {
            qrPaymentRepository.fetchQrPaymentInitialData(paymentStartBody)
                .map { it.data }
                .catch {
                    stopLoading()
                }
                .collect {
                    _qrPaymentData.value = Outcome.success(it)
                    stopLoading()
                }
        }
    }

    fun fetchQrPaymentData(orderId: String) {
        startLoading()
        viewModelScope.launch {
            qrPaymentRepository.fetchQrPaymentData(orderId)
                .map { it.data }
                .catch {
                    stopLoading()
                }
                .collect {
                    _qrPaymentData.value = Outcome.success(it)
                    stopLoading()
                }
        }
    }

    private val _messageStringIdLiveData: MutableLiveData<Event<Int>> = MutableLiveData()

    val messageStringIdLiveData: LiveData<Event<Int>>
        get() = _messageStringIdLiveData

    private fun onError(error: Throwable) {
        stopLoading()
        _messageStringIdLiveData.value = Event(R.string.something_went_wrong)
    }

}