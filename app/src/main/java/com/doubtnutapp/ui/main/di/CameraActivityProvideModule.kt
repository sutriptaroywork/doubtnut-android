package com.doubtnutapp.ui.main.di

import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.camerascreen.apiService.CameraScreenApiService
import com.doubtnutapp.camera.service.CropQuestionService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
object CameraActivityProvideModule {

    @Provides
    @JvmStatic
    fun provideCameraServiceApi(@ApiRetrofit retrofit: Retrofit) = retrofit.create(CameraScreenApiService::class.java)

    @Provides
    @JvmStatic
    fun provideCropQuestionService(@ApiRetrofit retrofit: Retrofit) = retrofit.create(
        CropQuestionService::class.java)
}