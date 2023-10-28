package com.doubtnutapp.videoPage.di

import com.doubtnutapp.data.addtoplaylist.service.AddToPlaylistService
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.videoPage.service.VideoPageService
import com.doubtnut.core.di.scope.PerActivity
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
object VideoPageActivityProvideModule {

    @Provides
    @JvmStatic
    @PerActivity
    fun provideVideoPageService(@ApiRetrofit retrofit: Retrofit): VideoPageService =
            retrofit.create(VideoPageService::class.java)

    @Provides
    @JvmStatic
    @PerActivity
    fun providerAddToPlaylistService(@ApiRetrofit retrofit: Retrofit): AddToPlaylistService {
        return retrofit.create(AddToPlaylistService::class.java)
    }


}