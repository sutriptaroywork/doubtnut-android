package com.doubtnutapp.matchquestion.di

import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.videoPage.service.VideoPageService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

/**
 * Created by Sachin Saxena on 14/6/19.
 */
@Module
abstract class MatchPageFeedbackFragmentModule {

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideVideoPageService(@ApiRetrofit retrofit: Retrofit): VideoPageService =
            retrofit.create(VideoPageService::class.java)

    }

}