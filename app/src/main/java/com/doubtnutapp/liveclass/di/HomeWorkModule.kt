package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.viewmodel.HomeworkViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class HomeWorkModule {

    @Binds
    @IntoMap
    @ViewModelKey(HomeworkViewModel::class)
    abstract fun bindViewModel(viewModel: HomeworkViewModel): ViewModel

}