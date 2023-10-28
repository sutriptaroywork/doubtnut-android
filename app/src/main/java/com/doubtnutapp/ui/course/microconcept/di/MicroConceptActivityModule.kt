package com.doubtnutapp.ui.course.microconcept.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.microconcept.repository.MicroConceptRepositoryImpl
import com.doubtnutapp.data.microconcept.service.MicroConceptService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.microconcept.MicroConceptRepository
import com.doubtnutapp.ui.course.microconcept.MicroConceptViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class MicroConceptActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(MicroConceptViewModel::class)
    @PerActivity
    abstract fun bindMicroConceptViewModel(microConceptViewModel: MicroConceptViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindMicroConceptRepository(microConceptRepositoryImpl: MicroConceptRepositoryImpl): MicroConceptRepository

    @Module
    companion object {

        @Provides
        @JvmStatic
        @PerActivity
        fun bindMicroConceptService(@ApiRetrofit retrofit: Retrofit): MicroConceptService {
            return retrofit.create(MicroConceptService::class.java)
        }
    }
}