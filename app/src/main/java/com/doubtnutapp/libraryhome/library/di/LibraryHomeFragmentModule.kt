package com.doubtnutapp.libraryhome.library.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.WidgetPlanButtonVM
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.library.repository.LibraryHomeFragmentRepositoryImpl
import com.doubtnutapp.data.library.service.LibraryHomeFragmentService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerFragment
import com.doubtnutapp.domain.library.repository.LibraryHomeFragmentRepository
import com.doubtnutapp.libraryhome.library.viewmodel.LibraryHomeFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
abstract class LibraryHomeFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(LibraryHomeFragmentViewModel::class)
    @PerFragment
    abstract fun bindLibraryHomeFragmentViewModel(libraryHomeFragmentViewModel: LibraryHomeFragmentViewModel): ViewModel

    @Binds
    abstract fun bindLibraryHomeFragmentRepository(libraryHomeFragmentRepositoryImpl: LibraryHomeFragmentRepositoryImpl): LibraryHomeFragmentRepository

    @Binds
    @IntoMap
    @ViewModelKey(WidgetPlanButtonVM::class)
    internal abstract fun bindWidgetPlanButtonVM(viewModel: WidgetPlanButtonVM): ViewModel

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideLibraryHomeFragmentServer(@ApiRetrofit retrofit: Retrofit): LibraryHomeFragmentService =
                retrofit.create(LibraryHomeFragmentService::class.java)

    }

}