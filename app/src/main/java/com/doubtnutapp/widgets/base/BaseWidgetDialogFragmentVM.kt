package com.doubtnutapp.widgets.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.widgets.data.entities.BaseWidgetData
import com.doubtnutapp.widgets.data.repository.WidgetRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class BaseWidgetDialogFragmentVM @Inject constructor(
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

    fun getDialogData(
        widgetType: String,
        studentId: String? = null,
        assortmentId: String? = null,
        testId: String? = null,
        tabNumber: String? = null
    ) {
        startLoading()
        viewModelScope.launch {
            widgetRepository.getDialogData(
                widgetType = widgetType,
                studentId = studentId,
                assortmentId = assortmentId,
                testId = testId,
                tabNumber = tabNumber
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
