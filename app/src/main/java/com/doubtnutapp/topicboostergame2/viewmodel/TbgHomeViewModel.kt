package com.doubtnutapp.topicboostergame2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.*
import com.doubtnutapp.data.remote.repository.TopicBoosterGameRepository2
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Created by devansh on 23/06/21.
 */

class TbgHomeViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val topicBoosterGameRepository: TopicBoosterGameRepository2,
    private val analyticsPublisher: AnalyticsPublisher,
) : BaseViewModel(compositeDisposable) {

    //region LiveData
    private val _homeLiveData = MutableLiveData<Outcome<TbgHomeData>>()
    val homeLiveData: LiveData<Outcome<TbgHomeData>>
        get() = _homeLiveData

    private val _quizHistoryLiveData = MutableLiveData<Outcome<QuizHistoryViewMore>>()
    val quizHistoryLiveData: LiveData<Outcome<QuizHistoryViewMore>>
        get() = _quizHistoryLiveData

    private val _leaderboardLiveData = MutableLiveData<Outcome<Leaderboard>>()
    val leaderboardLiveData: LiveData<Outcome<Leaderboard>>
        get() = _leaderboardLiveData

    private var _quizHistoryPage = 1
    //endregion

    var subjectData: SubjectData? = null
        private set

    fun getTbgHomeData() {
        _homeLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            topicBoosterGameRepository.getTbgHomeData()
                .map { it.data }
                .catch {
                    _homeLiveData.value = Outcome.loading(false)
                    _homeLiveData.value = Outcome.failure(it)
                }
                .collect {
                    it.levelGames = List(it.totalGame) { gameNumber ->
                        LevelGameNumber(
                            gameNumber = gameNumber + 1,
                            isDone = gameNumber < it.totalGameWon,
                        )
                    }.apply { lastOrNull()?.isLast = true }

                    subjectData = it.subjects

                    _homeLiveData.value = Outcome.loading(false)
                    _homeLiveData.value = Outcome.success(it)
                }
        }
    }

    fun getQuizHistory(){
        _quizHistoryLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            topicBoosterGameRepository.getQuizHistory(_quizHistoryPage)
                .catch {
                    _quizHistoryLiveData.value = Outcome.loading(false)
                }
                .collect {
                    _quizHistoryLiveData.value = Outcome.loading(false)
                    _quizHistoryLiveData.value = Outcome.success(it.data)
                    _quizHistoryPage = it.data.page
                }
        }
    }

    fun getLeaderboardData(id: Int, pageNo: Int){
        _leaderboardLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            topicBoosterGameRepository.getLeaderboardList(id, pageNo)
                .catch {
                    _leaderboardLiveData.value = Outcome.loading(false)
                }
                .collect {
                    _leaderboardLiveData.value = Outcome.loading(false)
                    _leaderboardLiveData.value = Outcome.success(it.data)
                }
        }

    }

    fun sendEvent(
        eventName: String,
        params: HashMap<String, Any> = hashMapOf(),
        ignoreMoengage: Boolean = true
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                params,
                ignoreMoengage = ignoreMoengage
            )
        )
    }
}