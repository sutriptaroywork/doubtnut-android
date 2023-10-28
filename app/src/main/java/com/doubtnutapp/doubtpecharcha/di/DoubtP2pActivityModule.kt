package com.doubtnutapp.doubtpecharcha.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.doubtpecharcha.viewmodel.DoubtP2pViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class DoubtP2pActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(DoubtP2pViewModel::class)
    abstract fun bindDoubtP2pViewModel(viewModel: DoubtP2pViewModel): ViewModel
}
