package com.doubtnutapp.widgettest.model

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Initial<T> : Resource<T>()
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}