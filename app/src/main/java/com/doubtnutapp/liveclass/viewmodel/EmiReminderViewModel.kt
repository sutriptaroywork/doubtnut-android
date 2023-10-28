package com.doubtnutapp.liveclass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.EMIDialogData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class EmiReminderViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _emiLiveData: MutableLiveData<Outcome<EMIDialogData>> = MutableLiveData()

    val emiLiveData: LiveData<Outcome<EMIDialogData>>
        get() = _emiLiveData

    var extraParams: HashMap<String, Any> = hashMapOf()

    fun getEmiReminderData(assortmentId: Int) {
        _emiLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getEmiReminderData(assortmentId)
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data
                        }
                        .subscribeToSingle({
                            _emiLiveData.value = Outcome.success(it)
                            _emiLiveData.value = Outcome.loading(false)
                        }, {
                            _emiLiveData.value = Outcome.loading(false)
                        })
    }

}