package com.doubtnutapp.gamification.userProfileData.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.gamification.gamificationbadges.userProfile.repository.UserProfileRepositoryImpl
import com.doubtnutapp.data.gamification.gamificationbadges.userProfile.repository.UserProfileService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerFragment
import com.doubtnutapp.domain.gamification.userProfile.repository.UserProfileRepository
import com.doubtnutapp.gamification.userProfileData.viewmodel.ProfileViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class ProfileFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(ProfileViewModel::class)
    @PerFragment
    abstract fun bindProfileViewModel(profileViewModel: ProfileViewModel): ViewModel


    @Binds
    abstract fun bindUserProfileRepositoryImpl(userProfileRepositoryImpl: UserProfileRepositoryImpl): UserProfileRepository

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideUpdateProfileService(@ApiRetrofit retrofit: Retrofit): UserProfileService =
                retrofit.create(UserProfileService::class.java)

    }




}