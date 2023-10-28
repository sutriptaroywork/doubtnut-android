package com.doubtnutapp.liveclass.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.payment.ApbCashPaymentData
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ApbCashPaymentViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _apbCashPaymentLiveData: MutableLiveData<Outcome<ApbCashPaymentData>> = MutableLiveData()

    val apbCashPaymentLiveData: LiveData<Outcome<ApbCashPaymentData>>
        get() = _apbCashPaymentLiveData

    var extraParams: HashMap<String, Any> = hashMapOf()

    fun getApbCashPaymentData() {
        _apbCashPaymentLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.apbRepository.getApbCashPaymentData()
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data
                        }
                        .subscribeToSingle({
                            _apbCashPaymentLiveData.value = Outcome.success(it)
                            _apbCashPaymentLiveData.value = Outcome.loading(false)
                        }, {
                            _apbCashPaymentLiveData.value = Outcome.loading(false)
                        })
    }
}