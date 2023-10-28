package com.doubtnutapp.utils

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dummy.DummyViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 04-10-2021
 */

@Module
abstract class VideoDownloadOptionsDialogModule {

    @Binds
    @IntoMap
    @ViewModelKey(DummyViewModel::class)
    abstract fun bind(dummyViewModel: DummyViewModel): ViewModel
}