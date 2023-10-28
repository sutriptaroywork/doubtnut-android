package com.doubtnutapp.studygroup.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.studygroup.viewmodel.SgPersonalChatViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SgPersonalChatModule {

    @Binds
    @IntoMap
    @ViewModelKey(SgPersonalChatViewModel::class)
    abstract fun bindViewModel(viewModel: SgPersonalChatViewModel): ViewModel

}