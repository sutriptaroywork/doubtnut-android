package com.doubtnutapp.quiztfs.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.quiztfs.QuizStatusData
import com.doubtnutapp.quiztfs.repository.QuizTfsRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 16-09-2021
 */

class QuizTfsStatusViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val quizTfsRepository: QuizTfsRepository
) : BaseViewModel(compositeDisposable) {

    private val _status: MutableLiveData<Outcome<QuizStatusData>> = MutableLiveData()
    val status: LiveData<Outcome<QuizStatusData>> get() = _status

    fun getStatus(
        classCode: String,
        language: String,
        subject: String
    ) {
        startLoading()
        viewModelScope.launch {
            quizTfsRepository.getQuizTfsStatus(classCode, language, subject)
                .map { it.data }
                .catch {
                    onError(it)
                    stopLoading()
                }
                .collect {
                    _status.value = Outcome.success(it)
                    stopLoading()
                }
        }
    }

    private fun onError(error: Throwable) {
        _status.value = if (error is HttpException) {
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
        _status.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _status.value = Outcome.loading(false)
    }
}