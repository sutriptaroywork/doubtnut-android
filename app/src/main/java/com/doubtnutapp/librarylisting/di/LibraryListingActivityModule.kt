package com.doubtnutapp.librarylisting.di

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.doubtnutapp.data.newlibrary.repository.LibraryHistoryRepositoryImpl
import com.doubtnutapp.data.newlibrary.repository.LibraryListingRepositoryImpl
import com.doubtnutapp.data.newlibrary.service.LibraryHomeService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.newlibrary.repository.LibraryHistoryRepository
import com.doubtnutapp.domain.newlibrary.repository.LibraryListingRepository
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.librarylisting.viewmodel.LibraryListingCommonViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
@Module
abstract class LibraryListingActivityModule {

    @Binds
    @PerActivity
    abstract fun provides(activity: LibraryListingActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(LibraryListingCommonViewModel::class)
    abstract fun bindLibraryListViewModel(libraryListViewModel: LibraryListingCommonViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindRepository(libraryListingRepositoryImpl: LibraryListingRepositoryImpl): LibraryListingRepository

    @Binds
    @PerActivity
    abstract fun bindHistoryRepository(libraryHistoryRepositoryImpl: LibraryHistoryRepositoryImpl): LibraryHistoryRepository

}