package com.doubtnutapp.wallet.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.payment.repository.PaymentRepositoryImpl
import com.doubtnutapp.data.payment.service.PaymentService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerFragment
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import com.doubtnutapp.wallet.viewmodel.WalletViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class WalletModule {

    @Binds
    @IntoMap
    @ViewModelKey(WalletViewModel::class)
    abstract fun bindWalletViewModel(walletViewModel: WalletViewModel): ViewModel

    @Module
    companion object {
        @Provides
        @PerFragment
        @JvmStatic
        fun providePaymentService(@ApiRetrofit retrofit: Retrofit): PaymentService {
            return retrofit.create(PaymentService::class.java)
        }
    }

    @Binds
    @PerFragment
    abstract fun bindRepository(paymentRepositoryImpl: PaymentRepositoryImpl): PaymentRepository

}