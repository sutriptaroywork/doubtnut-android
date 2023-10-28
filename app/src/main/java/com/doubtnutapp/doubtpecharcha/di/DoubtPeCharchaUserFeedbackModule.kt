package com.doubtnutapp.doubtpecharcha.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.doubtpecharcha.ui.activity.UserFeedbackActivityViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
abstract class DoubtPeCharchaUserFeedbackModule {

    @Binds
    @ViewModelKey(UserFeedbackActivityViewModel::class)
    @IntoMap
    abstract fun provideViewModel(userFeedbackActivityViewModel: UserFeedbackActivityViewModel):ViewModel
}