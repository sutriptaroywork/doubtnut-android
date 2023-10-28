package com.doubtnutapp.liveclass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.model.NextVideoDialogData
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class NextVideoViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) :
    BaseViewModel(compositeDisposable) {

    private val _nextVideoLiveData
            : MutableLiveData<Outcome<NextVideoDialogData>> = MutableLiveData()
    val nextVideoLiveData: LiveData<Outcome<NextVideoDialogData>>
        get() = _nextVideoLiveData

    fun getNextVideos(qid: String?, type: String?) {
        startLoading()
        viewModelScope.launch {
            DataHandler.INSTANCE.courseRepository.getNextVideos(qid = qid, type = type)
                .map {
                    it.data
                }.catch { e ->
                    _nextVideoLiveData.value = Outcome.Failure(e)
                    stopLoading()
                }.collect {
                    _nextVideoLiveData.value = Outcome.success(it)
                    stopLoading()
                }
        }
    }

    fun startLoading() {
        _nextVideoLiveData.value = Outcome.loading(true)
    }

    fun stopLoading() {
        _nextVideoLiveData.value = Outcome.loading(false)
    }

}