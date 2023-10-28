package com.doubtnutapp.matchquestion.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.camerascreen.datasource.CropScreenConfigDataSource
import com.doubtnutapp.data.camerascreen.datasource.LocalCropScreenConfigDataSource
import com.doubtnutapp.camera.service.CropQuestionRepositoryImpl
import com.doubtnutapp.camera.service.CropQuestionService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.camera.service.CropQuestionRepository
import com.doubtnutapp.matchquestion.viewmodel.CropQuestionViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by Sachin Saxena on 2020-10-19.
 */
@Module
abstract class CropQuestionActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(CropQuestionViewModel::class)
    abstract fun bindCropQuestionViewModel(viewModel: CropQuestionViewModel): ViewModel

    @Module
    companion object {
        @Provides
        @PerActivity
        @JvmStatic
        fun provideCropQuestionService(@ApiRetrofit retrofit: Retrofit): CropQuestionService {
            return retrofit.create(CropQuestionService::class.java)
        }
    }

    @Binds
    abstract fun bindCropQuestionRepository(cropQuestionRepositoryImpl: CropQuestionRepositoryImpl): CropQuestionRepository

    @Binds
    internal abstract fun bindCropScreenDataSource(configDataSources: LocalCropScreenConfigDataSource): CropScreenConfigDataSource
}