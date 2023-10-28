package com.doubtnutapp.dnr.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dnr.viewmodel.DnrHomeFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class DnrHomeFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(DnrHomeFragmentViewModel::class)
    abstract fun bindViewModel(dnrHomeFragmentViewModel: DnrHomeFragmentViewModel): ViewModel
}
