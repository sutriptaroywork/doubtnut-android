package com.doubtnutapp.doubtplan

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.domain.payment.entities.BreakThrough
import com.doubtnutapp.domain.payment.entities.DoubtPlanDetail
import com.doubtnutapp.domain.payment.interactor.FetchBreakThroughUseCase
import com.doubtnutapp.domain.payment.interactor.GetDoubtPlanDetailUseCase
import com.doubtnutapp.domain.payment.interactor.SubmitFeedbackUseCase
import com.doubtnutapp.payment.event.PaymentEventManager
import com.doubtnutapp.plus
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 16/10/20.
 */
class DoubtPackageViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase,
    private val getDoubtPlanDetailUseCase: GetDoubtPlanDetailUseCase,
    private val paymentEventManager: PaymentEventManager,
    private val fetchBreakThroughUseCase: FetchBreakThroughUseCase
) : BaseViewModel(compositeDisposable) {

    private val _messageStringIdLiveData: MutableLiveData<Event<Int>> = MutableLiveData()

    val messageStringIdLiveData: LiveData<Event<Int>>
        get() = _messageStringIdLiveData

    private val _breakThroughList: MutableLiveData<Event<List<BreakThrough>>> = MutableLiveData()

    val breakThroughList: LiveData<Event<List<BreakThrough>>>
        get() = _breakThroughList

    private val _planDetail: MutableLiveData<Event<DoubtPlanDetail>> = MutableLiveData()

    val planDetail: LiveData<Event<DoubtPlanDetail>>
        get() = _planDetail

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    fun startLoading() {
        isLoading.postValue(true)
    }

    fun stopLoading() {
        isLoading.postValue(false)
    }

    fun fetchPlanDetail() {
        startLoading()
        compositeDisposable + getDoubtPlanDetailUseCase
            .execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onPlanDetailSuccess, this::onError)
    }

    private fun onPlanDetailSuccess(doubtPlanDetail: DoubtPlanDetail) {
        stopLoading()
        _planDetail.postValue(Event(doubtPlanDetail))
    }

    private fun onError(error: Throwable) {
        stopLoading()
        _messageStringIdLiveData.value = Event(R.string.something_went_wrong)
        logException(error)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException ||
                error is NullPointerException ||
                error is ClassCastException ||
                error is FormatException ||
                error is IllegalArgumentException
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

    fun publishEventWith(eventName: String, variantId: String) {
        paymentEventManager.eventWith(eventName, variantId)
    }

    fun publishPaymentSelection(id: String, variantId: String) {
        paymentEventManager.publishPaymentSelection(id, variantId)
    }
}
