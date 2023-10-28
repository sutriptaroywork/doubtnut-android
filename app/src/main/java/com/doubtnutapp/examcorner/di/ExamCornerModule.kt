package com.doubtnutapp.examcorner.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.examcorner.viewmodel.ExamCornerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ExamCornerModule {

    @Binds
    @IntoMap
    @ViewModelKey(ExamCornerViewModel::class)
    abstract fun bindExamCornerViewModel(examCornerViewModel: ExamCornerViewModel): ViewModel
}
