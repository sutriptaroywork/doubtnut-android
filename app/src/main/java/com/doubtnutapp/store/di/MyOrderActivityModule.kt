package com.doubtnutapp.store.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.store.repository.MyOrderRepositoryImpl
import com.doubtnutapp.data.store.repository.MyOrderService
import com.doubtnutapp.data.store.repository.StoreService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.store.repository.MyOrderRepository
import com.doubtnutapp.store.viewmodel.MyOrderViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.subjects.PublishSubject
import retrofit2.Retrofit

@Module
abstract class MyOrderActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(MyOrderViewModel::class)
    @PerActivity
    abstract fun bindMyOrderActivityViewModel(myOrderViewModel: MyOrderViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindMyOrderRepository(myOrderRepositoryImpl: MyOrderRepositoryImpl): MyOrderRepository

    @Module
    companion object {

        @Provides
        @JvmStatic
        @PerActivity
        fun provideMyOrderService(@ApiRetrofit retrofit: Retrofit): MyOrderService =
                retrofit.create(MyOrderService::class.java)

        @Provides
        @JvmStatic
        @PerActivity
        fun provideStoreService(@ApiRetrofit retrofit: Retrofit): StoreService =
                retrofit.create(StoreService::class.java)

        @Provides
        @JvmStatic
        @PerActivity
        fun providePublishSubjectString(): PublishSubject<String> = PublishSubject.create()
    }

}