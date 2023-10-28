package com.doubtnutapp.bottomsheet

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class BaseWidgetPaginatedBottomSheetDialogFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(BaseWidgetPaginatedBottomSheetDialogFragmentVM::class)
    abstract fun bindBaseWidgetPaginatedBottomSheetDialogFragmentVM(viewModel: BaseWidgetPaginatedBottomSheetDialogFragmentVM): ViewModel
}
