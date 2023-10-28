package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.viewmodel.LiveClassChatViewModel
import com.doubtnutapp.liveclass.viewmodel.LiveClassViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class LiveClassChatActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(LiveClassChatViewModel::class)
    abstract fun bindViewModel(viewModel: LiveClassChatViewModel): ViewModel

}