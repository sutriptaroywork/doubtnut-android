package com.doubtnutapp.studygroup.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.studygroup.viewmodel.SgChatRequestViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SgChatRequestModule {

    @Binds
    @IntoMap
    @ViewModelKey(SgChatRequestViewModel::class)
    abstract fun bindViewModel(viewModel: SgChatRequestViewModel): ViewModel
}