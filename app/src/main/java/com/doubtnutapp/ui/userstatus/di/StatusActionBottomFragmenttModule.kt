package com.doubtnutapp.ui.userstatus.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dummy.DummyViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 30-09-2021
 */

@Module
abstract class StatusActionBottomFragmenttModule {

    @Binds
    @IntoMap
    @ViewModelKey(DummyViewModel::class)
    abstract fun bindDummy(dummyViewModel: DummyViewModel): ViewModel
}