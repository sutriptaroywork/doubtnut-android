package com.doubtnutapp.quiztfs.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.quiztfs.viewmodel.QuizTfsFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class QuizTfsFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(QuizTfsFragmentViewModel::class)
    abstract fun bindViewModel(viewModel: QuizTfsFragmentViewModel): ViewModel
}