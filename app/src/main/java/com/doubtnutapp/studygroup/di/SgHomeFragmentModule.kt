package com.doubtnutapp.studygroup.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.studygroup.viewmodel.SgHomeViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SgHomeFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(SgHomeViewModel::class)
    abstract fun bindViewModel(viewModel: SgHomeViewModel): ViewModel
}