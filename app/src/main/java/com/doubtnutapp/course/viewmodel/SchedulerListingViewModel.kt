package com.doubtnutapp.course.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Log
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.course.entities.SchedulerListingEntity
import com.doubtnutapp.domain.course.repository.CourseRepository
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.plus
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 23/12/21
 */

class SchedulerListingViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val courseRepository: CourseRepository,
    private val analyticsPublisher: AnalyticsPublisher
) : BaseViewModel(compositeDisposable) {

    private val _schedulerListingData: MutableLiveData<Outcome<SchedulerListingEntity>> =
        MutableLiveData()
    val schedulerListingData: LiveData<Outcome<SchedulerListingEntity>> get() = _schedulerListingData

    fun getSchedulerListing(selectFilterIdsList: MutableSet<String>, slot: String?, page: Int) {
        startLoading()
        compositeDisposable +
                courseRepository.getSchedulerListing(
                    selectFilterIdsList,
                    slot,
                    page
                )
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(this::onSuccess, this::onError)
    }

    private fun onSuccess(data: SchedulerListingEntity) {
        _schedulerListingData.value = Outcome.success(data)
        stopLoading()
    }

    private fun onError(error: Throwable) {
        stopLoading()
        _schedulerListingData.value = if (error is HttpException) {
            val errorCode = error.response()?.code()
            when (errorCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
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

    private fun startLoading() {
        _schedulerListingData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _schedulerListingData.value = Outcome.loading(false)
    }
}