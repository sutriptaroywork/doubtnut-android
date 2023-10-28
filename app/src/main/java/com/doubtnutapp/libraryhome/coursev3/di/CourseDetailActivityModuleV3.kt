package com.doubtnutapp.libraryhome.coursev3.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnutapp.libraryhome.coursev3.ui.CourseDetailActivityV3
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseDetailViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class CourseDetailActivityModuleV3 : BaseActivityModule<CourseDetailActivityV3>() {

    @Binds
    @IntoMap
    @ViewModelKey(CourseDetailViewModel::class)
    internal abstract fun bindCourseDetailViewModel(courseDetailViewModel: CourseDetailViewModel): ViewModel

}