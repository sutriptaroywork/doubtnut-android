package com.doubtnutapp.feed.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.feed.TopIconsRepository
import com.doubtnutapp.feed.entity.TopIconsData
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class TopIconsViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val topIconsRepository: TopIconsRepository
) : BaseViewModel(compositeDisposable) {

    private val _topIconsData = MutableLiveData<Outcome<TopIconsData>>()
    val topIconsData: LiveData<Outcome<TopIconsData>>
        get() = _topIconsData

    fun fetchAllHomeTopIconsData(screen: String, userAssortment: String, screenWidth: Int?) {
        _topIconsData.value = Outcome.loading(true)
        viewModelScope.launch {
            topIconsRepository.getAllHomeTopIcons(
                screen = screen,
                userAssortment = userAssortment,
                screenWidth = screenWidth
            )
                .catch {
                    _topIconsData.value = Outcome.loading(false)
                    _topIconsData.value = Outcome.apiError(it)
                }
                .collect {
                    _topIconsData.value = Outcome.loading(false)
                    _topIconsData.value = Outcome.success(it)
                }
        }
    }
}