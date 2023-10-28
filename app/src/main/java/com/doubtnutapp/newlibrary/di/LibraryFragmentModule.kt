package com.doubtnutapp.newlibrary.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.newlibrary.viewmodel.LibraryViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class LibraryFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(LibraryViewModel::class)
    abstract fun bindViewModel(libraryViewModel: LibraryViewModel): ViewModel
}