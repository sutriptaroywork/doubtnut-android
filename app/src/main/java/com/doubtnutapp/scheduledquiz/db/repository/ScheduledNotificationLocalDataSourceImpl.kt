package com.doubtnutapp.scheduledquiz.db.repository

import com.doubtnutapp.scheduledquiz.db.ScheduledNotificationDao
import com.doubtnutapp.scheduledquiz.di.datasource.ScheduledNotificationLocalDataSource
import com.doubtnutapp.scheduledquiz.di.model.ScheduledQuizNotificationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduledNotificationLocalDataSourceImpl(private val scheduledNotificationDao: ScheduledNotificationDao) : ScheduledNotificationLocalDataSource {
    override suspend fun getAllScheduledNotificationsFromLocal(): List<ScheduledQuizNotificationModel> {
        return scheduledNotificationDao.getAllScheduledNotifications()
    }

    override suspend fun getTopScheduledQuizNotificationFromLocal(): ScheduledQuizNotificationModel {
        return scheduledNotificationDao.getTopScheduledNotification()
    }

    override suspend fun saveScheduledNotificationsToLocal(scheduledQuizNotificationModels: List<ScheduledQuizNotificationModel>) {
        CoroutineScope(Dispatchers.IO).launch {
            scheduledNotificationDao.saveScheduledNotifications(scheduledQuizNotificationModels);
        }
    }

    override suspend fun clearTopScheduledQuizNotificationFromLocal() {
        CoroutineScope(Dispatchers.IO).launch {
            scheduledNotificationDao.clearTopScheduledNotification();
        }
    }

    override suspend fun clearAllScheduledNotificationsFromLocal() {
        CoroutineScope(Dispatchers.IO).launch {
            scheduledNotificationDao.deleteAllNotifications();
        }
    }

    override suspend fun deleteExpiredNotification() {
        scheduledNotificationDao.deleteExpiredNotification()
    }
}