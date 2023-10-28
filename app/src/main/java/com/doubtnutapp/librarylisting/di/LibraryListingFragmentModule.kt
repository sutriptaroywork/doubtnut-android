package com.doubtnutapp.librarylisting.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandlerImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerFragment
import com.doubtnutapp.librarylisting.viewmodel.LibraryListingViewModel
import com.doubtnutapp.likeDislike.di.LikeDislikeVideoBindModule
import com.doubtnutapp.sharing.di.WhatsAppSharingBindModule
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
@Module(includes = [WhatsAppSharingBindModule::class, LikeDislikeVideoBindModule::class])
abstract class LibraryListingFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(LibraryListingViewModel::class)
    abstract fun bindLibraryListViewModel(libraryListingViewModel: LibraryListingViewModel): ViewModel

    @Binds
    @PerFragment
    abstract fun bindNetworkErrorHandler(networkErrorHandlerImpl: NetworkErrorHandlerImpl): NetworkErrorHandler

}