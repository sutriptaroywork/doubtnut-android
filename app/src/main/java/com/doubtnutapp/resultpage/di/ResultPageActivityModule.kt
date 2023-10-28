package com.doubtnutapp.resultpage.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.resultpage.ui.ResultPageVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ResultPageActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(ResultPageVM::class)
    abstract fun bindViewModel(viewModel: ResultPageVM): ViewModel
}