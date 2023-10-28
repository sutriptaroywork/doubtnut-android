package com.doubtnutapp.shorts.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.shorts.viewmodel.ShortsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ShortsModule {

    @Binds
    @IntoMap
    @ViewModelKey(ShortsViewModel::class)
    abstract fun bindViewModel(viewModel: ShortsViewModel): ViewModel

}