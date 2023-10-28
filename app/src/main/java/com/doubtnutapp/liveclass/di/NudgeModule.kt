package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.course.viewmodel.CourseViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.payment.repository.PaymentRepositoryImpl
import com.doubtnutapp.data.payment.service.PaymentService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import com.doubtnutapp.libraryhome.course.viewmodel.CoursesViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class NudgeModule {

    @Binds
    @IntoMap
    @ViewModelKey(CoursesViewModel::class)
    abstract fun bindCourseViewModel(viewModel: CoursesViewModel): ViewModel

}