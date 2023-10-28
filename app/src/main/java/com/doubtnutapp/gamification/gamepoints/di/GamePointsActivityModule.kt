package com.doubtnutapp.gamification.gamepoints.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.gamification.gamePoints.repository.GamePointsRepositoryImpl
import com.doubtnutapp.data.gamification.gamePoints.service.GamePointsService
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandlerImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.gamification.gamePoints.repository.GamePointsRepository
import com.doubtnutapp.gamification.gamepoints.ui.GamePointsActivity
import com.doubtnutapp.gamification.gamepoints.ui.viewmodel.GamePointsViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class GamePointsActivityModule : BaseActivityModule<GamePointsActivity>() {

    @Binds
    @IntoMap
    @ViewModelKey(GamePointsViewModel::class)
    abstract fun bindGamePointsViewModel(badgesViewModel: GamePointsViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindNetworkErrorHandler(networkErrorHandlerImpl: NetworkErrorHandlerImpl): NetworkErrorHandler

    @Binds
    @PerActivity
    abstract fun bindGamePointsRepository(gamePointsRepositoryImpl: GamePointsRepositoryImpl): GamePointsRepository


    @Module
    companion object {

        @Provides
        @PerActivity
        @JvmStatic
        fun provideGamePointsService(@ApiRetrofit retrofit: Retrofit): GamePointsService =
                retrofit.create(GamePointsService::class.java)

    }


}