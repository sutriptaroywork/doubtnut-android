package com.doubtnutapp.dictionary.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dictionary.viewmodel.DictionaryActivityViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class DictionaryActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(DictionaryActivityViewModel::class)
    abstract fun bindViewModel(viewModel: DictionaryActivityViewModel): ViewModel
}
