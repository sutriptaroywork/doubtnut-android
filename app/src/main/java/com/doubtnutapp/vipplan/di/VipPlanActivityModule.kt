package com.doubtnutapp.vipplan.di

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.payment.repository.PaymentRepositoryImpl
import com.doubtnutapp.data.payment.service.PaymentService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import com.doubtnutapp.vipplan.ui.VipPlanActivity
import com.doubtnutapp.vipplan.viewmodel.VipPlanViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by Anand Gaurav on 2019-12-14.
 */
@Module
abstract class VipPlanActivityModule {

    @Binds
    @PerActivity
    abstract fun provides(activity: VipPlanActivity): AppCompatActivity

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