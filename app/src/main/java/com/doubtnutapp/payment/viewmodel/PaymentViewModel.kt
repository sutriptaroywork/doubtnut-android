package com.doubtnutapp.payment.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.domain.payment.entities.*
import com.doubtnutapp.domain.payment.interactor.FetchBreakThroughUseCase
import com.doubtnutapp.domain.payment.interactor.GetPaymentInitiateInfoUseCase
import com.doubtnutapp.domain.payment.interactor.SubmitFeedbackUseCase
import com.doubtnutapp.domain.payment.interactor.SubmitPaymentSuccess
import com.doubtnutapp.payment.event.PaymentEventManager
import com.doubtnutapp.plus
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-13.
 */

class PaymentViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase,
    private val getPaymentInitiateInfoUseCase: GetPaymentInitiateInfoUseCase,
    private val submitPaymentSuccess: SubmitPaymentSuccess,
    private val paymentEventManager: PaymentEventManager,
    private val fetchBreakThroughUseCase: FetchBreakThroughUseCase
) : BaseViewModel(compositeDisposable) {

    private val _messageStringIdLiveData: MutableLiveData<Event<Int>> = MutableLiveData()

    val messageStringIdLiveData: LiveData<Event<Int>>
        get() = _messageStringIdLiveData

    private val _transactionInfo: MutableLiveData<Event<Taxation>> = MutableLiveData()

    val transactionInfo: LiveData<Event<Taxation>>
        get() = _transactionInfo

    private val _breakThroughList: MutableLiveData<Event<List<BreakThrough>>> = MutableLiveData()

    val breakThroughList: LiveData<Event<List<BreakThrough>>>
        get() = _breakThroughList

    private val _serverResponseOnPayment: MutableLiveData<Event<ServerResponsePayment>> =
        MutableLiveData()

    val serverResponseOnPayment: LiveData<Event<ServerResponsePayment>>
        get() = _serverResponseOnPayment


    private val _planDetail: MutableLiveData<Event<PlanDetail>> = MutableLiveData()

    val planDetail: LiveData<Event<PlanDetail>>
        get() = _planDetail

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()


    fun startLoading() {
        isLoading.postValue(true)
    }

    fun stopLoading() {
        isLoading.postValue(false)
    }

    private fun onPlanDetailSuccess(planDetail: PlanDetail) {
        stopLoading()
        _planDetail.postValue(Event(planDetail))
    }


    fun fetchPaymentInitialInfo(paymentStartBody: PaymentStartBody) {
        startLoading()
        compositeDisposable + getPaymentInitiateInfoUseCase
            .execute(paymentStartBody)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onSuccess, this::onError)
    }

    fun submitRazorPaySuccessToServer(orderId: String, paymentId: String, signature: String) {
        startLoading()
        val paymentResponse = hashMapOf<String, String>()
        paymentResponse["razorpay_order_id"] = orderId
        paymentResponse["razorpay_payment_id"] = paymentId
        paymentResponse["razorpay_signature"] = signature
        compositeDisposable + submitPaymentSuccess
            .execute(SubmitPaymentSuccess.RequestValues("RAZORPAY", paymentResponse))
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
        _serverResponseOnPayment.postValue(
            Event(
                ServerResponsePayment(
                    "FAILURE",
                    "",
                    null,
                    null,
                    null,
                    ""
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

    fun submitFeedback(message: String) {
        startLoading()
        compositeDisposable + submitFeedbackUseCase
            .execute(message)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onFeedbackSubmitSuccess, this::onError)
    }

    private fun onFeedbackSubmitSuccess(any: Any) {
        stopLoading()
        _messageStringIdLiveData.value = Event(R.string.feedback_submitted_successfully)
    }

    fun fetchBreakThrough() {
        startLoading()
        compositeDisposable + fetchBreakThroughUseCase
            .execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onBreakThroughSuccess, this::onError)
    }

    private fun onBreakThroughSuccess(breakThroughList: List<BreakThrough>) {
        stopLoading()
        _breakThroughList.postValue(Event(breakThroughList))
    }


}