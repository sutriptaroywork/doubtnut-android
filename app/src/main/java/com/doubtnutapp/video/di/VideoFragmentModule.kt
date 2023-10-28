package com.doubtnutapp.video.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.video.VideoFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class VideoFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(VideoFragmentViewModel::class)
    abstract fun bindVideoFragmentViewModel(videoFragmentViewModel: VideoFragmentViewModel): ViewModel

}