package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.viewmodel.VideoStatusViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class VideoStatusFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(VideoStatusViewModel::class)
    abstract fun bindViewModel(viewModel: VideoStatusViewModel): ViewModel

}