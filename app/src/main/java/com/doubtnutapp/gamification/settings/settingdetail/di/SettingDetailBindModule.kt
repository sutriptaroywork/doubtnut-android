package com.doubtnutapp.gamification.settings.settingdetail.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.gamification.settings.repository.SettingDetailRepositoryImpl
import com.doubtnutapp.data.gamification.settings.repository.SettingDetailService
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandlerImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.gamification.settings.repository.SettingDetailsRepository
import com.doubtnutapp.gamification.settings.settingdetail.ui.SettingDetailActivity
import com.doubtnutapp.gamification.settings.settingdetail.viewmodel.SettingDetailViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by shrreya on 2/7/19.
 */
@Module
abstract class SettingDetailBindModule : BaseActivityModule<SettingDetailActivity>() {

    @Binds
    @IntoMap
    @ViewModelKey(SettingDetailViewModel::class)
    abstract fun BindSettingDetailViewModel(settingDetailViewModel: SettingDetailViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindNetworkErrorHandler(networkErrorHandler: NetworkErrorHandlerImpl): NetworkErrorHandler

    @Binds
    @PerActivity
    abstract fun bindSettingDetailsRepository(settingDetailRepositoryImpl: SettingDetailRepositoryImpl): SettingDetailsRepository

    @Module
    companion object {

        @Provides
        @PerActivity
        @JvmStatic
        fun provideSettingDetailService(@ApiRetrofit retrofit: Retrofit): SettingDetailService {
            return retrofit.create(SettingDetailService::class.java)
        }

    }

}