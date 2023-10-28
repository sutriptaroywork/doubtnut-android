package com.doubtnutapp.quiztfs.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.quiztfs.DailyPracticeData
import com.doubtnutapp.quiztfs.repository.DailyPracticeRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 01-09-2021
 */
class DailyPracticeViewModel @Inject constructor(
    private val dailyPracticeRepository: DailyPracticeRepository,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _dailyPracticeData: MutableLiveData<Outcome<DailyPracticeData>> = MutableLiveData()
    val dailyPracticeData: LiveData<Outcome<DailyPracticeData>> get() = _dailyPracticeData

    fun fetch(type: String) {
        startLoading()
        viewModelScope.launch {
            dailyPracticeRepository.getDailyPracticeData(type)
                .map { it.data }
                .catch {
                    onError(it)
                    stopLoading()
                }
                .collect {
                    _dailyPracticeData.value = Outcome.success(it)
                    stopLoading()
                }
        }
    }

    private fun onError(error: Throwable) {
        _dailyPracticeData.value = if (error is HttpException) {
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
        _dailyPracticeData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _dailyPracticeData.value = Outcome.loading(false)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}