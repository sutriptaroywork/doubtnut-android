package com.doubtnutapp.store.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.store.repository.ConvertCoinsRepositoryImpl
import com.doubtnutapp.data.store.repository.ConvertCoinsService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.store.repository.ConvertCoinsRepository
import com.doubtnutapp.store.viewmodel.ConvertCoinsViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.subjects.PublishSubject
import retrofit2.Retrofit

@Module
abstract class ConvertCoinsActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(ConvertCoinsViewModel::class)
    @PerActivity
    abstract fun bindConvertCoinsViewModel(convertCoinsViewModel: ConvertCoinsViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindConvertCoinsRepository(convertCoinsRepositoryImpl: ConvertCoinsRepositoryImpl): ConvertCoinsRepository

    @Module
    companion object {

        @Provides
        @JvmStatic
        @PerActivity
        fun provideConvertCoinsService(@ApiRetrofit retrofit: Retrofit): ConvertCoinsService =
                retrofit.create(ConvertCoinsService::class.java)

        @Provides
        @JvmStatic
        @PerActivity
        fun providePublishSubjectString(): PublishSubject<String> = PublishSubject.create()
    }

}