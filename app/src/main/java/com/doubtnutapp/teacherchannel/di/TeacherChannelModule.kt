package com.doubtnutapp.teacherchannel.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.studygroup.viewmodel.UpdateSgInfoViewModel
import com.doubtnutapp.teacherchannel.viewmodel.TeacherChannelViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class TeacherChannelModule {

    @Binds
    @IntoMap
    @ViewModelKey(TeacherChannelViewModel::class)
    abstract fun bindViewModel(viewModel: TeacherChannelViewModel): ViewModel
}