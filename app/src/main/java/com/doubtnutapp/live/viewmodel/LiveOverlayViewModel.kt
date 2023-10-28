package com.doubtnutapp.live.viewmodel

import androidx.lifecycle.MutableLiveData
import com.doubtnut.core.data.remote.ResponseMeta
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.authToken
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.Comment
import com.google.gson.internal.LinkedTreeMap
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LiveOverlayViewModel @Inject constructor(
    val analyticsPublisher: AnalyticsPublisher,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    val liveViewerCountLiveData: MutableLiveData<Int> = MutableLiveData()
    val latestCommentsLiveData: MutableLiveData<List<Comment>> = MutableLiveData()

    fun getLiveViewerCount(postId: String) {
        compositeDisposable.add(Observable.interval(0, 5, TimeUnit.SECONDS)
            .debounce(300, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .switchMap {
                DataHandler.INSTANCE.teslaRepository.getLiveViewerCount(postId)
                    .onErrorResumeNext(
                        Observable.just(
                            ApiResponse(
                                ResponseMeta(0, "", null),
                                Any(),
                                null
                            )
                        )
                    )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.data != null && it.data is LinkedTreeMap<*, *>)
                    liveViewerCountLiveData.postValue((it.data["count"] as Double).toInt())
                else liveViewerCountLiveData.postValue(0)
            }, {
                liveViewerCountLiveData.postValue(0)
            })
        )
    }

    fun getLiveStreamComments(postId: String) {
        compositeDisposable.add(Observable.interval(0, 5, TimeUnit.SECONDS)
            .debounce(300, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .switchMap {
                DataHandler.INSTANCE.commentRepository.getCommentsObservable(
                    authToken(DoubtnutApp.INSTANCE),
                    "new_feed_type", postId, "1"
                )
                    .onErrorResumeNext(
                        Observable.just(
                            ApiResponse(
                                ResponseMeta(0, "", null),
                                arrayListOf<Comment>(),
                                null
                            )
                        )
                    )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.data != null)
                    latestCommentsLiveData.postValue(it.data.take(4))
                else latestCommentsLiveData.postValue(arrayListOf())
            }, {
                latestCommentsLiveData.postValue(arrayListOf())
            })
        )
    }

    fun addComment(postId: String, message: String): RetrofitLiveData<ApiResponse<Comment>> {
        return DataHandler.INSTANCE.commentRepository.addComment(
            authToken(DoubtnutApp.INSTANCE),
            "new_feed_type", postId, "", message,
            null, null, null, null, null
        )
    }

    fun onPause() {
        compositeDisposable.clear()
    }
}