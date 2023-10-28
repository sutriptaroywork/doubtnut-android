package com.doubtnutapp.gamification.friendbadgesscreen.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.gamification.gamificationbadges.userbadges.repository.UserBadgeRepositoryImpl
import com.doubtnutapp.data.gamification.gamificationbadges.userbadges.repository.UserBadgeService
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandlerImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.gamification.userbadge.repository.UserBadgesRepository
import com.doubtnutapp.gamification.friendbadgesscreen.ui.FriendBadgesActivity
import com.doubtnutapp.gamification.friendbadgesscreen.ui.viewmodel.FriendBadgesViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class FriendBadgesActivityModule : BaseActivityModule<FriendBadgesActivity>() {
    @Binds
    @IntoMap
    @ViewModelKey(FriendBadgesViewModel::class)
    abstract fun bindFriendBadgesViewModel(friendBadgesViewModel: FriendBadgesViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindNetworkErrorHandler(networkErrorHandler: NetworkErrorHandlerImpl): NetworkErrorHandler

    @Binds
    @PerActivity
    abstract fun bindUserBadgeRepository(userBadgeRepositoryImpl: UserBadgeRepositoryImpl): UserBadgesRepository

    @Module
    companion object {

        @Provides
        @PerActivity
        @JvmStatic
        fun provideUserBadgeService(@ApiRetrofit retrofit: Retrofit): UserBadgeService {
            return retrofit.create(UserBadgeService::class.java)
        }

    }
}