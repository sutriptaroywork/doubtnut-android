package com.doubtnutapp.feed.dimodule

import androidx.lifecycle.ViewModel
import com.doubtnutapp.TrialHeaderVM
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.feed.viewmodel.FeedViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class FeedFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(FeedViewModel::class)
    internal abstract fun bindFeedViewModel(feedViewModel: FeedViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TrialHeaderVM::class)
    internal abstract fun bindTrialHeaderVM(feedViewModel: TrialHeaderVM): ViewModel
}

