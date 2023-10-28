package com.doubtnutapp.newlibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearPapers
import com.doubtnutapp.newlibrary.repository.LibraryPreviousYearPapersRepository
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

class LibraryPreviousYearPapersViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val repository: LibraryPreviousYearPapersRepository
) : BaseViewModel(compositeDisposable) {

    private val _data: MutableLiveData<Outcome<LibraryPreviousYearPapers>> = MutableLiveData()
    val data: LiveData<Outcome<LibraryPreviousYearPapers>> get() = _data

    fun getPreviousPapersTabsAndFilterData(examId: String) {
        startLoading()
        viewModelScope.launch {
            repository.getPreviousPapersTabsAndFilterData(examId).map { it.data }
                .catch {
                    onError(it)
                    stopLoading()
                }
                .collect {
                    _data.value = Outcome.success(it)
                    stopLoading()
                }
        }
    }

    private fun onError(error: Throwable) {
        _data.value = if (error is HttpException) {
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

    private fun startLoading() {
        _data.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _data.value = Outcome.loading(false)
    }
}