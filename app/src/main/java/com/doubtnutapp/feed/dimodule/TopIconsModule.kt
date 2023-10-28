package com.doubtnutapp.feed.dimodule

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.feed.viewmodel.TopIconsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 18-09-2021
 */

@Module
abstract class TopIconsModule {

    @Binds
    @IntoMap
    @ViewModelKey(TopIconsViewModel::class)
    abstract fun provideTopIconsViewModel(topIconsViewModel: TopIconsViewModel): ViewModel
}