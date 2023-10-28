package com.doubtnutapp.liveclass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.model.FilterData
import com.doubtnutapp.toHashMapOfStringVString
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import javax.inject.Inject

class FilterViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) :
    BaseViewModel(compositeDisposable) {

    private val _filterLiveData
            : MutableLiveData<Outcome<FilterData>> = MutableLiveData()
    val filterLiveData: LiveData<Outcome<FilterData>>
        get() = _filterLiveData


    fun getFilters(
        source: String,
        assortmentId: String,
        filters: HashMap<String, MutableList<String>>
    ) {
        startLoading()
        viewModelScope.launch {
            DataHandler.INSTANCE.courseRepository.getFilters(source, assortmentId, filters)
                .map {
                    it.data
                }.catch { e ->
                    _filterLiveData.value = Outcome.Failure(e)
                    stopLoading()
                }.collect {
                    _filterLiveData.value = Outcome.success(it)
                    stopLoading()
                }
        }
    }

    fun startLoading() {
        _filterLiveData.value = Outcome.loading(true)
    }

    fun stopLoading() {
        _filterLiveData.value = Outcome.loading(false)
    }

}