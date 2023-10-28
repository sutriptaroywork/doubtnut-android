package com.doubtnutapp.gamification.mybio.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.gamification.mybio.repository.UserBioRepositoryImpl
import com.doubtnutapp.data.gamification.mybio.service.UserBioService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.domain.gamification.mybio.repository.UserBioRepository
import com.doubtnutapp.gamification.mybio.viewmodel.MyBioViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class UserBioFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(MyBioViewModel::class)
    abstract fun bindUserBioViewModel(myBioViewModel: MyBioViewModel): ViewModel

    @Binds
    abstract fun bindUserBioRepository(userBioRepositoryImpl: UserBioRepositoryImpl): UserBioRepository

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun bindUserBioService(@ApiRetrofit retrofit: Retrofit): UserBioService {
            return retrofit.create(UserBioService::class.java)
        }
    }
}