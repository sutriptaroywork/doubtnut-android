package com.doubtnutapp.quiztfs.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.quiztfs.QuizTfsData
import com.doubtnutapp.quiztfs.repository.QuizTfsRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class QuizTfsSolutionViewModel @Inject constructor(
    private val quizTfsRepository: QuizTfsRepository,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _quizQnaInfoApiLiveData: MutableLiveData<Outcome<QuizTfsData>> =
        MutableLiveData()

    val quizQnaInfoApiLiveData: LiveData<Outcome<QuizTfsData>>
        get() = _quizQnaInfoApiLiveData

    private fun startLoading() {
        _quizQnaInfoApiLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _quizQnaInfoApiLiveData.value = Outcome.loading(false)
    }

    fun getQuizSolution(
        id: String,
        date: String
    ) {
        startLoading()
        viewModelScope.launch {
            quizTfsRepository.getQuizSolution(id, date)
                .map {
                    it.data
                }
                .catch {
                    stopLoading()
                    onError(it)
                }.collect {
                    stopLoading()
                    _quizQnaInfoApiLiveData.value = Outcome.success(it)
                }
        }
    }

    private fun onError(error: Throwable) {
        _quizQnaInfoApiLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }
}