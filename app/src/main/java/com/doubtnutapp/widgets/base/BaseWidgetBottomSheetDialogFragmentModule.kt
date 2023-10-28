package com.doubtnutapp.widgets.base

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class BaseWidgetBottomSheetDialogFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(BaseWidgetBottomSheetDialogFragmentVM::class)
    abstract fun bindBaseWidgetBottomSheetDialogFragmentVM(viewModel: BaseWidgetBottomSheetDialogFragmentVM): ViewModel
}