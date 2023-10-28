package com.doubtnutapp.vipplan.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.vipplan.viewmodel.PaymentHelpViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class PaymentHelpActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(PaymentHelpViewModel::class)
    abstract fun bindPaymentHelpViewModel(paymentHelpViewModel: PaymentHelpViewModel): ViewModel

}