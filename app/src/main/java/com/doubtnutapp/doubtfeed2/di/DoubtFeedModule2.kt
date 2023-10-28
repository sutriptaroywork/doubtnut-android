package com.doubtnutapp.doubtfeed2.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.doubtfeed2.viewmodel.DfPreviousDoubtsViewModel
import com.doubtnutapp.doubtfeed2.viewmodel.DoubtFeedViewModel2
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by devansh on 7/5/21.
 */

@Module
abstract class DoubtFeedModule2 {

    @Binds
    @IntoMap
    @ViewModelKey(DoubtFeedViewModel2::class)
    abstract fun bindDoubtFeedViewModel(viewModel: DoubtFeedViewModel2): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DfPreviousDoubtsViewModel::class)
    abstract fun bindDfPreviousDoubtsViewModel(viewModel: DfPreviousDoubtsViewModel): ViewModel
}
