package com.doubtnutapp.studygroup.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.studygroup.viewmodel.StudyGroupViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class StudyGroupChatModule {

    @Binds
    @IntoMap
    @ViewModelKey(StudyGroupViewModel::class)
    abstract fun bindStudyGroupViewModel(viewModel: StudyGroupViewModel): ViewModel

}