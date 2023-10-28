package com.doubtnutapp.doubtfeed2.leaderboard.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.doubtfeed2.leaderboard.data.model.Leaderboard
import com.doubtnutapp.doubtfeed2.leaderboard.data.repository.LeaderboardRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by devansh on 12/7/21.
 */

class LeaderboardViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val leaderboardRepository: LeaderboardRepository,
) : BaseViewModel(compositeDisposable) {

    private val _leaderboardLiveData = MutableLiveData<Outcome<Leaderboard>>()
    val leaderboardLiveData: LiveData<Outcome<Leaderboard>>
        get() = _leaderboardLiveData

    fun getLeaderboard() {
        _leaderboardLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            leaderboardRepository.getLeaderboard()
                .catch {
                    _leaderboardLiveData.value = Outcome.loading(true)
                    _leaderboardLiveData.value = Outcome.failure(it)
                }
                .collect {
                    _leaderboardLiveData.value = Outcome.loading(true)
                    _leaderboardLiveData.value = Outcome.success(it.data)
                }
        }
    }
}
