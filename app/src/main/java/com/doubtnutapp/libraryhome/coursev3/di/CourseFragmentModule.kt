package com.doubtnutapp.libraryhome.coursev3.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.WidgetPlanButtonVM
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseViewModelV3
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class CourseFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(CourseViewModelV3::class)
    abstract fun bindCourseViewModel(courseViewModel: CourseViewModelV3): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WidgetPlanButtonVM::class)
    internal abstract fun bindWidgetPlanButtonVM(viewModel: WidgetPlanButtonVM): ViewModel

}