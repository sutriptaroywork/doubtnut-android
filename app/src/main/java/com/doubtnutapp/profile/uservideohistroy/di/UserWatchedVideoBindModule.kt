package com.doubtnutapp.profile.uservideohistroy.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.data.addtoplaylist.repository.AddToPlaylistRepositoryImpl
import com.doubtnutapp.data.addtoplaylist.service.AddToPlaylistService
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.profile.watchedvideo.repository.WatchedVideosRepositoryImpl
import com.doubtnutapp.data.profile.watchedvideo.service.WatchedVideoService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.addtoplaylist.repository.AddToPlaylistRepository
import com.doubtnutapp.domain.profile.watchedvideo.repository.WatchedVideoRepository
import com.doubtnutapp.profile.uservideohistroy.ui.UserWatchedVideoViewModel
import com.doubtnutapp.sharing.di.WhatsAppSharingBindModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module(includes = [WhatsAppSharingBindModule::class])
abstract class UserWatchedVideoBindModule {

    @Binds
    @IntoMap
    @ViewModelKey(UserWatchedVideoViewModel::class)
    @PerActivity
    abstract fun bindUserWatchedVideoViewModel(userWatchedVideoViewModel: UserWatchedVideoViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindWatchedVideosRepository(watchedVideosRepositoryImpl: WatchedVideosRepositoryImpl): WatchedVideoRepository


    @Binds
    @PerActivity
    abstract fun bindAddToPlaylistRepository(addToPlaylistRepositoryImpl: AddToPlaylistRepositoryImpl): AddToPlaylistRepository


    @Module
    companion object {

        @Provides
        @JvmStatic
        @PerActivity
        fun provideWatchedVideoService(@ApiRetrofit retrofit: Retrofit): WatchedVideoService = retrofit.create(WatchedVideoService::class.java)

        @Provides
        @JvmStatic
        fun providerAddToPlaylistService(@ApiRetrofit retrofit: Retrofit): AddToPlaylistService {
            return retrofit.create(AddToPlaylistService::class.java)
        }
    }

}