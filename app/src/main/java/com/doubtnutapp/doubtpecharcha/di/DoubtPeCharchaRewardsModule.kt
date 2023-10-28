package com.doubtnutapp.doubtpecharcha.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.doubtpecharcha.viewmodel.DoubtPeCharchaRewardsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class DoubtPeCharchaRewardsModule {

    @Binds
    @IntoMap
    @ViewModelKey(DoubtPeCharchaRewardsViewModel::class)
    abstract fun bindsDoubtPeCharchaViewModel(doubtPeCharchaRewardsModule: DoubtPeCharchaRewardsViewModel): ViewModel
}