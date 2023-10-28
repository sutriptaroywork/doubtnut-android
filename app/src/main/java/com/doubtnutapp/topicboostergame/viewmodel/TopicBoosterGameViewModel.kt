package com.doubtnutapp.topicboostergame.viewmodel

import android.content.Context
import android.nfc.FormatException
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
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.topicboostergame.*
import com.doubtnutapp.data.remote.repository.TopicBoosterGameRepository
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.domain.resourcelisting.entities.ResourceListingEntity
import com.doubtnutapp.domain.resourcelisting.interactor.GetResourceListingUseCase
import com.doubtnutapp.resourcelisting.mapper.ResourceListingMapper
import com.doubtnutapp.resourcelisting.model.QuestionMetaDataModel
import com.doubtnutapp.resourcelisting.model.ResourceListingData
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.screennavigator.Screen
import com.doubtnutapp.screennavigator.TextSolutionScreen
import com.doubtnutapp.screennavigator.VideoScreen
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by devansh on 27/2/21.
 */

class TopicBoosterGameViewModel @Inject constructor(
    private val topicBoosterGameRepository: TopicBoosterGameRepository,
    private val userPreference: UserPreference,
    val deeplinkAction: DeeplinkAction,
    private val analyticsPublisher: AnalyticsPublisher,
    private val getResourceListingUseCase: GetResourceListingUseCase,
    compositeDisposable: CompositeDisposable,
    private val resourceListingMapper: ResourceListingMapper,
    val screenNavigator: Navigator,
) : BaseViewModel(compositeDisposable) {

    var parentQuestionId: String = ""
    var chapterAlias: String? = null
    var opponentImageUrl: String = ""
    var opponentName: String = ""
    var opponentImageBackgroundColor: Int = 0
    var solutionsPlaylistId: String = ""
    var questionIds: ArrayList<String>? = null

    var additionalDetails: AdditionalDetails? = null

    var userAttemptedCount: Int = 0
    var userCorrectCount: Int = 0
    var userScore: Int = 0

    var opponentAttemptedCount = 0
    var opponentCorrectCount: Int = 0
    var opponentScore: Int = 0

    private val correctlyAnsweredQuestions = mutableSetOf<Int>()

    inline val totalQuestions: Int
        get() = questionsListLiveData.value?.size ?: -1

    val userImageUrl: String
        get() = userPreference.getUserImageUrl()

    val isWalletRewardEnabled: Boolean
        get() = additionalDetails?.isWalletReward ?: false

    //region LiveData
    private val _questionsListLiveData = MutableLiveData<List<TopicBoosterGameQuestion>>()
    val questionsListLiveData: LiveData<List<TopicBoosterGameQuestion>>
        get() = _questionsListLiveData

    private val _gameBannerDataLiveData = MutableLiveData<TopicBoosterGameBannerData?>()
    val gameBannerDataLiveData: LiveData<TopicBoosterGameBannerData?>
        get() = _gameBannerDataLiveData

    private val _quizSolutionsLiveData: MutableLiveData<Outcome<ResourceListingData>> =
        MutableLiveData()
    val quizSolutionsLiveData: LiveData<Outcome<ResourceListingData>>
        get() = _quizSolutionsLiveData

    private val _navigateScreenLiveData: MutableLiveData<Event<Pair<Screen, Map<String, Any?>?>>> =
        MutableLiveData()
    val navigateScreenLiveData: LiveData<Event<Pair<Screen, Map<String, Any?>?>>>
        get() = _navigateScreenLiveData

    private val _resultLiveData = MutableLiveData<TopicBoosterGameResult>()
    val resultLiveData: LiveData<TopicBoosterGameResult>
        get() = _resultLiveData
    //endregion

    fun getTopicBoosterGameQuestionsList(
        questionId: String,
        testUuid: String?,
        chapterAlias: String?
    ) {
        viewModelScope.launch {
            if (testUuid == null) {
                topicBoosterGameRepository.getTopicBoosterGameBanner(questionId)
            } else {
                flow { emit(null) }
            }.map {
                if (it != null) {
                    additionalDetails = it.data.additionalDetails
                }
                _gameBannerDataLiveData.value = it?.data
                val _testUuid = testUuid ?: it?.data?.testUuId
                val _chapterAlias = chapterAlias ?: it?.data?.chapterAlias
                if (_testUuid != null && _chapterAlias != null) {
                    topicBoosterGameRepository.getTopicBoosterGameQuestionsList(
                        _testUuid,
                        _chapterAlias,
                        additionalDetails?.totalQuestions ?: 5,
                        additionalDetails?.expiry ?: 30
                    )
                } else {
                    flow { emit(null) }
                }.single()
            }.catch { }.collect {
                if (it != null) {
                    it.data.forEach { question ->
                        question.optionStatusList = MutableList(question.options.size) {
                            OptionStatus.UNSELECTED
                        }
                    }
                    resetGameState()
                    solutionsPlaylistId = it.data.firstOrNull()?.solutionsPlaylistId.orEmpty()
                    questionIds = ArrayList(it.data.map { it.questionId.toString() })
                    _questionsListLiveData.value = it.data
                }
            }
        }
    }

    private fun resetGameState() {
        userAttemptedCount = 0
        userCorrectCount = 0
        userScore = 0

        opponentAttemptedCount = 0
        opponentCorrectCount = 0
        opponentScore = 0

        correctlyAnsweredQuestions.clear()
    }

    fun saveUserResponse(
        question: TopicBoosterGameQuestion, isCorrect: Boolean,
        parentQuestionId: String, questionNumber: Int,
    ) {
        viewModelScope.launch {
            with(question) {
                topicBoosterGameRepository.saveUserResponse(
                    TopicBoosterGameUserResponse(
                        testUuid = testUuid,
                        questionId = questionId,
                        isCorrect = isCorrect.toInt(),
                        subject = subject,
                        chapter = chapter,
                        parentQuestionId = parentQuestionId,
                        score = score,
                    )
                ).catch { }.collect()
            }
        }
        if (isCorrect) {
            correctlyAnsweredQuestions.add(questionNumber)
        }
    }

    fun submitResult(gameResult: Int) {
        viewModelScope.launch {
            topicBoosterGameRepository.submitResult(
                gameResult,
                additionalDetails?.isWalletReward == true
            )
                .catch { }
                .collect {
                    if (it.data.rewardMessage != null) {
                        _resultLiveData.value = it.data
                    }
                }
        }
    }

    fun getQuizSolutions(
        context: Context,
        playlistId: String,
        questionIds: List<String>?,
        isQuit: Boolean
    ) {
        _quizSolutionsLiveData.value = Outcome.loading(true)
        compositeDisposable.add(
            getResourceListingUseCase
                .execute(GetResourceListingUseCase.Param(1, playlistId, "", false, questionIds))
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({ onSuccess(context, it, isQuit) }, this::onError)
        )
    }

    private fun onSuccess(
        context: Context,
        videoPlaylistEntity: ResourceListingEntity,
        isQuit: Boolean
    ) {
        _quizSolutionsLiveData.value = Outcome.loading(false)
        val playlistData = resourceListingMapper.map(videoPlaylistEntity)
        playlistData.playlist?.filterIsInstance<QuestionMetaDataModel>()
            ?.forEachIndexed { index, model ->
                model.hideOverflowMenu = true
                model.questionMeta = context.getString(R.string.quiz_question_number, index + 1)
                model.isFullWidthCard = false
                if (isQuit.not()) {
                    if (index in correctlyAnsweredQuestions) {
                        model.label = context.getString(R.string.correct)
                        model.labelColor = "#3bb54a"
                    } else {
                        model.label = context.getString(R.string.incorrect)
                        model.labelColor = "#ff0000"
                    }
                }
            }
        _quizSolutionsLiveData.value = Outcome.success(playlistData)
    }

    private fun onError(error: Throwable) {
        _quizSolutionsLiveData.value = Outcome.loading(false)
        _quizSolutionsLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
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

    fun openVideoScreen(action: PlayVideo, page: String) {
        sendEvent(
            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, hashMapOf(
                EventConstants.QUESTION_ID to action.videoId,
                EventConstants.SOURCE to page
            )
        )
        val screen =
            if (action.resourceType == SOLUTION_RESOURCE_TYPE_VIDEO) VideoScreen else TextSolutionScreen
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

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        params[Constants.GAME_VARIANT_ID] = additionalDetails?.variantId ?: -1
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
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