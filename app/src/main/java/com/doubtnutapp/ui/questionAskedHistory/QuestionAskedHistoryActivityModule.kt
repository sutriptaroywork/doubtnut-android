package com.doubtnutapp.ui.questionAskedHistory

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class QuestionAskedHistoryActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(QuestionAskedHistoryViewModel::class)
    abstract fun bindViewModel(viewModel: QuestionAskedHistoryViewModel): ViewModel
}