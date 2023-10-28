package com.doubtnutapp.data.remote

import com.doubtnutapp.data.remote.util.LiveDataCallAdapterFactory
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Singleton
class NetworkBuilderFactory {

    // this same instance of okhttpclient should be used across the app
    // if we need to change builder configuration for certain requests, use
    // httpClientBuilderFactory.create()......build()
    private val okHttpClient by lazy { OkHttpClient() }

    fun okhttpBuilder(): OkHttpClient.Builder = okHttpClient.newBuilder()

    // retrofit without base url and associated client
    fun retrofitBuilder(gson: Gson) =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
}
