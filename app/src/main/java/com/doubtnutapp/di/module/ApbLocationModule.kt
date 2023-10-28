package com.doubtnutapp.di.module

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.payment.ApbLocationActivity
import com.doubtnutapp.payment.ApbLocationViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ApbLocationModule : BaseActivityModule<ApbLocationActivity>() {
    @Binds
    @IntoMap
    @ViewModelKey(ApbLocationViewModel::class)
    internal abstract fun bindApbLocationViewModel(apbLocationViewModel: ApbLocationViewModel): ViewModel
}
