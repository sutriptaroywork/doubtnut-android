package com.doubtnutapp.resultpage.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.base.CoreViewModel
import com.doubtnut.core.data.remote.Resource
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.resultpage.model.ResultPageData
import com.doubtnutapp.resultpage.repository.ResultPageRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ResultPageVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val repository: ResultPageRepository,
    private val analyticsPublisher: IAnalyticsPublisher
) : CoreViewModel(compositeDisposable) {

    private val _pageData = MutableLiveData<Resource<ResultPageData>>()
    val pageData: LiveData<Resource<ResultPageData>> = _pageData

    fun getResultPageData(page: String?,type:String?,source:String?) {
        viewModelScope.launch {
            _pageData.postValue(Resource.Loading())
            repository.getResultPageData(page,type,source)
                .catch {
                    _pageData.postValue(Resource.Error(it.message.toString()))
                    it.printStackTrace()
                }.collect {
                    _pageData.postValue(Resource.Success(it))
                }
        }
    }

    fun trackView(state: ViewTrackingBus.State) {
        when (state.state) {
            ViewTrackingBus.VIEW_ADDED -> {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = state.trackParams[EventConstants.WIDGET_TYPE].toString() + EventConstants.VIEWED_APPEND,
                        params = hashMapOf<String, Any>(
                            EventConstants.VIEW_ID to state.trackId,
                            EventConstants.ITEM_POSITION to state.position.toString()
                        ).apply {
                            putAll(state.trackParams)
                        })
                )
            }
            ViewTrackingBus.VIEW_REMOVED -> {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = state.trackParams[EventConstants.WIDGET_TYPE].toString() + EventConstants.REMOVED_APPEND,
                        params = hashMapOf<String, Any>(
                            EventConstants.VIEW_ID to state.trackId,
                            EventConstants.ITEM_POSITION to state.position.toString()
                        ).apply {
                            putAll(state.trackParams)
                        })
                )
            }
            ViewTrackingBus.TRACK_VIEW_DURATION -> {
                if (state.time > 3000) {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            name = state.trackParams[EventConstants.WIDGET_TYPE].toString() + EventConstants.VIEWED_DURATION_APPEND,
                            params = hashMapOf<String, Any>(
                                EventConstants.VIEW_ID to state.trackId,
                                EventConstants.ITEM_POSITION to state.position.toString(),
                                EventConstants.DURATION to state.time.toString()
                            ).apply {
                                putAll(state.trackParams)
                            })
                    )
                }

            }
        }
    }

}