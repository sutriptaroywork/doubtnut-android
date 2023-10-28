package com.doubtnutapp.gamification.otheruserprofile.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerFragment
import com.doubtnutapp.gamification.otheruserprofile.viewmodel.OtherUserAchievementViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class OtherUserAchievementBindModule {

    @Binds
    @IntoMap
    @ViewModelKey(OtherUserAchievementViewModel::class)
    @PerFragment
    abstract fun bindOtherUserAchievementViewModel(otherUserAchievementViewModel: OtherUserAchievementViewModel): ViewModel

}