package com.doubtnutapp.revisioncorner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.revisioncorner.PerformanceReport
import com.doubtnutapp.data.remote.repository.RevisionCornerRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Created by devansh on 16/08/21.
 */

class RcStatsViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val revisionCornerRepository: RevisionCornerRepository,
    private val analyticsPublisher: AnalyticsPublisher,
) : BaseViewModel(compositeDisposable) {

    private val _performanceReport = MutableLiveData<Outcome<PerformanceReport>>()
    val performanceReport: LiveData<Outcome<PerformanceReport>>
        get() = _performanceReport

    fun getPerformanceReport() {
        viewModelScope.launch {
            _performanceReport.value = Outcome.loading(true)
            revisionCornerRepository.getPerformanceReport()
                .catch { e ->
                    _performanceReport.value = Outcome.loading(false)
                    _performanceReport.value = Outcome.failure(e)
                }
                .collect {
                    _performanceReport.value = Outcome.loading(false)
                    _performanceReport.value = Outcome.success(it.data)
                }
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}