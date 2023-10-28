package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.viewmodel.EmiReminderViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class EmiReminderModule {

    @Binds
    @IntoMap
    @ViewModelKey(EmiReminderViewModel::class)
    abstract fun bindViewModel(viewModel: EmiReminderViewModel): ViewModel
}