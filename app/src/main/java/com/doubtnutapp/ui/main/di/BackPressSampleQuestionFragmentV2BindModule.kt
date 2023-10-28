package com.doubtnutapp.ui.main.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.data.camerascreen.datasource.ConfigDataSources
import com.doubtnutapp.data.camerascreen.datasource.CropScreenConfigDataSource
import com.doubtnutapp.data.camerascreen.datasource.LocalConfigDataSource
import com.doubtnutapp.data.camerascreen.datasource.LocalCropScreenConfigDataSource
import com.doubtnutapp.data.camerascreen.repository.CameraScreenRepositoryImpl
import com.doubtnutapp.camera.service.CropQuestionRepositoryImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.domain.camerascreen.repository.CameraScreenRepository
import com.doubtnutapp.camera.service.CropQuestionRepository
import com.doubtnutapp.camera.viewmodel.CameraActivityViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class BackPressSampleQuestionFragmentV2BindModule {

    @Binds
    @IntoMap
    @ViewModelKey(CameraActivityViewModel::class)
    abstract fun bindViewModel(viewModel: CameraActivityViewModel): ViewModel

    @Binds
    abstract fun bindCameraRepository(cameraScreenRepositoryImpl: CameraScreenRepositoryImpl): CameraScreenRepository

    @Binds
    abstract fun bindCropQuestionRepository(cropQuestionRepositoryImpl: CropQuestionRepositoryImpl): CropQuestionRepository

    @Binds
    internal abstract fun bindConfigDataSource(configDataSources: LocalConfigDataSource): ConfigDataSources

    @Binds
    internal abstract fun bindCropScreenConfigDataSource(configDataSources: LocalCropScreenConfigDataSource): CropScreenConfigDataSource
}