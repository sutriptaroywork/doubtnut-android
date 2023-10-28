package com.doubtnut.referral.di

import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnut.referral.data.remote.ReferralService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
object ReferralModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideReferralService(@ApiRetrofit retrofit: Retrofit): ReferralService =
        retrofit.create(ReferralService::class.java)
}