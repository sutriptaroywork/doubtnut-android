package com.doubtnutapp.gamification.leaderboard.ui.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.gamification.leaderboard.entity.LeaderboardEntity
import com.doubtnutapp.domain.gamification.leaderboard.interactor.GetGameLeaders
import com.doubtnutapp.gamification.event.GamificationEventManager
import com.doubtnutapp.gamification.leaderboard.mapper.GameLeaderMapper
import com.doubtnutapp.gamification.leaderboard.model.LeaderboardData
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class GameLeaderBoardViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable,
        private val getGameLeaders: GetGameLeaders,
        private val gameLeaderMapper: GameLeaderMapper,
        private val gamificationEventManager: GamificationEventManager
) : BaseViewModel(compositeDisposable) {

    private val _gameLeaderLiveData = MutableLiveData<Outcome<LeaderboardData>>()

    val gameLeaderLiveData: LiveData<Outcome<LeaderboardData>>
        get() = _gameLeaderLiveData

    fun getGameLeaders() {
        _gameLeaderLiveData.value = Outcome.loading(true)

        getGameLeaders.execute(Unit)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onSuccess, this::onError)
    }

    private fun onSuccess(leaderboardEntity: LeaderboardEntity) {

        _gameLeaderLiveData.value = Outcome.loading(false)

        _gameLeaderLiveData.value = Outcome.success(gameLeaderMapper.map(leaderboardEntity))
    }

    private fun onError(throwable: Throwable) {
        _gameLeaderLiveData.value = Outcome.loading(false)
        _gameLeaderLiveData.value = getOutComeError(throwable)
        logException(throwable)
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

    fun sendSelectedTabListener(tabName: String, ignoreSnowplow: Boolean = false) {
        gamificationEventManager.eventWith(tabName, ignoreSnowplow = ignoreSnowplow)
    }
}