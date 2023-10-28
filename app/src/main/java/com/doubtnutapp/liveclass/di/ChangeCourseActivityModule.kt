package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.viewmodel.CourseSwitchViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ChangeCourseActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(CourseSwitchViewModel::class)
    abstract fun bindViewModel(switchViewModel: CourseSwitchViewModel): ViewModel
}