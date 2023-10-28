package com.doubtnutapp.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.doubtnutapp.model.AppActiveFeedback

@Dao
interface FeedbackDao {

    @Query("SELECT * FROM active_feedback")
    fun getActiveFeedbacks(): LiveData<List<AppActiveFeedback>>

    @Query("SELECT * FROM active_feedback where status = 'active' AND" + " type = :type")
    fun getActiveFeedbackForType(type: String): LiveData<List<AppActiveFeedback>>

    @Query("SELECT * FROM active_feedback where status = 'active'")
    fun getActiveFeedback(): LiveData<List<AppActiveFeedback>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllFeedbacks(event: List<AppActiveFeedback>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFeedback(event: AppActiveFeedback)

    @Delete
    fun delete(event: AppActiveFeedback)

    @Query("SELECT count FROM active_feedback where type = :type")
    fun getCountForType(type: String): Int

    @Query("UPDATE active_feedback SET status = :status where type = :type")
    fun updateFeedbackStatus(status: String, type: String)

    @Query("DELETE from active_feedback")
    fun clearActiveFeedbacks()

    @Query("DELETE from active_feedback where id = :id")
    fun clearNpsFeedbacks(id: String)
}
