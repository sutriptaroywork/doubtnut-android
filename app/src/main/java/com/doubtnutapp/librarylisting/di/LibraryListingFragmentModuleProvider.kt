package com.doubtnutapp.librarylisting.di

import com.doubtnut.core.di.scope.PerFragment
import com.doubtnutapp.librarylisting.ui.LibraryListingFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Created by Anand Gaurav on 2019-10-07.
 */
@Module
abstract class LibraryListingFragmentModuleProvider {

    @ContributesAndroidInjector(modules = [LibraryListingFragmentModule::class])
    @PerFragment
    abstract fun contributeLibraryListFragmentInjector(): LibraryListingFragment
}

