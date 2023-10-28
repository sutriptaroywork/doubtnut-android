package com.doubtnutapp.ui.mockTest.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.data.remote.repository.MockTestRepository
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.ui.mockTest.viewmodel.MockTestAnalysisViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MockTestAnalysisModule {

    @Binds
    @IntoMap
    @ViewModelKey(MockTestAnalysisViewModel::class)
    abstract fun bindMockTestAnalysisModule(mockTestAnalysisViewModel: MockTestAnalysisViewModel): ViewModel
}