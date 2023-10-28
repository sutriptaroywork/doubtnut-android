package com.doubtnutapp.newglobalsearch.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Constants
import com.doubtnutapp.base.*
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.newglobalsearch.model.SearchPlaylistViewItem
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SearchFragmentViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _recentSearchClicked = MutableLiveData<Pair<String,Boolean>>()

    val recentSearchClicked: LiveData<Pair<String,Boolean>>
        get() = _recentSearchClicked

    private val _topTagSubjectClicked = MutableLiveData<NewTrendingSubjectClicked>()

    val topTagSubjectClicked: LiveData<NewTrendingSubjectClicked>
        get() = _topTagSubjectClicked

    private val _postSearchClicked = MutableLiveData<SearchPlaylistViewItem>()

    val postSearchClicked: LiveData<SearchPlaylistViewItem>
        get() = _postSearchClicked

    private val _postSearchVipItemClicked = MutableLiveData<SearchPlaylistViewItem>()

    val postSearchVipItemClicked: LiveData<SearchPlaylistViewItem>
        get() = _postSearchVipItemClicked

    private val _sendEventLiveData = MutableLiveData<Any>()

    val sendEventLiveData: LiveData<Any>
        get() = _sendEventLiveData

    private val _sendToTabLiveData = MutableLiveData<Any>()

    val sendToTabLiveData: LiveData<Any>
        get() = _sendToTabLiveData

    fun handleAction(action: Any) {
        when (action) {
            is TrendingRecentSearchItemClicked -> _recentSearchClicked.value = Pair(action.text,action.isRecentSearch)

            is NewTrendingSubjectClicked -> {
                _sendEventLiveData.value = action
                _topTagSubjectClicked.value = action
            }

            is SearchPlaylistClicked -> _postSearchClicked.value = action.playlist

            is OpenLibraryVideoPlayListScreen -> openPlayListScreen(action)

            is OpenLibraryPlayListActivity -> openLibraryListingActivity(action)

            is OpenPDFViewScreen -> openPDFViewerScreen(action)

            is SearchPlaylistClickedEvent -> _sendEventLiveData.value = action

            is TrendingPlaylistClicked -> _sendEventLiveData.value = action

            is TrendingPlaylistMongoEvent -> _sendEventLiveData.value = action

            is PlayVideo -> openVideoScreen(action)

            is SearchVipPlaylistClicked -> _postSearchVipItemClicked.value = action.playlist

            is NewTrendingSearchClicked -> _sendEventLiveData.value = action

            is NewRecentSearchClicked -> _sendEventLiveData.value = action

            is NewTrendingRecentDoubtClicked -> _sendEventLiveData.value = action

            is NewTrendingMostWatchedClicked -> _sendEventLiveData.value = action

            is NewTrendingBookClicked -> _sendEventLiveData.value = action

            is NewTrendingPdfClicked -> _sendEventLiveData.value = action

            is NewTrendingExamPaperClicked -> _sendEventLiveData.value = action

            is TrendingBookClicked -> _sendEventLiveData.value = action

            is TrendingCourseClicked -> _sendEventLiveData.value = action

            is SeeAllSearchResults -> _sendToTabLiveData.value = action

            is CourseBannerClicked -> _sendEventLiveData.value = action

            is AdvancedFilterClicked -> _sendEventLiveData.value = action

            is UpcomingLiveVideo -> handleUpcomingVideo(action)
        }
    }

    private fun openVideoScreen(action: PlayVideo) {
        val screen = when (action.resourceType) {
            SOLUTION_RESOURCE_TYPE_VIDEO -> VideoScreen
            "youtube" -> VideoYouTubeScreen
            "liveclass" -> LiveClassesScreen
            "livevideo" -> VideoScreen
            else -> TextSolutionScreen
        }
        _navigateLiveData.value = Event(NavigationModel(screen, hashMapOf(
                Constants.PAGE to action.page,
                Constants.QUESTION_ID to action.videoId,
                Constants.PARENT_ID to 0,
                Constants.PLAYLIST_ID to action.playlistId
        )))
    }

    private fun openPlayListScreen(action: OpenLibraryVideoPlayListScreen) {
        _navigateLiveData.value = Event(NavigationModel(LibraryVideoPlayListScreen, hashMapOf(
                SCREEN_NAV_PARAM_PLAYLIST_ID to action.playlistId,
                SCREEN_NAV_PARAM_PLAYLIST_TITLE to (action.playlistName),
                Constants.PAGE to Constants.PAGE_SEARCH_SRP
        )))
    }

    private fun openLibraryListingActivity(action: OpenLibraryPlayListActivity) {
        _navigateLiveData.value = Event(NavigationModel(LibraryPlayListScreen, hashMapOf(
                LibraryListingActivity.INTENT_EXTRA_PLAYLIST_ID to action.playlistId,
                LibraryListingActivity.INTENT_EXTRA_PACKAGE_ID to action.packageDetailsId.orEmpty(),
                LibraryListingActivity.INTENT_EXTRA_PLAYLIST_TITLE to action.playlistName,
                LibraryListingActivity.INTENT_EXTRA_PLAYLIST_PAGE to Constants.PAGE_SEARCH_SRP
        )))
    }

    private fun openPDFViewerScreen(action: OpenPDFViewScreen) {
        _navigateLiveData.value = Event(NavigationModel(PDFViewerScreen, hashMapOf(
                SCREEN_NAV_PARAM_PDF_URL to action.pdfUrl
        )))
    }

    private fun handleUpcomingVideo(action: UpcomingLiveVideo) {
        _navigateLiveData.value = Event(NavigationModel(NoScreen, hashMapOf()))
    }

}
