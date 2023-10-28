package com.doubtnutapp.quiztfs.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.quiztfs.viewmodel.QuizTfsStatusViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 16-09-2021
 */

@Module
abstract class QuizTfsStatusModule {

    @Binds
    @IntoMap
    @ViewModelKey(QuizTfsStatusViewModel::class)
    abstract fun bindQuizTfsStatusViewModel(quizTfsStatusViewModel: QuizTfsStatusViewModel): ViewModel
}