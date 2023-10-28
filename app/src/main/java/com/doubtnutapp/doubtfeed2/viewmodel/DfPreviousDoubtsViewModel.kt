package com.doubtnutapp.doubtfeed2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.TopicClicked
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.fromJson
import com.doubtnutapp.data.remote.models.doubtfeed2.Topic
import com.doubtnutapp.data.remote.repository.DoubtFeedRepository2
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.widgetmanager.widgets.DoubtFeedWidget
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Created by devansh on 17/07/21.
 */

class DfPreviousDoubtsViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val doubtFeedRepository: DoubtFeedRepository2,
    private val gson: Gson,
) : BaseViewModel(compositeDisposable) {

    //region live data
    private val _previousDoubtFeedLiveData = MutableLiveData<Outcome<DoubtFeedWidget.Data>>()
    val previousDoubtFeedLiveData: LiveData<Outcome<DoubtFeedWidget.Data>>
        get() = _previousDoubtFeedLiveData
    //endregion

    var topicsList: List<Topic> = emptyList()
        private set

    var lastTopicPosition: Int = 0

    var currentTopic: Topic? = null
        private set

    fun initTopicsData(topics: Array<Topic>) {
        topicsList = topics.toList()
        lastTopicPosition = topicsList.indexOfFirst { it.isSelected }.coerceAtLeast(0)
        currentTopic = topicsList[lastTopicPosition]
    }

    // Send null on first api call to get topics list
    fun getPreviousDoubtFeed(topicId: String? = null, cached: Boolean = true) {
        _previousDoubtFeedLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            doubtFeedRepository.getPreviousDoubtFeed(topicId)
                .catch { e ->
                    _previousDoubtFeedLiveData.value = Outcome.loading(false)
                    _previousDoubtFeedLiveData.value = Outcome.failure(e)
                }
                .collect {
                    val currentTopicId = topicId ?: topicsList.getOrNull(lastTopicPosition)?.key
                    doubtFeedRepository.cachePreviousDoubtFeedApiResponse(currentTopicId, it)

                    _previousDoubtFeedLiveData.value = Outcome.loading(false)
                    _previousDoubtFeedLiveData.value = Outcome.success(it.data)
                }
        }
    }

    fun getPreviousDoubtFeedForTopic(action: TopicClicked) {
        topicsList[lastTopicPosition].isSelected = false
        topicsList[action.position].isSelected = true
        currentTopic = topicsList[action.position]
        getPreviousDoubtFeed(action.key)
    }

    fun submitDoubtCompletion(goalId: Int) {
        viewModelScope.launch {
            doubtFeedRepository.submitDoubtCompletionForPrevious(goalId)
                .catch { }
                .collect()
        }
    }

    fun getCarouselsFromJson(carouselsJson: String): List<WidgetEntityModel<*, *>> {
        return gson.fromJson(carouselsJson)
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }
}
