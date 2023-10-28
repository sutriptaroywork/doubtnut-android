package com.doubtnutapp.topicboostergame2.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.PlayVideo
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.TbgResult
import com.doubtnutapp.data.remote.repository.TopicBoosterGameRepository2
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.base.SolutionResourceType
import com.doubtnutapp.domain.resourcelisting.entities.ResourceListingEntity
import com.doubtnutapp.domain.resourcelisting.interactor.GetResourceListingUseCase
import com.doubtnutapp.resourcelisting.mapper.ResourceListingMapper
import com.doubtnutapp.resourcelisting.model.QuestionMetaDataModel
import com.doubtnutapp.resourcelisting.model.ResourceListingData
import com.doubtnutapp.screennavigator.Screen
import com.doubtnutapp.screennavigator.TextSolutionScreen
import com.doubtnutapp.screennavigator.VideoScreen
import com.doubtnutapp.topicboostergame2.ui.TbgResultFragmentArgs
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by devansh on 30/06/21.
 */

class TbgResultViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val topicBoosterGameRepository: TopicBoosterGameRepository2,
    private val analyticsPublisher: AnalyticsPublisher,
    private val getResourceListingUseCase: GetResourceListingUseCase,
    private val resourceListingMapper: ResourceListingMapper,
) : BaseViewModel(compositeDisposable) {

    //region LiveData
    private val _resultLiveData = MutableLiveData<Outcome<TbgResult>>()
    val resultLiveData: LiveData<Outcome<TbgResult>>
        get() = _resultLiveData

    private val _quizSolutionsLiveData: MutableLiveData<Outcome<ResourceListingData>> =
        MutableLiveData()
    val quizSolutionsLiveData: LiveData<Outcome<ResourceListingData>>
        get() = _quizSolutionsLiveData

    private val _navigateScreenLiveData: MutableLiveData<Event<Pair<Screen, Map<String, Any?>?>>> =
        MutableLiveData()
    val navigateScreenLiveData: LiveData<Event<Pair<Screen, Map<String, Any?>?>>>
        get() = _navigateScreenLiveData
    //endregion

    var solutionsPlaylistId: Long = -1
    private var questionIds = emptyList<Long>()
    private var correctQuestionIds = setOf<String>()

    fun submitResult(args: TbgResultFragmentArgs) {
        val requestBody = mapOf(
            "inviter_id" to args.inviterId.orEmpty(),
            "invitee_id" to args.inviteeId.orEmpty(),
            "game_id" to args.gameId,
            "total_score" to args.totalScore,
            "inviter_score" to args.inviterScore,
            "invitee_score" to args.inviteeScore,
            "inviter_correct_questions" to (args.inviterCorrectQuestions ?: longArrayOf()),
            "invitee_correct_questions" to (args.inviteeCorrectQuestions ?: longArrayOf()),
            "all_questions" to (args.questionIds ?: longArrayOf()),
            "topic" to args.topic.orEmpty(),
            "is_quit" to args.isQuit,
        ).toRequestBody()

        _resultLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            topicBoosterGameRepository.submitResult(requestBody)
                .catch { onResultFailure(it) }
                .collect { onResultSuccess(it.data) }
        }
    }

    fun getPreviousResult(gameId: String) {
        _resultLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            topicBoosterGameRepository.getPreviousResult(gameId)
                .catch { onResultFailure(it) }
                .collect { onResultSuccess(it.data) }
        }
    }

    private fun onResultSuccess(data: TbgResult) {
        solutionsPlaylistId = data.solutionsPlaylistId
        questionIds = data.allQuestionIds
        correctQuestionIds = data.correctQuestionIds.map { it.toString() }.toSet()

        _resultLiveData.value = Outcome.loading(false)
        _resultLiveData.value = Outcome.success(data)
    }

    private fun onResultFailure(e: Throwable) {
        _resultLiveData.value = Outcome.loading(false)
        _resultLiveData.value = Outcome.failure(e)
    }

    fun getQuizSolutions(context: Context) {
        _quizSolutionsLiveData.value = Outcome.loading(true)
        compositeDisposable + getResourceListingUseCase
            .execute(
                GetResourceListingUseCase.Param(
                    page = 1,
                    playlistId = solutionsPlaylistId.toString(),
                    packageDetailsId = "",
                    autoPlayData = false,
                    questionIds = questionIds.map { it.toString() }
                )
            )
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({ onSuccess(context, it) }, {})
    }

    private fun onSuccess(context: Context, videoPlaylistEntity: ResourceListingEntity) {
        _quizSolutionsLiveData.value = Outcome.loading(false)
        val playlistData = resourceListingMapper.map(videoPlaylistEntity)
        playlistData.playlist?.filterIsInstance<QuestionMetaDataModel>()
            ?.forEachIndexed { index, model ->
                model.hideOverflowMenu = true
                model.questionMeta = context.getString(R.string.quiz_question_number, index + 1)
                model.isFullWidthCard = false
                if (model.questionId in correctQuestionIds) {
                    model.label = context.getString(R.string.correct)
                    model.labelColor = "#3bb54a"
                } else {
                    model.label = context.getString(R.string.incorrect)
                    model.labelColor = "#ff0000"
                }
            }
        _quizSolutionsLiveData.value = Outcome.success(playlistData)
    }

    fun openVideoScreen(action: PlayVideo, page: String) {
        sendEvent(
            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, hashMapOf(
                EventConstants.QUESTION_ID to action.videoId,
                EventConstants.SOURCE to page
            )
        )
        val screen =
            if (action.resourceType == SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO) VideoScreen else TextSolutionScreen
        _navigateScreenLiveData.value = Event(
            Pair(
                screen, mapOf(
                    Constants.PAGE to page,
                    Constants.QUESTION_ID to action.videoId,
                    Constants.PARENT_ID to 0,
                    Constants.PLAYLIST_ID to action.playlistId
                )
            )
        )
    }

    fun sendEvent(
        eventName: String,
        params: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false,
        ignoreMoengage: Boolean = true
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                params,
                ignoreSnowplow = ignoreSnowplow,
                ignoreMoengage = ignoreMoengage
            )
        )
    }

    fun sendEventByEventTracker(eventName: String) {
        application.getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(application).toString())
            .addStudentId(UserUtil.getStudentId())
            .addScreenName(Constants.PAGE_TOPIC_BOOSTER_GAME)
            .track()
    }

    fun sendEventByQid(eventName: String, qid: String) {
        application.getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(application).toString())
            .addStudentId(UserUtil.getStudentId())
            .addScreenName(Constants.PAGE_TOPIC_BOOSTER_GAME)
            .addEventParameter(Constants.QUESTION_ID, qid)
            .track()
    }

    fun sendCleverTapEvent(eventName: String) {
        application.getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(application).toString())
            .addStudentId(UserUtil.getStudentId())
            .addScreenName(Constants.PAGE_TOPIC_BOOSTER_GAME)
            .cleverTapTrack()
    }

    private val application: DoubtnutApp
        get() = DoubtnutApp.INSTANCE
}