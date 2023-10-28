package com.doubtnutapp.videoPage.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.data.addtoplaylist.repository.AddToPlaylistRepositoryImpl
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.gamification.gamePoints.repository.GamePointsRepositoryImpl
import com.doubtnutapp.data.gamification.gamePoints.service.GamePointsService
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandlerImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.addtoplaylist.repository.AddToPlaylistRepository
import com.doubtnutapp.domain.gamification.gamePoints.repository.GamePointsRepository
import com.doubtnutapp.gamification.gamepoints.ui.viewmodel.GamePointsViewModel
import com.doubtnutapp.sharing.di.WhatsAppSharingBindModule
import com.doubtnutapp.similarVideo.viewmodel.NcertSimilarViewModel
import com.doubtnutapp.videoPage.viewmodel.VideoPageViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module(includes = [WhatsAppSharingBindModule::class])
abstract class VideoPageActivityBindModule {

    @Binds
    @IntoMap
    @ViewModelKey(GamePointsViewModel::class)
    abstract fun bindGamePointsViewModel(badgesViewModel: GamePointsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NcertSimilarViewModel::class)
    abstract fun bindNcertSimilarViewModel(ncertSimilarViewModel: NcertSimilarViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VideoPageViewModel::class)
    @PerActivity
    abstract fun bindVideoPageActivityViewModel(videoPageViewModel: VideoPageViewModel): ViewModel


    @Binds
    @PerActivity
    abstract fun bindNetworkErrorHandler(networkErrorHandlerImpl: NetworkErrorHandlerImpl): NetworkErrorHandler

    @Binds
    @PerActivity
    abstract fun bindGamePointsRepository(gamePointsRepositoryImpl: GamePointsRepositoryImpl): GamePointsRepository


    @Binds
    @PerActivity
    abstract fun bindAddToPlaylistRepository(addToPlaylistRepositoryImpl: AddToPlaylistRepositoryImpl): AddToPlaylistRepository


    @Module
    companion object {

        @Provides
        @PerActivity
        @JvmStatic
        fun provideGamePointsService(@ApiRetrofit retrofit: Retrofit): GamePointsService =
                retrofit.create(GamePointsService::class.java)

    }
}