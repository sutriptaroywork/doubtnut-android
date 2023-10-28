package com.doubtnutapp.similarplaylist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.addtoplaylist.interactor.RemovePlaylistUseCase
import com.doubtnutapp.domain.addtoplaylist.interactor.SubmitPlayListsUseCase
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.domain.similarVideo.entities.SimilarPlaylistEntity
import com.doubtnutapp.domain.similarVideo.entities.SimilarVideoEntity
import com.doubtnutapp.domain.similarVideo.interactor.*
import com.doubtnutapp.likeDislike.LikeDislikeVideo
import com.doubtnutapp.plus
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.similarVideo.model.AskNowViewItem
import com.doubtnutapp.similarVideo.model.PostCommunityViewItem
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.util.*
import javax.inject.Inject

class SimilarPlaylistFragmentViewModel @Inject constructor(
    private val getSimilarPlaylistUseCase: GetSimilarPlaylistUseCase,
    private val likeDislikeVideo: LikeDislikeVideo,
    private val postQuestionToCommunity: PostQuestionToCommunity,
    private val submitQuestionMatchFeedback: SubmitSimilarQuestionFeedback,
    private val whatsappSharing: WhatsAppSharing,
    private val getPreviousSimilarVideoInteractor: GetPreviousSimilarVideoInteractor,
    private val saveSimilarVideoInteractor: SaveSimilarVideoInteractor,
    compositeDisposable: CompositeDisposable,
    private val submitPlayListsUseCase: SubmitPlayListsUseCase,
    private val removePlaylistUseCase: RemovePlaylistUseCase
) : BaseViewModel(compositeDisposable) {

    private val FEEDBACK_YES = 1
    private val FEEDBACK_NO = 0
    private val screenName: String = "similar"

    private val _navigateScreenLiveData: MutableLiveData<Pair<Screen, Map<String, Any?>?>> =
        MutableLiveData()

    val navigateScreenLiveData: LiveData<Pair<Screen, Map<String, Any?>?>>
        get() = _navigateScreenLiveData

    private val _navigateToSameScreenLiveData: MutableLiveData<Event<Pair<Screen, Map<String, Any?>>>> =
        MutableLiveData()

    val navigateToSameScreenLiveData: LiveData<Event<Pair<Screen, Map<String, Any?>>>>
        get() = _navigateToSameScreenLiveData

    private val _navigateToNewScreenLiveData: MutableLiveData<Event<Pair<Screen, Map<String, Any?>>>> =
        MutableLiveData()

    val navigateToNewScreenLiveData: LiveData<Event<Pair<Screen, Map<String, Any?>>>>
        get() = _navigateToNewScreenLiveData

    val whatsAppShareableData: LiveData<Event<Triple<String?, String?, String?>>>
        get() = whatsappSharing.whatsAppShareableData

    private val _showProgressLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val showProgressLiveData: LiveData<Boolean>
        get() = _showProgressLiveData

    val showWhatsappProgressLiveData: LiveData<Boolean>
        get() = whatsappSharing.showWhatsAppProgressLiveData

    private val _similarVideoLiveData: MutableLiveData<Outcome<SimilarPlaylistEntity>> =
        MutableLiveData()

    val similarVideoLiveData: LiveData<Outcome<SimilarPlaylistEntity>>
        get() = _similarVideoLiveData

    private val _feedbackViewReplacement: MutableLiveData<RecyclerViewItem> = MutableLiveData()

    val feedbackViewReplacement: LiveData<RecyclerViewItem>
        get() = _feedbackViewReplacement

    private val _postQuestionResponse: MutableLiveData<Outcome<Unit>> = MutableLiveData()

    val postQuestionResponse: LiveData<Outcome<Unit>>
        get() = _postQuestionResponse

    private val _addToPlayListLiveData: MutableLiveData<Event<String>> = MutableLiveData()

    val addToPlayListLiveData: LiveData<Event<String>>
        get() = _addToPlayListLiveData

    private val _onAddToWatchLater: MutableLiveData<Event<String>> = MutableLiveData()

    val onAddToWatchLater: LiveData<Event<String>>
        get() = _onAddToWatchLater

    private val _eventLiveData: MutableLiveData<String> = MutableLiveData()

    val eventLiveData: LiveData<String>
        get() = _eventLiveData

    private val _emptyStackLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val emptyStackLiveData: LiveData<Boolean>
        get() = _emptyStackLiveData

    var askedQuestionId: String = ""

    var supportedResourceType: String = "video"

    private var similarVideoEntity: SimilarVideoEntity? = null

    fun handleAction(action: Any) {
        when (action) {
            is LikeVideo -> {
                likeDislikeVideo.likeDislikeVideo(action.videoId, screenName, action.isLiked)
            }

            is ShareOnWhatApp -> shareOnWhatsApp(action)
            is PlayVideo -> openVideoScreen(action)
            is SubmitFeedBack -> submitFeedback(action)
            is OpenCameraFragment -> _navigateScreenLiveData.value = Pair(CameraScreen, null)
            is PostQuestion -> postQuestionToCommunity()
            is AddToPlayList -> addToPlayList(action.videoId)
            is OpenWhatsapp -> openWhatsapp(action.externalUrl)
            is OpenPCPopup -> openUnlockPopup()
            is WatchLaterRequest -> addToWatchLater(action.id)
        }
    }

    private fun addToWatchLater(id: String) {
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

    private fun openWhatsapp(externalUrl: String) {
        val args = hashMapOf(
            Constants.EXTERNAL_URL to externalUrl
        )
        _navigateLiveData.value = Event(NavigationModel(ExternalUrlScreen, args))
        sendEvent(EventConstants.EVENT_WHATSAPP_CARD_CLICK + "SimilarVideo")

    }

    private fun addToPlayList(videoId: String) {
        _addToPlayListLiveData.value = Event(videoId)
    }

    private fun openUnlockPopup() {
        _navigateLiveData.value = Event(NavigationModel(PCUnlockScreen, null))
    }

    private fun shareOnWhatsApp(action: ShareOnWhatApp) {
        whatsappSharing.shareOnWhatsApp(action)
    }

    private fun openVideoScreen(action: PlayVideo) {
        val argument = hashMapOf(
            Constants.PAGE to action.page,
            Constants.QUESTION_ID to action.videoId
        )
        val screen =
            if (action.resourceType == SOLUTION_RESOURCE_TYPE_VIDEO) VideoScreen else TextSolutionScreen

        if (action.resourceType == supportedResourceType) {
            _navigateToSameScreenLiveData.value = Event(Pair(screen, argument))
        } else {
            _navigateToNewScreenLiveData.value = Event(Pair(screen, argument))
        }
    }

    private fun onSimilarVideoSuccess(similarPlaylistItem: SimilarPlaylistEntity) {
        _similarVideoLiveData.value = Outcome.loading(false)
        this.similarVideoEntity = similarVideoEntity
        _similarVideoLiveData.value = Outcome.success(similarPlaylistItem)
    }

    private fun onMatchQuestionError(error: Throwable) {
        _similarVideoLiveData.value = Outcome.loading(false)
        _similarVideoLiveData.value = if (error is HttpException) {
            val errorCode = error.response()?.code()
            when (errorCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    fun getSimilarResults(questionId: String) {
        addSimilarVideoData()
        _similarVideoLiveData.value = Outcome.loading(true)
        compositeDisposable + getSimilarPlaylistUseCase
            .execute(questionId)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onSimilarVideoSuccess, this::onMatchQuestionError)
    }

    fun getPreviousVideo() {
//        _similarVideoLiveData.value = Outcome.loading(true)
//        compositeDisposable.add(getPreviousSimilarVideoInteractor
//                .execute(GetPreviousSimilarVideoInteractor.None())
//                .applyIoToMainSchedulerOnSingle()
//                .subscribeToSingle(this::onSimilarVideoSuccess, this::noVideoOnStack))
    }

    private fun noVideoOnStack(error: Throwable) {
        _similarVideoLiveData.value = Outcome.loading(false)
        if (error == EmptyStackException()) {
            _emptyStackLiveData.value = true
        }
    }

    private fun addSimilarVideoData() {
        similarVideoEntity?.let {
            compositeDisposable.add(saveSimilarVideoInteractor
                .execute(SaveSimilarVideoInteractor.Param(it))
                .applyIoToMainSchedulerOnCompletable()
                .subscribe({}, {
                    it.printStackTrace()
                })
            )
        }

    }

    private fun submitFeedback(action: SubmitFeedBack) {
        when (action.isLiked) {
            FEEDBACK_YES -> _feedbackViewReplacement.value = AskNowViewItem(
                R.string.title_matchQuestion_askQuestion,
                R.string.buttonText_matchQuestion_askNow,
                action.bgColorOfFeedView,
                R.layout.item_similar_video_askquestion
            )

            FEEDBACK_NO -> _feedbackViewReplacement.value = PostCommunityViewItem(
                R.string.title_matchQuestion_postCommunity,
                R.string.buttonText_matchQuestion_postOnCommunity,
                action.bgColorOfFeedView,
                R.layout.item_similar_video_postcommunity
            )
        }
        submitQuestionMatchFeedback.execute(action.isLiked).applyIoToMainSchedulerOnCompletable()
            .subscribe()
    }

    private fun postQuestionToCommunity() {
        _showProgressLiveData.value = true
        compositeDisposable + postQuestionToCommunity.execute(
            PostQuestionToCommunity.Param(
                askedQuestionId,
                ""
            )
        )
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable(this::onPostQuestionSuccess, this::onPostQuestionError)

    }

    private fun onPostQuestionError(error: Throwable) {
        _showProgressLiveData.value = false
        _postQuestionResponse.value = if (error is HttpException) {
            val errorCode = error.response()?.code()
            when (errorCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    private fun onPostQuestionSuccess() {
        _showProgressLiveData.value = false
        _postQuestionResponse.value = Outcome.success(Unit)
    }

    private fun sendEvent(eventName: String) {
        _eventLiveData.value = eventName
    }

}