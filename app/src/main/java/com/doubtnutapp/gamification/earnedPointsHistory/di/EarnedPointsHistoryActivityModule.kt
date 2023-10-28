package com.doubtnutapp.gamification.earnedPointsHistory.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.gamification.earnedPointsHistory.repository.EarnedPointsHistoryRepositoryImpl
import com.doubtnutapp.data.gamification.earnedPointsHistory.service.EarnedPointsHistoryService
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandlerImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.gamification.earnedPointsHistory.repository.EarnedPointsHistoryRepository
import com.doubtnutapp.gamification.earnedPointsHistory.ui.EarnedPointsHistoryActivity
import com.doubtnutapp.gamification.earnedPointsHistory.ui.viewmodel.EarnedPointsHistoryViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class EarnedPointsHistoryActivityModule : BaseActivityModule<EarnedPointsHistoryActivity>() {


    @Binds
    @IntoMap
    @ViewModelKey(EarnedPointsHistoryViewModel::class)
    abstract fun bindEarnedPointsHistoryViewModel(earnedPointsHistoryViewModel: EarnedPointsHistoryViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindNetworkErrorHandler(networkErrorHandler: NetworkErrorHandlerImpl): NetworkErrorHandler

    @Binds
    @PerActivity
    abstract fun bindEarnedPointsHistoryRepository(earnedPointsHistoryRepositoryImpl: EarnedPointsHistoryRepositoryImpl): EarnedPointsHistoryRepository

    @Module
    companion object {

        @Provides
        @PerActivity
        @JvmStatic
        fun provideEarnedPointsHistoryService(@ApiRetrofit retrofit: Retrofit): EarnedPointsHistoryService {
            return retrofit.create(EarnedPointsHistoryService::class.java)
        }


    }


}
