package com.doubtnutapp.ui.onboarding.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.ui.onboarding.viewmodel.SelectClassViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Anand Gaurav on 2019-07-18.
 */
@Module
abstract class SelectClassModule {

    @Binds
    @IntoMap
    @ViewModelKey(SelectClassViewModel::class)
    abstract fun bindSelectClassViewModel(selectClassViewModel: SelectClassViewModel): ViewModel
}