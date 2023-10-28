package com.doubtnut.core.dummy

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class CoreDummyModule {

    @Binds
    @IntoMap
    @ViewModelKey(CoreDummyVM::class)
    abstract fun bindDummyViewModel(dummyViewModel: CoreDummyVM): ViewModel
}
