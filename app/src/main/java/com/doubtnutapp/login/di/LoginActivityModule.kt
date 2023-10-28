package com.doubtnutapp.login.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.camerascreen.datasource.ConfigDataSources
import com.doubtnutapp.data.camerascreen.datasource.LocalConfigDataSource
import com.doubtnutapp.login.viewmodel.LoginViewModel
import com.doubtnutapp.login.repository.LoginRepository
import com.doubtnutapp.login.repository.LoginRepositoryImpl
import com.doubtnutapp.login.repository.LoginService
import com.doubtnutapp.ui.splash.SplashRepository
import com.doubtnutapp.ui.splash.SplashRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by Anand Gaurav on 2019-08-16.
 */
@Module
abstract class LoginActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindLoginViewModel(loginViewModel: LoginViewModel): ViewModel

    @Binds
    abstract fun bindRepository(loginRepositoryImpl: LoginRepositoryImpl): LoginRepository

    @Binds
    abstract fun bindSplashRepository(splashRepositoryImpl: SplashRepositoryImpl): SplashRepository

    @Binds
    internal abstract fun bindConfigDataSource(configDataSources: LocalConfigDataSource): ConfigDataSources


    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideLoginService(@ApiRetrofit retrofit: Retrofit): LoginService {
            return retrofit.create(LoginService::class.java)
        }

    }
}