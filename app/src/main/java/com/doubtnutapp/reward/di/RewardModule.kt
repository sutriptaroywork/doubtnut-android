package com.doubtnutapp.reward.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.reward.viewmodel.RewardViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class RewardModule {

    @Binds
    @IntoMap
    @ViewModelKey(RewardViewModel::class)
    abstract fun bindRewardViewModel(rewardViewModel: RewardViewModel): ViewModel

}