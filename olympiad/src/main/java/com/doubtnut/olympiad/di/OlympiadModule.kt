package com.doubtnut.olympiad.di

import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnut.olympiad.data.remote.OlympiadService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
object OlympiadModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideOlympiadService(@ApiRetrofit retrofit: Retrofit): OlympiadService =
        retrofit.create(OlympiadService::class.java)
}