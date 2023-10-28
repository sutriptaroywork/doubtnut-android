package com.doubtnutapp.ui.mockTest.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.libraryhome.mocktest.viewmodel.MockTestSyllabusViewModel
import com.doubtnutapp.ui.mockTest.MockTestListViewModel
import com.doubtnutapp.ui.mockTest.MockTestQuestionViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Anand Gaurav on 2019-07-20.
 */
@Module
abstract class MockTestCommonModule {

    @Binds
    @IntoMap
    @ViewModelKey(MockTestListViewModel::class)
    abstract fun bindMockTestListViewModel(formulaSheetViewModel: MockTestListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MockTestQuestionViewModel::class)
    abstract fun bindMockTestQuestionViewModel(formulaSheetViewModel: MockTestQuestionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MockTestSyllabusViewModel::class)
    abstract fun bindMockTestSyllabusViewModel(syllabusViewModel: MockTestSyllabusViewModel): ViewModel
}