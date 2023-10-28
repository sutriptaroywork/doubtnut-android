package com.doubtnutapp.liveclass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.repository.TopDoubtRepository
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TopDoubtViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val topDoubtRepository: TopDoubtRepository
) : BaseViewModel(compositeDisposable) {

    private val _topDoubtData
            : MutableLiveData<Outcome<List<WidgetEntityModel<*, *>>>> = MutableLiveData()

    val topDoubtData: LiveData<Outcome<List<WidgetEntityModel<*, *>>>>
        get() = _topDoubtData

    var extraParams: HashMap<String, Any> = hashMapOf()

    fun startLoading() {
        _topDoubtData.value = Outcome.loading(true)
    }

    fun stopLoading() {
        _topDoubtData.value = Outcome.loading(false)
    }

    fun fetchTopDoubtAnswerData(entityId: String, batchId: String?) {
        startLoading()
        viewModelScope.launch {
            topDoubtRepository.fetchTopDoubtAnswerData(entityId, batchId)
                .map {
                    it.data.map { widget ->
                        if (widget != null) {
                            if (widget.extraParams == null) {
                                widget.extraParams = hashMapOf()
                            }
                            widget.extraParams?.putAll(extraParams)
                        }
                    }
                    it.data
                }
                .catch {
                    onError(it)
                }
                .collect {
                    _topDoubtData.value = Outcome.success(it)
                    stopLoading()
                }
        }
    }

    private val _messageStringIdLiveData: MutableLiveData<Event<Int>> = MutableLiveData()

    val messageStringIdLiveData: LiveData<Event<Int>>
        get() = _messageStringIdLiveData

    private fun onError(error: Throwable) {
        stopLoading()
        _messageStringIdLiveData.value = Event(R.string.something_went_wrong)
    }

}