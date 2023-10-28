package com.doubtnutapp.store.viewmodel


import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.OpenLibraryPlayListActivity
import com.doubtnutapp.base.OpenLibraryVideoPlayListScreen
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.store.event.StoreEventManager
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class StoreItemBuyViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable,
        private val storeEventManager: StoreEventManager
) : BaseViewModel(compositeDisposable) {

    fun publishTabSelectedEvent(event: String, ignoreSnowplow: Boolean = false) {
        storeEventManager.eventWith(event, ignoreSnowplow = ignoreSnowplow)
    }

    fun handleStoreItemClick(action: Any) {
        when (action) {
            is OpenLibraryPlayListActivity -> openLibraryContentListScreen(action)
            is OpenLibraryVideoPlayListScreen -> openLibraryVideoPlaylistScreen(action)
        }
    }

    private fun openLibraryVideoPlaylistScreen(action: OpenLibraryVideoPlayListScreen) {
        val arg = hashMapOf(
                SCREEN_NAV_PARAM_PLAYLIST_ID to action.playlistId,
                SCREEN_NAV_PARAM_PLAYLIST_TITLE to action.playlistName
        )
        _navigateLiveData.value = Event(NavigationModel(LibraryVideoPlayListScreen, arg))
    }

    private fun openLibraryContentListScreen(action: OpenLibraryPlayListActivity) {
        _navigateLiveData.value = Event(NavigationModel(LibraryPlayListScreen, hashMapOf(
                LibraryListingActivity.INTENT_EXTRA_PLAYLIST_ID to action.playlistId,
                LibraryListingActivity.INTENT_EXTRA_PACKAGE_ID to action.packageDetailsId.orEmpty(),
                LibraryListingActivity.INTENT_EXTRA_PLAYLIST_TITLE to action.playlistName
        )))
    }
}