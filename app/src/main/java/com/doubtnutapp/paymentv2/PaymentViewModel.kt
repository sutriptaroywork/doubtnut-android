package com.doubtnutapp.paymentv2

import android.nfc.FormatException
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.domain.payment.entities.PaymentDiscount
import com.doubtnutapp.domain.payment.entities.PaymentStartBody
import com.doubtnutapp.domain.payment.entities.ServerResponsePayment
import com.doubtnutapp.domain.payment.entities.Taxation
import com.doubtnutapp.domain.payment.interactor.GetContinuePaymentInfoUseCase
import com.doubtnutapp.domain.payment.interactor.GetPaymentDiscountUseCase
import com.doubtnutapp.domain.payment.interactor.GetPaymentInitiateInfoUseCase
import com.doubtnutapp.domain.payment.interactor.SubmitPaymentSuccess
import com.doubtnutapp.plus
import com.doubtnutapp.studygroup.service.StudyGroupRepository
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class PaymentViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val getPaymentInitiateInfoUseCase: GetPaymentInitiateInfoUseCase,
    private val submitPaymentSuccess: SubmitPaymentSuccess,
    private val getPaymentDiscountUseCase: GetPaymentDiscountUseCase,
    private val continuePaymentInfoUseCase: GetContinuePaymentInfoUseCase,
    private val studyGroupRepository: StudyGroupRepository
) : BaseViewModel(compositeDisposable) {

    private val _messageStringIdLiveData: MutableLiveData<Event<Int>> = MutableLiveData()

    val messageStringIdLiveData: LiveData<Event<Int>>
        get() = _messageStringIdLiveData

    private val _transactionInfo: MutableLiveData<Event<Taxation>> = MutableLiveData()

    val transactionInfo: LiveData<Event<Taxation>>
        get() = _transactionInfo

    private val _paymentDiscountInfo: MutableLiveData<Event<PaymentDiscount>> = MutableLiveData()

    val paymentDiscountInfo: LiveData<Event<PaymentDiscount>>
        get() = _paymentDiscountInfo

    private val _serverResponseOnPayment: MutableLiveData<Event<ServerResponsePayment>> =
        MutableLiveData()

    val serverResponseOnPayment: LiveData<Event<ServerResponsePayment>>
        get() = _serverResponseOnPayment

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    fun startLoading() {
        isLoading.postValue(true)
    }

    fun stopLoading() {
        isLoading.postValue(false)
    }

    fun fetchPaymentInitialInfo(paymentStartBody: PaymentStartBody) {
        startLoading()
        compositeDisposable + getPaymentInitiateInfoUseCase
            .execute(paymentStartBody)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onSuccess, this::onError)
    }

    fun fetchContinuePaymentInfo(orderId: String) {
        startLoading()
        compositeDisposable + continuePaymentInfoUseCase
            .execute(orderId)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onSuccess, this::onError)
    }

    fun fetchPaymentDiscountInfo(orderId: String) {
        startLoading()
        compositeDisposable + getPaymentDiscountUseCase
            .execute(GetPaymentDiscountUseCase.Param(orderId))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onPaymentDiscountSuccess, this::onPaymentDiscountError)
    }

    fun submitPaymentSuccessToServer(source: String, paymentProviderData: Bundle) {
        startLoading()
        val paymentProviderDataMap = hashMapOf<String, String>()

        // this only works with string keys so make sure that all payment provider data is in string
        val iterator = paymentProviderData.keySet().iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            paymentProviderData.getString(key)?.let {
                paymentProviderDataMap[key] = it
            }
        }
        compositeDisposable + submitPaymentSuccess
            .execute(SubmitPaymentSuccess.RequestValues(source, paymentProviderDataMap))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(
                this::onSubmitPaymentSuccessFromServer,
                this::onPaymentRejectedFromServer
            )
    }

    private fun onSubmitPaymentSuccessFromServer(ServerResponsePayment: ServerResponsePayment) {
        stopLoading()
        _serverResponseOnPayment.postValue(Event(ServerResponsePayment))
    }

    private fun onPaymentRejectedFromServer(error: Throwable) {
        stopLoading()
        _messageStringIdLiveData.value = Event(R.string.something_went_wrong)
        _serverResponseOnPayment.postValue(
            Event(
                ServerResponsePayment(
                    "FAILURE",
                    "",
                    null,
                    null,
                    null
                ,
                    ""
                )
            )
        )
        logException(error)
    }

    fun onPaymentDiscountSuccess(paymentDiscount: PaymentDiscount) {
        stopLoading()
        _paymentDiscountInfo.postValue(Event(paymentDiscount))
    }

    private fun onPaymentDiscountError(error: Throwable) {
        stopLoading()
        _messageStringIdLiveData.value = Event(R.string.something_went_wrong)
        _paymentDiscountInfo.postValue(
            Event(
                PaymentDiscount(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            )
        )
        logException(error)
    }

    fun onSuccess(taxation: Taxation) {
        stopLoading()
        _transactionInfo.postValue(Event(taxation))
    }

    private fun onError(error: Throwable) {
        stopLoading()
        _messageStringIdLiveData.value = Event(R.string.something_went_wrong)
        logException(error)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException
                || error is NullPointerException
                || error is ClassCastException
                || error is FormatException
                || error is IllegalArgumentException
            ) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    fun joinTeacherStudyGroup(courseId: String, assortmentType: String, batchId: String) {
        compositeDisposable +
                studyGroupRepository.joinTeachersGroup(
                    courseId, assortmentType, batchId, null
                )
                    .applyIoToMainSchedulerOnCompletable()
                    .subscribeToCompletable({})
    }
}