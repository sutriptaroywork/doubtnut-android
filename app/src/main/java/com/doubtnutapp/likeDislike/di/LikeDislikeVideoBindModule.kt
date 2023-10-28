package com.doubtnutapp.likeDislike.di

import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnutapp.data.likeDislike.repository.LikeDislikeRepositoryImpl
import com.doubtnutapp.data.likeDislike.service.LikeDislikeService
import com.doubtnutapp.domain.likeDislike.interactor.LikeDisLikeVideo
import com.doubtnutapp.domain.likeDislike.repository.LikeDisLikeRepository
import com.doubtnutapp.likeDislike.LikeDislikeVideo
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import retrofit2.Retrofit

@Module
abstract class LikeDislikeVideoBindModule {

    @Binds
    abstract fun bindWhatsAppRepository(likeDislikeRepositoryImpl: LikeDislikeRepositoryImpl): LikeDisLikeRepository

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun providerLikeDislikeService(@ApiRetrofit retrofit: Retrofit): LikeDislikeService {
            return retrofit.create(LikeDislikeService::class.java)
        }


        @Provides
        @JvmStatic
        fun providesLikeDislike(
                likeDisLikeVideo: LikeDisLikeVideo,
                compositeDisposable: CompositeDisposable) = LikeDislikeVideo(
                likeDisLikeVideo,
                compositeDisposable
        )
    }
}