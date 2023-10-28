package com.doubtnutapp.socket.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.socket.viewmodel.SocketManagerViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class SocketManagerModule {

    @Binds
    @IntoMap
    @ViewModelKey(SocketManagerViewModel::class)
    abstract fun bindSocketManagerViewModel(viewModel: SocketManagerViewModel): ViewModel
}