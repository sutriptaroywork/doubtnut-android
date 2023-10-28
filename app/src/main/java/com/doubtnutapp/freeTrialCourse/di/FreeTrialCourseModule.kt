package com.doubtnutapp.freeTrialCourse.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerFragment
import com.doubtnutapp.freeTrialCourse.ui.FreeTrialCourseViewModel
import com.doubtnutapp.freeTrialCourse.model.FreeTrialCourseImpl
import com.doubtnutapp.freeTrialCourse.repository.FreeTrialCourseRepository
import com.doubtnutapp.freeTrialCourse.repository.FreeTrialCourseService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class FreeTrialCourseModule {
    @Binds
    @IntoMap
    @ViewModelKey(FreeTrialCourseViewModel::class)
    abstract fun bindViewModel(freeTrialCourseViewModel: FreeTrialCourseViewModel):ViewModel


    @Binds
    abstract fun bindRepository(freeTrialCourseRepository:FreeTrialCourseImpl):FreeTrialCourseRepository

    @Module
    companion object{
        @PerFragment
        @JvmStatic
        @Provides
        fun providesFreeTrialCourseService(@ApiRetrofit retrofit: Retrofit):FreeTrialCourseService{
            return retrofit.create(FreeTrialCourseService::class.java)
        }

    }


}