package com.doubtnutapp.textsolution.di

import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.textsolution.service.TextSolutionService
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.videoPage.di.VideoPageActivityProvideModule
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
@Module(includes = [VideoPageActivityProvideModule::class])
object TextSolutionActivityProviderModule {

    @Provides
    @JvmStatic
    @PerActivity
    fun provideTextSolutionService(@ApiRetrofit retrofit: Retrofit): TextSolutionService =
            retrofit.create(TextSolutionService::class.java)
}