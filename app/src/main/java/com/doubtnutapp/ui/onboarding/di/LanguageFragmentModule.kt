package com.doubtnutapp.ui.onboarding.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dummy.DummyViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 27-09-2021
 */

@Module
abstract class LanguageFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(DummyViewModel::class)
    abstract fun bindLanguageFragment(dummyViewModel: DummyViewModel): ViewModel
}