package com.doubtnutapp.pcmunlockpopup.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.pcmunlockpopup.apiservice.PCMUnlockApiService
import com.doubtnutapp.data.pcmunlockpopup.repository.PCMUnlockRepositoryImpl
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandlerImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.pcmunlockpopup.repository.PCMUnlockRepository
import com.doubtnutapp.pcmunlockpopup.ui.PCMUnlockPopActivity
import com.doubtnutapp.pcmunlockpopup.ui.viewmodel.PCMUnlockPopViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class PCMUnlockPopActivityModule : BaseActivityModule<PCMUnlockPopActivity>() {

    @Binds
    @IntoMap
    @ViewModelKey(PCMUnlockPopViewModel::class)
    abstract fun bindPCMUnlockPopViewModel(pcmUnlockPopViewModel: PCMUnlockPopViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindPCMUnlockRepository(pcmUnlockRepositoryImpl: PCMUnlockRepositoryImpl): PCMUnlockRepository

    @Binds
    @PerActivity
    abstract fun bindNetworkErrorHandler(networkErrorHandler: NetworkErrorHandlerImpl): NetworkErrorHandler

    @Module
    companion object {

        @Provides
        @PerActivity
        @JvmStatic
        fun providePCMUnlockApiService(@ApiRetrofit retrofit: Retrofit): PCMUnlockApiService {
            return retrofit.create(PCMUnlockApiService::class.java)
        }

    }

}