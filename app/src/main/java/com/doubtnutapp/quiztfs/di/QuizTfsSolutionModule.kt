package com.doubtnutapp.quiztfs.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.quiztfs.viewmodel.QuizTfsSolutionViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class QuizTfsSolutionModule {

    @Binds
    @IntoMap
    @ViewModelKey(QuizTfsSolutionViewModel::class)
    abstract fun bindQuizTfsSolutionViewModel(quizTfsSolutionViewModel: QuizTfsSolutionViewModel): ViewModel

}