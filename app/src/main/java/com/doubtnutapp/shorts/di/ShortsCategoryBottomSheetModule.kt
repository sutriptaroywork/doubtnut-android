package com.doubtnutapp.shorts.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.shorts.viewmodel.ShortsCategoryBottomSheetVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ShortsCategoryBottomSheetModule {

    @Binds
    @IntoMap
    @ViewModelKey(ShortsCategoryBottomSheetVM::class)
    abstract fun bindViewModel(viewModel: ShortsCategoryBottomSheetVM): ViewModel
}