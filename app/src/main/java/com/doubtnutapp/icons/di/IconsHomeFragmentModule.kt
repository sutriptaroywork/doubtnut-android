package com.doubtnutapp.icons.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.icons.ui.viewmodel.IconsHomeFragmentVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class IconsHomeFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(IconsHomeFragmentVM::class)
    abstract fun bindViewModel(viewModel: IconsHomeFragmentVM): ViewModel
}