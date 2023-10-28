package com.doubtnutapp.resourcelisting.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.data.addtoplaylist.repository.AddToPlaylistRepositoryImpl
import com.doubtnutapp.data.addtoplaylist.service.AddToPlaylistService
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.newlibrary.repository.LibraryHistoryRepositoryImpl
import com.doubtnutapp.data.resourcelisting.repository.ResourceListingRepositoryImpl
import com.doubtnutapp.data.resourcelisting.service.ResourceListingService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerFragment
import com.doubtnutapp.domain.addtoplaylist.repository.AddToPlaylistRepository
import com.doubtnutapp.domain.newlibrary.repository.LibraryHistoryRepository
import com.doubtnutapp.domain.resourcelisting.repository.ResourceListingRepository
import com.doubtnutapp.likeDislike.di.LikeDislikeVideoBindModule
import com.doubtnutapp.resourcelisting.viewmodel.ResourceListingViewModel
import com.doubtnutapp.sharing.di.WhatsAppSharingBindModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Module(includes = [WhatsAppSharingBindModule::class, LikeDislikeVideoBindModule::class])
abstract class ResourceListingModule {

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun providerResourceListingService(@ApiRetrofit retrofit: Retrofit): ResourceListingService {
            return retrofit.create(ResourceListingService::class.java)
        }

        @Provides
        @JvmStatic
        fun providerAddToPlaylistService(@ApiRetrofit retrofit: Retrofit): AddToPlaylistService {
            return retrofit.create(AddToPlaylistService::class.java)
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(ResourceListingViewModel::class)
    abstract fun bindResourceListingViewModel(resourceListingViewModel: ResourceListingViewModel): ViewModel

    @Binds
    abstract fun bindResourceListingRepository(resourceListingRepositoryImpl: ResourceListingRepositoryImpl): ResourceListingRepository

    @Binds
    abstract fun bindHistoryRepository(libraryHistoryRepositoryImpl: LibraryHistoryRepositoryImpl): LibraryHistoryRepository

    @Binds
    @PerFragment
    abstract fun bindAddToPlaylistRepository(addToPlaylistRepositoryImpl: AddToPlaylistRepositoryImpl): AddToPlaylistRepository

}