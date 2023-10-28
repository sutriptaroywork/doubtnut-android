package com.doubtnutapp.libraryhome.coursev3.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseViewModelV3
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class CourseSelectionFragmentModule {
    @Binds
    @IntoMap
    @ViewModelKey(CourseViewModelV3::class)
    abstract fun bindCourseViewModel(viewModel: CourseViewModelV3): ViewModel

}