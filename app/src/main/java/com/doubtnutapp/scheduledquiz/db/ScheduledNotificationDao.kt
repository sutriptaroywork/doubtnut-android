package com.doubtnutapp.scheduledquiz.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.doubtnutapp.scheduledquiz.di.model.ScheduledQuizNotificationModel

@Dao
interface ScheduledNotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveScheduledNotifications(scheduledQuizNotifications: List<ScheduledQuizNotificationModel>)

    @Query("DELETE FROM scheduled_notifications")
    suspend fun deleteAllNotifications()

    @Query("SELECT * FROM scheduled_notifications")
    suspend fun getAllScheduledNotifications(): List<ScheduledQuizNotificationModel>

    @Query("DELETE FROM scheduled_notifications WHERE id IN (SELECT id FROM scheduled_notifications ORDER BY id ASC LIMIT 1)")
    suspend fun clearTopScheduledNotification(): Int

    @Query("SELECT * FROM scheduled_notifications ORDER BY id ASC LIMIT 1")
    suspend fun getTopScheduledNotification(): ScheduledQuizNotificationModel

    @Query("DELETE FROM scheduled_notifications WHERE expiryInMillis IS NOT NULL AND expiryInMillis < :expiry")
    suspend fun deleteExpiredNotification(expiry: Long = System.currentTimeMillis())

}