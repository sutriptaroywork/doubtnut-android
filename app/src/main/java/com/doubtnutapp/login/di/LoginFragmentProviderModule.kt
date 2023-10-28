package com.doubtnutapp.login.di

import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.ui.splash.SplashService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
object LoginFragmentProviderModule {

    @Provides
    @JvmStatic
    fun providerSplashService(@ApiRetrofit retrofit: Retrofit): SplashService {
        return retrofit.create(SplashService::class.java)
    }
}