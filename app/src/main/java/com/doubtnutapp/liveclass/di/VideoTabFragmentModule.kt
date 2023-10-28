package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseViewModelV3
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class VideoTabFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(CourseViewModelV3::class)
    abstract fun bindCoursesViewModel(coursesViewModel: CourseViewModelV3): ViewModel

}