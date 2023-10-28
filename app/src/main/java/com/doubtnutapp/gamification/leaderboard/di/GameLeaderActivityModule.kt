package com.doubtnutapp.gamification.leaderboard.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.gamification.leaderboard.repository.GameLeaderBoardRepositoryImpl
import com.doubtnutapp.data.gamification.leaderboard.service.GameLeaderBoardService
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandlerImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.gamification.leaderboard.repository.GameLeaderBoardRepository
import com.doubtnutapp.gamification.leaderboard.ui.GameLeaderBoardActivity
import com.doubtnutapp.gamification.leaderboard.ui.viewmodel.GameLeaderBoardViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class GameLeaderActivityModule : BaseActivityModule<GameLeaderBoardActivity>() {

    @Binds
    @IntoMap
    @ViewModelKey(GameLeaderBoardViewModel::class)
    abstract fun bindGameLeaderViewModel(viewModel: GameLeaderBoardViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindGameLeaderBoardRepository(gameLeaderBoardRepositoryImpl: GameLeaderBoardRepositoryImpl): GameLeaderBoardRepository

    @Binds
    @PerActivity
    abstract fun providesNetworkHandler(networkErrorHandlerImpl: NetworkErrorHandlerImpl): NetworkErrorHandler

    @Module
    companion object {

        @Provides
        @PerActivity
        @JvmStatic
        fun provideGameLeaderBoardService(@ApiRetrofit retrofit: Retrofit): GameLeaderBoardService = retrofit.create(GameLeaderBoardService::class.java)
    }
}