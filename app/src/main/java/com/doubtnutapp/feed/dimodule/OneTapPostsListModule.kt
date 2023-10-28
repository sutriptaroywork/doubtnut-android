package com.doubtnutapp.feed.dimodule

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.feed.viewmodel.OneTapPostsListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface OneTapPostsListModule {

    @Binds
    @IntoMap
    @ViewModelKey(OneTapPostsListViewModel::class)
    abstract fun providesOneTapPostsViewModel(oneTapPostsListViewModel: OneTapPostsListViewModel): ViewModel
}