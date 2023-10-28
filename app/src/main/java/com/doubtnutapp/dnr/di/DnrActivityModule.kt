package com.doubtnutapp.dnr.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dnr.viewmodel.DnrActivityViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class DnrActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(DnrActivityViewModel::class)
    abstract fun bindViewModel(dnrActivityViewModel: DnrActivityViewModel): ViewModel
}
