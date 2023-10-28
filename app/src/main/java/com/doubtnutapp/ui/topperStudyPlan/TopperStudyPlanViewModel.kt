package com.doubtnutapp.ui.topperStudyPlan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.Constants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.PlayVideo
import com.doubtnutapp.base.PublishSnowplowEvent
import com.doubtnutapp.base.WatchLaterRequest
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.remote.repository.TopperStudyPlanRepository
import com.doubtnutapp.domain.addtoplaylist.interactor.RemovePlaylistUseCase
import com.doubtnutapp.domain.addtoplaylist.interactor.SubmitPlayListsUseCase
import com.doubtnutapp.plus
import com.doubtnutapp.resourcelisting.ui.ResourceListingFragment
import com.doubtnutapp.screennavigator.Screen
import com.doubtnutapp.screennavigator.VideoScreen
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.utils.Event
import com.doubtnutapp.videoPage.event.VideoPageEventManager
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class TopperStudyPlanViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable,
        private val whatsAppSharing: WhatsAppSharing,
        private val removePlaylistUseCase: RemovePlaylistUseCase,
        private val videoPageEventManager: VideoPageEventManager,
        private val analyticsPublisher: AnalyticsPublisher,
        private val submitPlayListsUseCase: SubmitPlayListsUseCase,
        private val topperStudyPlanRepository: TopperStudyPlanRepository)
    : BaseViewModel(compositeDisposable) {

    private val _navigateScreenLiveData: MutableLiveData<Pair<Screen, Map<String, Any?>?>> = MutableLiveData()

    val navigateScreenLiveData: LiveData<Pair<Screen, Map<String, Any?>?>>
        get() = _navigateScreenLiveData

    private val _onAddToWatchLater: MutableLiveData<Event<String>> = MutableLiveData()

    val onAddToWatchLater: LiveData<Event<String>>
        get() = _onAddToWatchLater

    val showWhatsAppShareProgressBar = whatsAppSharing.showWhatsAppProgressLiveData

    val whatsAppShareableData: LiveData<Event<Triple<String?, String?, String?>>>
        get() = whatsAppSharing.whatsAppShareableData

    fun fetchPersonalizedData() =
            topperStudyPlanRepository.fetchPersonalizedData()

    fun fetchChapterDetailsData(questionId: Long, isNextChapter: Boolean) =
            topperStudyPlanRepository.fetchChapterDetailsData(questionId, isNextChapter)

    fun handleAction(action: Any) {
        when (action) {
            is WatchLaterRequest -> addToWatchLater(action.id)
            is ShareOnWhatApp -> shareOnWhatsApp(action)
            is PlayVideo -> openVideoScreen(action)
            is PublishSnowplowEvent -> performClickAction(action.event)
            else -> {
            }
        }
    }

    private fun performClickAction(event: StructuredEvent) {
        analyticsPublisher.publishEvent(event)
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

    private fun shareOnWhatsApp(action: ShareOnWhatApp) {
        whatsAppSharing.shareOnWhatsApp(action)
    }

    private fun openVideoScreen(action: PlayVideo) {
        publishPlayVideoClickEvent(action.videoId, ResourceListingFragment.TAG)
        _navigateScreenLiveData.value = Pair(VideoScreen, hashMapOf(
                Constants.PAGE to action.page,
                Constants.QUESTION_ID to action.videoId,
                Constants.PARENT_ID to 0,
                Constants.PLAYLIST_ID to action.playlistId
        ))
    }

    private fun publishPlayVideoClickEvent(questionId: String, source: String) {
        videoPageEventManager.playVideoClick(questionId, source)
    }

}