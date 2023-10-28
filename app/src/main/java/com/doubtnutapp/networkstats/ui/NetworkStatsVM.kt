package com.doubtnutapp.networkstats.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.networkstats.models.VideoStatsData
import com.doubtnutapp.networkstats.repository.NetworkStatsRepository
import com.doubtnut.core.data.remote.Resource
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class NetworkStatsVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val repository: NetworkStatsRepository
) :
    BaseViewModel(compositeDisposable) {

    private val _videoStats = MutableLiveData<Resource<List<VideoStatsData>>>()
    val videoStats: LiveData<Resource<List<VideoStatsData>>> = _videoStats

    init {
        getVideoStats()
    }

    private fun getVideoStats() {
        viewModelScope.launch {
            _videoStats.postValue(Resource.Loading())
            repository.getVideoStats()?.collect {
                _videoStats.postValue(Resource.Success(it))
            }
        }
    }

    fun deleteAllVideoData() {
        viewModelScope.launch {
            repository.deleteAllData()
        }
    }
}