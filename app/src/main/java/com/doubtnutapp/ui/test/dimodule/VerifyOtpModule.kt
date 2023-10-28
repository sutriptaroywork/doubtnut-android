package com.doubtnutapp.ui.test.dimodule

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.ui.onboarding.viewmodel.VerifyOtpViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Anand Gaurav on 2019-07-20.
 */
@Module
abstract class VerifyOtpModule {

    @Binds
    @IntoMap
    @ViewModelKey(VerifyOtpViewModel::class)
    abstract fun bindVerifyOtpViewModel(verifyOtpViewModel: VerifyOtpViewModel): ViewModel
}