package com.doubtnutapp.widgettest.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.widgettest.viewmodel.ApiTestViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ApiTestModule {

    @Binds
    @IntoMap
    @ViewModelKey(ApiTestViewModel::class)
    abstract fun bindApiTestViewModel(apiTestViewModel: ApiTestViewModel): ViewModel

}