package com.doubtnutapp.doubtpecharcha.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.doubtpecharcha.model.P2pRoomData
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import javax.inject.Inject

class DoubtP2pHelperEntryViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
) : BaseViewModel(compositeDisposable) {

    private val _p2pRoomData = MutableLiveData<P2pRoomData>()
    val p2pRoomId: LiveData<P2pRoomData>
        get() = _p2pRoomData

    fun getHelperData(roomId: String) {
        viewModelScope.launch {
            DataHandler.INSTANCE.doubtPeCharchaRepository
                .getHelperData(roomId)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        _p2pRoomData.postValue(it)
                    },
                    {
                        it.printStackTrace()
                    }
                )
        }
    }

    fun sendEvent(
        event: String,
        params: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false
    ) {
        analyticsPublisher.publishEvent(AnalyticsEvent(event, params, ignoreSnowplow))
    }
}
