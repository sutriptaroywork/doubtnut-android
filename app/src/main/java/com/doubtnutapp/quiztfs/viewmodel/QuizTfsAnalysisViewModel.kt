package com.doubtnutapp.quiztfs.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.quiztfs.AnalysisData
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
 * Created by Mehul Bisht on 10-09-2021
 */

class QuizTfsAnalysisViewModel @Inject constructor(
    private val quizTfsRepository: QuizTfsRepository,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _analysisData: MutableLiveData<Outcome<AnalysisData>> = MutableLiveData()
    val analysisData: LiveData<Outcome<AnalysisData>> get() = _analysisData

    fun fetchAnalysisData(page: Int, date: String?, filter: String?) {
        startLoading()
        viewModelScope.launch {
            quizTfsRepository.getQuizTfsAnalysisData(page, date, filter)
                .map { it.data }
                .catch {
                    onError(it)
                    stopLoading()
                }
                .collect {
                    _analysisData.value = Outcome.success(it)
                    stopLoading()
                }
        }
    }

    private fun onError(error: Throwable) {
        _analysisData.value = if (error is HttpException) {
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
        _analysisData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _analysisData.value = Outcome.loading(false)
    }
}