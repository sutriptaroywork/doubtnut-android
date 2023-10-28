package com.doubtnutapp

import android.app.Application
import okhttp3.OkHttpClient

object StethoUtils {

    const val TAG = "com.doubtnutapp.StethoUtils"

    @JvmStatic
    fun install(application: Application) {
    }

    @JvmStatic
    fun addNetworkInterceptor(okHttpClientBuilder: OkHttpClient.Builder) {
    }
}
