package com.doubtnutapp.liveclass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.core.data.remote.ResponseMeta
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.VideoStatus
import com.doubtnutapp.plus
import com.doubtnutapp.utils.Event
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VideoStatusViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _videoStatus: MutableLiveData<Event<VideoStatus>> = MutableLiveData()

    val videoStatus: LiveData<Event<VideoStatus>>
        get() = _videoStatus

    private val _loading: MutableLiveData<Event<Boolean>> = MutableLiveData()

    val loading: LiveData<Event<Boolean>>
        get() = _loading

    private val _messageLiveData: MutableLiveData<Event<Int>> = MutableLiveData()

    val messageLiveData: LiveData<Event<Int>>
        get() = _messageLiveData

    var isLiveClassStatsPollingStarted = false

    fun getLiveClassStats(id: String) {
        isLiveClassStatsPollingStarted = true
        compositeDisposable +
                Observable.interval(0, 5, TimeUnit.SECONDS)
                    .switchMap {
                        DataHandler.INSTANCE.courseRepository.getVideoStatus(id)
                            .toObservable()
                            .onErrorResumeNext(
                                Observable.just(
                                    ApiResponse(
                                        ResponseMeta(
                                            200,
                                            "APP_HANDLED", "true"
                                        ),
                                        VideoStatus(
                                            state = "",
                                            timeRemaining = "",
                                            text = "",
                                            thumbnail = ""
                                        ), null
                                    )
                                )
                            )
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (it.meta.message != "APP_HANDLED") {
                            _videoStatus.value = Event(it.data)
                        }
                    }
    }

    fun fetchInitialData(id: String) {
        startLoading()
        compositeDisposable + DataHandler.INSTANCE
            .courseRepository
            .getVideoStatus(id)
            .applyIoToMainSchedulerOnSingle()
            .map {
                it.data
            }.subscribeToSingle({
                stopLoading()
                _videoStatus.value = Event(it)
            }, {
                stopLoading()
                _messageLiveData.value = Event(R.string.something_went_wrong)
            })
    }

    private fun startLoading() {
        _loading.value = Event(true)
    }

    private fun stopLoading() {
        _loading.value = Event(false)
    }

}