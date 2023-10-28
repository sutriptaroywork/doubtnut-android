package com.doubtnutapp.paymentplan.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.payment.repository.PaymentRepositoryImpl
import com.doubtnutapp.data.payment.service.PaymentService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import com.doubtnutapp.paymentplan.viewmodel.PaymentPlanViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by Mehul Bisht on 07-10-2021
 */

@Module
abstract class PaymentPlanActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(PaymentPlanViewModel::class)
    abstract fun bindPaymentPlanViewModel(paymentPlanViewModel: PaymentPlanViewModel): ViewModel

    @Module
    companion object {
        @Provides
        @PerActivity
        @JvmStatic
        fun providePaymentService(@ApiRetrofit retrofit: Retrofit): PaymentService {
            return retrofit.create(PaymentService::class.java)
        }
    }

    @Binds
    @PerActivity
    abstract fun bindRepository(paymentRepositoryImpl: PaymentRepositoryImpl): PaymentRepository

}