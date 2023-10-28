package com.doubtnutapp.quiztfs.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.quiztfs.viewmodel.QuizTfsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class QuizTfsModule {

    @Binds
    @IntoMap
    @ViewModelKey(QuizTfsViewModel::class)
    abstract fun bindQuizTfsViewModel(quizTfsViewModel: QuizTfsViewModel): ViewModel

}