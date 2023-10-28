package com.doubtnutapp.matchquestion.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.data.addtoplaylist.repository.AddToPlaylistRepositoryImpl
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.domain.addtoplaylist.repository.AddToPlaylistRepository
import com.doubtnutapp.matchquestion.viewmodel.MatchQuestionFragmentViewModel
import com.doubtnutapp.sharing.di.WhatsAppSharingBindModule
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module(includes = [WhatsAppSharingBindModule::class])

abstract class MatchQuestionFragmentBindModule {

    @Binds
    @IntoMap
    @ViewModelKey(MatchQuestionFragmentViewModel::class)
    abstract fun bindMatchQuestionActivityViewModel(matchQuestionFragmentViewModel: MatchQuestionFragmentViewModel): ViewModel

    @Binds
    abstract fun bindAddToPlaylistRepository(addToPlaylistRepositoryImpl: AddToPlaylistRepositoryImpl): AddToPlaylistRepository
}