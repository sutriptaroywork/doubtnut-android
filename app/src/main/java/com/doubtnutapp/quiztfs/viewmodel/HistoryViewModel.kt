package com.doubtnutapp.quiztfs.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.quiztfs.HistoryData
import com.doubtnutapp.quiztfs.repository.HistoryRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 06-09-2021
 */
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _history: MutableLiveData<Outcome<HistoryData>> = MutableLiveData()
    val history: LiveData<Outcome<HistoryData>> get() = _history

    fun fetchHistory(page: Int) {
        startLoading()
        viewModelScope.launch {
            historyRepository.getHistory(page)
                .map { it.data }
                .catch {
                    stopLoading()
                    onError(it)
                }
                .collect {
                    stopLoading()
                    _history.value = Outcome.success(it)
                }
        }
    }

    private fun onError(error: Throwable) {
        _history.value = if (error is HttpException) {
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
        _history.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _history.value = Outcome.loading(false)
    }
}