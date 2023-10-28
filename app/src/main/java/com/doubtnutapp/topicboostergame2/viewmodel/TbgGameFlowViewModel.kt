package com.doubtnutapp.topicboostergame2.viewmodel

import androidx.lifecycle.*
import androidx.navigation.NavDirections
import com.doubtnutapp.Log
import com.doubtnutapp.NavGraphGameFlowDirections
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.models.topicboostergame.OptionStatus
import com.doubtnutapp.data.remote.models.topicboostergame.TopicBoosterGameBannerData
import com.doubtnutapp.data.remote.models.topicboostergame2.QuestionSubmit
import com.doubtnutapp.data.remote.models.topicboostergame2.TbgGameData
import com.doubtnutapp.data.remote.models.topicboostergame2.TbgQuestion
import com.doubtnutapp.data.remote.repository.TopicBoosterGameRepository2
import com.doubtnutapp.plus
import com.doubtnutapp.socket.OnResponseData
import com.doubtnutapp.socket.SocketEventType
import com.doubtnutapp.socket.SocketManager
import com.doubtnutapp.topicboostergame2.ui.TbgQuizFragmentArgs
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

/**
 * Created by devansh on 25/06/21.
 */

class TbgGameFlowViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val topicBoosterGameRepository2: TopicBoosterGameRepository2,
    private val socketManager: SocketManager,
    private val gson: Gson,
) : BaseViewModel(compositeDisposable) {

    companion object {
        const val SOCKET_PATH = "live-quiz"
        const val MAX_SUBMISSIONS_FOR_QUESTION = 2
    }

    //region Socket instance variables
    val socketMessageLiveData: MediatorLiveData<Event<SocketEventType>> = MediatorLiveData()

    val isSocketConnected: Boolean
        get() = socketManager.isSocketConnected
    //endregion

    //region LiveData
    /**
     * Same LiveData is used for API responses of both inviter and invitee.
     * In a game session, only one of [getGameData] or [acceptInvite] will be called.
     */
    private val _gameDataLiveData = MutableLiveData<TbgGameData>()
    val gameLiveData: LiveData<TbgGameData>
        get() = _gameDataLiveData

    private val _opponentQuestionSubmittedLiveData = MutableLiveData<Event<QuestionSubmit>>()
    val opponentQuestionSubmittedLiveData: LiveData<Event<QuestionSubmit>>
        get() = _opponentQuestionSubmittedLiveData
    //endregion

    inline val questionsList: List<TbgQuestion>
        get() = gameLiveData.value?.questions.orEmpty()

    inline val messages: Array<String>
        get() = gameLiveData.value?.chatActions?.messages?.toTypedArray() ?: emptyArray()

    inline val emojis: List<String>
        get() = gameLiveData.value?.chatActions?.emojis.orEmpty()

    inline val topic: String
        get() = gameLiveData.value?.topic.orEmpty()

    inline val gameId: String
        get() = gameLiveData.value?.gameId.orEmpty()

    inline val musicUrl: String?
        get() = gameLiveData.value?.musicUrl

    var isInviterFlowExecuted = false
    var isInviteeFlowExecuted = false

    private val socketEventObserver = Observer<Event<SocketEventType>> {
        val type = it.peekContent()
        if (type is OnResponseData && type.data is QuestionSubmit) {
            handleQuestionSubmitSocketEvent(type.data)
        }
    }

    init {
        socketManager(SOCKET_PATH, socketMessageLiveData)
        socketMessageLiveData.observeForever(socketEventObserver)
    }

    /**
     * Called by inviter
     */
    fun getGameData(
        chapterAlias: String, gameId: String, inviteeIds: Array<String>?, isWhatsApp: Boolean
    ) {
        isInviterFlowExecuted = true
        viewModelScope.launch {
            topicBoosterGameRepository2.getGameData(chapterAlias, gameId, inviteeIds, isWhatsApp)
                .catch {
                    Log.e(it)
                }
                .collect {
                    onGameDataSuccess(it.data)
                }
        }
    }

    /**
     * Called by invitee
     */
    fun acceptInvite(
        gameId: String, inviterId: String, isInviterOnline: Boolean, chapterAlias: String
    ) {
        viewModelScope.launch {
            topicBoosterGameRepository2
                .acceptInvite(gameId, inviterId, isInviterOnline, chapterAlias)
                .catch { }
                .collect { onGameDataSuccess(it.data) }
        }
    }

    private fun onGameDataSuccess(data: TbgGameData) {
        data.questions?.forEach { question ->
            question.optionStatusList = MutableList(question.options.size) {
                OptionStatus.UNSELECTED
            }
        }
        _gameDataLiveData.value = data
    }

    fun connectSocket() {
        socketManager.connect()
    }

    fun joinSocket(gameId: String) {
        val message = JSONObject(mapOf("game_id" to gameId)).toString()
        compositeDisposable.add(
            socketManager.join(message)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun gameBeginEvent(inviterId: String, inviteeId: String, gameId: String) {
        val message = JSONObject(
            mapOf(
                "inviter_id" to inviterId,
                "invitee_id" to inviteeId,
                "game_id" to gameId,
                "image" to UserUtil.getProfileImage(),
                "name" to UserUtil.getStudentName(),
            )
        ).toString()
        compositeDisposable.add(
            socketManager.sendEvent(SocketManager.EVENT_GAME_BEGIN, message)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun checkInviterOnline(inviterId: String, gameId: String) {
        isInviteeFlowExecuted = true
        val message = JSONObject(mapOf("inviter_id" to inviterId, "game_id" to gameId,)).toString()
        compositeDisposable.add(
            socketManager.sendEvent(SocketManager.EVENT_INVITER_ONLINE, message)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun sendMessage(message: String) {
        val messageJson = JSONObject(mapOf("message" to message, "game_id" to gameId)).toString()
        compositeDisposable.add(
            socketManager.sendEvent(SocketManager.EVENT_GAME_MESSAGE, messageJson)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun sendEmoji(emoji: String) {
        val messageJson = JSONObject(mapOf("emoji" to emoji, "game_id" to gameId)).toString()
        compositeDisposable.add(
            socketManager.sendEvent(SocketManager.EVENT_GAME_EMOJI, messageJson)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun handleQuestionSubmitSocketEvent(data: QuestionSubmit) {
        //Opponent submitted the answer
        //Null checks for backwards compatibility with older app versions not sending these keys
        if (data.totalScore != null) {
            opponentScore = data.totalScore
        }
        data.correctQuestionIds?.let {
            storeOpponentCorrectQuestions(it)
        }
        _opponentQuestionSubmittedLiveData.value = Event(data)
    }

    fun disposeSocket() {
        socketManager.disposeSocket()
    }

    override fun onCleared() {
        super.onCleared()
        socketMessageLiveData.removeObserver(socketEventObserver)
        disposeSocket()
    }

    /**
     * ---------------------------------------------------------------------
     * Older view model code
     */
    //region Topic booster game view model
    val opponentImageBackgroundColor: Int
        get() = Utils.parseColor(gameLiveData.value?.inviterData?.backgroundColor)

    private inline val questionIds: LongArray
        get() = gameLiveData.value?.questions?.map { it.questionId }?.toLongArray() ?: LongArray(0)

    var userScore: Int = 0
    var opponentScore: Int = 0

    private var remainingSubmissionsForQuestion: Int = MAX_SUBMISSIONS_FOR_QUESTION

    private val userCorrectQuestions = mutableSetOf<Long>()
    val opponentCorrectQuestions = mutableSetOf<Long>()

    inline val totalQuestions: Int
        get() = questionsList.size

    inline val totalScore: Int
        get() = questionsList.sumBy { it.maxScore }

    val userImageUrl: String
        get() = UserUtil.getProfileImage()

    //region LiveData
    private val _gameBannerDataLiveData = MutableLiveData<TopicBoosterGameBannerData?>()
    val gameBannerDataLiveData: LiveData<TopicBoosterGameBannerData?>
        get() = _gameBannerDataLiveData

    private val _isSubmissionDoneForQuestionLiveData = MutableLiveData<Event<Boolean>>()
    val isSubmissionDoneForQuestionLiveData: LiveData<Event<Boolean>>
        get() = _isSubmissionDoneForQuestionLiveData
    //endregion

    fun answerSubmitted() {
        remainingSubmissionsForQuestion -= 1
        if (remainingSubmissionsForQuestion == 0) {
            _isSubmissionDoneForQuestionLiveData.value = Event(true)
        }
        remainingSubmissionsForQuestion.coerceAtLeast(0)
    }

    fun resetQuestionSubmissionState() {
        remainingSubmissionsForQuestion = MAX_SUBMISSIONS_FOR_QUESTION
    }

    fun submitAnswer(questionId: Long, questionSubmit: QuestionSubmit, isCorrect: Boolean) {
        answerSubmitted()
        if (isCorrect) {
            userCorrectQuestions.add(questionId)
        }
        questionSubmit.correctQuestionIds = userCorrectQuestions.toList()
        compositeDisposable + socketManager.sendEvent(
            SocketManager.EVENT_QUESTION_SUBMIT, gson.toJson(questionSubmit)
        ).subscribeOn(Schedulers.io()).subscribe()
    }

    private fun storeOpponentCorrectQuestions(correctQuestionIds: List<Long>) {
        opponentCorrectQuestions.clear()
        opponentCorrectQuestions.addAll(correctQuestionIds)
    }

    fun getShowResultAction(args: TbgQuizFragmentArgs, isQuit: Boolean): NavDirections {
        val (inviterScore, inviteeScore) = if (args.isInviter) {
            Pair(userScore, opponentScore)
        } else {
            Pair(opponentScore, userScore)
        }

        val (inviterCorrectQuestions, inviteeCorrectQuestions) = if (args.isInviter) {
            Pair(userCorrectQuestions, opponentCorrectQuestions)
        } else {
            Pair(opponentCorrectQuestions, userCorrectQuestions)
        }

        return NavGraphGameFlowDirections.actionShowResult(
            inviterId = args.inviterId,
            inviteeId = args.inviteeId,
            gameId = args.gameId,
            totalScore = totalScore,
            inviterScore = inviterScore,
            inviteeScore = inviteeScore,
            inviterCorrectQuestions = inviterCorrectQuestions.toLongArray(),
            inviteeCorrectQuestions = inviteeCorrectQuestions.toLongArray(),
            topic = topic,
            questionIds = questionIds,
            submitResult = true,
            isQuit = isQuit,
        )
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }
    //endregion
}