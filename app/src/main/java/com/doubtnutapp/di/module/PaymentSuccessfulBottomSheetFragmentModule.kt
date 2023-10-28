package com.doubtnutapp.di.module

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.viewmodel.ReferralViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class PaymentSuccessfulBottomSheetFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(ReferralViewModel::class)
    abstract fun bindViewModel(viewModel: ReferralViewModel): ViewModel
}
