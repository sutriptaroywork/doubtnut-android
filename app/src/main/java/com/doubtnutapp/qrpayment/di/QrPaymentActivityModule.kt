package com.doubtnutapp.qrpayment.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.qrpayment.viewmodel.QrPaymentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class QrPaymentActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(QrPaymentViewModel::class)
    abstract fun bindQrPaymentViewModel(qrPaymentViewModel: QrPaymentViewModel): ViewModel

}