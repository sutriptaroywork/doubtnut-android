package com.doubtnutapp.di.module

import com.doubtnutapp.data.common.repository.test.TestRepositoryImpl
import com.doubtnutapp.data.videoPage.repository.VideoPageRepositoryImpl
import com.doubtnutapp.db.DoubtnutDatabase
import com.doubtnutapp.domain.common.repository.TestRepository
import com.doubtnutapp.domain.videoPage.repository.VideoPageRepository
import com.doubtnutapp.fallbackquiz.db.FallbackQuizDao
import com.doubtnutapp.scheduledquiz.db.ScheduledNotificationDao
import com.doubtnutapp.scheduledquiz.db.repository.ScheduledNotifRemoteDataSourceImpl
import com.doubtnutapp.scheduledquiz.db.repository.ScheduledNotificationLocalDataSourceImpl
import com.doubtnutapp.scheduledquiz.db.repository.ScheduledQuizNotificationRepository
import com.doubtnutapp.scheduledquiz.di.datasource.ScheduledNotificationLocalDataSource
import com.doubtnutapp.scheduledquiz.di.datasource.ScheduledNotificationRemoteDataSource
import com.doubtnutapp.scheduledquiz.di.remote.api.services.ScheduledQuizNotificationService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object RepositoryModule {

    @Provides
    @JvmStatic
    @Singleton
    fun bindQuizRepository(): TestRepository {
        return TestRepositoryImpl()
    }

    @Provides
    @JvmStatic
    @Singleton
    fun bindVideoRepository(videoPageRepositoryImpl: VideoPageRepositoryImpl): VideoPageRepository {
        return videoPageRepositoryImpl
    }

    @Provides
    @JvmStatic
    fun provideScheduledNotificationRepository(
        scheduledNotificationRemoteDataSource: ScheduledNotificationRemoteDataSource,
        scheduledNotifLocalDataSource: ScheduledNotificationLocalDataSource
    ): ScheduledQuizNotificationRepository {
        return ScheduledQuizNotificationRepository(
            scheduledNotificationRemoteDataSource,
            scheduledNotifLocalDataSource
        )
    }

    @Provides
    @JvmStatic
    fun provideScheduledNotificationRemoteDataSource(scheduledQuizNotificationService: ScheduledQuizNotificationService): ScheduledNotificationRemoteDataSource {
        return ScheduledNotifRemoteDataSourceImpl(scheduledQuizNotificationService)
    }

    @Provides
    @JvmStatic
    fun provideScheduledNotificationLocalDataSource(scheduledNotificationDao: ScheduledNotificationDao): ScheduledNotificationLocalDataSource {
        return ScheduledNotificationLocalDataSourceImpl(scheduledNotificationDao)
    }

    @Provides
    @JvmStatic
    internal fun bindScheduleNotifDao(doubtnutDatabase: DoubtnutDatabase): ScheduledNotificationDao {
        return doubtnutDatabase.scheduledNotifDao()
    }

    @Provides
    @JvmStatic
    internal fun bindFallbackQuizDao(doubtnutDatabase: DoubtnutDatabase): FallbackQuizDao {
        return doubtnutDatabase.fallbackQuizDao()
    }
}
