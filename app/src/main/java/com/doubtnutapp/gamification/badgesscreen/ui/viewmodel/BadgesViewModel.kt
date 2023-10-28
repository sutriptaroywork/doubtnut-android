package com.doubtnutapp.gamification.badgesscreen.ui.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnutapp.Constants
import com.doubtnutapp.Log
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.OpenBadgeProgressDialog
import com.doubtnutapp.base.OpenPage
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.domain.gamification.userbadge.entity.BadgeProgressEntity
import com.doubtnutapp.domain.gamification.userbadge.entity.BaseUserBadge
import com.doubtnutapp.domain.gamification.userbadge.interactor.GetUserBadgeProgress
import com.doubtnutapp.domain.gamification.userbadge.interactor.GetUserBadges
import com.doubtnutapp.gamification.badgesscreen.mapper.BadgeMapper
import com.doubtnutapp.gamification.badgesscreen.mapper.UserBadgeProgressMapper
import com.doubtnutapp.gamification.badgesscreen.model.Badge
import com.doubtnutapp.gamification.badgesscreen.model.BadgeProgress
import com.doubtnutapp.gamification.badgesscreen.model.BaseBadgeViewType
import com.doubtnutapp.gamification.event.GamificationEventManager
import com.doubtnutapp.gamification.gamepoints.mapper.ActionTypeToScreenMapper
import com.doubtnutapp.plus
import com.doubtnutapp.screennavigator.BadgeProgressDialogScreen
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class BadgesViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val badgeMapper: BadgeMapper,
    private val getUserBadges: GetUserBadges,
    private val getUserBadgeProgress: GetUserBadgeProgress,
    private val whatsAppSharing: WhatsAppSharing,
    private val userBadgeProgressMapper: UserBadgeProgressMapper,
    private val actionTypeToScreenMapper: ActionTypeToScreenMapper,
    private val userPreference: UserPreference,
    private val gamificationEventManager: GamificationEventManager
) : BaseViewModel(compositeDisposable) {

    private val _userBadgesLiveData = MutableLiveData<Outcome<List<BaseBadgeViewType>>>()
    val userBadgesLiveData: LiveData<Outcome<List<BaseBadgeViewType>>>
        get() = _userBadgesLiveData

    private val _userBadgesProgressLiveData = MutableLiveData<Outcome<BadgeProgress>>()
    val userBadgesProgressLiveData: LiveData<Outcome<BadgeProgress>>
        get() = _userBadgesProgressLiveData

    val whatsAppShareableData: LiveData<Event<Triple<String?, String?, String?>>>
        get() = whatsAppSharing.whatsAppShareableData

    val showWhatsAppProgressLiveData: LiveData<Boolean>
        get() = whatsAppSharing.showWhatsAppProgressLiveData

    fun handleAction(action: Any) {
        when (action) {
            is ShareOnWhatApp -> {
                gamificationEventManager.eventWith(EventConstants.EVENT_NAME_BADGE_SHARE_CLICK)
                whatsAppSharing.shareOnWhatsApp(action)
            }

            is OpenBadgeProgressDialog -> {
                openDialog(action)
            }
            is OpenPage -> navigateToPage(action)
        }
    }

    private fun openDialog(action: OpenBadgeProgressDialog) {

        val arg = hashMapOf(
            Constants.BADGE_ID to action.badgeId,
            Constants.NUDE_DESCRIPTION to action.description,
            Constants.IMAGE_URL to action.imageUrl,
            Constants.FEATURE_TYPE to action.featureType,
            Constants.SHARING_MESSAGE to action.sharingMessage,
            Constants.ACTION_PAGE to action.actionPage

        )
        _navigateLiveData.value = Event(NavigationModel(BadgeProgressDialogScreen, arg))
    }

    private fun navigateToPage(action: Any) {
        gamificationEventManager.eventWith(
            EventConstants.EVENT_NAME_EARN_BADGE_CLICK,
            ignoreSnowplow = true
        )
        val screen = actionTypeToScreenMapper.map(action)
        _navigateLiveData.value = Event(NavigationModel(screen, null))
    }

    fun getUserBadges(userId: String) {
        _userBadgesLiveData.value = Outcome.loading(true)
        compositeDisposable + getUserBadges.execute(GetUserBadges.Param(userId))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({ onBadgeSuccess(it, userId) }, this::onBadgeError)

    }

    private fun onBadgeSuccess(userBadgesList: List<BaseUserBadge>, userId: String) {
        _userBadgesLiveData.value = Outcome.loading(false)
        _userBadgesLiveData.value = Outcome.success(
            userBadgesList.map {
                badgeMapper.map(it).also {
                    if (it.viewType != R.layout.item_badge_header) {
                        it as Badge
                        it.isOwn = (userPreference.getUserStudentId() == userId)
                    }
                }
            }
        )
    }

    private fun onBadgeError(error: Throwable) {
        _userBadgesLiveData.value = Outcome.loading(false)
        _userBadgesLiveData.value = getOutComeError(error)
        logException(error)
    }

    fun getUserBadgeProgress(badgeId: String) {
        _userBadgesLiveData.value = Outcome.loading(true)
        compositeDisposable + getUserBadgeProgress.execute(GetUserBadgeProgress.Param(badgeId))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onBadgeProgressSuccess, this::onBadgeProgressError)
    }

    private fun onBadgeProgressSuccess(userBadgesProgress: BadgeProgressEntity) {
        _userBadgesProgressLiveData.value = Outcome.loading(false)
        _userBadgesProgressLiveData.value = Outcome.success(
            userBadgeProgressMapper.map(userBadgesProgress)

        )
    }

    private fun onBadgeProgressError(error: Throwable) {
        _userBadgesProgressLiveData.value = Outcome.loading(false)
        _userBadgesProgressLiveData.value = getOutComeError(error)
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
}