package com.doubtnutapp.topicboostergame2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame2.LevelData
import com.doubtnutapp.data.remote.repository.TopicBoosterGameRepository2
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.HashMap
import javax.inject.Inject

/**
 * Created by devansh on 24/06/21.
 */

class LevelsViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val topicBoosterGameRepository: TopicBoosterGameRepository2,
    private val analyticsPublisher: AnalyticsPublisher,
) : BaseViewModel(compositeDisposable) {

    private val _levelsLiveData = MutableLiveData<Outcome<LevelData>>()
    val levelsLiveData: LiveData<Outcome<LevelData>>
        get() = _levelsLiveData

    fun getLevels() {
        _levelsLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            topicBoosterGameRepository.getLevels()
                .catch {
                    _levelsLiveData.value = Outcome.loading(false)
                }
                .collect {
                    _levelsLiveData.value = Outcome.loading(false)
                    _levelsLiveData.value = Outcome.success(it.data)
                }
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }

}