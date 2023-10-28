package com.doubtnutapp.libraryhome.mocktest.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.mocktestLibrary.repository.MockTestRepositoryImpl
import com.doubtnutapp.data.mocktestLibrary.service.MockTestService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.domain.mockTestLibrary.repository.MockTestRepository
import com.doubtnutapp.libraryhome.mocktest.viewmodel.MockTestViewModel
import com.doubtnutapp.ui.mockTest.MockTestListViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class ReviewQuestionModule {

    @Binds
    @IntoMap
    @ViewModelKey(MockTestListViewModel::class)
    abstract fun bindMockTestListViewModel(mockTestListViewModel: MockTestListViewModel): ViewModel

}