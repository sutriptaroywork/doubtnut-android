package com.doubtnut.core.utils

import androidx.lifecycle.MutableLiveData
import com.doubtnut.core.data.remote.Outcome
import retrofit2.HttpException
import java.net.HttpURLConnection

object NetworkApiUtils {

    fun <T> onApiError(
        mutableLiveData: MutableLiveData<Outcome<T>>,
        error: Throwable
    ) {

        mutableLiveData.value = Outcome.loading(false)
        mutableLiveData.value = if (error is HttpException) {
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