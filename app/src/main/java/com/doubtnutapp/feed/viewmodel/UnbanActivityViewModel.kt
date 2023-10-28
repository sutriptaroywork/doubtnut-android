package com.doubtnutapp.feed.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.utils.UserUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class UnbanActivityViewModel @Inject constructor(val analyticsPublisher: AnalyticsPublisher) : ViewModel() {

    private var subscribe: Disposable? = null

    private val _eventLiveData: MutableLiveData<String> = MutableLiveData()
    val eventLiveData: LiveData<String>
        get() = _eventLiveData

    fun sendUnbanRequest() {
        subscribe = DataHandler.INSTANCE.socialRepository
                .sendUnbanRequest(UserUtil.getStudentId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.error == null) {
                        _eventLiveData.value = it.meta.message
                    }
                }, {
                    _eventLiveData.value = it.message
                })
    }

    override fun onCleared() {
        super.onCleared()
        subscribe?.dispose()
    }
}