package com.doubtnutapp.common.ui.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.common.data.BookCallData
import com.doubtnutapp.common.di.module.CommonRepository
import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnutapp.data.Outcome
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class BookCallDialogFragmentVM @Inject constructor(
    private val commonRepository: CommonRepository,
) : ViewModel() {

    private val _bookCallData: MutableLiveData<Outcome<BookCallData>> =
        MutableLiveData()
    val bookCallData: LiveData<Outcome<BookCallData>>
        get() = _bookCallData

    private val _responseLiveData: MutableLiveData<Outcome<BaseResponse>> =
        MutableLiveData()
    val responseLiveData: LiveData<Outcome<BaseResponse>>
        get() = _responseLiveData

    fun getBookCallData() {
        _bookCallData.value = Outcome.loading(true)
        viewModelScope.launch {
            commonRepository.getBookCallData()
                .catch {
                    _bookCallData.value = Outcome.loading(false)
                    _bookCallData.value = Outcome.Failure(it)
                }
                .collect {
                    _bookCallData.value = Outcome.loading(false)
                    _bookCallData.value = Outcome.success(it)
                }
        }
    }

    fun bookCall() {
        val data = (bookCallData.value as? Outcome.Success)?.data ?: return
        _responseLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            commonRepository.bookCall(
                dateId = data.dateItems?.firstOrNull { it.isSelected == true }?.id
                    ?: return@launch,
                timeId = data.timeItems?.firstOrNull { it.isSelected == true }?.id
                    ?: return@launch,
            )
                .catch {
                    _responseLiveData.value = Outcome.loading(false)
                    _responseLiveData.value = Outcome.Failure(it)
                }
                .collect {
                    _bookCallData.value = Outcome.loading(false)
                    _responseLiveData.value = Outcome.success(it)
                }
        }
    }
}
