package com.doubtnutapp.quiztfs.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.quiztfs.viewmodel.QuizTfsAnalysisViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 10-09-2021
 */

@Module
abstract class QuizTfsAnalysisModule {

    @Binds
    @IntoMap
    @ViewModelKey(QuizTfsAnalysisViewModel::class)
    abstract fun bindQuizTfsAnalysisViewModel(quizTfsAnalysisViewModel: QuizTfsAnalysisViewModel): ViewModel
}