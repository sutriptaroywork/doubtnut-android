package com.doubtnutapp.freeclasses.module

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.freeclasses.viewmodels.FilterListBottomSheetDialogVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Akshat Jindal on 01/02/22.
 */
@Module
abstract class FilterListBottomSheetDialogFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(FilterListBottomSheetDialogVM::class)
    abstract fun providesFilterListBottomSheetDialogFragmentVM(viewModel: FilterListBottomSheetDialogVM): ViewModel
}