package com.doubtnutapp.profile.uservideohistroy.ui

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.Constants
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.addtoplaylist.interactor.RemovePlaylistUseCase
import com.doubtnutapp.domain.addtoplaylist.interactor.SubmitPlayListsUseCase
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.domain.profile.watchedvideo.entities.WatchedVideoListEntity
import com.doubtnutapp.domain.profile.watchedvideo.interactor.GetUsersWatchedVideo
import com.doubtnutapp.domain.profile.watchedvideo.interactor.LikeWatchedVideo
import com.doubtnutapp.plus
import com.doubtnutapp.profile.uservideohistroy.mapper.WatchVideoMapper
import com.doubtnutapp.profile.uservideohistroy.model.WatchedVideo
import com.doubtnutapp.profile.uservideohistroy.model.WatchedVideoMetaInfo
import com.doubtnutapp.screennavigator.Screen
import com.doubtnutapp.screennavigator.TextSolutionScreen
import com.doubtnutapp.screennavigator.VideoScreen
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.utils.Event
import com.google.android.exoplayer2.util.Log
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class UserWatchedVideoViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable,
        private val likedMatchedQuestion: LikeWatchedVideo,
        private val getUsersWatchedVideo: GetUsersWatchedVideo,
        private val watchVideoMapper: WatchVideoMapper,
        private val whatsAppSharing: WhatsAppSharing,
        private val submitPlayListsUseCase: SubmitPlayListsUseCase,
        private val removePlaylistUseCase: RemovePlaylistUseCase
) : BaseViewModel(compositeDisposable) {

    private val whiteColorBg: String = "#FFFFFF"
    private val screenName: String = "history"


    private var isFirstPage = false

    private val _navigateScreenLiveData: MutableLiveData<Pair<Screen, Map<String, Any?>?>> = MutableLiveData()

    val navigateScreenLiveData: LiveData<Pair<Screen, Map<String, Any?>?>>
        get() = _navigateScreenLiveData

    private val _watchedVideoLiveData: MutableLiveData<Outcome<List<WatchedVideo>>> = MutableLiveData()

    val watchedVideoLiveData: LiveData<Outcome<List<WatchedVideo>>> get() = _watchedVideoLiveData


    val whatsAppShareableData: LiveData<Event<Triple<String?, String?, String?>>>
        get() = whatsAppSharing.whatsAppShareableData


    val showWhatsappProgressLiveData: LiveData<Boolean>
        get() = whatsAppSharing.showWhatsAppProgressLiveData

    private val _whatsAppShareProgressLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val whatsAppShareProgressLiveData: LiveData<Boolean>
        get() = _whatsAppShareProgressLiveData

    private val _isFirstPageEmptyLiveData: MutableLiveData<Outcome<List<WatchedVideoMetaInfo>>> = MutableLiveData()

    val isFirstPageEmptyLiveData: LiveData<Outcome<List<WatchedVideoMetaInfo>>>
        get() = _isFirstPageEmptyLiveData

    private val _addToPlayListLiveData: MutableLiveData<String> = MutableLiveData()

    val addToPlayListLiveData: LiveData<String>
        get() = _addToPlayListLiveData

    private val _onAddToWatchLater: MutableLiveData<Event<String>> = MutableLiveData()

    val onAddToWatchLater: LiveData<Event<String>>
        get() = _onAddToWatchLater

    fun getWatchedVideos(pageNumber: Int) {

        isFirstPage = pageNumber == 1

        _watchedVideoLiveData.value = Outcome.loading(true)
        compositeDisposable + getUsersWatchedVideo
                .execute(pageNumber)
                .applyIoToMainSchedulerOnSingle()
                .subscribe(this::onWatchedVideoResponseSuccess, this::onError)
    }

    fun handleAction(action: Any) {
        when (action) {
            is LikeVideo -> compositeDisposable + likedMatchedQuestion.execute(
                    LikeWatchedVideo.Param(action.videoId, screenName, action.isLiked)
            ).applyIoToMainSchedulerOnCompletable().subscribe()

            is AddToPlayList -> addToPlayList(action.videoId)
            is ShareOnWhatApp -> shareOnWhatsApp(action)
            is PlayVideo -> openVideoScreen(action)
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

    private fun addToPlayList(videoId: String) {
        _addToPlayListLiveData.value = videoId
    }

    private fun openVideoScreen(action: PlayVideo) {
        val argument = hashMapOf(
                Constants.PAGE to action.page,
                Constants.QUESTION_ID to action.videoId,
                Constants.PARENT_ID to "0"
        )

        val screen = if (action.resourceType == SOLUTION_RESOURCE_TYPE_VIDEO) VideoScreen else TextSolutionScreen

        _navigateScreenLiveData.value = Pair(screen, argument)

    }

    private fun onWatchedVideoResponseSuccess(watchedVideoListEntity: WatchedVideoListEntity) {
        _watchedVideoLiveData.value = Outcome.loading(false)
        val watchVideoList = watchedVideoListEntity.run {
            watchVideoMapper.map(watchedVideoListEntity)
        }

        if (isFirstPage && watchedVideoListEntity.noWatchedData != null) {
            if (watchedVideoListEntity.noWatchedData!!.isNotEmpty()) {
                _isFirstPageEmptyLiveData.value = Outcome.success(watchVideoList.nowatchedData!!)
                return
            }
        }

        if (watchVideoList.watchedVideo != null) {
            _watchedVideoLiveData.value = Outcome.success(watchVideoList.watchedVideo)
        }


    }

    private fun onError(error: Throwable) {
        Log.d("errorRequest", error.toString())
        _watchedVideoLiveData.value = Outcome.loading(false)
        _watchedVideoLiveData.value = if (error is HttpException) {
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
                com.doubtnutapp.Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    private fun shareOnWhatsApp(action: ShareOnWhatApp) {

        whatsAppSharing.shareOnWhatsApp(action)
    }

    override fun onCleared() {
        super.onCleared()
        whatsAppSharing.dispose()
    }


}