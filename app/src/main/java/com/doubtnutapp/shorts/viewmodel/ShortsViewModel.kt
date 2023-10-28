package com.doubtnutapp.shorts.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.shorts.model.ShortsListData
import com.doubtnutapp.shorts.repository.ShortsRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class ShortsViewModel @Inject constructor(
    private val shortsRepository: ShortsRepository,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    var extraParams: HashMap<String, Any> = hashMapOf()

    var itemsAdded = 0

    private val _widgetLiveData: MutableLiveData<Outcome<ShortsListData>> = MutableLiveData()
    val widgetLiveData: LiveData<Outcome<ShortsListData>>
        get() = _widgetLiveData

    private fun startLoading() {
        _widgetLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _widgetLiveData.value = Outcome.loading(false)
    }

    fun fetchShortsList(lastId: String?, qid: String?, type: String) {
        startLoading()
        viewModelScope.launch {
            shortsRepository.fetchShortsList(lastId, qid, type)
                .map {
                    it.data.widgets.mapIndexedNotNull { index, widget ->
                        if (widget != null) {
                            if (widget.extraParams == null) {
                                widget.extraParams = hashMapOf()
                            }
                            widget.extraParams?.putAll(extraParams)
                            widget.extraParams?.put(
                                EventConstants.ITEM_PARENT_POSITION,
                                itemsAdded + index
                            )
                        }
                    }
                    it.data
                }.catch {
                    stopLoading()
                    onError(it)
                }.collect {
                    if (!it.widgets.isNullOrEmpty()) {
                        itemsAdded.plus(it.widgets.size)
                    }
                    stopLoading()
                    _widgetLiveData.value = Outcome.success(it)
                }
        }
    }

    private fun onError(error: Throwable) {
        _widgetLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(
                    error.message
                        ?: ""
                )
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }
}