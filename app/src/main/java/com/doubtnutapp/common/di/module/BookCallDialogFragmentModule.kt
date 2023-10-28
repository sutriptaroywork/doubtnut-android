package com.doubtnutapp.common.di.module

import androidx.lifecycle.ViewModel
import com.doubtnutapp.common.ui.dialog.BookCallDialogFragmentVM
import com.doubtnut.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class BookCallDialogFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(BookCallDialogFragmentVM::class)
    abstract fun bindBookCallDialogFragmentVM(viewModel: BookCallDialogFragmentVM): ViewModel
}
