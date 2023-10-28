package com.doubtnutapp.ui.mockTest.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.structuredCourse.repository.LiveClassesRepositoryImpl
import com.doubtnutapp.data.structuredCourse.service.LiveClassesService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.domain.liveclasseslibrary.repository.LiveClassesRepository
import com.doubtnutapp.ui.mockTest.MockTestSubscriptionViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by Anand Gaurav on 2020-02-22.
 */
@Module
abstract class MockTestSubscriptionModule {

    @Binds
    @IntoMap
    @ViewModelKey(MockTestSubscriptionViewModel::class)
    abstract fun bindViewModel(viewModel: MockTestSubscriptionViewModel): ViewModel

    @Binds
    abstract fun bindLiveClassesRepository(liveClassesRepositoryImpl: LiveClassesRepositoryImpl): LiveClassesRepository

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideLiveClassesService(@ApiRetrofit retrofit: Retrofit): LiveClassesService =
                retrofit.create(LiveClassesService::class.java)

    }

}