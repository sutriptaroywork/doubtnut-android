package com.doubtnutapp.libraryhome.coursev3.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseScheduleViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class CourseScheduleFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(CourseScheduleViewModel::class)
    abstract fun bindCourseScheduleViewModel(courseScheduleViewModel: CourseScheduleViewModel): ViewModel

}