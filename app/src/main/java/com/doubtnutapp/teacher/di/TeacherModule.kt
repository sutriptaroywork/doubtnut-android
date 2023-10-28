package com.doubtnutapp.teacher.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.teacher.viewmodel.TeacherViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class TeacherModule {

    @Binds
    @IntoMap
    @ViewModelKey(TeacherViewModel::class)
    abstract fun bindTeacherViewModel(teacherViewModel: TeacherViewModel): ViewModel

}