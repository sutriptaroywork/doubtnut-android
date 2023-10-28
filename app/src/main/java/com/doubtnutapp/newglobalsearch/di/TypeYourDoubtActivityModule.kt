package com.doubtnutapp.newglobalsearch.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.newglobalsearch.repository.TypeYourDoubtRepositoryImpl
import com.doubtnutapp.data.newglobalsearch.service.TypeYourDoubtService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.newglobalsearch.repository.TypeYourDoubtRepository
import com.doubtnutapp.newglobalsearch.viewmodel.TypeYourDoubtViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import io.reactivex.subjects.PublishSubject
import retrofit2.Retrofit

@Module
abstract class TypeYourDoubtActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(TypeYourDoubtViewModel::class)
    @PerActivity
    abstract fun bindTypeYourDoubtViewModel(tydViewModel: TypeYourDoubtViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindTypeYourDoubtRepository(typeYourDoubtRepository: TypeYourDoubtRepositoryImpl):
            TypeYourDoubtRepository


    @Module
    companion object {

        @Provides
        @JvmStatic
        @PerActivity
        fun provideTypeYourDoubtService(@ApiRetrofit retrofit: Retrofit): TypeYourDoubtService =
                retrofit.create(TypeYourDoubtService::class.java)

        @Provides
        @JvmStatic
        @PerActivity
        fun providePublishSubject(): PublishSubject<String> = PublishSubject.create()
    }
}