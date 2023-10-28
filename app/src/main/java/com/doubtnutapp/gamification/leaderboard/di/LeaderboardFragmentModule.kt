package com.doubtnutapp.gamification.leaderboard.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.gamification.leaderboard.ui.LeaderboardViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class LeaderboardFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(LeaderboardViewModel::class)
    abstract fun bindLeaderboardViewModel(leaderboardViewModel: LeaderboardViewModel) : ViewModel


}