package com.doubtnutapp.revisioncorner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.revisioncorner.ResultInfo
import com.doubtnutapp.data.remote.repository.RevisionCornerRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class RcResultViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val revisionCornerRepository: RevisionCornerRepository,
        ) : BaseViewModel(compositeDisposable) {


    private val _resultLiveData = MutableLiveData<Outcome<ResultInfo>>()
    val resultLiveData : LiveData<Outcome<ResultInfo>>
        get() = _resultLiveData

    fun getResultInfoData(widgetId: String){
        _resultLiveData.postValue(Outcome.loading(true))
        viewModelScope.launch {
            revisionCornerRepository.getResultInfoData(widgetId)
                .catch {
                    _resultLiveData.value = Outcome.loading(false)
                    _resultLiveData.value = Outcome.failure(it)
                }
                .collect {
                    _resultLiveData.value = Outcome.loading(false)
                    _resultLiveData.value = Outcome.success(it.data)
                }
        }
    }

}