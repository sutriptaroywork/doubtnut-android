package com.doubtnutapp.gamification.popactivity.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.*
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.gamification.popactivity.model.GamificationPopup
import com.doubtnutapp.gamification.popactivity.popupbuilder.PopupBuilder
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.utils.Event

class GamificationPopupViewModel(
    private val userPreference: UserPreference,
    private val popupBuilder: PopupBuilder
) {

    private val _popViewTypeLiveData = MutableLiveData<Event<GamificationPopup?>>()

    val popViewTypeLiveData: LiveData<Event<GamificationPopup?>>
        get() = _popViewTypeLiveData

    private val _closeEventLiveData = MutableLiveData<Event<Unit>>()

    val closeEventLiveData: LiveData<Event<Unit>>
        get() = _closeEventLiveData

    private val _navigateLiveData = MutableLiveData<Event<NavigationModel>>()

    val navigateLiveData: LiveData<Event<NavigationModel>>
        get() = _navigateLiveData

    fun getPopUpData(popupData: Map<String, String>) {
        val popupBadge = popupBuilder.build(popupData)

        if (popupBadge != null) {
            _popViewTypeLiveData.value = Event(popupBadge)
        } else {
            _popViewTypeLiveData.value = null
        }
    }

    fun handleAction(action: Any) {
        when (action) {
            FinishActivity -> _closeEventLiveData.value = Event(Unit)

            ViewNowClicked -> {
                val seenOnce = userPreference.isUserSeenBadgeScreenOnce()
                val arg = hashMapOf(SCREEN_NAV_PARAM_USER_ID to userPreference.getUserStudentId())

                if (seenOnce) {
                    _navigateLiveData.value = Event(NavigationModel(BadgesScreen, arg))
                } else {
                    userPreference.setUserBadgeScreenOnceToTrue()
                    _navigateLiveData.value = Event(NavigationModel(ProfileScreen, null))
                }
            }

            is OpenTopicPage -> {
                val arg = hashMapOf(
                    SCREEN_NAV_PARAM_PLAYLIST_ID to (action.playlistId ?: ""),
                    SCREEN_NAV_PARAM_PLAYLIST_TITLE to (action.title
                        ?: "")
                )

                val screen = if (action.playlistId.isNullOrBlank()) {
                    LibraryScreen
                } else {
                    if (action.isLast == 0) LibraryPlayListScreen else LibraryVideoPlayListScreen
                }

                _navigateLiveData.value = Event(NavigationModel(screen, arg))
            }
            is OpenBadgesActivity -> {
                val arg = hashMapOf(SCREEN_NAV_PARAM_USER_ID to userPreference.getUserStudentId())

                _navigateLiveData.value = Event(NavigationModel(BadgesScreen, arg))
            }

        }
    }
}