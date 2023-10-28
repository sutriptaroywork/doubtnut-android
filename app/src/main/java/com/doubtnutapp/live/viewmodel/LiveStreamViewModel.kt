package com.doubtnutapp.live.viewmodel

import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.ViewState
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LiveStreamViewModel @Inject constructor(
        val analyticsPublisher: AnalyticsPublisher,
        compositeDisposable: CompositeDisposable) : BaseViewModel(compositeDisposable) {

    private val teslaRepository = DataHandler.INSTANCE.teslaRepository

    val stateLiveData: MutableLiveData<ViewState> = MutableLiveData(ViewState.none())
    val liveStreamUrlLiveData: MutableLiveData<String> = MutableLiveData()
    val liveStreamEndLiveData: MutableLiveData<Boolean> = MutableLiveData()

    fun getStreamUrl(postId: String) {
        stateLiveData.postValue(ViewState.loading("Connecting.."))
        compositeDisposable + teslaRepository.getLiveStreamUrl(postId).subscribeOn(Schedulers.io()).subscribe({
            if (it.error == null && it.meta.code == 200 && it.data != null) {
                stateLiveData.postValue(ViewState.success())
                liveStreamUrlLiveData.postValue(it.data)
            }
        }, {
            stateLiveData.postValue(ViewState.error("Error connecting to live session"))
        })
    }

    fun endStream(postId: String) {
        stateLiveData.postValue(ViewState.loading("Ending live stream..."))
        compositeDisposable.add(teslaRepository.endLiveStream(postId)
                .subscribeOn(Schedulers.io()).subscribe({
                    if (it.error == null && it.meta.code == 200) {
                        stateLiveData.postValue(ViewState.success())
                        liveStreamEndLiveData.postValue(true)
                    }
                }, {
                    stateLiveData.postValue(ViewState.error("Unable to end live stream, please try again"))
                }))
    }

    fun viewerJoined(postId: String) {
        teslaRepository.liveViewerJoined(postId).subscribeOn(Schedulers.io()).subscribe()
    }

    fun viewerLeft(postId: String) {
        teslaRepository.liveViewerLeft(postId).subscribeOn(Schedulers.io()).subscribe()
    }
}