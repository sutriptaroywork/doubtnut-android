package com.doubtnutapp.textsolution.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.addtoplaylist.interactor.RemovePlaylistUseCase
import com.doubtnutapp.domain.addtoplaylist.interactor.SubmitPlayListsUseCase
import com.doubtnutapp.domain.textsolution.entities.TextSolutionDataEntity
import com.doubtnutapp.domain.textsolution.interactor.*
import com.doubtnutapp.plus
import com.doubtnutapp.sharing.VIDEO_CHANNEL
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.textsolution.event.TextSolutionEventManager
import com.doubtnutapp.textsolution.mapper.TextSolutionMapper
import com.doubtnutapp.textsolution.model.TextAnswerData
import com.doubtnutapp.textsolution.ui.TextSolutionActivity
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.util.*
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
class TextSolutionViewModel @Inject constructor(
    private val likeVideo: LikedDislikedTextSolutionInteractor,
    private val videoDataUseCase: GetTextSolutionData,
    private val saveVideoInteractor: SaveTextSolutionInteractor,
    private val getPreviousVideoInteractor: GetPreviousTextSolutionInteractor,
    private val requestVideoSolutionInteractor: RequestVideoSolutionInteractor,
    private val updateTextSolutionEngagementUseCase: UpdateTextSolutionEngagementUseCase,
    private val whatsappSharing: WhatsAppSharing,
    private val textSolutionMapper: TextSolutionMapper,
    compositeDisposable: CompositeDisposable,
    private val videoPageEventManager: TextSolutionEventManager,
    private val submitPlayListsUseCase: SubmitPlayListsUseCase,
    private val removePlaylistUseCase: RemovePlaylistUseCase
) : BaseViewModel(compositeDisposable) {

    private val whiteColorBg: String = "#FFFFFF"
    private var event_param_page = ""
    private var event_param_parent_id = ""

    val whatsAppShareableData: LiveData<Event<Triple<String?, String?, String?>>>
        get() = whatsappSharing.whatsAppShareableData

    val showWhatsappProgressLiveData: LiveData<Boolean>
        get() = whatsappSharing.showWhatsAppProgressLiveData


    private val _showProgressLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val showProgressLiveData: LiveData<Boolean>
        get() = _showProgressLiveData

    private val _emptyStackLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val emptyStackLiveData: LiveData<Boolean>
        get() = _emptyStackLiveData


    private val _getVideoLiveData: MutableLiveData<Outcome<TextAnswerData>> = MutableLiveData()

    val getVideoLiveData: LiveData<Outcome<TextAnswerData>>
        get() = _getVideoLiveData

    private val _likeCountLiveData: MutableLiveData<Pair<String, Boolean>> = MutableLiveData()

    val likeCountLiveData: LiveData<Pair<String, Boolean>>
        get() = _likeCountLiveData

    private val _disLikeCountLiveData: MutableLiveData<Pair<String, Boolean>> = MutableLiveData()

    val disLikeCountLiveData: LiveData<Pair<String, Boolean>>
        get() = _disLikeCountLiveData


    private val _shareCountLiveData: MutableLiveData<String> = MutableLiveData()

    val shareCountLiveData: LiveData<String>
        get() = _shareCountLiveData


    private var videoStackData: TextSolutionDataEntity? = null

    private val _onAddToWatchLater: MutableLiveData<Event<String>> = MutableLiveData()

    val onAddToWatchLater: LiveData<Event<String>>
        get() = _onAddToWatchLater

    fun addToWatchLater(id: String) {
        submitPlayLists(id, mutableListOf("1"))
    }

    var backPressBottomSheetDeeplink: String? = null

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


    fun shareOnWhatsApp(featureType: String, thumbnailImage: String, controlParams: HashMap<String, String>, questionId: String, sharingMessage: String) {
        videoStackData?.let {
            it.shareCount = it.shareCount.plus(1)
            _shareCountLiveData.value = it.shareCount.toString()
        }

        whatsappSharing.shareOnWhatsApp(ShareOnWhatApp(
                VIDEO_CHANNEL,
                featureType,
                thumbnailImage,
                controlParams,
                whiteColorBg,
                sharingMessage,
                questionId
        ))
    }


    fun viewVideo(questionId: String, playListId: String?, mcId: String?, page: String,
                  mcClass: String, referredStudentId: String?, parentId: String?, ocrText: String?,
                  html: String?) {
        addVideoData()
        event_param_page = page
        if (parentId != null) {
            event_param_parent_id = parentId
        }
        _getVideoLiveData.value = Outcome.loading(true)
        compositeDisposable.add(videoDataUseCase
                .execute(GetTextSolutionData.Param(questionId, playListId, mcId, page, mcClass,
                        referredStudentId, parentId, ocrText, html))
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onVideoPageViewSuccess, this::onVideoPageViewError))
    }

    private fun onVideoPageViewSuccess(textSolutionDataEntity: TextSolutionDataEntity) {
        _getVideoLiveData.value = Outcome.loading(false)
        val matchResult = textSolutionDataEntity.run {
            textSolutionMapper.map(textSolutionDataEntity)
        }
        _getVideoLiveData.value = Outcome.success(matchResult)
        videoStackData = textSolutionDataEntity
        _disLikeCountLiveData.value = Pair(matchResult.dislikesCount.toString(), matchResult.isDisliked)
        _likeCountLiveData.value = Pair(matchResult.likeCount.toString(), matchResult.isLiked)
        _shareCountLiveData.value = videoStackData?.shareCount.toString()
        backPressBottomSheetDeeplink = textSolutionDataEntity.backPressBottomSheetDeeplink
    }

    private fun onVideoPageViewError(error: Throwable) {
        _getVideoLiveData.value = Outcome.loading(false)
        _getVideoLiveData.value = if (error is HttpException) {
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
        logException(error)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException
                    || error is NullPointerException
                    || error is ClassCastException
                    || error is FormatException
                    || error is IllegalArgumentException) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }


    fun getPreviousVideo() {
        _getVideoLiveData.value = Outcome.loading(true)
        compositeDisposable.add(getPreviousVideoInteractor
                .execute(GetPreviousTextSolutionInteractor.None())
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::videoOnStackSuccess, this::noVideoOnStack))
    }

    private fun videoOnStackSuccess(textSolutionDataEntity: TextSolutionDataEntity) {
        _getVideoLiveData.value = Outcome.loading(false)
        val matchResult = textSolutionDataEntity.run {
            textSolutionMapper.map(textSolutionDataEntity)
        }
        videoStackData = textSolutionDataEntity
        _getVideoLiveData.value = Outcome.success(matchResult)
        _disLikeCountLiveData.value = Pair(matchResult.dislikesCount.toString(), matchResult.isDisliked)
        _likeCountLiveData.value = Pair(matchResult.likeCount.toString(), matchResult.isLiked)
        _shareCountLiveData.value = matchResult.shareCount.toString()
    }

    private fun noVideoOnStack(error: Throwable) {
        _getVideoLiveData.value = Outcome.loading(false)
        if (error == EmptyStackException()) {
            _emptyStackLiveData.value = true
        }
    }

    fun addVideoData() {
        videoStackData?.let {
            compositeDisposable.add(saveVideoInteractor
                    .execute(SaveTextSolutionInteractor.Param(it))
                    .applyIoToMainSchedulerOnCompletable()
                    .subscribe())
        }
    }

    fun requestVideoSolution(questionId: String) {
        compositeDisposable.add(requestVideoSolutionInteractor
            .execute(RequestVideoSolutionInteractor.Param(questionId))
            .applyIoToMainSchedulerOnCompletable()
            .subscribe(
                {
                    videoStackData?.bannerData = null
                },
                {
                    it.printStackTrace()
                }
            ))
    }

    fun likeButtonClicked(videoName: String, questionId: String, answerId: String, viewTime: Long, page: String) {
        onVideoLikeDislikeEvent(true, questionId)
        videoStackData?.let {
            if (it.isLiked) {
                it.likeCount = (it.likeCount - 1)
                it.isLiked = false
                _likeCountLiveData.value = Pair(it.likeCount.toString(), it.isLiked)
                likeVideo.execute(LikedDislikedTextSolutionInteractor.Param(videoName, questionId, answerId, viewTime.toString(), page, it.isLiked, "")).applyIoToMainSchedulerOnCompletable().subscribe()

            } else {
                it.isLiked = true
                it.likeCount = (it.likeCount.plus(1))
                _likeCountLiveData.value = Pair(it.likeCount.toString(), it.isLiked)
                likeVideo.execute(LikedDislikedTextSolutionInteractor.Param(videoName, questionId, answerId, viewTime.toString(), page, it.isLiked, "")).applyIoToMainSchedulerOnCompletable().subscribe()

            }
        }
    }

    fun disLikeButtonClicked(videoName: String, questionId: String, answerId: String, viewTime: Long, page: String, feedback: String) {
        onVideoLikeDislikeEvent(false, questionId)
        videoStackData?.let {
            if (it.isDisliked) {
                it.dislikesCount = (it.dislikesCount - 1)
                it.isDisliked = false
                _disLikeCountLiveData.value = Pair(it.dislikesCount.toString(), it.isDisliked)
                likeVideo.execute(LikedDislikedTextSolutionInteractor.Param(videoName, questionId, answerId, viewTime.toString(), page, it.isDisliked, feedback)).applyIoToMainSchedulerOnCompletable().subscribe()
            } else {
                it.isDisliked = true
                it.dislikesCount = (it.dislikesCount.plus(1))
                _disLikeCountLiveData.value = Pair(it.dislikesCount.toString(), it.isDisliked)
                likeVideo.execute(LikedDislikedTextSolutionInteractor.Param(videoName, questionId, answerId, viewTime.toString(), page, it.isDisliked, feedback)).applyIoToMainSchedulerOnCompletable().subscribe()
            }
        }
    }
    
    fun updateEngagementTime(viewId: String, isBack: String, engagementTime: String) {
        updateTextSolutionEngagementUseCase.execute(UpdateTextSolutionEngagementUseCase.Param(viewId, isBack, engagementTime, "")).applyIoToMainSchedulerOnCompletable().subscribe()
    }

    fun onVideoLikeDislikeEvent(isLiked: Boolean, questionId: String) {
        videoPageEventManager.onVideoLikeDislike(isLiked, questionId, TextSolutionActivity.TAG)
    }

    fun onVideoCommentEvent(questionId: String) {
        videoPageEventManager.onVideoComment(questionId, TextSolutionActivity.TAG)
    }

    fun onVideoShareEvent(questionId: String) {
        videoPageEventManager.onVideoShare(questionId, TextSolutionActivity.TAG)
    }

    fun publishEventWith(eventName: String) {
        videoPageEventManager.eventWith(eventName)
    }

    fun publishPlayVideoClickEvent(questionId: String, source: String) {
        videoPageEventManager.playVideoClick(questionId, source)
    }

    fun publishCameraButtonClickEvent(source: String) {
        videoPageEventManager.cameraButtonClicked(source)
    }


    fun sendEvent(event: String, param: HashMap<String, Any>) {
        videoPageEventManager.eventWith(event, param)
    }


}