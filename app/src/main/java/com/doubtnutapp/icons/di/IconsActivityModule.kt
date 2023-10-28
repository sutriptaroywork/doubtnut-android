package com.doubtnutapp.icons.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.icons.ui.viewmodel.IconsActivityVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class IconsActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(IconsActivityVM::class)
    abstract fun bindViewModel(viewModel: IconsActivityVM): ViewModel
}