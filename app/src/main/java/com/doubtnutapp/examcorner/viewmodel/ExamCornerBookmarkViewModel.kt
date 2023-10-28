package com.doubtnutapp.examcorner.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.examcorner.model.ApiExamCornerBookmarkData
import com.doubtnutapp.examcorner.repository.ExamCornerBookmarkRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExamCornerBookmarkViewModel @Inject constructor(
    private val examCornerBookmarkRepository: ExamCornerBookmarkRepository,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    var extraParams: HashMap<String, Any> = hashMapOf()

    var itemsAdded = 0

    private val _examCornerLiveData: MutableLiveData<Outcome<ApiExamCornerBookmarkData>> =
        MutableLiveData()
    val examCornerLiveData: LiveData<Outcome<ApiExamCornerBookmarkData>>
        get() = _examCornerLiveData

    private fun startLoading() {
        _examCornerLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _examCornerLiveData.value = Outcome.loading(false)
    }

    fun fetchExamCornerBookmarkData(page: Int) {
        startLoading()
        viewModelScope.launch {
            examCornerBookmarkRepository.fetchExamCornerBookmarkData(page)
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
                }.collect {
                    if (!it.widgets.isNullOrEmpty()) {
                        itemsAdded.plus(it.widgets.size)
                    }
                    stopLoading()
                    _examCornerLiveData.value = Outcome.success(it)
                }
        }
    }

}