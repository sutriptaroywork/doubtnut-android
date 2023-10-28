package com.doubtnutapp.networkstats.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.networkstats.ui.NetworkStatsVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class NetworkStatsActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(NetworkStatsVM::class)
    abstract fun bindViewModel(networkStatsVM: NetworkStatsVM): ViewModel
}