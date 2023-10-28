package com.doubtnutapp.examcorner.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.examcorner.viewmodel.ExamCornerBookmarkViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ExamCornerBookmarkModule {

    @Binds
    @IntoMap
    @ViewModelKey(ExamCornerBookmarkViewModel::class)
    abstract fun bindExamCornerBookmarkViewModel(examCornerBookmarkViewModel: ExamCornerBookmarkViewModel): ViewModel
}
