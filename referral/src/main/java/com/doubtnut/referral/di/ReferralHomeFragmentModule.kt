package com.doubtnut.referral.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.referral.ui.ReferralHomeFragmentVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ReferralHomeFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(ReferralHomeFragmentVM::class)
    abstract fun bindViewModel(viewModel: ReferralHomeFragmentVM): ViewModel

}