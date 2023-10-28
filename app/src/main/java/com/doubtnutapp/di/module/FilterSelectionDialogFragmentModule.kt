package com.doubtnutapp.di.module

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.ui.dialog.FilterSelectionDialogFragmentVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class FilterSelectionDialogFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(FilterSelectionDialogFragmentVM::class)
    abstract fun bindViewModel(viewModel: FilterSelectionDialogFragmentVM): ViewModel
}
