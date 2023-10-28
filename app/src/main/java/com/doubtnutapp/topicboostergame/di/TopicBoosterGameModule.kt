package com.doubtnutapp.topicboostergame.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.resourcelisting.repository.ResourceListingRepositoryImpl
import com.doubtnutapp.data.resourcelisting.service.ResourceListingService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.domain.resourcelisting.repository.ResourceListingRepository
import com.doubtnutapp.topicboostergame.viewmodel.TopicBoosterGameViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by devansh on 27/2/21.
 */

@Module
abstract class TopicBoosterGameModule {

    @Binds
    @IntoMap
    @ViewModelKey(TopicBoosterGameViewModel::class)
    abstract fun bindTopicBoosterGameViewModel(topicBoosterGameViewModel: TopicBoosterGameViewModel): ViewModel

    @Binds
    abstract fun bindResourceListingRepository(resourceListingRepositoryImpl: ResourceListingRepositoryImpl): ResourceListingRepository

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun providerResourceListingService(@ApiRetrofit retrofit: Retrofit): ResourceListingService {
            return retrofit.create(ResourceListingService::class.java)
        }
    }

}