package com.doubtnutapp.doubtfeed2.reward.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.doubtfeed2.reward.viewmodel.RewardViewModel
import com.doubtnutapp.doubtfeed2.reward.viewmodel.ScratchCardViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class DfRewardModule {

    @Binds
    @IntoMap
    @ViewModelKey(RewardViewModel::class)
    abstract fun bindRewardViewModel(rewardViewModel: RewardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScratchCardViewModel::class)
    abstract fun bindScratchCardViewModel(scratchCardViewModel: ScratchCardViewModel): ViewModel
}
