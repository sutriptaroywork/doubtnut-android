package com.doubtnutapp.notification.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.remote.api.services.NotificationService
import com.doubtnutapp.data.remote.repository.NotificationCenterRepository
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.notification.NotificationViewModel
import com.doubtnutapp.notification.NotificationCenterActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class NotificationCenterActivityModule : BaseActivityModule<NotificationCenterActivity>() {


    @Binds
    @IntoMap
    @ViewModelKey(NotificationViewModel::class)
    internal abstract fun bindNotificationCenterViewModel(notificationViewModel: NotificationViewModel): ViewModel

    @Module
    companion object {
        @Provides
        @PerActivity
        @JvmStatic
        fun provideNotificationService(@ApiRetrofit retrofit: Retrofit): NotificationService {
            return retrofit.create(NotificationService::class.java)
        }
    }

    abstract fun bindNotificationCenterRepository(notificationCenterRepository: NotificationCenterRepository): NotificationCenterRepository

}