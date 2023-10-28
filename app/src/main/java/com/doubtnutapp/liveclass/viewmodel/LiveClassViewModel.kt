package com.doubtnutapp.liveclass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.CoreActions
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.data.remote.repository.SnackBarRepositoryImpl
import com.doubtnutapp.data.remote.repository.TopDoubtRepository
import com.doubtnutapp.data.remote.repository.UserActivityRepository
import com.doubtnutapp.domain.addtoplaylist.interactor.RemovePlaylistUseCase
import com.doubtnutapp.domain.addtoplaylist.interactor.SubmitPlayListsUseCase
import com.doubtnutapp.domain.videoPage.interactor.LikedDislikedVideoInteractor
import com.doubtnutapp.liveclass.adapter.LiveClassPollData
import com.doubtnutapp.liveclass.adapter.LiveClassPollOptions
import com.doubtnutapp.liveclass.adapter.LiveClassPollsList
import com.doubtnutapp.plus
import com.doubtnutapp.socket.SocketEventType
import com.doubtnutapp.socket.SocketManager
import com.doubtnutapp.utils.Event
import com.doubtnutapp.videoPage.event.VideoPageEventManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 27/04/20.
 */
class LiveClassViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val socketManager: SocketManager,
    private val videoPageEventManager: VideoPageEventManager,
    private val userActivityRepository: UserActivityRepository,
    private val topDoubtRepository: TopDoubtRepository,
    private val submitPlayListsUseCase: SubmitPlayListsUseCase,
    private val removePlaylistUseCase: RemovePlaylistUseCase,
    private val snackBarRepository: SnackBarRepositoryImpl,
    private val likeVideo: LikedDislikedVideoInteractor
) : BaseViewModel(compositeDisposable) {

    val socketMessage: MediatorLiveData<Event<SocketEventType>> = MediatorLiveData()

    companion object {
        const val PATH = "liveclass"
        var purchasePopupSnackbarVideoPage = 1
    }

    init {
        socketManager(PATH, socketMessage)
    }

    private val _feedbackLiveData: MutableLiveData<Outcome<TagsData>> = MutableLiveData()
    private val _feedbackStatusLiveData: MutableLiveData<Outcome<FeedbackStatusResponse>> =
        MutableLiveData()
    private val _homeworkLiveData: MutableLiveData<Outcome<HomeWorkData>> = MutableLiveData()
    private val _bookmarkLiveData: MutableLiveData<Outcome<BookmarkData>> = MutableLiveData()

    val feedbackLiveData: LiveData<Outcome<TagsData>>
        get() = _feedbackLiveData

    val feedbackStatusLiveData: LiveData<Outcome<FeedbackStatusResponse>>
        get() = _feedbackStatusLiveData

    val homeworkLiveData: LiveData<Outcome<HomeWorkData>>
        get() = _homeworkLiveData

    val bookmarkLiveData: LiveData<Outcome<BookmarkData>>
        get() = _bookmarkLiveData

    private val _snackBarData = MutableLiveData<SnackBarData>()
    val snackBarData: LiveData<SnackBarData>
        get() = _snackBarData

    private fun startLoading() {
        _feedbackLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _feedbackLiveData.value = Outcome.loading(false)
    }

    private val _quizDetailLiveData: MutableLiveData<Outcome<List<LiveClassPopUpItem>>> =
        MutableLiveData()

    val quizDetailLiveData: LiveData<Outcome<List<LiveClassPopUpItem>>>
        get() = _quizDetailLiveData

    fun getLiveClassQuizDetail(resourceId: String) {
        startLoading()
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getLiveClassPopUpDetail(resourceId)
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data.list.mapNotNull { popUp ->
                            when (popUp.type) {
                                "live_class_polls" -> {
                                    val pollData = LiveClassPollData(
                                        question = popUp.pollData?.question,
                                        questionTextColor = popUp.pollData?.questionTextColor,
                                        questionTextSize = popUp.pollData?.questionTextSize,
                                        publishId = popUp.publishId?.toIntOrNull() ?: 0,
                                        quizQuestionId = popUp.pollData?.quizQuestionId,
                                        expiry = popUp.pollData?.expiry,
                                        expiryTextColor = popUp.pollData?.expiryTextColor,
                                        expiryTextSize = popUp.pollData?.expiryTextSize,
                                        responseExpiry = popUp.pollData?.responseExpiry,
                                        answer = popUp.pollData?.answer,
                                        showCloseBtn = popUp.pollData?.showCloseBtn,
                                        bgColor = popUp.pollData?.bgColor,
                                        optionsList = popUp.pollData?.items?.map { option ->
                                            LiveClassPollOptions(
                                                option.key.orEmpty(),
                                                option.value.orEmpty(),
                                                0.0,
                                                "",
                                                false, null, false
                                            )
                                        }.orEmpty()
                                    )
                                    LiveClassPollsList(
                                        mutableListOf(pollData),
                                        popUp.detailId?.toLongOrNull() ?: 0,
                                        popUp.liveAt ?: -1
                                    )
                                }
                                "quiz" -> {
                                    LiveQuizData(
                                        popUp.quizResourceId ?: 0,
                                        popUp.liveClassResourceId ?: 0,
                                        popUp.liveAt ?: -1,
                                        popUp.list.orEmpty(),
                                        popUp.ended ?: false
                                    )
                                }
                                else -> {
                                    null
                                }
                            }
                        }
                    }
                    .map { apiQuizDetail ->
                        val quizList: MutableList<LiveClassPopUpItem> = mutableListOf()
                        apiQuizDetail.forEach { item ->
                            if (item is LiveQuizData) {
                                if (item.liveAt != null && item.liveAt != -1L) {
                                    var liveAt = item.liveAt ?: 0
                                    item.list.forEach { qna ->
                                        val quiz = item.copy()
                                        quiz.liveAt = liveAt
                                        quiz.list = listOf(qna)
                                        quizList.add(quiz)
                                        liveAt += (qna.expiry.toLongOrNull() ?: 0)
                                    }
                                }
                            } else {
                                quizList.add(item)
                            }
                        }
                        quizList
                    }
                    .subscribeToSingle({
                        _quizDetailLiveData.value = Outcome.success(it)
                        stopLoading()
                    }, {
                        stopLoading()
                    })
    }

    fun getFeedbackData(detailId: String?) {
        _feedbackLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getFeedbackData(detailId)
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        val commentTag = arrayListOf<PreComment>()
                        it.data.preDoubts?.forEach { comment ->
                            commentTag.add(PreComment(comment, "1"))
                        }
                        it.data.preComments?.forEach { comment ->
                            commentTag.add(PreComment(comment, "0"))
                        }
                        it.data.commentTags = commentTag
                        it.data
                    }
                    .subscribeToSingle({
                        _feedbackLiveData.value = Outcome.success(it)
                        _feedbackLiveData.value = Outcome.loading(false)
                    }, {
                        _feedbackLiveData.value = Outcome.loading(false)
                    })
    }

    fun getHomeworkData(questionId: String, tabId: String = "", page: String) {
        _homeworkLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getHomeworkData(questionId, tabId, page)
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _homeworkLiveData.value = Outcome.success(it)
                        _homeworkLiveData.value = Outcome.loading(false)
                    }, {
                        _homeworkLiveData.value = Outcome.loading(false)
                    })
    }

    fun isFeedbackRequired(detailId: String) {
        _feedbackStatusLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.isFeedbackRequired(detailId)
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _feedbackStatusLiveData.value = Outcome.success(it)
                        _feedbackStatusLiveData.value = Outcome.loading(false)
                    }, {
                        _feedbackStatusLiveData.value = Outcome.loading(false)
                    })
    }

    fun bookmark(id: String?, assortmentId: String?) {
        startLoading()
        viewModelScope.launch {
            DataHandler.INSTANCE.courseRepository.bookmark(id, assortmentId)
                .map {
                    it.data
                }.catch { e ->
                    _bookmarkLiveData.value = Outcome.Failure(e)
                    stopLoading()
                }.collect {
                    _bookmarkLiveData.value = Outcome.success(it)
                    stopLoading()
                }
        }
    }

    fun storeLiveClassVideoWatchCoreAction() {
        viewModelScope.launch {
            userActivityRepository.storeCoreActionDone(CoreActions.LIVE_CLASS_VIDEO_WATCH).catch { }
                .collect()
        }
    }

    //socket implementation start
    fun connectSocket() {
        socketManager.connect()
    }

    fun joinSocket(vararg message: Any?) {
        compositeDisposable.add(
            socketManager.join(*message)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    fun sendMessage(vararg message: Any?) {
        compositeDisposable.add(
            socketManager.sendMessage(*message)
                .subscribeOn(Schedulers.io())
                .subscribe()
        )
    }

    private fun disposeSocket() {
        socketManager.disposeSocket()
    }

    fun isSocketConnected(): Boolean {
        return socketManager.isSocketConnected
    }

    fun sendEvent(
        event: String, param: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false
    ) {
        videoPageEventManager.eventWith(event, param, ignoreSnowplow)
    }

    private val _topDoubtQuestions
            : MutableLiveData<Outcome<List<TopDoubtQuestion>?>> = MutableLiveData()

    val topDoubtQuestions: LiveData<Outcome<List<TopDoubtQuestion>?>>
        get() = _topDoubtQuestions

    fun fetchTopDoubtQuestions(
        entityType: String,
        entityId: String,
        page: String,
        batchId: String?
    ) {
        viewModelScope.launch {
            topDoubtRepository.fetchTopDoubtQuestions(entityType, entityId, page, batchId)
                .map { it.data }
                .catch {}
                .collect {
                    _topDoubtQuestions.value = Outcome.success(it)
                }
        }
    }

    private val _onAddToWatchLater: MutableLiveData<Event<String>> = MutableLiveData()

    val onAddToWatchLater: LiveData<Event<String>>
        get() = _onAddToWatchLater

    fun addToWatchLater(id: String) {
        submitPlayLists(id, mutableListOf("1"))
    }

    private fun submitPlayLists(id: String, playListIds: List<String>) {
        compositeDisposable + submitPlayListsUseCase
            .execute(Pair(id, playListIds))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _onAddToWatchLater.value = Event(id)
            }, {
            })
    }

    fun removeFromPlaylist(id: String, playListId: String) {
        compositeDisposable + removePlaylistUseCase
            .execute(Pair(id, playListId))
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({})
    }

    override fun onCleared() {
        super.onCleared()
        disposeSocket()
    }
    //socket implementation end

    fun getSnackBar(
        qid: String?
    ) {
        viewModelScope.launch {
            snackBarRepository.getSnackBarData(
                source = "video_page",
                page = purchasePopupSnackbarVideoPage++,
                qid = qid,
            )
                .catch { exception ->
                    Log.e(exception)
                }
                .collect {
                    if (it.data?.title.isNullOrEmpty()) return@collect
                    _snackBarData.value = it.data ?: return@collect
                }
        }
    }

    fun likeButtonClicked(
        videoName: String,
        questionId: String,
        answerId: String,
        viewTime: Long,
        page: String,
        viewId: String,
        isLiked: Boolean
    ) {
        likeVideo.execute(
            LikedDislikedVideoInteractor.Param(
                videoName,
                questionId,
                answerId,
                viewTime.toString(),
                page,
                isLiked,
                "",
                viewId
            )
        ).applyIoToMainSchedulerOnCompletable().subscribe()
    }

    fun disLikeButtonClicked(
        videoName: String,
        questionId: String,
        answerId: String,
        viewTime: Long,
        page: String,
        feedback: String,
        viewId: String,
        isDisliked: Boolean
    ) {
        likeVideo.execute(
            LikedDislikedVideoInteractor.Param(
                videoName,
                questionId,
                answerId,
                viewTime.toString(),
                page,
                isDisliked,
                feedback,
                viewId
            )
        ).applyIoToMainSchedulerOnCompletable().subscribe()
    }

}