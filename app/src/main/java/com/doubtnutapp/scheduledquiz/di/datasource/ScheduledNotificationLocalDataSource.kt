package com.doubtnutapp.scheduledquiz.di.datasource

import com.doubtnutapp.scheduledquiz.di.model.ScheduledQuizNotificationModel

interface ScheduledNotificationLocalDataSource {
    suspend fun getAllScheduledNotificationsFromLocal(): List<ScheduledQuizNotificationModel>
    suspend fun getTopScheduledQuizNotificationFromLocal(): ScheduledQuizNotificationModel
    suspend fun saveScheduledNotificationsToLocal(scheduledQuizNotificationModels: List<ScheduledQuizNotificationModel>)
    suspend fun clearTopScheduledQuizNotificationFromLocal()
    suspend fun clearAllScheduledNotificationsFromLocal()
    suspend fun deleteExpiredNotification()
}