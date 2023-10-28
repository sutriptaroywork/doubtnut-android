package com.doubtnutapp.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.auth.respository.AuthRepository
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _googleAuthLiveData: MutableLiveData<Outcome<Any>> =
        MutableLiveData()

    val googleAuthLiveData: LiveData<Outcome<Any>>
        get() = _googleAuthLiveData

    private fun startLoading() {
        _googleAuthLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _googleAuthLiveData.value = Outcome.loading(false)
    }

    /**fun verifyGoogleAuth(
        token: String
    ) {
        startLoading()
        viewModelScope.launch {
            authRepository.verifyGoogleAuth(token)
                .map {
                    it.data
                }
                .catch {
                    stopLoading()
                    onError(it)
                }.collect {
                    stopLoading()
                    _googleAuthLiveData.value = Outcome.success(it)
                }
        }
    }*/

    private fun onError(error: Throwable) {
        _googleAuthLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                // TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }
}
