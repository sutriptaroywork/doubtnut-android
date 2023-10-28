package com.doubtnutapp.doubtpecharcha.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.doubtpecharcha.viewmodel.DoubtCollectionViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class P2PDoubtCollectionFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(DoubtCollectionViewModel::class)
    abstract fun bindViewModel(viewModel: DoubtCollectionViewModel): ViewModel
}
