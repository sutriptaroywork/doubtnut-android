package com.doubtnut.scholarship.di.module

import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnut.scholarship.data.remote.ScholarshipRepository
import com.doubtnut.scholarship.data.remote.ScholarshipRepositoryImpl
import com.doubtnut.scholarship.data.remote.ScholarshipService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
object ScholarshipModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideScholarshipService(@ApiRetrofit retrofit: Retrofit): ScholarshipService =
        retrofit.create(ScholarshipService::class.java)

    @Provides
    @JvmStatic
    @Singleton
    fun provideScholarshipRepository(
        scholarshipService: ScholarshipService
    ): ScholarshipRepository = ScholarshipRepositoryImpl(scholarshipService)
}