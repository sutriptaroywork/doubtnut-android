package com.doubtnutapp.dummy

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Deprecated("use CoreDummyVM and CoreDummyModule")
@Module
abstract class DummyModule {

    @Binds
    @IntoMap
    @ViewModelKey(DummyViewModel::class)
    abstract fun bindDummyViewModel(dummyViewModel: DummyViewModel): ViewModel
}
