package com.doubtnutapp.textsolution.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.data.textsolution.repository.TextSolutionRepositoryImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.textsolution.repository.TextSolutionRepository
import com.doubtnutapp.textsolution.viewmodel.TextSolutionViewModel
import com.doubtnutapp.videoPage.di.VideoPageActivityBindModule
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Created by Anand Gaurav on 2019-08-28.
 */

@Module(includes = [VideoPageActivityBindModule::class])
abstract class TextSolutionActivityBindingModule {

    @Binds
    @IntoMap
    @ViewModelKey(TextSolutionViewModel::class)
    @PerActivity
    abstract fun bindTextSolutionViewModel(textSolutionViewModel: TextSolutionViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindTextSolutionRepository(textSolutionRepositoryImpl: TextSolutionRepositoryImpl): TextSolutionRepository
}