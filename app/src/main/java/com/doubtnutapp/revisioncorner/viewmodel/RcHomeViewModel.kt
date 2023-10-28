package com.doubtnutapp.revisioncorner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.revisioncorner.RevisionCornerHomeData
import com.doubtnutapp.data.remote.models.revisioncorner.RulesInfo
import com.doubtnutapp.data.remote.repository.RevisionCornerRepository
import com.doubtnutapp.utils.Event
import com.doubtnutapp.widgetmanager.WidgetTypes
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Created by devansh on 10/08/21.
 */

class RcHomeViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val revisionCornerRepository: RevisionCornerRepository,
) : BaseViewModel(compositeDisposable) {

    //region LiveData
    private val _homeLiveData = MutableLiveData<Outcome<RevisionCornerHomeData>>()
    val homeLiveData: LiveData<Outcome<RevisionCornerHomeData>>
        get() = _homeLiveData

    private val _rulesLiveData = MutableLiveData<Event<RulesInfo?>>()
    val rulesLiveData: LiveData<Event<RulesInfo?>>
        get() = _rulesLiveData
    //endregion

    private var rulesMap: Map<String, RulesInfo> = emptyMap()

    fun getRevisionCornerHomeData() {
        _homeLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            revisionCornerRepository.getRevisionCornerHomeData()
                .catch { e ->
                    _homeLiveData.value = Outcome.loading(false)
                    _homeLiveData.value = Outcome.failure(e)
                }
                .collect {
                    rulesMap = it.data.rules.orEmpty()
                    _homeLiveData.value = Outcome.loading(false)
                    _homeLiveData.value = Outcome.success(it.data)
                }
        }
    }

    fun handleRxBusEvent(event: Any) {
        when (event) {
            is WidgetClickedEvent -> {
                val extraParams = event.extraParams ?: return

                when (val clickSource = event.extraParams[Constants.WIDGET_CLICK_SOURCE]) {
                    Constants.WIDGET_PARENT_TOP_ICON_2 -> {
                        event.extraParams[Constants.WIDGET_ID]?.let { widgetId ->
                            _rulesLiveData.value = Event(rulesMap[widgetId])
                        }
                        sendEvent(EventConstants.RC_INFO_CLICK, event.extraParams, ignoreSnowplow = true)
                    }

                    Constants.WIDGET_TOP_100_QUESTIONS -> {
                        sendEvent(EventConstants.RC_TOP_100_QUESTION_CLICK, ignoreSnowplow = true)
                    }

                    Constants.WIDGET_REVISION_CORNER_BANNER -> {
                        sendEvent(EventConstants.RC_BANNER_WIDGET_CLICK, event.extraParams, ignoreSnowplow = true)
                    }

                    Constants.WIDGET_FORMULA_SHEET -> {
                        sendEvent(EventConstants.RC_FORMULA_CLICK, ignoreSnowplow = true)
                    }

                    WidgetTypes.TYPE_ICON_CTA -> {
                        sendEvent(EventConstants.RC_SHORT_TEST_SUBJECT_CLICK, event.extraParams, ignoreSnowplow = true)
                    }
                }
            }
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}