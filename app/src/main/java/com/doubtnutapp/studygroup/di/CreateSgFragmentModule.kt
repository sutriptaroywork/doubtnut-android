package com.doubtnutapp.studygroup.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.studygroup.viewmodel.SgCreateViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class CreateSgFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(SgCreateViewModel::class)
    abstract fun bindViewModel(createViewModel: SgCreateViewModel): ViewModel
}