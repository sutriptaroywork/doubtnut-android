package com.doubtnutapp.ui.likeuserlist

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class LikedUserListActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(LikedUserViewModel::class)
    abstract fun bindViewModel(viewModel: LikedUserViewModel): ViewModel
}