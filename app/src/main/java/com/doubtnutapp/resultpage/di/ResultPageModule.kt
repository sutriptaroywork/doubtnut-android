package com.doubtnutapp.resultpage.di

import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.resultpage.repository.ResultPageRepository
import com.doubtnutapp.resultpage.repository.ResultPageRepositoryImpl
import com.doubtnutapp.resultpage.repository.ResultPageService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
object ResultPageModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideResultPageService(@ApiRetrofit retrofit: Retrofit): ResultPageService =
        retrofit.create(ResultPageService::class.java)

    @Provides
    @JvmStatic
    @Singleton
    fun provideResultPageRepository(
        resultPageService: ResultPageService
    ): ResultPageRepository = ResultPageRepositoryImpl(resultPageService)
}