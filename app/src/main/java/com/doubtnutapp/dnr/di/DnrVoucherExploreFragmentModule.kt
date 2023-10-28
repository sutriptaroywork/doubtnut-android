package com.doubtnutapp.dnr.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dnr.viewmodel.DnrVoucherExploreViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class DnrVoucherExploreFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(DnrVoucherExploreViewModel::class)
    abstract fun bind(viewModel: DnrVoucherExploreViewModel): ViewModel
}
