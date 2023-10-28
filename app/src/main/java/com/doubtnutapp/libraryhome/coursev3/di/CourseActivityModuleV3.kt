package com.doubtnutapp.libraryhome.coursev3.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.libraryhome.coursev3.ui.CourseDetailActivityV3
import com.doubtnutapp.course.viewmodel.CourseViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseViewModelV3
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class CourseActivityModuleV3 : BaseActivityModule<CourseDetailActivityV3>() {
    @Binds
    @IntoMap
    @ViewModelKey(CourseViewModelV3::class)
    abstract fun bindViewModel(courseViewModel: CourseViewModelV3): ViewModel
}