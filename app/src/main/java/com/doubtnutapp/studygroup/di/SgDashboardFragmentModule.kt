package com.doubtnutapp.studygroup.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.studygroup.viewmodel.StudyGroupDashboardViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SgDashboardFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(StudyGroupDashboardViewModel::class)
    abstract fun bindViewModel(viewModel: StudyGroupDashboardViewModel): ViewModel
}