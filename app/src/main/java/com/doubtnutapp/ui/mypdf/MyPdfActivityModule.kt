package com.doubtnutapp.ui.mypdf

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MyPdfActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(MyPdfViewModel::class)
    abstract fun bindViewModel(viewModel: MyPdfViewModel): ViewModel
}