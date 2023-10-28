package com.doubtnutapp.home.dimodule

import androidx.lifecycle.ViewModel
import com.doubtnutapp.TrialHeaderVM
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.common.repo.PopUpRepositoryImpl
import com.doubtnutapp.data.common.service.PopUpService
import com.doubtnutapp.data.homefeed.HomeScreenApiService
import com.doubtnutapp.data.homefeed.HomeScreenRepositoryImpl
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandlerImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerFragment
import com.doubtnutapp.dnr.di.DnrRewardModule
import com.doubtnutapp.domain.common.repository.PopUpRepository
import com.doubtnutapp.domain.homefeed.repository.HomeScreenRepository
import com.doubtnutapp.home.HomeFragmentViewModel
import com.doubtnutapp.sharing.di.WhatsAppSharingBindModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module(includes = [HomeFeedFragmentModule.HomeFeedFragmentProviderModule::class, WhatsAppSharingBindModule::class, DnrRewardModule::class])

abstract class HomeFeedFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(HomeFragmentViewModel::class)
    internal abstract fun bindHomeFragmentViewModel(homeFragmentViewModel: HomeFragmentViewModel): ViewModel

    @Binds
    @PerFragment
    internal abstract fun bindHomeTopIconRepository(homeScreenTopIconRepositoryImpl: HomeScreenRepositoryImpl): HomeScreenRepository

    @Binds
    @PerFragment
    internal abstract fun bindPopUpRepository(popUpRepositoryImpl: PopUpRepositoryImpl): PopUpRepository

    @Binds
    @PerFragment
    internal abstract fun provideNetworkErrorHandler(networkErrorHandlerImpl: NetworkErrorHandlerImpl): NetworkErrorHandler

    @Binds
    @IntoMap
    @ViewModelKey(TrialHeaderVM::class)
    internal abstract fun bindTrialHeaderVM(feedViewModel: TrialHeaderVM): ViewModel

    @Module
    object HomeFeedFragmentProviderModule {

        @Provides
        @PerFragment
        @JvmStatic
        internal fun bindHomeTopIconRepository(@ApiRetrofit retrofit: Retrofit): HomeScreenApiService {
            return retrofit.create(HomeScreenApiService::class.java)
        }

        @Provides
        @PerFragment
        @JvmStatic
        internal fun bindPopUpService(@ApiRetrofit retrofit: Retrofit): PopUpService {
            return retrofit.create(PopUpService::class.java)
        }


    }
}