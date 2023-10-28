package com.doubtnutapp.login.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dummy.DummyViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 29-09-2021
 */

@Module
abstract class LoginBackPressDialogFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(DummyViewModel::class)
    abstract fun bindDummyViewModel(dummyViewModel: DummyViewModel): ViewModel
}