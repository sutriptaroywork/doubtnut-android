package com.doubtnutapp.course.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.course.viewmodel.SchedulerListingViewModel
import com.doubtnut.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 23/12/21
 */

@Module
abstract class SchedulerListingActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(SchedulerListingViewModel::class)
    abstract fun bind(viewModel: SchedulerListingViewModel): ViewModel
}