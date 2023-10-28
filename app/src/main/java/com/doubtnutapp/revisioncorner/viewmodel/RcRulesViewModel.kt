package com.doubtnutapp.revisioncorner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.revisioncorner.RulesInfo
import com.doubtnutapp.data.remote.repository.RevisionCornerRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.HashMap
import javax.inject.Inject

class RcRulesViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val revisionCornerRepository: RevisionCornerRepository,
    private val analyticsPublisher: AnalyticsPublisher,
    ) : BaseViewModel(compositeDisposable) {

    private val _ruleLiveData = MutableLiveData<Outcome<RulesInfo>>()
    val ruleLiveData : LiveData<Outcome<RulesInfo>>
        get() = _ruleLiveData

    fun getRulesData(widgetId : Int, topic : String?, subject : String?){
        _ruleLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            revisionCornerRepository.getRuleInfoData(widgetId, topic, subject)
                .catch {
                    _ruleLiveData.value = Outcome.loading(false)
                    _ruleLiveData.value = Outcome.failure(it)
                }
                .collect {
                    _ruleLiveData.value = Outcome.loading(false)
                    _ruleLiveData.value = Outcome.success(it.data)
                }
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}