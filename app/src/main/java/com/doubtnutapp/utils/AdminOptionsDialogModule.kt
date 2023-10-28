package com.doubtnutapp.utils

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dummy.DummyViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 24-09-2021
 */

@Module
abstract class AdminOptionsDialogModule {

    @Binds
    @IntoMap
    @ViewModelKey(DummyViewModel::class)
    abstract fun bindViewModel(dummyViewModel: DummyViewModel): ViewModel
}