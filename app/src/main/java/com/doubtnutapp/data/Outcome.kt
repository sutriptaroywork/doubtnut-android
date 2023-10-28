package com.doubtnutapp.data

sealed class Outcome<T> {

    data class Progress<T>(var loading: Boolean) : Outcome<T>()
    data class Success<T>(var data: T) : Outcome<T>()
    data class Failure<T>(val e: Throwable) : Outcome<T>()
    data class ApiError<T>(val e: Throwable) : Outcome<T>()
    data class BadRequest<T>(val data: String) : Outcome<T>()

    companion object {

        fun <T> loading(isLoading: Boolean): Outcome<T> = Progress(isLoading)

        fun <T> success(data: T): Success<T> = Success(data)

        fun <T> failure(e: Throwable): Outcome<T> = Failure(e)

        fun <T> apiError(e: Throwable): Outcome<T> = ApiError(e)
        fun <T> badRequest(data: String): Outcome<T> = BadRequest(data)
    }
}
