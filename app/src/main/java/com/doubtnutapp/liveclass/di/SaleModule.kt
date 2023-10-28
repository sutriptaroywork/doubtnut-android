package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.viewmodel.LiveClassFeedbackViewModel
import com.doubtnutapp.liveclass.viewmodel.LiveClassQnaViewModel
import com.doubtnutapp.liveclass.viewmodel.SaleViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SaleModule {

    @Binds
    @IntoMap
    @ViewModelKey(SaleViewModel::class)
    abstract fun bindViewModel(viewModel: SaleViewModel): ViewModel
}