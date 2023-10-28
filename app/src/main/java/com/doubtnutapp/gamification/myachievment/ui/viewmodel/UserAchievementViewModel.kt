package com.doubtnutapp.gamification.myachievment.ui.viewmodel

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.base.*
import com.doubtnutapp.gamification.event.GamificationEventManager
import com.doubtnutapp.gamification.gamepoints.mapper.ActionTypeToScreenMapper
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.screennavigator.SCREEN_NAV_PARAM_USER_ID
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class UserAchievementViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable,
        private val gamificationEventManager: GamificationEventManager,
        private val actionTypeToScreenMapper: ActionTypeToScreenMapper
) : BaseViewModel(compositeDisposable) {

    fun handleAction(action: Any, userId : String) {
        if (action is OpenDailyStreakPage) {
            publishEventWith(EventConstants.EVENT_NAME_DAILY_STREAK_CLICK)
        } else if (action is OpenOthersProfile) {
            gamificationEventManager.onOtherProfileClick(EventConstants.EVENT_NAME_LEADERBOARD_CLICK)
        }

        when (action) {
            is OpenOthersProfile -> openScreen(action, userId)
            is OpenDailyStreakPage -> openScreen(action, userId)
            is OpenLeaderBoardActivity -> openScreen(action, userId)
            is OpenBadgesActivity -> openScreen(action, userId)
        }
    }


    private fun openScreen(action: Any, userId: String) {
        val arg = hashMapOf(
                SCREEN_NAV_PARAM_USER_ID to userId
        )

        val screen = actionTypeToScreenMapper.map(action)
        _navigateLiveData.value = Event(NavigationModel(screen, arg))

    }

    fun publishEventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        gamificationEventManager.eventWith(eventName, ignoreSnowplow = ignoreSnowplow)
    }
}
