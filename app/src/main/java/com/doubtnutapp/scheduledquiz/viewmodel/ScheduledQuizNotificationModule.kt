package com.doubtnutapp.scheduledquiz.viewmodel

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnutapp.fallbackquiz.ui.QuizFallbackActivity
import com.doubtnutapp.fallbackquiz.viewmodel.FallbackQuizViewModel
import com.doubtnutapp.scheduledquiz.ui.ScheduledQuizNotificationActivity
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ScheduledQuizNotificationModule :
    BaseActivityModule<ScheduledQuizNotificationActivity>() {
    @Binds
    @IntoMap
    @ViewModelKey(ScheduledQuizNotificationViewModel::class)
    internal abstract fun bindScheduleNotificationViewModel(scheduledQuizNotificationViewModel: ScheduledQuizNotificationViewModel): ViewModel

}

@Module
abstract class FallbackQuizModule : BaseActivityModule<QuizFallbackActivity>() {
    @Binds
    @IntoMap
    @ViewModelKey(FallbackQuizViewModel::class)
    internal abstract fun bindFallbackQuizViewModel(fallbackQuizViewModel: FallbackQuizViewModel): ViewModel

}