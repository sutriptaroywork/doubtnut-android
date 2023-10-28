package com.doubtnutapp.libraryhome.course

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.libraryhome.course.viewmodel.CoursesViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class CoursesFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(CoursesViewModel::class)
    abstract fun bindCoursesViewModel(coursesViewModel: CoursesViewModel): ViewModel

}