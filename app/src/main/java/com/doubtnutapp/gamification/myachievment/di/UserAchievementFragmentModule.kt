package com.doubtnutapp.gamification.myachievment.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.gamification.myachievment.ui.viewmodel.UserAchievementViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class UserAchievementFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(UserAchievementViewModel::class)
    abstract fun bindUserAchievementViewModel(viewModel: UserAchievementViewModel): ViewModel
}