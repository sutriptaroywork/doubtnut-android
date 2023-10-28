package com.doubtnutapp.di.module

import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.newlibrary.service.LibraryHomeService
import com.doubtnutapp.data.videoPage.service.VideoPageService
import com.doubtnutapp.scheduledquiz.di.remote.api.services.ScheduledQuizNotificationService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
object ApiServiceModule {

    @Provides
    @JvmStatic
    fun providerLibraryHomeService(@ApiRetrofit retrofit: Retrofit): LibraryHomeService {
        return retrofit.create(LibraryHomeService::class.java)
    }

    @Provides
    @JvmStatic
    fun providerVideoService(@ApiRetrofit retrofit: Retrofit): VideoPageService {
        return retrofit.create(VideoPageService::class.java)
    }

    @Provides
    @JvmStatic
    fun provideScheduledNotifService(@ApiRetrofit retrofit: Retrofit): ScheduledQuizNotificationService {
        return retrofit.create(ScheduledQuizNotificationService::class.java)
    }
}
