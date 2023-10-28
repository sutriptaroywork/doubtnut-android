package com.doubtnutapp.liveclass.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.liveclass.viewmodel.NextVideoViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class NextVideoModule {

    @Binds
    @IntoMap
    @ViewModelKey(NextVideoViewModel::class)
    abstract fun bindNextVideoViewModel(nextVideoViewModel: NextVideoViewModel): ViewModel

}