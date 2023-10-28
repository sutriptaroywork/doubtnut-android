package com.doubtnutapp.payment

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ApbLocationViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _apbLocationLiveData: MutableLiveData<Outcome<ApbLocationData>> = MutableLiveData()

    val apbLocationLiveData: LiveData<Outcome<ApbLocationData>>
        get() = _apbLocationLiveData

    var extraParams: HashMap<String, Any> = hashMapOf()

    fun getApbLocationData(lat: String?, long: String?) {
        _apbLocationLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.apbRepository.getApbLocationData(lat.orEmpty(), long.orEmpty())
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data
                        }
                        .subscribeToSingle({
                            _apbLocationLiveData.value = Outcome.success(it)
                            _apbLocationLiveData.value = Outcome.loading(false)
                        }, {
                            _apbLocationLiveData.value = Outcome.loading(false)
                        })
    }
}