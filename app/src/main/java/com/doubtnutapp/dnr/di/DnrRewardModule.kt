package com.doubtnutapp.dnr.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dnr.viewmodel.DnrRewardViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class DnrRewardModule {

    @Binds
    @IntoMap
    @ViewModelKey(DnrRewardViewModel::class)
    abstract fun bindViewModel(dnrRewardViewModel: DnrRewardViewModel): ViewModel
}
