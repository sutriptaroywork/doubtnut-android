package com.doubtnutapp.leaderboard.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.leaderboard.ui.activity.LeaderboardActivityVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class LeaderboardActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(LeaderboardActivityVM::class)
    internal abstract fun bindViewModel(viewModel: LeaderboardActivityVM): ViewModel

}