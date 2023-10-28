package com.doubtnut.noticeboard.di

import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnut.core.di.qualifier.MicroApiRetrofit
import com.doubtnut.noticeboard.data.remote.NoticeBoardMicroService
import com.doubtnut.noticeboard.data.remote.NoticeBoardService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
object NoticeBoardModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideNoticeBoardService(@ApiRetrofit retrofit: Retrofit): NoticeBoardService =
        retrofit.create(NoticeBoardService::class.java)

    @Provides
    @JvmStatic
    @Singleton
    fun provideNoticeBoardMicroService(@MicroApiRetrofit retrofit: Retrofit): NoticeBoardMicroService =
        retrofit.create(NoticeBoardMicroService::class.java)

}