package com.doubtnutapp.paymentv2

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.payment.repository.PaymentRepositoryImpl
import com.doubtnutapp.data.payment.service.PaymentService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import com.doubtnutapp.paymentv2.ui.PaymentActivity
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class PaymentActivityModule {

    @Binds
    @PerActivity
    abstract fun provides(activity: PaymentActivity): AppCompatActivity

    @Binds
    @IntoMap
    @ViewModelKey(PaymentViewModel::class)
    abstract fun bindPaymentViewModel(paymentViewModel: PaymentViewModel): ViewModel

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