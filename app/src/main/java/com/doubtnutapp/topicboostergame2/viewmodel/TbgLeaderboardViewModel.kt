package com.doubtnutapp.topicboostergame2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.Leaderboard
import com.doubtnutapp.data.remote.repository.TopicBoosterGameRepository2
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by devansh on 27/06/21.
 */

class TbgLeaderboardViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val topicBoosterGameRepository: TopicBoosterGameRepository2,
) : BaseViewModel(compositeDisposable) {

    private val _leaderboardLiveData = MutableLiveData<Outcome<Leaderboard>>()
    val leaderboardLiveData: LiveData<Outcome<Leaderboard>>
        get() = _leaderboardLiveData

    fun getLeaderboard() {
        _leaderboardLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            topicBoosterGameRepository.getLeaderboard()
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