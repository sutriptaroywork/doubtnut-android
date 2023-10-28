package com.doubtnutapp.di.module

import androidx.lifecycle.ViewModelProvider
import com.doubtnutapp.base.ViewModelFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelProviderModule {

    @Binds
    internal abstract fun bindViewFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}
