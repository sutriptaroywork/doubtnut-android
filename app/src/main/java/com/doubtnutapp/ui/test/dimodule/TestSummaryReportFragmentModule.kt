package com.doubtnutapp.ui.test.dimodule

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.ui.test.TestSummaryFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class TestSummaryReportFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(TestSummaryFragmentViewModel::class)
    abstract fun bindTestViewModel(testSummaryFragmentViewModel: TestSummaryFragmentViewModel): ViewModel
}