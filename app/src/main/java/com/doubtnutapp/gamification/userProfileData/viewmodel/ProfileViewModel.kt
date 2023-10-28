package com.doubtnutapp.gamification.userProfileData.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.gamification.userProfile.interactor.GetUserProfile
import com.doubtnutapp.domain.gamification.userProfile.model.UserProfileEntity
import com.doubtnutapp.gamification.event.GamificationEventManager
import com.doubtnutapp.gamification.event.SettingEventManager
import com.doubtnutapp.gamification.userProfileData.mapper.UserProfileDTOMapper
import com.doubtnutapp.gamification.userProfileData.mapper.UserRecentBadgesMapper
import com.doubtnutapp.gamification.userProfileData.model.MyBadgesItemDataModel
import com.doubtnutapp.gamification.userProfileData.model.UserProfileDTO
import com.doubtnutapp.plus
import com.doubtnutapp.screennavigator.LoginScreen
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    disposable: CompositeDisposable,
    private val getUserProfileUseCase: GetUserProfile,
    private val userProfileDTOMapper: UserProfileDTOMapper,
    private val recentBadgesMapper: UserRecentBadgesMapper,
    private val settingEventManager: SettingEventManager,
    private val gamificationEventManager: GamificationEventManager

) : BaseViewModel(disposable) {

    private val currentLoginState: Boolean = false

    private val _profileImageLiveData: MutableLiveData<String> = MutableLiveData()

    val profileImageLiveData: LiveData<String>
        get() = _profileImageLiveData

    private val _profileVipLiveData: MutableLiveData<String> = MutableLiveData()

    val profileVipLiveData: LiveData<String>
        get() = _profileVipLiveData

    private val _bannerImageLiveData: MutableLiveData<String> = MutableLiveData()

    val bannerImageLiveData: LiveData<String>
        get() = _bannerImageLiveData

    private val _userProfileName = MutableLiveData<String>()
    val userProfileName: LiveData<String>
        get() = _userProfileName

    private val _userProfileLevel = MutableLiveData<String>()
    val userProfileLevel: LiveData<String>
        get() = _userProfileLevel

    private val _userRecentBadges = MutableLiveData<List<MyBadgesItemDataModel>>()
    val userRecentBadgesList: LiveData<List<MyBadgesItemDataModel>>
        get() = _userRecentBadges

    private val _userProfileLiveData = MutableLiveData<Outcome<UserProfileDTO>>()
    val userProfileLiveData: LiveData<Outcome<UserProfileDTO>>
        get() = _userProfileLiveData

    private val _isLogInLiveData = MutableLiveData<Boolean>().apply {
        value = false
    }

    val isLogInLiveData: LiveData<Boolean>
        get() = _isLogInLiveData

    private val _pointsToEarnedLiveData = MutableLiveData<String>()
    val pointsToEarnedLiveData: LiveData<String>
        get() = _pointsToEarnedLiveData

    fun getUserProfile() {
        _userProfileLiveData.value = Outcome.loading(true)

        compositeDisposable + getUserProfileUseCase
            .execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onSuccess, this::onError)
    }

    private fun onSuccess(userProfileEntity: UserProfileEntity) {
        _userProfileLiveData.value = Outcome.loading(false)
        val userProfileData = userProfileEntity.run {
            userProfileDTOMapper.map(userProfileEntity)
        }

        _userProfileName.value = userProfileEntity.userName
        _userProfileLevel.value = userProfileEntity.userLevel
        _profileImageLiveData.value = userProfileEntity.profileImage
        _bannerImageLiveData.value = userProfileEntity.bannerImage
        _isLogInLiveData.value = userProfileEntity.isLoggedIn
        _pointsToEarnedLiveData.value = userProfileEntity.pointsToEarned
        if (userProfileEntity.subscriptionStatus != null && userProfileEntity.subscriptionStatus == true) {
            _profileVipLiveData.value = userProfileEntity.subscriptionImageUrl
        }

        _userRecentBadges.value = userProfileEntity.userRecentBadges?.map {
            recentBadgesMapper.map(it)
        }


        _userProfileLiveData.value = Outcome.success(userProfileData)
    }

    private fun onError(error: Throwable) {
        _userProfileLiveData.value = Outcome.loading(false)
        _userProfileLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
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

    fun clickForLogin(isLogIn: Boolean) {
        if (!isLogIn) {
            settingEventManager.eventWith(EventConstants.LOGIN_CLICK_PROFILE, ignoreSnowplow = true)
            _navigateLiveData.value = Event(NavigationModel(LoginScreen, null))
        }
    }

    fun publishEventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        settingEventManager.eventWith(eventName, ignoreSnowplow = ignoreSnowplow)
    }

}
