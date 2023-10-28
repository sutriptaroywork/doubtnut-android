package com.doubtnutapp.base.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.doubtnutapp.data.Outcome

inline fun <T> LiveData<Outcome<T>>.observeK(
    owner: LifecycleOwner,
    crossinline success: (T) -> Unit = {},
    crossinline apiError: (Throwable) -> Unit = {},
    crossinline unAuthorizedCallback: () -> Unit = {},
    crossinline ioExceptionCallback: () -> Unit = {},
    crossinline progressStateCallback: (Boolean) -> Unit = {},
    crossinline connectionTimeoutCallback: () -> Unit = {}
) {
    this.observe(owner) {
        when (it) {
            is Outcome.Success -> success(it.data)
            is Outcome.Failure -> ioExceptionCallback()
            is Outcome.BadRequest -> unAuthorizedCallback()
            is Outcome.ApiError -> apiError(it.e)
            is Outcome.Progress -> progressStateCallback(it.loading)
        }
    }
}

inline fun <T> LiveData<Outcome<T>>.observeL(
    owner: LifecycleOwner,
    crossinline success: (T) -> Unit = {},
    crossinline apiError: (Throwable) -> Unit = {},
    crossinline unAuthorizedCallback: () -> Unit = {},
    crossinline ioExceptionCallback: (Throwable) -> Unit = {},
    crossinline progressStateCallback: (Boolean) -> Unit = {}
) {
    this.observe(owner) {
        when (it) {
            is Outcome.Success -> success(it.data)
            is Outcome.Failure -> ioExceptionCallback(it.e)
            is Outcome.BadRequest -> unAuthorizedCallback()
            is Outcome.ApiError -> apiError(it.e)
            is Outcome.Progress -> progressStateCallback(it.loading)
        }
    }
}
