package com.doubtnutapp.newglobalsearch.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnut.core.di.qualifier.MicroApiRetrofit
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.data.newglobalsearch.repository.TrendingSearchRepositoryImpl
import com.doubtnutapp.data.newglobalsearch.service.InAppSearchMicroApiService
import com.doubtnutapp.data.newglobalsearch.service.InAppSearchService
import com.doubtnutapp.domain.newglobalsearch.repository.TrendingSearchRepository
import com.doubtnutapp.newglobalsearch.viewmodel.InAppSearchViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.subjects.PublishSubject
import retrofit2.Retrofit

@Module
abstract class InAppSearchActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(InAppSearchViewModel::class)
    @PerActivity
    abstract fun bindInAppSearchActivityViewModel(inAppSearchViewModel: InAppSearchViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindInAppSearchRepository(trendingSearchRepository: TrendingSearchRepositoryImpl):
            TrendingSearchRepository


    @Module
    companion object {

        @Provides
        @JvmStatic
        @PerActivity
        fun provideInAppSearchService(@ApiRetrofit retrofit: Retrofit): InAppSearchService =
                retrofit.create(InAppSearchService::class.java)

        @Provides
        @JvmStatic
        @PerActivity
        fun provideInAppSearchMicroApiService(@MicroApiRetrofit retrofit: Retrofit): InAppSearchMicroApiService {
            return retrofit.create(InAppSearchMicroApiService::class.java)
        }

        @Provides
        @JvmStatic
        fun providePublishSubjectString(): PublishSubject<String> = PublishSubject.create()
    }
}