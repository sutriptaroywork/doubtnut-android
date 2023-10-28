package com.doubtnutapp.ui.test.dimodule

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.ui.test.TestQuestionViewModel
import com.doubtnutapp.ui.test.TestViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Anand Gaurav on 2019-07-20.
 */
@Module
abstract class TestQuestionActivityModule {

    @Binds
    @IntoMap
    @ViewModelKey(TestQuestionViewModel::class)
    abstract fun bindTestQuestionViewModel(viewModel: TestQuestionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TestViewModel::class)
    abstract fun bindTestViewModel(viewModel: TestViewModel): ViewModel
}