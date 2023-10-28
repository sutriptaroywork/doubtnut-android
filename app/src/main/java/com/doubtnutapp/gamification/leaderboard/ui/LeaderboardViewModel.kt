package com.doubtnutapp.gamification.leaderboard.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.gamification.event.GamificationEventManager
import com.doubtnutapp.gamification.leaderboard.model.GameLeader
import com.doubtnutapp.gamification.leaderboard.model.LeaderboardData
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.screennavigator.OthersProfileScreen
import com.doubtnutapp.screennavigator.ProfileScreen
import com.doubtnutapp.screennavigator.SCREEN_NAV_PARAM_USER_ID
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class LeaderboardViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val gamificationEventManager: GamificationEventManager
) : BaseViewModel(compositeDisposable) {

    private var dataState = 0

    private val _gameLeaderLiveData = MutableLiveData<List<GameLeader?>>()

    val gameLeaderLiveData: LiveData<List<GameLeader?>>
        get() = _gameLeaderLiveData

    private val _topLeaderBoardLiveData = MutableLiveData<List<GameLeader>>()

    val topLeaderBoardLiveData: LiveData<List<GameLeader>>
        get() = _topLeaderBoardLiveData

    private val _ownGameResultLiveData = MutableLiveData<GameLeader>()

    val ownGameResultLiveData: LiveData<GameLeader>
        get() = _ownGameResultLiveData

    fun mapData(allData: LeaderboardData?, state: Int) {
        dataState = state
        var leadersData: List<GameLeader>? = mutableListOf()

        if (dataState == 2)
            leadersData = allData?.allLeaderboardData
        else if (dataState == 1)
            leadersData = allData?.dailyLeaderboardData

        val topLeadersList = getLeadersRankedLower(leadersData)

        val ownGameResult: GameLeader? = getOwnGameResultIfNotInTopTen(leadersData)

        var restOfTheLeadersList = getTopLeadersList(leadersData)?.run {
            minus(ownGameResult)
        }
        if (restOfTheLeadersList?.size == 0)
            restOfTheLeadersList = topLeadersList

        if (restOfTheLeadersList?.isNotEmpty()!!) {
            _gameLeaderLiveData.value = restOfTheLeadersList
        }

        _topLeaderBoardLiveData.value = topLeadersList

        if (ownGameResult != null) {
            _ownGameResultLiveData.value = ownGameResult
        }
    }

    private fun getOwnGameResultIfNotInTopTen(leadersList: List<GameLeader>?): GameLeader? {
        val LAST_RANK = 10
        return leadersList?.find {
            it.isOwn && it.rank > LAST_RANK
        }
    }

    private fun getLeadersRankedLower(leadersList: List<GameLeader>?): List<GameLeader>? {
        val topThree = 1..3
        return leadersList?.filter {
            it.rank in topThree
        }
    }

    private fun getTopLeadersList(leadersList: List<GameLeader>?): List<GameLeader>? {

        return leadersList?.filter {
            it.rank > 3
        }
    }

    fun onMoreOptions(userId: Int) {
        val arg = hashMapOf(
            SCREEN_NAV_PARAM_USER_ID to userId.toString()
        )
        _navigateLiveData.value = Event(NavigationModel(OthersProfileScreen, arg))
    }

    fun closeScreen() {
        _navigateLiveData.value = Event(NavigationModel(ProfileScreen, HashMap()))
    }

    fun sendEvent(eventName: String) {
        gamificationEventManager.eventWith(eventName)
    }
}
