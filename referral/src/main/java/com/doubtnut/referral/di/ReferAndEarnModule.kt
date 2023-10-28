package com.doubtnut.referral.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.referral.ui.ReferAndEarnViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ReferAndEarnModule {

    @Binds
    @IntoMap
    @ViewModelKey(ReferAndEarnViewModel::class)
    abstract fun providesReferAndEarnViewModel(viewModel: ReferAndEarnViewModel): ViewModel
}