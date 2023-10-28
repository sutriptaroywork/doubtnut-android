package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.viewmodel.VideoBlockedViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class VideoBlockedFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(VideoBlockedViewModel::class)
    abstract fun bindViewModel(viewModel: VideoBlockedViewModel): ViewModel

}