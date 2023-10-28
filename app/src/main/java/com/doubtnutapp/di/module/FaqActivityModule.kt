package com.doubtnutapp.di.module

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.faq.viewmodel.FaqViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class FaqActivityModule {
    @Binds
    @IntoMap
    @ViewModelKey(FaqViewModel::class)
    abstract fun bindViewModel(viewModel: FaqViewModel): ViewModel
}
