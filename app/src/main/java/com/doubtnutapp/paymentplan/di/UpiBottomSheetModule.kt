package com.doubtnutapp.paymentplan.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dummy.DummyViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 18-10-2021
 */

@Module
abstract class UpiBottomSheetModule {

    @Binds
    @IntoMap
    @ViewModelKey(DummyViewModel::class)
    abstract fun bind(dummyViewModel: DummyViewModel): ViewModel
}