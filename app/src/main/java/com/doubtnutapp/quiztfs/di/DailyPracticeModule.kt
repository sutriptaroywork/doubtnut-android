package com.doubtnutapp.quiztfs.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.quiztfs.viewmodel.DailyPracticeViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 01-09-2021
 */

@Module
abstract class DailyPracticeModule {

    @Binds
    @IntoMap
    @ViewModelKey(DailyPracticeViewModel::class)
    abstract fun bindDailyPracticeViewModel(dailyPracticeViewModel: DailyPracticeViewModel): ViewModel
}