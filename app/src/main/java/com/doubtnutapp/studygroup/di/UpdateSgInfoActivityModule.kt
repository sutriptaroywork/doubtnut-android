package com.doubtnutapp.studygroup.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.studygroup.viewmodel.UpdateSgInfoViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class UpdateSgInfoActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(UpdateSgInfoViewModel::class)
    abstract fun bindViewModel(viewModel: UpdateSgInfoViewModel): ViewModel
}