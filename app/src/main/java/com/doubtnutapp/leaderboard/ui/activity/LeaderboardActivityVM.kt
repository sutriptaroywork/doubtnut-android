package com.doubtnutapp.leaderboard.ui.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.leaderboard.data.entity.LeaderboardData
import com.doubtnutapp.leaderboard.data.remote.LeaderboardRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class LeaderboardActivityVM @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository,
) : ViewModel() {

    private val _widgetsLiveData: MutableLiveData<Outcome<LeaderboardData>> = MutableLiveData()
    val widgetsLiveData: LiveData<Outcome<LeaderboardData>>
        get() = _widgetsLiveData

    private fun startLoading() {
        _widgetsLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _widgetsLiveData.value = Outcome.loading(false)
    }

    fun getTestLeaderboardData(
        source: String,
        assortmentId: String?,
        testId: String?,
        type: String?
    ) {
        startLoading()
        viewModelScope.launch {
            leaderboardRepository.getTestLeaderboardData(
                source, assortmentId, testId, type
            )
                .catch {
                    it.printStackTrace()
                    stopLoading()
                }
                .collect {
                    stopLoading()
                    _widgetsLiveData.value = Outcome.success(it)
                }
        }
    }

    fun getPaidUserChampionshipLeaderboard(
        source: String,
        assortmentId: String?,
        testId: String?,
        type: String?
    ) {
        startLoading()
        viewModelScope.launch {
            leaderboardRepository.getPaidUserChampionshipLeaderboard(
                source, assortmentId, testId, type
            )
                .catch {
                    it.printStackTrace()
                    stopLoading()
                }
                .collect {
                    stopLoading()
                    _widgetsLiveData.value = Outcome.success(it)
                }
        }
    }

}