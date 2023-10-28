package com.doubtnutapp.pcmunlockpopup.ui.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.pcmunlockpopup.BadgeRequiredType.BADGE_TYPE_INVITE
import com.doubtnutapp.domain.pcmunlockpopup.BadgeRequiredType.BADGE_TYPE_LEVEL
import com.doubtnutapp.domain.pcmunlockpopup.BadgeRequiredType.PC_BADGE_LOCK_DIALOG_VIEW
import com.doubtnutapp.domain.pcmunlockpopup.entity.PCMUnlockDataEntity
import com.doubtnutapp.domain.pcmunlockpopup.interactor.GetPCMUnlockData
import com.doubtnutapp.pcmunlockpopup.event.PCMUnlockPopUpEventManager
import com.doubtnutapp.plus
import com.doubtnutapp.screennavigator.GamePointsScreen
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class PCMUnlockPopViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val getPCMUnlockData: GetPCMUnlockData,
    private val pcmUnlockPopUpEventManager: PCMUnlockPopUpEventManager
) : BaseViewModel(compositeDisposable) {

    private val _unlockDataLiveData = MutableLiveData<Outcome<PCMUnlockDataEntity>>()
    val unlockDataLiveData: LiveData<Outcome<PCMUnlockDataEntity>>
        get() = _unlockDataLiveData

    private val _finishActivityLiveData = MutableLiveData<Event<Boolean>>()
    val finishActivityLiveData: LiveData<Event<Boolean>>
        get() = _finishActivityLiveData

    private val _errorString = MutableLiveData<String>()
    val errorString: LiveData<String>
        get() = _errorString

    fun getUnlockData() {
        pcmUnlockPopUpEventManager.eventWith(PC_BADGE_LOCK_DIALOG_VIEW)
        _unlockDataLiveData.value = Outcome.loading(true)
        compositeDisposable + getPCMUnlockData.execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onSuccess, this::onError)

    }

    fun onClick(badgeType: String) {
        if (badgeType == BADGE_TYPE_INVITE) {
            pcmUnlockPopUpEventManager.popUpPcClick(BADGE_TYPE_INVITE)
        } else if (badgeType == BADGE_TYPE_LEVEL) {
            pcmUnlockPopUpEventManager.popUpPcClick(BADGE_TYPE_LEVEL)
            _navigateLiveData.value = Event(NavigationModel(GamePointsScreen, null))
        }
        _finishActivityLiveData.value = Event(true)
    }

    fun closePopUpActivity() {
        _finishActivityLiveData.value = Event(true)
    }

    private fun onSuccess(data: PCMUnlockDataEntity) {
        _unlockDataLiveData.value = Outcome.loading(false)
        _unlockDataLiveData.value = Outcome.success(data)
    }

    private fun onError(throwable: Throwable) {
        _unlockDataLiveData.value = Outcome.loading(false)
        _unlockDataLiveData.value = getOutComeError(throwable)
        _errorString.value = throwable.message
        logException(throwable)
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