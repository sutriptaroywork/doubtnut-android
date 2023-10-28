package com.doubtnutapp.newglobalsearch.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dummy.DummyViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 05-10-2021
 */

@Module
abstract class NoDataFoundFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(DummyViewModel::class)
    abstract fun bind(dummyViewModel: DummyViewModel): ViewModel
}