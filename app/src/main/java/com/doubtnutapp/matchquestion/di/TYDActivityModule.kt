package com.doubtnutapp.matchquestion.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.ui.ask.AskQuestionViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class TYDActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(AskQuestionViewModel::class)
    abstract fun bindAskQuestionViewModel(viewModel: AskQuestionViewModel): ViewModel
}