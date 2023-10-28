package com.doubtnutapp.ui.cameraGuide.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.ui.cameraGuide.CameraGuideViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Anand Gaurav on 2019-07-23.
 */
@Module
abstract class CameraGuideModule {

    @Binds
    @IntoMap
    @ViewModelKey(CameraGuideViewModel::class)
    abstract fun bindCameraGuideViewModel(timelineViewModel: CameraGuideViewModel): ViewModel
}