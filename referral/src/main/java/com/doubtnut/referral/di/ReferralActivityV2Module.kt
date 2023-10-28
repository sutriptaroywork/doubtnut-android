package com.doubtnut.referral.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.referral.ui.ReferralActivityVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ReferralActivityV2Module {

    @Binds
    @IntoMap
    @ViewModelKey(ReferralActivityVM::class)
    abstract fun bindViewModel(viewModel: ReferralActivityVM): ViewModel

}