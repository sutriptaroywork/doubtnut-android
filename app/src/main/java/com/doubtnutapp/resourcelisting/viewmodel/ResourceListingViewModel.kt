package com.doubtnutapp.resourcelisting.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.Constants
import com.doubtnutapp.Log
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.addtoplaylist.interactor.RemovePlaylistUseCase
import com.doubtnutapp.domain.addtoplaylist.interactor.SubmitPlayListsUseCase
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.domain.newlibrary.entities.LibraryHistoryEntity
import com.doubtnutapp.domain.newlibrary.interactor.PutLibraryHistoryUseCase
import com.doubtnutapp.domain.resourcelisting.entities.ResourceListingEntity
import com.doubtnutapp.domain.resourcelisting.interactor.GetResourceListingForVideoTagUseCase
import com.doubtnutapp.domain.resourcelisting.interactor.GetResourceListingUseCase
import com.doubtnutapp.likeDislike.LikeDislikeVideo
import com.doubtnutapp.plus
import com.doubtnutapp.resourcelisting.mapper.ResourceListingMapper
import com.doubtnutapp.resourcelisting.model.PlayListMetaInfoDataModel
import com.doubtnutapp.resourcelisting.model.ResourceListingData
import com.doubtnutapp.resourcelisting.ui.ResourceListingFragment
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.utils.Event
import com.doubtnutapp.videoPage.event.VideoPageEventManager
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
class ResourceListingViewModel @Inject constructor(
    private val likeDislikeVideo: LikeDislikeVideo,
    private val whatsAppSharing: WhatsAppSharing,
    private val getResourceListingUseCase: GetResourceListingUseCase,
    private val getResourceListingForVideoTagUseCase: GetResourceListingForVideoTagUseCase,
    private val resourceListingMapper: ResourceListingMapper,
    private val putLibraryHistoryUseCase: PutLibraryHistoryUseCase,
    compositeDisposable: CompositeDisposable,
    private val videoPageEventManager: VideoPageEventManager,
    private val submitPlayListsUseCase: SubmitPlayListsUseCase,
    private val removePlaylistUseCase: RemovePlaylistUseCase
) : BaseViewModel(compositeDisposable) {

    private val screenName: String = "playlist"
    private var isFirstPage = false

    private val _navigateScreenLiveData: MutableLiveData<Pair<Screen, Map<String, Any?>?>> =
        MutableLiveData()

    val navigateScreenLiveData: LiveData<Pair<Screen, Map<String, Any?>?>>
        get() = _navigateScreenLiveData

    val showWhatsAppShareProgressBar = whatsAppSharing.showWhatsAppProgressLiveData

    val whatsAppShareableData: LiveData<Event<Triple<String?, String?, String?>>>
        get() = whatsAppSharing.whatsAppShareableData

    private val _getVideoPlaylistData: MutableLiveData<Outcome<ResourceListingData>> =
        MutableLiveData()

    val getVideoPlaylistData: LiveData<Outcome<ResourceListingData>>
        get() = _getVideoPlaylistData

    private val _addToPlayListLiveData: MutableLiveData<String> = MutableLiveData()

    val addToPlayListLiveData: LiveData<String>
        get() = _addToPlayListLiveData

    private val _isFirstPageEmptyLiveData: MutableLiveData<Outcome<List<PlayListMetaInfoDataModel>>> =
        MutableLiveData()

    val isFirstPageEmptyLiveData: LiveData<Outcome<List<PlayListMetaInfoDataModel>>>
        get() = _isFirstPageEmptyLiveData

    private val _eventLiveData: MutableLiveData<String> = MutableLiveData()

    val eventLiveData: LiveData<String>
        get() = _eventLiveData

    private val _onAddToWatchLater: MutableLiveData<Event<String>> = MutableLiveData()

    val onAddToWatchLater: LiveData<Event<String>>
        get() = _onAddToWatchLater

    fun handleAction(action: Any, page: String?) {
        when (action) {
            is LikeVideo ->
                likeDislikeVideo.likeDislikeVideo(action.videoId, screenName, action.isLiked)
            is AddToPlayList -> addToPlayList(action.videoId)
            is ShareOnWhatApp -> shareOnWhatsApp(action)
            is PlayVideo -> openVideoScreen(action, page)
            is OpenWhatsapp -> openWhatsapp(action.externalUrl)
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
        sendEvent(EventConstants.EVENT_WHATSAPP_CARD_CLICK + "videoPlayList")
        val args = hashMapOf(
            Constants.EXTERNAL_URL to externalUrl
        )
        _navigateLiveData.value = Event(NavigationModel(ExternalUrlScreen, args))
    }

    private fun addToPlayList(videoId: String) {
        _addToPlayListLiveData.value = videoId
    }

    private fun shareOnWhatsApp(action: ShareOnWhatApp) {
        whatsAppSharing.shareOnWhatsApp(action)
    }

    private fun openVideoScreen(action: PlayVideo, page: String? = null) {
        publishPlayVideoClickEvent(action.videoId, ResourceListingFragment.TAG)
        val screen =
            if (action.resourceType == SOLUTION_RESOURCE_TYPE_VIDEO) VideoScreen else TextSolutionScreen
        _navigateScreenLiveData.value = Pair(
            screen, hashMapOf(
                Constants.PAGE to (page ?: action.page),
                Constants.QUESTION_ID to action.videoId,
                Constants.PARENT_ID to 0,
                Constants.PLAYLIST_ID to action.playlistId
            )
        )
    }

    fun viewPlayList(
        page: Int,
        playlistId: String,
        autoPlay: Boolean,
        packageDetailsId: String?,
        questionIds: List<String>?,
    ) {
        isFirstPage = page == 1
        val allowAutoPlay = autoPlay && isFirstPage
        _getVideoPlaylistData.value = Outcome.loading(true)
        compositeDisposable.add(
            getResourceListingUseCase
                .execute(
                    GetResourceListingUseCase.Param(
                        page = page,
                        playlistId = playlistId,
                        packageDetailsId = packageDetailsId,
                        autoPlayData = allowAutoPlay,
                        questionIds = questionIds
                    )
                )
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({ onSuccess(it) }, this::onError)
        )
    }

    fun viewPlayListForVideoTag(
        page: Int,
        tagName: String,
        questionId: String,
        playlistId: String
    ) {
        isFirstPage = page == 1
        _getVideoPlaylistData.value = Outcome.loading(true)
        compositeDisposable.add(getResourceListingForVideoTagUseCase
            .execute(
                GetResourceListingForVideoTagUseCase.Param(
                    page,
                    tagName,
                    questionId,
                    playlistId
                )
            )
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({ onSuccess(it) }, this::onError)
        )
    }

    private fun onSuccess(videoPlaylistEntity: ResourceListingEntity) {
        _getVideoPlaylistData.value = Outcome.loading(false)
        var selectedItemIndex = 0
        val libraryActivitydata = resourceListingMapper.map(videoPlaylistEntity).run {
            copy(selectedContentIndex = selectedItemIndex)
        }
        if (isFirstPage && videoPlaylistEntity.metaInfo != null) {
            _isFirstPageEmptyLiveData.value = Outcome.success(libraryActivitydata.metaInfo!!)
        } else {
            _getVideoPlaylistData.value = Outcome.success(libraryActivitydata)
        }
    }

    private fun onError(error: Throwable) {
        _getVideoPlaylistData.value = Outcome.loading(false)
        _getVideoPlaylistData.value = if (error is HttpException) {
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

    fun putLibraryHistory(libraryHistoryEntity: LibraryHistoryEntity) {
        compositeDisposable + putLibraryHistoryUseCase.execute(libraryHistoryEntity)
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({})
    }

    private fun sendEvent(eventName: String) {
        _eventLiveData.value = eventName
    }

    fun publishPlayVideoClickEvent(questionId: String, source: String) {
        videoPageEventManager.playVideoClick(questionId, source)
    }

}