package com.doubtnutapp.gamification.badgesscreen.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.gamification.gamificationbadges.userbadges.repository.UserBadgeRepositoryImpl
import com.doubtnutapp.data.gamification.gamificationbadges.userbadges.repository.UserBadgeService
import com.doubtnutapp.data.whatsappsharing.repository.WhatsAppShareRepositoryImpl
import com.doubtnutapp.data.whatsappsharing.service.WhatsAppSharingService
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandlerImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.gamification.userbadge.repository.UserBadgesRepository
import com.doubtnutapp.domain.whatsappsharing.repository.WhatsAppShareRepository
import com.doubtnutapp.gamification.badgesscreen.ui.BadgesActivity
import com.doubtnutapp.gamification.badgesscreen.ui.viewmodel.BadgesViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class BadgesActivityModule : BaseActivityModule<BadgesActivity>() {

    @Binds
    @IntoMap
    @ViewModelKey(BadgesViewModel::class)
    abstract fun bindBadgesViewModel(badgesViewModel: BadgesViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindNetworkErrorHandler(networkErrorHandler: NetworkErrorHandlerImpl): NetworkErrorHandler

    @Binds
    @PerActivity
    abstract fun bindUserBadgeRepository(userBadgeRepositoryImpl: UserBadgeRepositoryImpl): UserBadgesRepository

    @Binds
    @PerActivity
    abstract fun bindWhatsAppRepository(whatsAppShareRepositoryImpl: WhatsAppShareRepositoryImpl): WhatsAppShareRepository

    @Module
    companion object {

        @Provides
        @PerActivity
        @JvmStatic
        fun provideUserBadgeService(@ApiRetrofit retrofit: Retrofit): UserBadgeService {
            return retrofit.create(UserBadgeService::class.java)
        }

        @Provides
        @PerActivity
        @JvmStatic
        fun provideWhatsAppSharingService(@ApiRetrofit retrofit: Retrofit): WhatsAppSharingService {
            return retrofit.create(WhatsAppSharingService::class.java)
        }

    }
}