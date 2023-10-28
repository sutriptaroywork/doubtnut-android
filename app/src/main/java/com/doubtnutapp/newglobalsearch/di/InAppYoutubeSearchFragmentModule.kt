package com.doubtnutapp.newglobalsearch.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.data.newglobalsearch.repository.YoutubeSearchRepositoryImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerFragment
import com.doubtnutapp.domain.newglobalsearch.repository.YoutubeSearchRepository
import com.doubtnutapp.newglobalsearch.viewmodel.YoutubeSearchFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class InAppYoutubeSearchFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(YoutubeSearchFragmentViewModel::class)
    abstract fun bindYoutubeSearchFragmentViewModel(youtubeSearchViewModel: YoutubeSearchFragmentViewModel): ViewModel

    @Binds
    @PerFragment
    abstract fun bindInAppYoutubeSearchRepository(youtubeSearchRepository: YoutubeSearchRepositoryImpl):
            YoutubeSearchRepository
}