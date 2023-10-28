package com.doubtnutapp.doubtfeed2.leaderboard.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.doubtfeed2.leaderboard.viewmodel.LeaderboardListViewModel
import com.doubtnutapp.doubtfeed2.leaderboard.viewmodel.LeaderboardViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by devansh on 12/07/21.
 */

@Module
abstract class LeaderboardModule {

    @Binds
    @IntoMap
    @ViewModelKey(LeaderboardViewModel::class)
    internal abstract fun bindLeaderboardViewModel(viewModel: LeaderboardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LeaderboardListViewModel::class)
    internal abstract fun bindLeaderboardListViewModel(viewModel: LeaderboardListViewModel): ViewModel
}
