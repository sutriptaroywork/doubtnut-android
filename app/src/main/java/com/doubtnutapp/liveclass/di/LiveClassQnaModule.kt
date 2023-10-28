package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.viewmodel.LiveClassQnaViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Anand Gaurav on 2020-02-22.
 */
@Module
abstract class LiveClassQnaModule {

    @Binds
    @IntoMap
    @ViewModelKey(LiveClassQnaViewModel::class)
    abstract fun bindViewModel(viewModel: LiveClassQnaViewModel): ViewModel
}