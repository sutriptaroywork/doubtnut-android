package com.doubtnutapp.ui.test.dimodule

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.ui.test.TestViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class TestQuestionFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(TestViewModel::class)
    abstract fun bindViewModel(viewModel: TestViewModel): ViewModel
}