package com.doubtnutapp.quiztfs.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.quiztfs.LiveQuestionsData
import com.doubtnutapp.quiztfs.repository.LiveQuestionsRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 25-08-2021
 */
class LiveQuestionsViewModel @Inject constructor(
    private val liveQuestionsRepository: LiveQuestionsRepository,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _data: MutableLiveData<Outcome<LiveQuestionsData>> = MutableLiveData()
    val data: LiveData<Outcome<LiveQuestionsData>> get() = _data

    init {
        fetchInitial()
    }

    fun fetchInitial() {
        startLoading()
        viewModelScope.launch {
            liveQuestionsRepository.fetchInitialData()
                .map { it.data }
                .catch {
                    stopLoading()
                    onError(it)
                }
                .collect {
                    _data.value = Outcome.success(it)
                    stopLoading()
                }
        }
    }

    fun fetch(
        classCode: String,
        medium: String,
        subject: String
    ) {
        startLoading()
        viewModelScope.launch {
            liveQuestionsRepository.fetchData(classCode, medium, subject)
                .map { it.data }
                .catch {
                    stopLoading()
                    onError(it)
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

    fun startLoading() {
        _data.value = Outcome.loading(true)
    }

    fun stopLoading() {
        _data.value = Outcome.loading(false)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}