package com.doubtnutapp.video

import androidx.room.*

@Dao
interface VideoStatusTrackDao {

    @Query("SELECT * FROM video_status_track where view_id is null")
    fun getNonViewIdEntity(): List<VideoStatusTrack>

    @Query("SELECT * FROM video_status_track where view_id is not null and is_view_tracked = :isViewTracked")
    fun getViewIdEntityWithStatus(isViewTracked: Boolean): List<VideoStatusTrack>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertVideoStatusTrack(videoStatusTrack: VideoStatusTrack): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateEntity(videoStatusTrack: VideoStatusTrack)

    @Query("SELECT * FROM video_status_track where view_id = :viewId")
    fun getVideoTrackerByViewId(viewId: String): VideoStatusTrack?

    @Query("SELECT * FROM video_status_track where id = :id")
    fun getVideoTrackerById(id: Long): VideoStatusTrack?

    @Query("UPDATE video_status_track SET is_view_tracked = :isViewTracked WHERE view_id = :viewId")
    fun updateVideoTrackStatus(viewId: String, isViewTracked: Boolean = true)

    @Query("DELETE FROM video_status_track where view_id is not null and is_view_tracked = :status")
    fun deleteCompletedTask(status: Boolean = true)

    @Query("DELETE FROM video_status_track where question_id is :questionId")
    fun deleteByQuestionID(questionId: String)

    @Query("DELETE FROM video_status_track where id is :id")
    fun deleteById(id: Long)
}