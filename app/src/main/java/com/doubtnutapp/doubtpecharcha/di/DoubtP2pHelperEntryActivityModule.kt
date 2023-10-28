package com.doubtnutapp.doubtpecharcha.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.doubtpecharcha.viewmodel.DoubtP2pHelperEntryViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class DoubtP2pHelperEntryActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(DoubtP2pHelperEntryViewModel::class)
    abstract fun bindDoubtP2pHelperEntryViewModel(viewModel: DoubtP2pHelperEntryViewModel): ViewModel
}
