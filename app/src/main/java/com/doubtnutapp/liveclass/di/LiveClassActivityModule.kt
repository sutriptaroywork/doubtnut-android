package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.data.addtoplaylist.repository.AddToPlaylistRepositoryImpl
import com.doubtnutapp.data.addtoplaylist.service.AddToPlaylistService
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.addtoplaylist.repository.AddToPlaylistRepository
import com.doubtnutapp.liveclass.viewmodel.LiveClassViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by Anand Gaurav on 2020-02-22.
 */
@Module
abstract class LiveClassActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(LiveClassViewModel::class)
    abstract fun bindViewModel(viewModel: LiveClassViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindAddToPlaylistRepository(addToPlaylistRepositoryImpl: AddToPlaylistRepositoryImpl): AddToPlaylistRepository

    @Module
    companion object {

        @Provides
        @JvmStatic
        @PerActivity
        fun providerAddToPlaylistService(@ApiRetrofit retrofit: Retrofit): AddToPlaylistService {
            return retrofit.create(AddToPlaylistService::class.java)
        }

    }

}