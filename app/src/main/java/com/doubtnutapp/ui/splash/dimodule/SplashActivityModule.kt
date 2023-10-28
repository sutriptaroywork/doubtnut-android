package com.doubtnutapp.ui.splash.dimodule

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.data.camerascreen.datasource.ConfigDataSources
import com.doubtnutapp.data.camerascreen.datasource.LocalConfigDataSource
import com.doubtnutapp.ui.splash.SplashService
import com.doubtnutapp.ui.splash.SplashRepositoryImpl
import com.doubtnutapp.ui.splash.SplashRepository
import com.doubtnutapp.ui.splash.SplashViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class SplashActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    internal abstract fun bindSplashActivityViewModel(SplashViewModel: SplashViewModel): ViewModel

    @Binds
    internal abstract fun bindSplashRepository(splashRepositoryImpl: SplashRepositoryImpl): SplashRepository

    @Binds
    internal abstract fun bindConfigDataSource(configDataSources: LocalConfigDataSource): ConfigDataSources

    @Module
    companion object {
        @Provides
        @PerActivity
        @JvmStatic
        internal fun provideSplashService(@ApiRetrofit retrofit: Retrofit): SplashService {
            return retrofit.create(SplashService::class.java)
        }
    }
}