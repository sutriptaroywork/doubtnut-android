package com.doubtnutapp.downloadedVideos

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class DownloadVideoActivityModule : BaseActivityModule<DownloadedVideosActivity>() {
    @Binds
    @IntoMap
    @ViewModelKey(DownloadedVideosViewModel::class)
    internal abstract fun bindDownloadedVideosViewModel(downloadedVideosViewModel: DownloadedVideosViewModel): ViewModel
}
