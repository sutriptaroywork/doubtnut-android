package com.doubtnutapp.revisioncorner.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.addtoplaylist.di.AddToPlaylistModule
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.resourcelisting.repository.ResourceListingRepositoryImpl
import com.doubtnutapp.data.resourcelisting.service.ResourceListingService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.domain.resourcelisting.repository.ResourceListingRepository
import com.doubtnutapp.revisioncorner.viewmodel.*
import com.doubtnutapp.sharing.di.WhatsAppSharingBindModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by devansh on 10/08/21.
 */

@Module(includes = [WhatsAppSharingBindModule::class, AddToPlaylistModule::class])
abstract class RevisionCornerModule {

    @Binds
    @IntoMap
    @ViewModelKey(RcHomeViewModel::class)
    abstract fun bindRevisionCornerViewModel(rcHomeViewModel: RcHomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RcStatsViewModel::class)
    abstract fun bindRcStatsViewModel(rcStatsViewModel: RcStatsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RcResultViewModel::class)
    abstract fun bindRcResultViewModel(rcResultViewModel: RcResultViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RcRulesViewModel::class)
    abstract fun bindRcRuleViewModel(rcRulesViewModel: RcRulesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RcResultListViewModel::class)
    abstract fun bindRcResultListViewModel(rcResultListViewModel: RcResultListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RcChapterSelectionViewModel::class)
    abstract fun bindRcChapterSelectionViewModel(rcChapterSelectionViewModel: RcChapterSelectionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RcShortTestViewModel::class)
    abstract fun bindRcShortTestViewModel(rcShortTestViewModel: RcShortTestViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RcShortTestSolutionViewModel::class)
    abstract fun bindRcShortTestSolutionViewModel(rcShortTestSolutionViewModel: RcShortTestSolutionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RcTestListViewModel::class)
    abstract fun bindRcTestListViewModel(rcTestListViewModel: RcTestListViewModel): ViewModel

    @Binds
    abstract fun bindResourceListingRepository(resourceListingRepositoryImpl: ResourceListingRepositoryImpl): ResourceListingRepository

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun providerResourceListingService(@ApiRetrofit retrofit: Retrofit): ResourceListingService {
            return retrofit.create(ResourceListingService::class.java)
        }
    }
}