package com.doubtnutapp.vipplan.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.payment.repository.PaymentRepositoryImpl
import com.doubtnutapp.data.payment.service.PaymentService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import com.doubtnutapp.vipplan.viewmodel.VipPlanViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class BundleModule {

    @Binds
    @IntoMap
    @ViewModelKey(VipPlanViewModel::class)
    abstract fun bindPaymentViewModel(viewModel: VipPlanViewModel): ViewModel

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