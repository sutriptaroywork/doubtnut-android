package com.doubtnutapp.ui.common.address

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SubmitAddressDialogFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(SubmitAddressDialogFragmentVM::class)
    abstract fun bind(viewModel: SubmitAddressDialogFragmentVM): ViewModel
}