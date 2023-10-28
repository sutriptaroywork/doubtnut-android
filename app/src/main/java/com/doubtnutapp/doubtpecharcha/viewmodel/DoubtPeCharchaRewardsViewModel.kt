package com.doubtnutapp.doubtpecharcha.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.utils.NetworkApiUtils
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.doubtpecharcha.model.DoubtPeCharchaRewardsResponse
import com.doubtnutapp.doubtpecharcha.service.DoubtPeCharchaApiService
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import javax.inject.Inject

class DoubtPeCharchaRewardsViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) :
    BaseViewModel(compositeDisposable) {

    private val doubtPeCharchaRepository = DataHandler.INSTANCE.doubtPeCharchaRepository

    private val _mutableLiveData =
        MutableLiveData<Outcome<DoubtPeCharchaRewardsResponse>>()

    val liveDataRewardsResponse: LiveData<Outcome<DoubtPeCharchaRewardsResponse>> = _mutableLiveData

    fun getRewardsResponse() {
        viewModelScope.launch {
            try {
                _mutableLiveData.postValue(Outcome.loading(true))
                val response = doubtPeCharchaRepository.getDoubtPeCharchaRewardsData()
                _mutableLiveData.value = Outcome.success(response)
                _mutableLiveData.postValue(Outcome.loading(false))
            } catch (e: Exception) {
                NetworkApiUtils.onApiError(_mutableLiveData, e)
                _mutableLiveData.postValue(Outcome.loading(false))
            }
        }
    }
}