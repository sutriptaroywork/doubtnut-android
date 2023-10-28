package com.doubtnutapp.doubtfeed.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.doubtfeed.viewmodel.DoubtFeedViewModel
import com.doubtnutapp.similarVideo.di.SimilarVideoFragmentBindModule
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by devansh on 7/5/21.
 */

@Module(includes = [SimilarVideoFragmentBindModule::class])
abstract class DoubtFeedModule {

    @Binds
    @IntoMap
    @ViewModelKey(DoubtFeedViewModel::class)
    abstract fun bindDoubtFeedViewModel(doubtFeedViewModel: DoubtFeedViewModel): ViewModel
}
