package com.doubtnutapp.di.module

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.ui.dialog.ChooseExamBottomSheetDialogFragmentVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ChooseExamBottomSheetDialogFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(ChooseExamBottomSheetDialogFragmentVM::class)
    abstract fun bindViewModel(viewModel: ChooseExamBottomSheetDialogFragmentVM): ViewModel
}
