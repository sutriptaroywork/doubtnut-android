package com.doubtnutapp.libraryhome.course.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.MultiSelectFilterWidgetModel
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.course.widgets.SaleWidgetModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.ActivateTrialData
import com.doubtnutapp.data.remote.models.ApiCourseData
import com.doubtnutapp.data.remote.models.NudgeData
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.liveclass.ui.dialog.CouponDialogData
import com.doubtnutapp.plus
import com.doubtnutapp.widgets.MultiSelectFilterWidgetV2Model
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class CoursesViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher
) : BaseViewModel(compositeDisposable) {

    var extraParams: HashMap<String, Any> = hashMapOf()

    var itemsAdded = 0

    private val _widgetsLiveData: MutableLiveData<Outcome<List<WidgetEntityModel<*, *>>>> =
        MutableLiveData()

    val widgetsLiveData: LiveData<Outcome<List<WidgetEntityModel<*, *>>>>
        get() = _widgetsLiveData

    private val _exploreLiveData: MutableLiveData<Outcome<ApiCourseData>> = MutableLiveData()
    val exploreLiveData: LiveData<Outcome<ApiCourseData>>
        get() = _exploreLiveData

    private val _activateVipLiveData: MutableLiveData<Outcome<ActivateTrialData>> =
        MutableLiveData()
    val activateVipLiveData: LiveData<Outcome<ActivateTrialData>>
        get() = _activateVipLiveData

    private val _nudgesLiveData: MutableLiveData<Outcome<NudgeData>> = MutableLiveData()

    val nudgesLiveData: LiveData<Outcome<NudgeData>>
        get() = _nudgesLiveData

    val couponLiveData: LiveData<Outcome<CouponDialogData>>
        get() = _couponLiveData

    val _couponLiveData: MutableLiveData<Outcome<CouponDialogData>> = MutableLiveData()

    private fun startLoading() {
        _widgetsLiveData.value = Outcome.loading(true)
    }

    private fun startExploreLoading() {
        _exploreLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _widgetsLiveData.value = Outcome.loading(false)
    }

    private fun stopExploreLoading() {
        _exploreLiveData.value = Outcome.loading(false)
    }

    fun fetchCourseData(pageNumber: Int, categoryId: String?, filtersList: List<String>? = null) {
        startExploreLoading()
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getLibraryCourse(
                    pageNumber,
                    categoryId,
                    filtersList
                )
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data.widgets.mapIndexed { index, widget ->
                            if (widget != null) {
                                if (widget.extraParams == null) {
                                    widget.extraParams = hashMapOf()
                                }
                                if (widget is MultiSelectFilterWidgetModel || widget is MultiSelectFilterWidgetV2Model) {
                                    extraParams[EventConstants.CATEGORY] = categoryId.orEmpty()
                                }
                                widget.extraParams?.putAll(extraParams)
                                widget.extraParams?.put(
                                    EventConstants.ITEM_PARENT_POSITION,
                                    itemsAdded + index
                                )
                            }
                            if (widget is SaleWidgetModel) {
                                widget.data.items?.forEach { _saleItem ->
                                    _saleItem.responseAtTimeInMillis = System.currentTimeMillis()
                                }
                            }
                        }
                        it.data
                    }
                    .subscribeToSingle({
                        if (!it.widgets.isNullOrEmpty()) {
                            itemsAdded.plus(it.widgets.size)
                        }
                        _exploreLiveData.value = Outcome.success(it)
                        stopExploreLoading()
                    }, this::onExploreError)
    }

    fun getVmcDetail() {
        startLoading()
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getVmcDetail()
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _widgetsLiveData.value = Outcome.success(it.widgets)
                        stopLoading()
                    }, this::onError)
    }

    fun publishEvent(event: AnalyticsEvent, sendToBranch: Boolean) {
        val paramsCopy: HashMap<String, Any> = HashMap(event.params)
        analyticsPublisher.publishEvent(event)
        if (sendToBranch)
            analyticsPublisher.publishBranchIoEvent(AnalyticsEvent(event.name, paramsCopy))
    }

    private val _recordedLiveData: MutableLiveData<Outcome<Pair<List<WidgetEntityModel<*, *>>,
            List<WidgetEntityModel<*, *>>>>> = MutableLiveData()

    val recordedLiveData: LiveData<Outcome<Pair<List<WidgetEntityModel<*, *>>,
            List<WidgetEntityModel<*, *>>>>>
        get() = _recordedLiveData

    fun fetchRecordedCourseData(subject: String?) {
        startLoading()
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getRecordedCourseData(subject)
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _recordedLiveData.value = Outcome.success(it.widgetsTop to it.widgets)
                        stopLoading()
                    }, this::onError)
    }

    fun getNudgeData(
        nudgeId: String?,
        page: String?,
        type: String?
    ) {
        startLoading()
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getNudgeDetails(nudgeId, page, type)
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _nudgesLiveData.value = Outcome.success(it)
                        stopLoading()
                    }, this::onError)
    }

    private fun onError(error: Throwable) {
        stopLoading()
        _widgetsLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(
                    error.message
                        ?: ""
                )
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    private fun onExploreError(error: Throwable) {
        stopLoading()
        _exploreLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(
                    error.message
                        ?: ""
                )
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    fun activateTrial(assortmentId: String?) {
        _activateVipLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.activateTrial(assortmentId.orEmpty())
                    .applyIoToMainSchedulerOnSingle()

                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _activateVipLiveData.value = Outcome.success(it)
                        _activateVipLiveData.value = Outcome.loading(false)
                    }, {
                        _activateVipLiveData.value = Outcome.loading(false)
                    })
    }

    fun getCouponData(hashMap: HashMap<String, Any>) {
        _couponLiveData.value = Outcome.loading(true)
        compositeDisposable + DataHandler.INSTANCE.courseRepository.getCouponData(hashMap)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    _couponLiveData.value = Outcome.loading(false)
                    _couponLiveData.value = Outcome.success(it)
                }, { _couponLiveData.value = Outcome.loading(false) })
    }


    fun trackView(state: ViewTrackingBus.State) {
        when (state.state) {
            ViewTrackingBus.VIEW_ADDED -> {
                analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                                state.trackParams[EventConstants.WIDGET_TYPE].toString() + EventConstants.VIEWED_APPEND,
                                hashMapOf<String, Any>(EventConstants.VIEW_ID to state.trackId,
                                        EventConstants.ITEM_POSITION to state.position.toString()).apply {
                                    putAll(state.trackParams)
                                }))
            }
            ViewTrackingBus.VIEW_REMOVED -> {
                analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                                state.trackParams[EventConstants.WIDGET_TYPE].toString() + EventConstants.REMOVED_APPEND,
                                hashMapOf<String, Any>(EventConstants.VIEW_ID to state.trackId,
                                        EventConstants.ITEM_POSITION to state.position.toString()).apply {
                                    putAll(state.trackParams)
                                }))
            }
            ViewTrackingBus.TRACK_VIEW_DURATION -> {
                analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                                state.trackParams[EventConstants.WIDGET_TYPE].toString() + EventConstants.VIEWED_DURATION_APPEND,
                                hashMapOf<String, Any>(EventConstants.VIEW_ID to state.trackId,
                                        EventConstants.ITEM_POSITION to state.position.toString(),
                                        EventConstants.DURATION to state.position.toString()).apply {
                                    putAll(state.trackParams)
                                }))
            }
        }
    }

}