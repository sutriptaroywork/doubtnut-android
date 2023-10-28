package com.doubtnutapp.videoPage.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.*
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.VideoTagClick
import com.doubtnutapp.base.WatchLaterRequest
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.PdfUrlData
import com.doubtnutapp.data.remote.models.doubtfeed.DoubtFeedBanner
import com.doubtnutapp.data.remote.models.videopageplaylist.VideoPagePlaylist
import com.doubtnutapp.data.remote.repository.DoubtFeedRepository
import com.doubtnutapp.data.remote.repository.UserActivityRepository
import com.doubtnutapp.data.remote.repository.VideoPageRepository2
import com.doubtnutapp.domain.addtoplaylist.interactor.RemovePlaylistUseCase
import com.doubtnutapp.domain.addtoplaylist.interactor.SubmitPlayListsUseCase
import com.doubtnutapp.domain.similarVideo.interactor.GetRecommendedClassesUseCase
import com.doubtnutapp.domain.videoPage.entities.VideoDataEntity
import com.doubtnutapp.domain.videoPage.interactor.*
import com.doubtnutapp.resourcelisting.model.QuestionMetaDataModel
import com.doubtnutapp.screennavigator.LibraryVideoPlayListScreen
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.sharing.VIDEO_CHANNEL
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.similarVideo.model.SimilarVideoList
import com.doubtnutapp.similarVideo.model.SimilarWidgetViewItem
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.NetworkUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.videoPage.event.VideoPageEventManager
import com.doubtnutapp.videoPage.mapper.VideoMapper
import com.doubtnutapp.videoPage.mapper.VideoPagePlaylistMapper
import com.doubtnutapp.videoPage.model.VideoBottomBarData
import com.doubtnutapp.videoPage.model.ViewAnswerData
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.google.gson.JsonSyntaxException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.util.*
import javax.inject.Inject

