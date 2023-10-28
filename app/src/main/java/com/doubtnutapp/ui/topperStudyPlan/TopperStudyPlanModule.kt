package com.doubtnutapp.ui.topperStudyPlan

import androidx.lifecycle.ViewModel
import com.doubtnutapp.data.addtoplaylist.repository.AddToPlaylistRepositoryImpl
import com.doubtnutapp.data.addtoplaylist.service.AddToPlaylistService
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerActivity
import com.doubtnutapp.domain.addtoplaylist.repository.AddToPlaylistRepository
import com.doubtnutapp.sharing.di.WhatsAppSharingBindModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module(includes = [WhatsAppSharingBindModule::class])
abstract class TopperStudyPlanModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun providerAddToPlaylistService(@ApiRetrofit retrofit: Retrofit): AddToPlaylistService {
            return retrofit.create(AddToPlaylistService::class.java)
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(TopperStudyPlanViewModel::class)
    abstract fun bindsTopperStudyPlanViewModel(topperStudyPlanViewModel: TopperStudyPlanViewModel): ViewModel

    @Binds
    @PerActivity
    abstract fun bindAddToPlaylistRepository(addToPlaylistRepositoryImpl: AddToPlaylistRepositoryImpl): AddToPlaylistRepository

}