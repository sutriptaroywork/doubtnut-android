package com.doubtnutapp.similarplaylist.di

import androidx.lifecycle.ViewModel
import com.doubtnutapp.data.addtoplaylist.repository.AddToPlaylistRepositoryImpl
import com.doubtnutapp.data.addtoplaylist.service.AddToPlaylistService
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.similarVideo.repository.SimilarVideoRepositoryImpl
import com.doubtnutapp.data.similarVideo.service.SimilarVideoService
import com.doubtnut.core.di.ViewModelKey
import com.doubtnut.core.di.scope.PerFragment
import com.doubtnutapp.domain.addtoplaylist.repository.AddToPlaylistRepository
import com.doubtnutapp.domain.similarVideo.repository.SimilarVideoRepository
import com.doubtnutapp.likeDislike.di.LikeDislikeVideoBindModule
import com.doubtnutapp.sharing.di.WhatsAppSharingBindModule
import com.doubtnutapp.similarplaylist.viewmodel.SimilarPlaylistFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module(includes = [WhatsAppSharingBindModule::class, LikeDislikeVideoBindModule::class])

abstract class SimilarPlaylistFragmentBindModule {

    @Binds
    @IntoMap
    @ViewModelKey(SimilarPlaylistFragmentViewModel::class)
    abstract fun bindSimilarPlaylistFragmentViewModel(similarPlaylistFragmentViewModel: SimilarPlaylistFragmentViewModel): ViewModel

    @Binds
    @PerFragment
    abstract fun bindSimilarVideoRepository(similarVideoRepositoryImpl: SimilarVideoRepositoryImpl): SimilarVideoRepository

    @Binds
    @PerFragment
    abstract fun bindAddToPlaylistRepository(addToPlaylistRepositoryImpl: AddToPlaylistRepositoryImpl): AddToPlaylistRepository

    @Module
    companion object {
        @Provides
        @JvmStatic
        @PerFragment
        fun provideMatchQuestionService(@ApiRetrofit retrofit: Retrofit): SimilarVideoService =
                retrofit.create(SimilarVideoService::class.java)

        @Provides
        @JvmStatic
        fun providerAddToPlaylistService(@ApiRetrofit retrofit: Retrofit): AddToPlaylistService {
            return retrofit.create(AddToPlaylistService::class.java)
        }
    }

}