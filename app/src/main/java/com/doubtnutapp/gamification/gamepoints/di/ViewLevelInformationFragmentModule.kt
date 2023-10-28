package com.doubtnutapp.gamification.gamepoints.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.gamification.gamepoints.ui.viewmodel.ViewLevelInformationViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewLevelInformationFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(ViewLevelInformationViewModel::class)
    abstract fun bindViewLevelInformationViewModel(viewModel: ViewLevelInformationViewModel): ViewModel

}