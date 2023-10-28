package com.doubtnutapp.gamification.otheruserprofile.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.gamification.gamificationbadges.userProfile.repository.UserProfileRepositoryImpl
import com.doubtnutapp.data.gamification.gamificationbadges.userProfile.repository.UserProfileService
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandlerImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.gamification.userProfile.repository.UserProfileRepository
import com.doubtnutapp.gamification.otheruserprofile.ui.OthersProfileActivity
import com.doubtnutapp.gamification.otheruserprofile.viewmodel.OtherUserProfileViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class OthersProfileActivityModule  :  BaseActivityModule<OthersProfileActivity>() {

    @Binds
    @IntoMap
    @ViewModelKey(OtherUserProfileViewModel::class)
    @PerActivity
    abstract fun bindProfileViewModel(profileViewModel: OtherUserProfileViewModel): ViewModel

    @Binds
    abstract fun bindUserProfileRepositoryImpl(userProfileRepositoryImpl: UserProfileRepositoryImpl): UserProfileRepository

    @Binds
    @PerActivity
    abstract fun bindNetworkErrorHandler(networkErrorHandler: NetworkErrorHandlerImpl): NetworkErrorHandler

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideUpdateProfileService(@ApiRetrofit retrofit: Retrofit): UserProfileService =
                retrofit.create(UserProfileService::class.java)
    }


}