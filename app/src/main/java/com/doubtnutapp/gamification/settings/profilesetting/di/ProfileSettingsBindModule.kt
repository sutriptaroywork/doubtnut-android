package com.doubtnutapp.gamification.settings.profilesetting.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.data.settings.repository.SettingsRepositoryImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.settings.repository.SettingRepository
import com.doubtnutapp.gamification.settings.profilesetting.viewmodel.ProfileSettingViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by shrreya on 29/6/19.
 */

@Module
abstract class ProfileSettingsBindModule {

    @Binds
    @IntoMap
    @ViewModelKey(ProfileSettingViewModel::class)
    abstract fun bindProfileSettingViewModel(profileSettingViewModel: ProfileSettingViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindSettingRepository(settingRepositoryImpl: SettingsRepositoryImpl): SettingRepository

}