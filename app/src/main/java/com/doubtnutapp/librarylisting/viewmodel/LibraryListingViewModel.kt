package com.doubtnutapp.librarylisting.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.Constants
import com.doubtnutapp.Log
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.domain.newlibrary.entities.FilterEntity
import com.doubtnutapp.domain.newlibrary.entities.LibraryListingEntity
import com.doubtnutapp.domain.newlibrary.interactor.GetLibraryListingDataUseCase
import com.doubtnutapp.librarylisting.mapper.LibraryListingViewMapper
import com.doubtnutapp.librarylisting.model.FilterInfo
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.likeDislike.LikeDislikeVideo
import com.doubtnutapp.newlibrary.event.LibraryEventManager
import com.doubtnutapp.plus
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
class LibraryListingViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val likeDislikeVideo: LikeDislikeVideo,
    private val whatsAppSharing: WhatsAppSharing,
    private val libraryListingDataUseCase: GetLibraryListingDataUseCase,
    private val libraryListingViewMapper: LibraryListingViewMapper,
    private val libraryEventManager: LibraryEventManager
) : BaseViewModel(compositeDisposable) {

    private val _listingLiveData: MutableLiveData<Outcome<List<RecyclerViewItem>>> = MutableLiveData()

    val listingLiveData: LiveData<Outcome<List<RecyclerViewItem>>>
        get() = _listingLiveData

    var filterData: MutableList<FilterInfo>? = null

    var parentTitle: String = ""

    val showWhatsAppShareProgressBar = whatsAppSharing.showWhatsAppProgressLiveData

    val whatsAppShareableData: LiveData<Event<Triple<String?, String?, String?>>>
        get() = whatsAppSharing.whatsAppShareableData

    private val _addToPlayListLiveData: MutableLiveData<Event<String>> = MutableLiveData()

    val addToPlayListLiveData: LiveData<Event<String>>
        get() = _addToPlayListLiveData

    private fun startLoading() {
        _listingLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _listingLiveData.value = Outcome.loading(false)
    }

    fun fetchListingData(pageNumber: Int, id: String, packageDetailsId: String, source: String) {
        startLoading()
        compositeDisposable + libraryListingDataUseCase
                .execute(GetLibraryListingDataUseCase.Param(pageNumber, id, packageDetailsId, source))
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onSuccess, this::onError)
    }

    fun onSuccess(entity: LibraryListingEntity) {
        onFilterSuccess(entity.filterList)
        val libraryListingData = entity.item?.map {
            libraryListingViewMapper.map(it)
        }
        _listingLiveData.value = Outcome.success(libraryListingData.orEmpty())
        stopLoading()
    }

    private fun onFilterSuccess(filterEntities: List<FilterEntity>?) {
        filterData = filterEntities?.map { FilterInfo(it.id, it.title, it.isLast) }?.toMutableList()
    }

    private fun onError(error: Throwable) {
        stopLoading()
        _listingLiveData.value = if (error is HttpException) {
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

    fun handleAction(action: Any, page: String? = null) {
        when (action) {
            is OpenLibraryVideoPlayListScreen -> openPlayListScreen(action, page)
            is OpenWhatsapp -> openWhatsapp(action.externalUrl)
            is OpenPDFViewScreen -> openPDFViewerScreen(action)
            is OpenLibraryPlayListActivity -> openLibraryListingActivity(action, page)
            is LikeVideo ->
                likeDislikeVideo.likeDislikeVideo(action.videoId, "playlist", action.isLiked)
            is AddToPlayList -> addToPlayList(action.videoId)
            is ShareOnWhatApp -> shareOnWhatsApp(action)
            is PlayVideo -> openVideoScreen(action, page)
            is OpenPCPopup -> openUnlockPopup()
            is OpenWebView -> openWebView(action)
            is PublishEvent -> sendEvent(action.event)
        }
    }

    private fun sendEvent(analyticsEvent: AnalyticsEvent) {
        libraryEventManager.sendEvent(analyticsEvent.name, analyticsEvent.params)
    }

    private fun openUnlockPopup() {
        _navigateLiveData.value = Event(NavigationModel(PCUnlockScreen, null))
    }

    private fun addToPlayList(videoId: String) {
        _addToPlayListLiveData.value = Event(videoId)
    }


    private fun shareOnWhatsApp(action: ShareOnWhatApp) {
        whatsAppSharing.shareOnWhatsApp(action)
    }

    private fun openVideoScreen(action: PlayVideo, page: String? = null) {
        val screen = if (action.resourceType == SOLUTION_RESOURCE_TYPE_VIDEO) VideoScreen else TextSolutionScreen
        _navigateLiveData.value = Event(NavigationModel(screen, hashMapOf(
                Constants.PAGE to (page ?: action.page),
                Constants.QUESTION_ID to action.videoId,
                Constants.PARENT_ID to 0,
                Constants.PLAYLIST_ID to action.playlistId
        )))
    }

    private fun openPDFViewerScreen(action: OpenPDFViewScreen) {
        libraryEventManager.onLibraryPlaylistTabSelectedFromSetting("PDFs", parentTitle)
        _navigateLiveData.value = Event(NavigationModel(PDFViewerScreen, hashMapOf(
                SCREEN_NAV_PARAM_PDF_URL to action.pdfUrl
        )))
    }

    private fun openLibraryListingActivity(action: OpenLibraryPlayListActivity, page: String?) {
        libraryEventManager.onLibraryPlaylistTabSelectedFromSetting(action.playlistName, parentTitle)
        _navigateLiveData.value = Event(NavigationModel(LibraryPlayListScreen, hashMapOf(
                LibraryListingActivity.INTENT_EXTRA_PLAYLIST_ID to action.playlistId,
                LibraryListingActivity.INTENT_EXTRA_PACKAGE_ID to action.packageDetailsId.orEmpty(),
                LibraryListingActivity.INTENT_EXTRA_PLAYLIST_TITLE to action.playlistName,
                LibraryListingActivity.INTENT_EXTRA_PLAYLIST_PAGE to page.orEmpty()
        )))
    }

    private fun openWhatsapp(externalUrl: String) {
        _navigateLiveData.value = Event(NavigationModel(ExternalUrlScreen, hashMapOf(
                Constants.EXTERNAL_URL to externalUrl
        )))
    }

    private fun openPlayListScreen(action: OpenLibraryVideoPlayListScreen, page: String? = null) {
        libraryEventManager.onLibraryPlaylistTabSelectedFromSetting(action.playlistName, parentTitle)
        val args = hashMapOf(
                SCREEN_NAV_PARAM_PLAYLIST_ID to action.playlistId,
                SCREEN_NAV_PARAM_PLAYLIST_TITLE to (action.playlistName),
                Constants.PACKAGE_DETAIL_ID to action.packageDetailsId.orEmpty()
        )
        if (!page.isNullOrEmpty())
            args[Constants.PAGE] = page
        _navigateLiveData.value = Event(NavigationModel(LibraryVideoPlayListScreen, args))
    }

    private fun openWebView(action: OpenWebView) {
        libraryEventManager.onLibraryPlaylistTabSelectedFromSetting(action.title
                ?: "webView", parentTitle)
        val args = hashMapOf(
                Constants.EXTERNAL_URL to (action.webPageUrl ?: "")
        )
        _navigateLiveData.value = Event(NavigationModel(WebViewScreen, args))
    }

    fun onLibraryFilterClick(title: String, parentTitle: String) {
        libraryEventManager.onLibraryFilterClick(title, parentTitle)
    }

    fun sendWebViewerEvent() {
        libraryEventManager.sendEvent(EventConstants.EVENT_EASY_READER_WEBVIEWER_OPEN, hashMapOf(
                EventConstants.SOURCE to "library"
        ))
    }

    fun sendPdfDownloadEvent() {
        libraryEventManager.sendEvent(EventConstants.PDF_DOWNLOAD_CLICK, hashMapOf(
                EventConstants.SOURCE to "library"
        ),ignoreSnowplow = true)
    }

    override fun onCleared() {
        super.onCleared()
        whatsAppSharing.dispose()
    }
}