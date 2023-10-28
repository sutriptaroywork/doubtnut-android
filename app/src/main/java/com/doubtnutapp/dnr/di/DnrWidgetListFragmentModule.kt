package com.doubtnutapp.dnr.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dnr.viewmodel.DnrWidgetListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 28/10/21
 */

@Module
abstract class DnrWidgetListFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(DnrWidgetListViewModel::class)
    abstract fun bind(viewModel: DnrWidgetListViewModel): ViewModel
}
