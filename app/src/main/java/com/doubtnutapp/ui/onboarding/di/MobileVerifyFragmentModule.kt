package com.doubtnutapp.ui.onboarding.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.ui.onboarding.viewmodel.MobileVerifyViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Anand Gaurav on 2019-07-20.
 */

@Module
abstract class MobileVerifyFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(MobileVerifyViewModel::class)
    abstract fun bindMobileVerifyViewModel(mobileVerifyViewModel: MobileVerifyViewModel): ViewModel
}