package com.doubtnutapp.downloadedVideos

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.di.module.BaseActivityModule
import com.doubtnutapp.liveclass.viewmodel.ApbCashPaymentViewModel
import com.doubtnutapp.payment.ApbCashPaymentActivity
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ApbCashPaymentModule : BaseActivityModule<ApbCashPaymentActivity>() {

    @Binds
    @IntoMap
    @ViewModelKey(ApbCashPaymentViewModel::class)
    internal abstract fun bindApbCashPaymentViewModel(apbCashPaymentViewModel: ApbCashPaymentViewModel): ViewModel
}
