package com.doubtnutapp.newlibrary.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.newlibrary.viewmodel.LibraryExamsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class LibraryExamBottomSheetModule {

    @Binds
    @IntoMap
    @ViewModelKey(LibraryExamsViewModel::class)
    abstract fun bindLibraryExamsViewModel(libraryExamsViewModel: LibraryExamsViewModel): ViewModel
}