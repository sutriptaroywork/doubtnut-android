package com.doubtnut.olympiad.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.olympiad.ui.viewmodel.OlympiadSuccessFragmentVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class OlympiadSuccessFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(OlympiadSuccessFragmentVM::class)
    abstract fun bindViewModel(viewModel: OlympiadSuccessFragmentVM): ViewModel
}