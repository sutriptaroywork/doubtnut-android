package com.doubtnutapp.gamification.friendbadgesscreen.ui.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.gamification.friendsbadges.entity.FriendsBadgeEntity
import com.doubtnutapp.gamification.friendbadgesscreen.mapper.FriendBadgeMapper
import com.doubtnutapp.gamification.friendbadgesscreen.model.FriendBadge
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class FriendBadgesViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable,
        private val friendBadgeMapper: FriendBadgeMapper
) : BaseViewModel(compositeDisposable) {

    private val _friendBadgesLiveData = MutableLiveData<Outcome<List<FriendBadge>>>()
    val friendBadgesLiveData: LiveData<Outcome<List<FriendBadge>>>
        get() = _friendBadgesLiveData


    fun getUserBadges(userId: String) {
//        _friendBadgesLiveData.value = Outcome.loading(true)
//        compositeDisposable + getFriendsBadges.execute(GetFriendsBadges.Param(userId))
//                .applyIoToMainSchedulerOnSingle()
//                .subscribeToSingle(this::onBadgeSuccess, this::onBadgeError)
    }

    private fun onBadgeSuccess(userBadgesList: List<FriendsBadgeEntity>) {
        _friendBadgesLiveData.value = Outcome.loading(false)
        _friendBadgesLiveData.value = Outcome.success(
                userBadgesList.map {
                    friendBadgeMapper.map(it)
                }
        )
    }

    private fun onBadgeError(error: Throwable) {
        _friendBadgesLiveData.value = Outcome.loading(false)
        _friendBadgesLiveData.value = getOutComeError(error)
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


}