class VideoPageViewModel @Inject constructor(
    private val likeVideo: LikedDislikedVideoInteractor,
    private val videoDataUseCase: GetVideoData,
    private val saveVideoInteractor: SaveVideoInteractor,
    private val getPreviousVideoInteractor: GetPreviousVideoInteractor,
    private val whatsappSharing: WhatsAppSharing,
    private val videoPageMapper: VideoMapper,
    private val videoPagePlaylistMapper: VideoPagePlaylistMapper,
    compositeDisposable: CompositeDisposable,
    private val videoPageEventManager: VideoPageEventManager,
    private val submitPlayListsUseCase: SubmitPlayListsUseCase,
    private val removePlaylistUseCase: RemovePlaylistUseCase,
    private val getVideoViewStackSizeUseCase: GetVideoViewStackSizeUseCase,
    private val userActivityRepository: UserActivityRepository,
    private val networkUtil: NetworkUtil,
    private val videoPageRepository2: VideoPageRepository2,
    private val doubtFeedRepository: DoubtFeedRepository,
    private val userPreference: UserPreference,
    private val recommendedClassesUseCase: GetRecommendedClassesUseCase
) : BaseViewModel(compositeDisposable) {

    companion object {
        private const val DEFAULT_RECOMMENDED_ITEM_POSITION = 0
    }

    private val whiteColorBg: String = "#FFFFFF"
    private var eventParamPage = ""
    private var eventParamParentId = ""

    var currentBottomSheetPlaylistVideoId: String? = null
    private val bottomSheetPlaylistVideoIdsStack = Stack<String>()

    var videoPlaylistQuestionId: String? = null

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

    private val _getVideoLiveData: MutableLiveData<Outcome<ViewAnswerData>> = MutableLiveData()

    val getVideoLiveData: LiveData<Outcome<ViewAnswerData>>
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

    private val _commentsCountLiveData: MutableLiveData<String> = MutableLiveData()

    val commentsCountLiveData: LiveData<String>
        get() = _commentsCountLiveData

    private var videoStackData: VideoDataEntity? = null

    private val _onAddToWatchLater: MutableLiveData<SingleEvent<String>> = MutableLiveData()

    val onAddToWatchLater: LiveData<SingleEvent<String>>
        get() = _onAddToWatchLater

    private val _expandLandscapeBottomSheet: MutableLiveData<Boolean> = MutableLiveData()
    var isLandscapeFragmentExpanded: Boolean = false

    val expandLandscapeBottomSheet: LiveData<Boolean>
        get() = _expandLandscapeBottomSheet

    var firstSimilarPipPlayableVideo: MutableLiveData<SimilarVideoList?> = MutableLiveData()

    private val _pdfUrlDataLiveData = MutableLiveData<PdfUrlData?>()

    val pdfUrlDataLiveData: LiveData<PdfUrlData?>
        get() = _pdfUrlDataLiveData

    private val _videoPlaylistLiveData = MutableLiveData<VideoPagePlaylist?>()
    val videoPlaylistLiveData: LiveData<VideoPagePlaylist?>
        get() = _videoPlaylistLiveData

    private inline val videoPlaylist: List<QuestionMetaDataModel>
        get() = videoPlaylistLiveData.value?.similarQuestions?.filterIsInstance<QuestionMetaDataModel>()
            ?: emptyList()

    private val _autoplayVideoBottomBarLiveData = MutableLiveData<VideoBottomBarData?>()
    val autoplayVideoBottomBarLiveData: LiveData<VideoBottomBarData?>
        get() = _autoplayVideoBottomBarLiveData

    private val _recommendedClassLiveData: MutableLiveData<SimilarWidgetViewItem> =
        MutableLiveData()
    val recommendedClassesData: LiveData<SimilarWidgetViewItem>
        get() = _recommendedClassLiveData

    private val bottomSheetType: String
        get() = _videoPlaylistLiveData.value?.bottomSheetType.orEmpty()

    private val _doubtFeedBannerLiveData = MutableLiveData<DoubtFeedBanner>()
    val doubtFeedBannerLiveData: LiveData<DoubtFeedBanner>
        get() = _doubtFeedBannerLiveData

    fun setLandscapeBottomSheetExpandedState(toExpand: Boolean) {
        isLandscapeFragmentExpanded = toExpand
        _expandLandscapeBottomSheet.value = toExpand
    }

    fun addToWatchLater(id: String) {
        submitPlayLists(id, mutableListOf("1"))
    }

    // D0 user data
    var hideBottomNavigation: Boolean? = null
    var backPressBottomSheetDeeplink: String? = null

    private fun submitPlayLists(id: String, playListIds: List<String>) {
        compositeDisposable + submitPlayListsUseCase
            .execute(Pair(id, playListIds))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _onAddToWatchLater.value = SingleEvent(id)
            }, {
            })
    }

    fun removeFromPlaylist(id: String, playListId: String) {
        compositeDisposable + removePlaylistUseCase
            .execute(Pair(id, playListId))
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({})
    }

    fun shareOnWhatsApp(
        featureType: String,
        thumbnailImage: String,
        controlParams: HashMap<String, String>,
        questionId: String,
        sharingMessage: String
    ) {
        videoStackData?.let {
            it.shareCount = it.shareCount.plus(1)
            _shareCountLiveData.value = it.shareCount.toString()
        }

        whatsappSharing.shareOnWhatsApp(
            ShareOnWhatApp(
                VIDEO_CHANNEL,
                featureType,
                thumbnailImage,
                controlParams,
                whiteColorBg,
                sharingMessage,
                questionId
            )
        )
    }

    // Common methods to be used by all fragments on video page providing video share functionality
    fun shareOnWhatsApp(action: ShareOnWhatApp) {
        whatsappSharing.shareOnWhatsApp(action)
    }

    fun viewVideo(
        questionId: String, playListId: String?, mcId: String?, page: String, mcClass: String,
        referredStudentId: String?, parentId: String?, isFromTopicBooster: Boolean,
        isFromSmartContent: Boolean, youtube_id: String?, ocr: String?, addToStack: Boolean,
        isVideoInPipMode: Boolean, isFilter: Boolean, parentPage: String? = null
    ) {
        if (addToStack) {
            addVideoData()
        }
        eventParamPage = page
        if (parentId != null) {
            eventParamParentId = parentId
        }
        _getVideoLiveData.value = Outcome.loading(true)
        compositeDisposable.add(
            videoDataUseCase
                .execute(
                    GetVideoData.Param(
                        questionId = questionId,
                        playListId = playListId,
                        mcId = mcId,
                        page = page,
                        mcClass = mcClass,
                        referredStudentId = referredStudentId,
                        parentId = parentId,
                        isFromTopicBooster = isFromTopicBooster,
                        token = Utils.decrypt(defaultPrefs().getString(Constants.GAME_TOKEN, ""))
                            .orEmpty(),
                        youtube_id = youtube_id,
                        ocr = ocr,
                        isInPipMode = isVideoInPipMode,
                        isNetworkSlow = !networkUtil.isConnectionFast(),
                        isFilter = isFilter,
                        parentPage = parentPage
                    )
                )
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    onVideoPageViewSuccess(it, isFromSmartContent)
                }, this::onVideoPageViewError)
        )
    }

    private fun onVideoPageViewSuccess(
        videoDataEntity: VideoDataEntity,
        isFromSmartContent: Boolean
    ) {
        videoDataEntity.apply {
            this.isFromSmartContent = isFromSmartContent
        }
        hideBottomNavigation = videoDataEntity.hideBottomNav
        backPressBottomSheetDeeplink = videoDataEntity.backPressBottomSheetDeeplink
        _getVideoLiveData.value = Outcome.loading(false)
        val matchResult = videoDataEntity.run {
            videoPageMapper.map(videoDataEntity)
        }
        _getVideoLiveData.value = Outcome.success(matchResult)
        videoStackData = videoDataEntity
        _disLikeCountLiveData.value =
            Pair(matchResult.dislikesCount.toString(), matchResult.isDisliked)
        _likeCountLiveData.value = Pair(matchResult.likeCount.toString(), matchResult.isLiked)
        _shareCountLiveData.value = videoStackData?.shareCount.toString()
        _commentsCountLiveData.value = videoStackData?.commentCount.toString()
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
                || error is IllegalArgumentException
            ) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    fun getPreviousVideo(page: String) {
        if (
            (page == Constants.PAGE_MPVP_BOTTOM_SHEET || page == Constants.PAGE_SRP
                    ) && bottomSheetPlaylistVideoIdsStack.isNotEmpty()
        ) {
            currentBottomSheetPlaylistVideoId = bottomSheetPlaylistVideoIdsStack.pop()
        }
        _getVideoLiveData.value = Outcome.loading(true)
        compositeDisposable.add(
            getPreviousVideoInteractor
                .execute(GetPreviousVideoInteractor.None())
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::videoOnStackSuccess, this::noVideoOnStack)
        )
    }

    private fun videoOnStackSuccess(videoDataEntity: VideoDataEntity) {
        _getVideoLiveData.value = Outcome.loading(false)
        val matchResult = videoDataEntity.run {
            videoPageMapper.map(videoDataEntity)
        }
        videoStackData = videoDataEntity
        _getVideoLiveData.value = Outcome.success(matchResult)
        _disLikeCountLiveData.value =
            Pair(matchResult.dislikesCount.toString(), matchResult.isDisliked)
        _likeCountLiveData.value = Pair(matchResult.likeCount.toString(), matchResult.isLiked)
        _shareCountLiveData.value = matchResult.shareCount.toString()
        _commentsCountLiveData.value = matchResult.commentCount.toString()

    }

    private fun noVideoOnStack(error: Throwable) {
        _getVideoLiveData.value = Outcome.loading(false)
        if (error == EmptyStackException()) {
            _emptyStackLiveData.value = true
        }
    }

    private fun addVideoData() {
        videoStackData?.let {
            compositeDisposable.add(
                saveVideoInteractor
                    .execute(SaveVideoInteractor.Param(it))
                    .applyIoToMainSchedulerOnCompletable()
                    .subscribe()
            )
        }

    }

    fun likeButtonClicked(
        videoName: String,
        questionId: String,
        answerId: String,
        viewTime: Long,
        page: String,
        viewId: String
    ) {
        onVideoLikeDislikeEvent(true, questionId)
        videoStackData?.let {
            if (it.isLiked) {
                it.likeCount = (it.likeCount - 1)
                it.isLiked = false
                _likeCountLiveData.value = Pair(it.likeCount.toString(), it.isLiked)
                likeVideo.execute(
                    LikedDislikedVideoInteractor.Param(
                        videoName,
                        questionId,
                        answerId,
                        viewTime.toString(),
                        page,
                        it.isLiked,
                        "",
                        viewId
                    )
                ).applyIoToMainSchedulerOnCompletable().subscribe()

            } else {
                it.isLiked = true
                it.likeCount = (it.likeCount.plus(1))
                _likeCountLiveData.value = Pair(it.likeCount.toString(), it.isLiked)
                likeVideo.execute(
                    LikedDislikedVideoInteractor.Param(
                        videoName,
                        questionId,
                        answerId,
                        viewTime.toString(),
                        page,
                        it.isLiked,
                        "",
                        viewId
                    )
                ).applyIoToMainSchedulerOnCompletable().subscribe()

            }
        }
    }

    fun disLikeButtonClicked(
        videoName: String,
        questionId: String,
        answerId: String,
        viewTime: Long,
        page: String,
        feedback: String,
        viewId: String
    ) {
        onVideoLikeDislikeEvent(false, questionId)
        videoStackData?.let {
            if (it.isDisliked) {
                it.dislikesCount = (it.dislikesCount - 1)
                it.isDisliked = false
                _disLikeCountLiveData.value = Pair(it.dislikesCount.toString(), it.isDisliked)
                likeVideo.execute(
                    LikedDislikedVideoInteractor.Param(
                        videoName,
                        questionId,
                        answerId,
                        viewTime.toString(),
                        page,
                        it.isDisliked,
                        feedback,
                        viewId
                    )
                ).applyIoToMainSchedulerOnCompletable().subscribe()
            } else {
                it.isDisliked = true
                it.dislikesCount = (it.dislikesCount.plus(1))
                _disLikeCountLiveData.value = Pair(it.dislikesCount.toString(), it.isDisliked)
                likeVideo.execute(
                    LikedDislikedVideoInteractor.Param(
                        videoName,
                        questionId,
                        answerId,
                        viewTime.toString(),
                        page,
                        it.isDisliked,
                        feedback,
                        viewId
                    )
                ).applyIoToMainSchedulerOnCompletable().subscribe()
            }
        }
    }

    fun executeIfVideoViewStackIsEmpty(body: () -> Unit) {
        getVideoViewStackSizeUseCase.execute()
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                if (it == 0) {
                    body()
                }
            }, {})
    }

    fun getPdfUrlData(
        entityId: String,
        limit: Int,
        title: String,
        fileName: String,
        persist: Boolean
    ) {
        compositeDisposable.add(
            DataHandler.INSTANCE.pdfRepository
                .getPdfUrl(entityId, limit, title, fileName, persist)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _pdfUrlDataLiveData.value = it
                }, {
                    _pdfUrlDataLiveData.value = null
                })
        )
    }

    fun getVideoPlaylist(questionId: String) {
        viewModelScope.launch {
            videoPageRepository2.getVideoPlaylist(questionId)
                .map { videoPagePlaylistMapper.map(it.data) }
                .catch { e ->
                    e.printStackTrace()
                    _videoPlaylistLiveData.value = null
                }
                .collect { videoPagePlaylist ->
                    videoPagePlaylist.similarQuestions.forEach {
                        if (it is QuestionMetaDataModel) {
                            it.isFullWidthCard = true
                            it.heightRatio = 6f / 16
                            it.ocrTextFontSize = 14
                        }
                    }
                    _videoPlaylistLiveData.value = videoPagePlaylist
                    videoPagePlaylist.similarQuestions.firstOrNull { it is QuestionMetaDataModel && it.questionId == questionId }
                        ?.let {
                            if (it is QuestionMetaDataModel) {
                                currentBottomSheetPlaylistVideoId = it.questionId
                                changeCurrentPlaylistPlayingVideo(currentBottomSheetPlaylistVideoId)
                                updateAutoplayPlaylistVideoData()
                            }
                        }
                }
        }
    }

    /**
     * @param currentVideoId should always be null. It will be non-null only in the special case
     * of this method getting called as a result when last bottom sheet video is loaded on backpress
     * @param addToBottomSheetStack will be false only in the special case of this method getting
     * called as a result when last bottom sheet video is loaded on backpress
     */
    fun changeCurrentPlaylistPlayingVideo(
        newVideoId: String?,
        currentVideoId: String? = null,
        addToBottomSheetStack: Boolean = true
    ): Pair<Int, Int> {
        val tempCurrentVideoId = currentVideoId ?: currentBottomSheetPlaylistVideoId
        videoPlaylist.firstOrNull { it.questionId == tempCurrentVideoId }?.label = null
        videoPlaylist.firstOrNull { it.questionId == tempCurrentVideoId }?.isPlaying = null
        videoPlaylist.firstOrNull { it.questionId == newVideoId }?.apply {
            label = "Playing"
            isPlaying = true
            labelColor = "#3bb54a"
        }

        if (addToBottomSheetStack) {
            bottomSheetPlaylistVideoIdsStack.push(currentBottomSheetPlaylistVideoId)
        }
        currentBottomSheetPlaylistVideoId = newVideoId

        updateAutoplayPlaylistVideoData()

        return Pair(
            videoPlaylistLiveData.value?.similarQuestions?.indexOfFirst {
                it is QuestionMetaDataModel && it.questionId == tempCurrentVideoId
            } ?: -1,
            videoPlaylistLiveData.value?.similarQuestions?.indexOfFirst {
                it is QuestionMetaDataModel && it.questionId == newVideoId
            } ?: -1
        )
    }

    fun clearBottomPlaylistVideoStack() {
        bottomSheetPlaylistVideoIdsStack.clear()
        currentBottomSheetPlaylistVideoId = null
        videoPlaylistQuestionId = null
    }

    fun isLastPlaylistVideo(): Boolean {
        return videoPlaylist.isEmpty() || videoPlaylist.last().questionId == currentBottomSheetPlaylistVideoId
    }

    private fun updateAutoplayPlaylistVideoData() {
        videoPlaylist.indexOfFirst { it.questionId == currentBottomSheetPlaylistVideoId }
            .takeIf { it != -1 }
            ?.let { index ->
                videoPlaylist.getOrNull(index + 1)?.let { model ->
                    _autoplayVideoBottomBarLiveData.value = VideoBottomBarData(
                        questionId = model.questionId,
                        questionMeta = model.questionMeta,
                        ocrText = if (model.ocrText?.contains("<math") == true) model.question else model.ocrText,
                        position = index + 1
                    )
                }
            }
    }

    fun getDoubtFeedVideoBanner(chapter: String) {
        viewModelScope.launch {
            doubtFeedRepository.getDoubtFeedVideoBanner(chapter)
                .catch { }
                .collect {
                    _doubtFeedBannerLiveData.value = it.data
                }
        }
    }

    fun storeNcertVideoWatchCoreAction() {
        userActivityRepository.setCoreActionStatusInPref(CoreActions.NCERT_VIDEO_WATCH, true)
    }

    private fun onVideoLikeDislikeEvent(isLiked: Boolean, questionId: String) {
        videoPageEventManager.onVideoLikeDislike(isLiked, questionId, VideoPageActivity.TAG)
    }

    fun onVideoCommentEvent(questionId: String) {
        videoPageEventManager.onVideoComment(questionId, VideoPageActivity.TAG)
    }

    fun onVideoShareEvent(questionId: String) {
        videoPageEventManager.onVideoShare(questionId, VideoPageActivity.TAG)
    }

    fun publishEventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        videoPageEventManager.eventWith(eventName, ignoreSnowplow)
    }

    fun publishPlayVideoClickEvent(questionId: String, source: String) {
        videoPageEventManager.playVideoClick(questionId, source)
    }

    fun publishCameraButtonClickEvent(source: String) {
        videoPageEventManager.cameraButtonClicked(source)
    }

    fun handleAction(action: Any, playListId: String, page: String) {
        when (action) {
            is VideoTagClick -> {
                openViewPlayListScreen(action.tagName, action.questionId, playListId)
                sendEvent(
                    EventConstants.EVENT_NAME_VIDEO_TAG_CLICK,
                    hashMapOf<String, Any>().apply {
                        put(Constants.ITEM, action.tagName)
                    }, ignoreSnowplow = true
                )
            }
            is WatchLaterRequest -> {
                addToWatchLater(action.id)
            }
            is ShareOnWhatApp -> {
                shareOnWhatsApp(action)
            }
        }
    }

    private fun openViewPlayListScreen(tagName: String, questionId: String, playListId: String) {
        _navigateLiveData.value =
            Event(NavigationModel(LibraryVideoPlayListScreen, hashMapOf<String, Any>().apply {
                put(Constants.PLAYLIST_ID, playListId)
                put(Constants.PLAYLIST_TITLE, tagName)
                put(Constants.QUESTION_ID, questionId)
                put(Constants.VIDEO_TAG_NAME, tagName)
                put(Constants.IS_FROM_VIDEO_TAG, true)
            }))
    }

    fun getRecommendedClasses(questionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = recommendedClassesUseCase.execute(questionId)
                _recommendedClassLiveData.postValue(SimilarWidgetViewItem(response.popularCourses))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendEvent(
        event: String, param: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false
    ) {
        videoPageEventManager.eventWith(event, param, ignoreSnowplow)
    }

    fun sendBottomSheetEvent(
        eventName: String,
        params: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false
    ) {
        params[EventConstants.BOTTOM_SHEET_TYPE] = bottomSheetType
        sendEvent(eventName, params, ignoreSnowplow = ignoreSnowplow)
    }

    fun postEvent(paramsMap: HashMap<String, Any>) {
        compositeDisposable + DataHandler.INSTANCE
            .courseRepository
            .postPaidContentEvent(paramsMap).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).map { it.data }.subscribeToSingle({

            }, {
            })
    }

    fun isDoubtFeed2Enabled(): Boolean = getDoubtFeed2Data() != null

    private fun getDoubtFeed2Data() = userPreference.getDoubtFeed2Data()
}