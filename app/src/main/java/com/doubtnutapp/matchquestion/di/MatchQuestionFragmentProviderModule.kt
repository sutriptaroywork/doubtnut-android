package com.doubtnutapp.matchquestion.di

import com.doubtnutapp.data.addtoplaylist.service.AddToPlaylistService
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.matchquestion.service.MatchQuestionService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
object MatchQuestionFragmentProviderModule {

    @Provides
    @JvmStatic
    fun provideMatchQuestionService2(@ApiRetrofit retrofit: Retrofit): MatchQuestionService =
        retrofit.create(MatchQuestionService::class.java)


    @Provides
    @JvmStatic
    fun providerAddToPlaylistService(@ApiRetrofit retrofit: Retrofit): AddToPlaylistService {
        return retrofit.create(AddToPlaylistService::class.java)
    }
}