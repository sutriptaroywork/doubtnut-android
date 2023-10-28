package com.doubtnut.olympiad.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.olympiad.ui.viewmodel.OlympiadActivityVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class OlympiadActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(OlympiadActivityVM::class)
    abstract fun bindViewModel(viewModel: OlympiadActivityVM): ViewModel
}