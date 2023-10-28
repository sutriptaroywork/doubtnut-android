package com.doubtnutapp.libraryhome.coursev3.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnutapp.libraryhome.coursev3.ui.SubjectDetailActivity
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseViewModelV3
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SubjectDetailActivityModule : BaseActivityModule<SubjectDetailActivity>() {

    @Binds
    @IntoMap
    @ViewModelKey(CourseViewModelV3::class)
    internal abstract fun bindCourseDetailViewModel(courseViewModelV3: CourseViewModelV3): ViewModel

}