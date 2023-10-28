package com.doubtnutapp.delegation.networkerror

interface NetworkErrorHandler {
    fun unAuthorizeUserError()
    fun onApiError(e: Throwable)
    fun ioExceptionHandler()
}
