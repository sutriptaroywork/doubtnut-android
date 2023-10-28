package com.doubtnutapp.doubtfeed.viewmodel

import android.content.Context
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
import com.doubtnutapp.base.UpdateTopicBoosterWidgetQuestion
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.doubtfeed.*
import com.doubtnutapp.data.remote.repository.DoubtFeedRepository
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.similarVideo.interactor.SubmitSimilarTopicBoosterQuestion
import com.doubtnutapp.widgetmanager.widgets.DoubtFeedDailyGoalWidget
import com.doubtnutapp.widgetmanager.widgets.TopicBoosterWidget
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by devansh on 7/5/21.
 */

class DoubtFeedViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val doubtFeedRepository: DoubtFeedRepository,
    private val submitSimilarTopicBoosterQuestion: SubmitSimilarTopicBoosterQuestion,
    private val userPreference: UserPreference,
    val deeplinkAction: DeeplinkAction,
) : BaseViewModel(compositeDisposable) {

    //region live data
    private val _doubtFeedLiveData = MutableLiveData<Outcome<DoubtFeed>>()
    val doubtFeedLiveData: LiveData<Outcome<DoubtFeed>>
        get() = _doubtFeedLiveData

    private val _doubtFeedProgressLiveData = MutableLiveData<DoubtFeedProgress>()
    val doubtFeedProgressLiveData: LiveData<DoubtFeedProgress>
        get() = _doubtFeedProgressLiveData
    //endregion

    val userImageUrl: String
        get() = userPreference.getUserImageUrl()

    var topicsList: List<Topic> = emptyList()
        private set

    var bottomSheetData: List<WidgetEntityModel<*, *>> = emptyList()
        private set

    var lastTopicPosition: Int = 0

    var taskCompletedPopupData: DoubtFeedDailyGoalTaskCompletedPopupData? = null
        private set

    var backPressPopupData: DoubtFeedBackPressPopupData? = null

    var currentTopic: Topic? = null
        private set

    var isPrevious: Boolean = false
        private set

    var dailyGoals: List<DoubtFeedDailyGoalWidget.Model> = emptyList()
        private set

    var isNewDailyGoalViewed: Boolean = false

    // Send null on first api call to get topics list
    fun getDoubtFeed(topicId: String? = null) {
        _doubtFeedLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            doubtFeedRepository.getDoubtFeed(topicId)
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
                    bottomSheetData = it.data.bottomSheetData
                    backPressPopupData = it.data.backPressPopupData
                    isPrevious = it.data.isPrevious
                    dailyGoals =
                        it.data.carousels.filterIsInstance<DoubtFeedDailyGoalWidget.Model>()

                    val currentTopicId = topicId ?: topicsList[lastTopicPosition].key
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

    fun getCurrentDoubtFeedProgress() {
        val topicId = currentTopic?.key ?: return
        viewModelScope.launch {
            doubtFeedRepository.getDoubtFeedProgress(topicId)
                .catch { }
                .collect {
                    _doubtFeedProgressLiveData.value = it.data
                }
        }
    }

    fun submitDoubtCompletion(goalId: Int, newSubtitle: String) {
        viewModelScope.launch {
            doubtFeedRepository.submitDoubtCompletion(goalId)
                .catch { }
                .collect {
                    taskCompletedPopupData = it.data

                    val currentDoubtFeed =
                        (doubtFeedLiveData.value as? Outcome.Success)?.data ?: return@collect
                    currentDoubtFeed.carousels
                        .filterIsInstance<DoubtFeedDailyGoalWidget.Model>()
                        .map { it.data }
                        .find { it.goalId == goalId }
                        ?.apply {
                            isDone = true
                            subtitle = newSubtitle
                        }
                    if (it.data.isTopicDone) {
                        it.data.newHeading?.let {
                            currentDoubtFeed.heading = it
                        }
                        sendEvent(EventConstants.DF_ALL_GOALS_COMPLETED_FOR_TOPIC_WITHIN_WINDOW)
                    }

                    currentTopic?.key?.let {
                        getDoubtFeed(it)
                    }
                }
        }
    }

    fun getNextIncompleteDailyGoal(): DoubtFeedDailyGoalWidget.Model? {
        return dailyGoals.firstOrNull { it.data.isDone.not() }
    }

    fun performAction(action: Any) {
        when (action) {
            is UpdateTopicBoosterWidgetQuestion -> {
                handleUpdateTopicBoosterWidgetQuestionAction(action)
            }
        }
    }

    fun performDeeplinkAction(context: Context, deeplink: String?) {
        deeplinkAction.performAction(context, deeplink)
    }

    private fun handleUpdateTopicBoosterWidgetQuestionAction(action: UpdateTopicBoosterWidgetQuestion) {
        submitTopicBoosterAnswer(action.data)
    }

    private fun submitTopicBoosterAnswer(data: TopicBoosterWidget.Data) {
        compositeDisposable.add(
            submitSimilarTopicBoosterQuestion.execute(
                SubmitSimilarTopicBoosterQuestion.Param(
                    data.submittedOption.toString(),
                    data.questionId,
                    data.submitUrlEndpoint,
                    data.widgetType
                )
            ).applyIoToMainSchedulerOnCompletable().subscribeToCompletable({})
        )
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }
}
