package com.doubtnut.core.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.doubtnut.core.data.remote.Outcome

fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, observer: (t: T) -> Unit) {
    this.observe(owner) {
        it?.let(observer)
    }
}

inline fun <T> LiveData<Outcome<T>>.observeK2(
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