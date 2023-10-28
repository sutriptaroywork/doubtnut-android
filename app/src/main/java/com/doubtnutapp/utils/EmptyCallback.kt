package com.doubtnutapp.utils

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * It is used as placeholder callback class when we to implement fire and forget strategy for RetroFit
 * Call.enqueue()
 */
class EmptyCallback<T> : Callback<T> {
    override fun onFailure(call: Call<T>?, t: Throwable?) {
    }

    override fun onResponse(call: Call<T>?, response: Response<T>?) {
    }
}