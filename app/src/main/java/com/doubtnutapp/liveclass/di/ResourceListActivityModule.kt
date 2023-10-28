package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.viewmodel.ResourceListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ResourceListActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(ResourceListViewModel::class)
    abstract fun bindExploreViewModel(exploreViewModel: ResourceListViewModel): ViewModel
}