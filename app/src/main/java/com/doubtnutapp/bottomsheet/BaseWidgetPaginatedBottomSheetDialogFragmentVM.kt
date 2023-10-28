package com.doubtnutapp.bottomsheet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.widgets.data.entities.BaseWidgetData
import com.doubtnutapp.widgets.data.repository.WidgetRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class BaseWidgetPaginatedBottomSheetDialogFragmentVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val widgetRepository: WidgetRepository,
) : BaseViewModel(compositeDisposable) {

    private val _widgetsLiveData: MutableLiveData<Outcome<BaseWidgetData>> =
        MutableLiveData()
    val widgetsLiveData: LiveData<Outcome<BaseWidgetData>>
        get() = _widgetsLiveData

    private fun startLoading() {
        _widgetsLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _widgetsLiveData.value = Outcome.loading(false)
    }

    var getPaginatedBottomSheetWidgetDataJob: Job? = null
    var tempTabId : String? = null

    fun getPaginatedBottomSheetWidgetData(
        id: String?,
        type: String,
        tabId: String?,
        page: Int
    ) {
        if (tempTabId != tabId) {
            getPaginatedBottomSheetWidgetDataJob?.cancel()
        }
        tempTabId = tabId

        startLoading()
        getPaginatedBottomSheetWidgetDataJob = viewModelScope.launch {
            widgetRepository.getPaginatedBottomSheetWidgetData(
                id = id,
                type = type,
                tabId = tabId,
                page = page,
            )
                .catch {
                    it.printStackTrace()
                    stopLoading()
                }
                .collect {
                    stopLoading()
                    _widgetsLiveData.value = Outcome.success(it)
                }
        }
    }
}
