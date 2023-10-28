package com.doubtnutapp.newglobalsearch.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.newglobalsearch.viewmodel.IasAllFilterFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class IasAllFiltersFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(IasAllFilterFragmentViewModel::class)
    abstract fun bindIasAllFilterFragmentViewModel(youtubeSearchViewModel: IasAllFilterFragmentViewModel): ViewModel

}