package com.doubtnutapp.addtoplaylist.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.addtoplaylist.AddToPlaylistViewModel
import com.doubtnutapp.data.addtoplaylist.repository.AddToPlaylistRepositoryImpl
import com.doubtnutapp.data.addtoplaylist.service.AddToPlaylistService
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.domain.addtoplaylist.repository.AddToPlaylistRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by Anand Gaurav on 2019-11-22.
 */
@Module()
abstract class AddToPlaylistModule {
    @Module
    companion object {
        @Provides
        @JvmStatic
        fun providerAddToPlaylistService(@ApiRetrofit retrofit: Retrofit): AddToPlaylistService {
            return retrofit.create(AddToPlaylistService::class.java)
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(AddToPlaylistViewModel::class)
    abstract fun bindAddToPlaylistViewModel(addToPlaylistViewModel: AddToPlaylistViewModel): ViewModel

    @Binds
    abstract fun bindAddToPlaylistRepository(addToPlaylistRepositoryImpl: AddToPlaylistRepositoryImpl): AddToPlaylistRepository
}
