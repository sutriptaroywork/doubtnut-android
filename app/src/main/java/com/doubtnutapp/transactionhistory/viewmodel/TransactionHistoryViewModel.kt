package com.doubtnutapp.transactionhistory.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.domain.payment.entities.TransactionHistoryItem
import com.doubtnutapp.domain.payment.interactor.GetTransactionHistoryUseCase
import com.doubtnutapp.domain.payment.interactor.TransactionHistoryParams
import com.doubtnutapp.plus
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-13.
 */
class TransactionHistoryViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val getTransactionHistoryUseCase: GetTransactionHistoryUseCase,
) : BaseViewModel(compositeDisposable) {

    private val _messageStringIdLiveData: MutableLiveData<Event<Int>> = MutableLiveData()

    val messageStringIdLiveData: LiveData<Event<Int>>
        get() = _messageStringIdLiveData

    private val _transactionHistoryV2List: MutableLiveData<Event<List<TransactionHistoryItem>>> =
        MutableLiveData()

    val transactionHistoryV2List: LiveData<Event<List<TransactionHistoryItem>>>
        get() = _transactionHistoryV2List

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    fun startLoading() {
        isLoading.postValue(true)
    }

    fun stopLoading() {
        isLoading.postValue(false)
    }

    fun getTransactionHistoryV2(status: String, page: Int) {
        startLoading()
        compositeDisposable + getTransactionHistoryUseCase
            .execute(TransactionHistoryParams(status, page))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onSuccessV2, this::onError)
    }

    private fun onSuccessV2(list: List<TransactionHistoryItem>) {
        stopLoading()
        _transactionHistoryV2List.postValue(Event(list))
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

}