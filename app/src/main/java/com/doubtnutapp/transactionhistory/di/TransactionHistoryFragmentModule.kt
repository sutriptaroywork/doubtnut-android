package com.doubtnutapp.transactionhistory.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.payment.repository.PaymentRepositoryImpl
import com.doubtnutapp.data.payment.service.PaymentService
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import com.doubtnutapp.transactionhistory.viewmodel.TransactionHistoryViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by Akshat Jindal 19/4/21
 */
@Module
abstract class TransactionHistoryFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(TransactionHistoryViewModel::class)
    abstract fun bindTransactionHistoryViewModel(transactionHistoryViewModel: TransactionHistoryViewModel): ViewModel

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun providePaymentService(@ApiRetrofit retrofit: Retrofit): PaymentService {
            return retrofit.create(PaymentService::class.java)
        }
    }

    @Binds
    abstract fun bindRepository(paymentRepositoryImpl: PaymentRepositoryImpl): PaymentRepository

}