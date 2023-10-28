package com.doubtnutapp.ui.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.course.SelectImageViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Mehul Bisht on 25-09-2021
 */

@Module
abstract class SelectImageDialogModule {

    @Binds
    @IntoMap
    @ViewModelKey(SelectImageViewModel::class)
    abstract fun bindSelectImageViewModel(viewModel: SelectImageViewModel): ViewModel
}