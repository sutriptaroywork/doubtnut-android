package com.doubtnutapp.home.dimodule

import androidx.lifecycle.ViewModel
import com.doubtnutapp.MainActivity
import com.doubtnutapp.MainViewModel
import com.doubtnutapp.WidgetPlanButtonVM
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.gamification.gamificationbadges.userProfile.repository.UserProfileRepositoryImpl
import com.doubtnutapp.data.gamification.gamificationbadges.userProfile.repository.UserProfileService
import com.doubtnutapp.data.mainscreen.apiservice.MainScreenService
import com.doubtnutapp.data.mainscreen.repository.MainScreenRepositoryImpl
import com.doubtnutapp.ui.onboarding.repository.OnBoardingRepositoryImpl
import com.doubtnutapp.ui.onboarding.repository.OnBoardingService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.gamification.userProfile.repository.UserProfileRepository
import com.doubtnutapp.domain.mainscrren.repository.MainScreenRepository
import com.doubtnutapp.ui.onboarding.repository.OnBoardingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class MainActivityModule : BaseActivityModule<MainActivity>() {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindsMainViewModel(mainViewModel: MainViewModel): ViewModel


    @Binds
    @PerActivity
    abstract fun bindMainRepositoryImpl(mainRepositoryImpl: MainScreenRepositoryImpl): MainScreenRepository

    @Binds
    abstract fun bindUserProfileRepositoryImpl(userProfileRepositoryImpl: UserProfileRepositoryImpl): UserProfileRepository

    @Binds
    @PerActivity
    abstract fun bindOnBoardingRepository1(onBoardingRepositoryImpl: OnBoardingRepositoryImpl): OnBoardingRepository

    @Binds
    @IntoMap
    @ViewModelKey(WidgetPlanButtonVM::class)
    internal abstract fun bindWidgetPlanButtonVM(viewModel: WidgetPlanButtonVM): ViewModel

    @Module
    companion object {

        @Provides
        @PerActivity
        @JvmStatic
        fun provideMainScreenService(@ApiRetrofit retrofit: Retrofit): MainScreenService {
            return retrofit.create(MainScreenService::class.java)
        }

        @Provides
        @PerActivity
        @JvmStatic
        fun provideUserProfileService(@ApiRetrofit retrofit: Retrofit): UserProfileService {
            return retrofit.create(UserProfileService::class.java)
        }

        @Provides
        @JvmStatic
        @PerActivity
        fun provideOnBoardingService(@ApiRetrofit retrofit: Retrofit): OnBoardingService =
                retrofit.create(OnBoardingService::class.java)
    }

}