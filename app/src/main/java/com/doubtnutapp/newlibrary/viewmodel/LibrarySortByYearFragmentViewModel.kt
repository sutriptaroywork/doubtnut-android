package com.doubtnutapp.newlibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearFilterResponse
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearSelectionResponse
import com.doubtnutapp.newlibrary.repository.LibraryPreviousYearPapersRepository
import com.doubtnutapp.orDefaultValue
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 26/11/21
 */

class LibrarySortByYearFragmentViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val repository: LibraryPreviousYearPapersRepository
) : BaseViewModel(compositeDisposable) {

    private val _filterData: MutableLiveData<Outcome<LibraryPreviousYearFilterResponse>> =
        MutableLiveData()
    val filterData: LiveData<Outcome<LibraryPreviousYearFilterResponse>> get() = _filterData

    private val _selectionData: MutableLiveData<Outcome<LibraryPreviousYearSelectionResponse>> =
        MutableLiveData()
    val selectionData: LiveData<Outcome<LibraryPreviousYearSelectionResponse>> get() = _selectionData

    fun getFilterData(
        examId: String,
        tabId: String,
        filterId: String,
        filterDataType: String,
        filterText: String?
    ) {
        startLoadingFilter()
        viewModelScope.launch {
            repository.getPreviousPapersYearData(
                examId, tabId, filterId, filterDataType, filterText
            ).map { it.data }
                .catch {
                    onFilterError(it)
                    stopLoadingFilter()
                }
                .collect {
                    _filterData.value = Outcome.success(it)
                    getSelectionData(
                        examId = examId,
                        tabId = tabId,
                        filterId = filterId,
                        filterDataType = filterDataType,
                        filterText = filterText,
                        id = it.selectedFilterData.find { selectedFilter ->
                            selectedFilter.isSelected
                        }?.id.orDefaultValue()
                    )
                    stopLoadingFilter()
                }
        }
    }

    fun getSelectionData(
        examId: String,
        tabId: String,
        filterId: String,
        filterDataType: String,
        filterText: String?,
        id: String
    ) {
        startLoadingSelection()
        viewModelScope.launch {
            repository.getPreviousPapersSelectionData(
                examId, tabId, id, filterId, filterDataType, filterText
            ).map { it.data }
                .catch {
                    onSelectionError(it)
                    stopLoadingSelection()
                }
                .collect {
                    _selectionData.value = Outcome.success(it)
                    stopLoadingSelection()
                }
        }
    }

    private fun onFilterError(error: Throwable) {
        _filterData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    private fun onSelectionError(error: Throwable) {
        _selectionData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    private fun startLoadingFilter() {
        _filterData.value = Outcome.loading(true)
    }

    private fun stopLoadingFilter() {
        _filterData.value = Outcome.loading(false)
    }

    private fun startLoadingSelection() {
        _selectionData.value = Outcome.loading(true)
    }

    private fun stopLoadingSelection() {
        _selectionData.value = Outcome.loading(false)
    }
}