package com.doubtnutapp.dnr.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.dnr.model.DnrMetaData
import com.doubtnutapp.dnr.model.DnrWidgetListData
import com.doubtnutapp.dnr.ui.fragment.DnrWidgetListFragment
import com.doubtnutapp.plus
import com.doubtnutapp.studygroup.service.DnrRepository
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import javax.inject.Inject

class DnrWidgetListViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val dnrRepository: DnrRepository,
    private val analyticsPublisher: AnalyticsPublisher
) : BaseViewModel(compositeDisposable) {

    private val _widgetListLiveData: MutableLiveData<Event<Outcome<DnrWidgetListData>>> = MutableLiveData()
    val widgetListLiveData: LiveData<Event<Outcome<DnrWidgetListData>>>
        get() = _widgetListLiveData

    private val _metaDataLiveData: MutableLiveData<DnrMetaData> = MutableLiveData()
    val metaDataLiveData: LiveData<DnrMetaData>
        get() = _metaDataLiveData

    fun getRequiredWidgetData(page: String, source: String) {
        _widgetListLiveData.value = Event(Outcome.loading(true))
        compositeDisposable +
            dnrRepository.getWidgetList(page, source)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        val data = it.data
                        if (page == DnrWidgetListFragment.INITIAL_PAGE) {
                            _metaDataLiveData.postValue(
                                DnrMetaData(
                                    toolbar = data.toolbarData
                                )
                            )
                        }
                        _widgetListLiveData.value = Event(Outcome.success(data))
                        _widgetListLiveData.value = Event(Outcome.loading(false))
                    },
                    {
                        _widgetListLiveData.value = Event(Outcome.loading(false))
                        _widgetListLiveData.value = Event(Outcome.apiError(it))
                        it.printStackTrace()
                    }
                )
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}
