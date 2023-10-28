package com.doubtnut.olympiad.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.olympiad.ui.viewmodel.OlympiadRegisterFragmentVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class OlympiadRegisterFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(OlympiadRegisterFragmentVM::class)
    abstract fun bindViewModel(viewModel: OlympiadRegisterFragmentVM): ViewModel
}