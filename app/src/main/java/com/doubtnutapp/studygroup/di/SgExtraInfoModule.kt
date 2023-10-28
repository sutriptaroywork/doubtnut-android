package com.doubtnutapp.studygroup.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.studygroup.viewmodel.SgExtraInfoViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SgExtraInfoModule {

    @Binds
    @IntoMap
    @ViewModelKey(SgExtraInfoViewModel::class)
    abstract fun bindViewModel(viewModel: SgExtraInfoViewModel): ViewModel
}