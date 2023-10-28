package com.doubtnutapp.quicksearch.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.quicksearch.viewmodel.NotificationSettingViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class NotificationSettingActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(NotificationSettingViewModel::class)
    abstract fun bindViewModel(viewModel: NotificationSettingViewModel): ViewModel
}