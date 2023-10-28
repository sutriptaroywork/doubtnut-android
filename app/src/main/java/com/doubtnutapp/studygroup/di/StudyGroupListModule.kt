package com.doubtnutapp.studygroup.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.studygroup.viewmodel.StudyGroupListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class StudyGroupListModule {

    @Binds
    @IntoMap
    @ViewModelKey(StudyGroupListViewModel::class)
    abstract fun bindStudyGroupListViewModel(viewModel: StudyGroupListViewModel): ViewModel
}