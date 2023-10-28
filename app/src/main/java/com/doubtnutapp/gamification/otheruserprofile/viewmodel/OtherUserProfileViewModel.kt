package com.doubtnutapp.gamification.otheruserprofile.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.gamification.userProfile.interactor.GetOthersUserProfile
import com.doubtnutapp.domain.gamification.userProfile.model.UserProfileEntity
import com.doubtnutapp.gamification.event.GamificationEventManager
import com.doubtnutapp.gamification.otheruserprofile.mapper.OthersUserProfileMapper
import com.doubtnutapp.gamification.otheruserprofile.model.OthersUserProfileDataModel
import com.doubtnutapp.plus
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class OtherUserProfileViewModel @Inject constructor(
    disposable: CompositeDisposable,
    private val getOthersUserProfile: GetOthersUserProfile,
    private val othersUserProfileMapper: OthersUserProfileMapper,
    private val gamificationEventManager: GamificationEventManager
) : BaseViewModel(disposable) {

    private val _othersUserProfileLiveData = MutableLiveData<OthersUserProfileDataModel>()
    val othersUserProfileLiveData: LiveData<OthersUserProfileDataModel>
        get() = _othersUserProfileLiveData

    private val _userProfileLiveData = MutableLiveData<Outcome<OthersUserProfileDataModel>>()
    val userProfileLiveData: LiveData<Outcome<OthersUserProfileDataModel>>
        get() = _userProfileLiveData

    fun getOthersUserProfile(userId: String) {
        _userProfileLiveData.value = Outcome.loading(true)
        compositeDisposable + getOthersUserProfile
            .execute(GetOthersUserProfile.Param(userId))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onSuccess, this::onError)
    }

    private fun onSuccess(userProfileEntity: UserProfileEntity) {
        _userProfileLiveData.value = Outcome.loading(false)
        val userProfileData = userProfileEntity.run {
            othersUserProfileMapper.map(userProfileEntity)
        }
        _othersUserProfileLiveData.value = userProfileData
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

    fun sendEvent(event: String) {
        gamificationEventManager.eventWith(event)
    }

}
