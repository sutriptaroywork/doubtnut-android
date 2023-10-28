package com.doubtnutapp.quiztfs.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.DoubtnutNetworkException
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.quiztfs.QuizTfsSubmitResponse
import com.doubtnutapp.quiztfs.repository.QuizTfsRepository
import com.doubtnutapp.utils.Event
import com.google.gson.GsonBuilder
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class QuizTfsFragmentViewModel @Inject constructor(
    private val quizTfsRepository: QuizTfsRepository,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _messageLiveData: MutableLiveData<Event<String>> = MutableLiveData()
    val messageLiveData: LiveData<Event<String>>
        get() = _messageLiveData

    private val _quizSubmitLiveData: MutableLiveData<Outcome<Pair<QuizTfsSubmitResponse, List<String>>>> =
        MutableLiveData()

    val quizSubmitLiveData: LiveData<Outcome<Pair<QuizTfsSubmitResponse, List<String>>>>
        get() = _quizSubmitLiveData

    private fun startLoading() {
        _quizSubmitLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _quizSubmitLiveData.value = Outcome.loading(false)
    }

    fun submitQuiz(map: HashMap<String, Any>, answer: List<String>) {
        map["answerSelected"] = answer
        startLoading()
        viewModelScope.launch {
            quizTfsRepository.submitQuiz(map)
                .map {
                    it.data
                }
                .catch {
                    stopLoading()
                    onError(it)
                }.collect {
                    stopLoading()
                    _quizSubmitLiveData.value = Outcome.success(Pair(it, answer))
                }
        }
    }

    private fun onError(error: Throwable) {
        try {
            if (error is HttpException) {
                val networkException = GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create()
                    .fromJson(
                        error.response()?.errorBody()?.string(),
                        DoubtnutNetworkException::class.java
                    )
                if (networkException.meta.message.isBlank().not()) {
                    _messageLiveData.value = Event(networkException.meta.message)
                    return
                }
            }
        } catch (e: Exception) {

        }

        _quizSubmitLiveData.value = if (error is HttpException) {
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
}