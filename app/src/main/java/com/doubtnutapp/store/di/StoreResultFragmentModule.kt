package com.doubtnutapp.store.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.store.repository.StoreRepositoryImpl
import com.doubtnutapp.data.store.repository.StoreService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerFragment
import com.doubtnutapp.domain.store.repository.StoreRepository
import com.doubtnutapp.store.viewmodel.StoreResultViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.subjects.PublishSubject
import retrofit2.Retrofit

@Module
abstract class StoreResultFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(StoreResultViewModel::class)
    abstract fun bindStoreResultActivityViewModel(storeResultViewModel: StoreResultViewModel): ViewModel

    @Binds
    @PerFragment
    abstract fun bindStoreRepository(storeRepositoryImpl: StoreRepositoryImpl): StoreRepository

    @Module
    companion object {

        @Provides
        @JvmStatic
        @PerFragment
        fun provideStoreService(@ApiRetrofit retrofit: Retrofit): StoreService =
                retrofit.create(StoreService::class.java)

        @Provides
        @JvmStatic
        @PerFragment
        fun providePublishSubjectString(): PublishSubject<String> = PublishSubject.create()
    }

}