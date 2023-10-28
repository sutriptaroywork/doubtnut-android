package com.doubtnutapp.libraryhome.coursev3.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseRecommendationViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class CourseRecommendationModule {

    @Binds
    @IntoMap
    @ViewModelKey(CourseRecommendationViewModel::class)
    abstract fun bindCourseRecommendationViewModel(courseRecommendationViewModel: CourseRecommendationViewModel): ViewModel

}