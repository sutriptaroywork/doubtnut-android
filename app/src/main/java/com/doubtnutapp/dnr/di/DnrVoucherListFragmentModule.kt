package com.doubtnutapp.dnr.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dnr.viewmodel.DnrVoucherViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class DnrVoucherListFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(DnrVoucherViewModel::class)
    abstract fun bind(viewModel: DnrVoucherViewModel): ViewModel
}
