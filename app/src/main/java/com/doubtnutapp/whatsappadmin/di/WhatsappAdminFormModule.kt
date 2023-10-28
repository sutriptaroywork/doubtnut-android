package com.doubtnutapp.whatsappadmin.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dummy.DummyViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 22-09-2021
 */

@Module
abstract class WhatsappAdminFormModule {

    @Binds
    @IntoMap
    @ViewModelKey(DummyViewModel::class)
    abstract fun bindDummyViewModel(dummyViewModel: DummyViewModel): ViewModel
}