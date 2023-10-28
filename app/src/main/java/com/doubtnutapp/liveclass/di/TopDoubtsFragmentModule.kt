package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.viewmodel.TopDoubtViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class TopDoubtsFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(TopDoubtViewModel::class)
    abstract fun bindTopDoubtViewModel(topDoubtViewModel: TopDoubtViewModel): ViewModel

}