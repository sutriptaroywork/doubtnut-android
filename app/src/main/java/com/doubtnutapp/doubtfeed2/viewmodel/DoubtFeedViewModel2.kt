package com.doubtnutapp.doubtfeed2.viewmodel

import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.Constants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.TopicClicked
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.doubtfeed2.DfPopupData
import com.doubtnutapp.data.remote.models.doubtfeed2.DoubtFeed
import com.doubtnutapp.data.remote.models.doubtfeed2.Topic
import com.doubtnutapp.data.remote.repository.DoubtFeedRepository2
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.utils.Event
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.widgets.DoubtFeedWidget
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by devansh on 14/7/21.
 */

class DoubtFeedViewModel2 @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val doubtFeedRepository: DoubtFeedRepository2,
    private val gson: Gson,
) : BaseViewModel(compositeDisposable) {

    //region live data
    private val _doubtFeedLiveData = MutableLiveData<Outcome<DoubtFeed>>()
    val doubtFeedLiveData: LiveData<Outcome<DoubtFeed>>
        get() = _doubtFeedLiveData

    private val _dailyGoalCompletedPopupLiveData = MutableLiveData<Event<DfPopupData>>()
    val dailyGoalCompletedPopupLiveData: LiveData<Event<DfPopupData>>
        get() = _dailyGoalCompletedPopupLiveData

    private val _previousDoubtFeedLiveData = MutableLiveData<DoubtFeedWidget.Data>()
    val previousDoubtFeedLiveData: LiveData<DoubtFeedWidget.Data>
        get() = _previousDoubtFeedLiveData

    private val _updateRewardDetailsLiveData = MutableLiveData<Event<Boolean>>()
    val updateRewardDetailsLiveData: LiveData<Event<Boolean>>
        get() = _updateRewardDetailsLiveData
    //endregion

    var topicsList: List<Topic> = emptyList()
        private set

    var lastTopicPosition: Int = 0

    var currentTopic: Topic? = null
        private set

    private var fetchPreviousDoubts = true

    private var benefitsPopupShownAuto = false

    // Send null on first api call to get topics list
    fun getDoubtFeed(topicId: String? = null, cached: Boolean = true) {
        _doubtFeedLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            doubtFeedRepository.getDoubtFeed(topicId, cached)
                .catch { e ->
                    _doubtFeedLiveData.value = Outcome.loading(false)
                    _doubtFeedLiveData.value = Outcome.failure(e)
                }
                .collect {
                    if (it.data.topics != null) {
                        topicsList = it.data.topics!!
                        lastTopicPosition =
                            topicsList.indexOfFirst { it.isSelected }.coerceAtLeast(0)
                        currentTopic = topicsList[lastTopicPosition]

                        sendEvent(
                            EventConstants.DF_NUMBER_OF_TOPICS_FOR_DAILY_GOALS,
                            hashMapOf(
                                Constants.TOPICS to topicsList.joinToString { it.title },
                                Constants.TOPICS_COUNT to topicsList.size
                            )
                        )
                    }
                    val currentTopicId = topicId ?: topicsList.getOrNull(lastTopicPosition)?.key
                    doubtFeedRepository.cacheDoubtFeedApiResponse(currentTopicId, it)

                    _doubtFeedLiveData.value = Outcome.loading(false)
                    _doubtFeedLiveData.value = Outcome.success(it.data)
                }
        }
    }

    fun getDoubtFeedForTopic(action: TopicClicked) {
        topicsList[lastTopicPosition].isSelected = false
        topicsList[action.position].isSelected = true
        currentTopic = topicsList[action.position]
        getDoubtFeed(action.key)
    }

    fun submitDoubtCompletion(goalId: Int) {
        viewModelScope.launch {
            doubtFeedRepository.submitDoubtCompletion(goalId)
                .catch { }
                .collect {
                    if (it.data.showPopup && it.data.popupData != null) {
                        _dailyGoalCompletedPopupLiveData.value = Event(it.data.popupData!!)
                        markStreak()
                        getPreviousDoubtFeed(forcedFetch = true)
                    }
                    currentTopic?.key?.let { topicKey ->
                        getDoubtFeed(topicId = topicKey, cached = false)
                    }
                }
        }
    }

    private fun markStreak() {
        viewModelScope.launch {
            doubtFeedRepository.markStreak()
                .catch { }
                .collect {
                    _updateRewardDetailsLiveData.value = Event(true)
                }
        }
    }

    fun getPreviousDoubtFeed(forcedFetch: Boolean = false) {
        if (fetchPreviousDoubts || forcedFetch) {
            viewModelScope.launch {
                doubtFeedRepository.getPreviousDoubtFeed(null)
                    .catch { }
                    .collect {
                        fetchPreviousDoubts = false
                        val currentTopicId = topicsList.getOrNull(lastTopicPosition)?.key
                        doubtFeedRepository.cachePreviousDoubtFeedApiResponse(currentTopicId, it)
                        _previousDoubtFeedLiveData.value = it.data
                    }
            }
        }
    }

    fun getDoubtFeedWidgetEntityModel(data: DoubtFeedWidget.Data): DoubtFeedWidget.Model {
        return DoubtFeedWidget.Model().apply {
            _widgetType = WidgetTypes.TYPE_DOUBT_FEED
            _widgetData = data
        }
    }

    fun getCarouselsJson(carousels: List<WidgetEntityModel<*, *>>?): String = gson.toJson(carousels)

    fun showBenefitsPopup(): Boolean = getBenefitsPopupShowCount() > 0 && benefitsPopupShownAuto.not()

    fun setBenefitsPopupShown() {
        benefitsPopupShownAuto = true
        defaultPrefs().edit {
            putInt(Constants.BENEFITS_POPUP_SHOW_COUNT, getBenefitsPopupShowCount() - 1)
        }
    }

    private fun getBenefitsPopupShowCount(): Int =
        defaultPrefs().getInt(
            Constants.BENEFITS_POPUP_SHOW_COUNT, Constants.MAX_BENEFITS_POPUP_SHOW_COUNT
        )

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}
