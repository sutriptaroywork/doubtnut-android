package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.viewmodel.LiveClassFeedbackViewModel
import com.doubtnutapp.liveclass.viewmodel.LiveClassQnaViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class LiveClassFeedbackModule {

    @Binds
    @IntoMap
    @ViewModelKey(LiveClassFeedbackViewModel::class)
    abstract fun bindViewModel(viewModel: LiveClassFeedbackViewModel): ViewModel
}