package com.doubtnutapp.newlibrary.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.newlibrary.viewmodel.LibrarySortByYearFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 27/11/21
 */

@Module
abstract class LibrarySortByYearFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(LibrarySortByYearFragmentViewModel::class)
    abstract fun bind(viewModel: LibrarySortByYearFragmentViewModel): ViewModel
}