package com.doubtnutapp.newlibrary.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.newlibrary.viewmodel.LibraryPreviousYearPapersViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 26/11/21
 */

@Module
abstract class LibraryPreviousYearPapersModule {

    @Binds
    @IntoMap
    @ViewModelKey(LibraryPreviousYearPapersViewModel::class)
    abstract fun bind(viewModel: LibraryPreviousYearPapersViewModel): ViewModel
}