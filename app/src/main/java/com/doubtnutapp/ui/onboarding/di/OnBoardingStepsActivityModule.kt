package com.doubtnutapp.ui.onboarding.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.ui.onboarding.repository.OnBoardingRepositoryImpl
import com.doubtnutapp.ui.onboarding.repository.OnBoardingService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.ui.onboarding.repository.OnBoardingRepository
import com.doubtnutapp.ui.onboarding.viewmodel.OnBoardingStepsViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by Sachin Saxena on 2020-02-11.
 */
@Module
abstract class OnBoardingStepsActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(OnBoardingStepsViewModel::class)
    @PerActivity
    abstract fun bindOnBoardingStepsViewModel(onBoardingStepsViewModel: OnBoardingStepsViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindOnBoardingRepository(onBoardingRepositoryImpl: OnBoardingRepositoryImpl): OnBoardingRepository

    @Module
    companion object {

        @Provides
        @JvmStatic
        @PerActivity
        fun provideOnBoardingService(@ApiRetrofit retrofit: Retrofit): OnBoardingService =
            retrofit.create(OnBoardingService::class.java)
    }
}