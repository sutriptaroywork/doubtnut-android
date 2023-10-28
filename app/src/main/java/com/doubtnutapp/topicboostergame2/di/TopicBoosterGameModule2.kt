package com.doubtnutapp.topicboostergame2.di

import androidx.lifecycle.ViewModel
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.resourcelisting.repository.ResourceListingRepositoryImpl
import com.doubtnutapp.data.resourcelisting.service.ResourceListingService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnutapp.domain.resourcelisting.repository.ResourceListingRepository
import com.doubtnutapp.topicboostergame2.ui.FaqBottomSheetDialogFragment
import com.doubtnutapp.topicboostergame2.viewmodel.*
import com.doubtnutapp.topicboostergame2.viewmodel.TbgGameFlowViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

/**
 * Created by devansh on 27/2/21.
 */

@Module
abstract class TopicBoosterGameModule2 {

    @Binds
    @IntoMap
    @ViewModelKey(TbgHomeViewModel::class)
    abstract fun bindTbgHomeViewModel(tbgHomeViewModel: TbgHomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LevelsViewModel::class)
    abstract fun bindLevelsViewModel(levelsViewModel: LevelsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChaptersViewModel::class)
    abstract fun bindChaptersViewModel(chaptersViewModel: ChaptersViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InviteFriendsViewModel::class)
    abstract fun bindInviteFriendViewModel(inviteFriendViewModel: InviteFriendsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(InviteFriendListViewModel::class)
    abstract fun bindInviteFriendListViewModel(inviteFriendListViewModel: InviteFriendListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TbgGameFlowViewModel::class)
    abstract fun bindTbgOpponentWaitViewModel(chaptersViewModel: TbgGameFlowViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TbgLeaderboardViewModel::class)
    abstract fun bindTbgLeaderboardViewModel(chaptersViewModel: TbgLeaderboardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TbgLeaderboardListViewModel::class)
    abstract fun bindTbgLeaderboardListViewModel(chaptersViewModel: TbgLeaderboardListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TbgResultViewModel::class)
    abstract fun bindTbgResultViewModel(tbgResultViewModel: TbgResultViewModel): ViewModel

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