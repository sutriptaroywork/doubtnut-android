package com.doubtnutapp.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.doubtnut.database.dao.BaseDao
import com.doubtnutapp.db.entity.VideoViewStats

@Dao
interface VideoViewStatsDao : BaseDao<VideoViewStats> {

    @Query("select count(*) from video_view_stats WHERE type = 'LF'")
    fun getLongFormVideoCount(): Int

    @Query("select count(*) from video_view_stats WHERE type = 'SF'")
    fun getShortFormVideoCount(): Int

    @Query("select SUM(engage_time) from video_view_stats WHERE type = 'LF'")
    fun getLongFormVideoEngageTime(): Long

    @Query("select * from video_view_stats WHERE type = 'SF'")
    fun getAllShortFormVideoStats(): List<VideoViewStats>?

    @Query("select * from video_view_stats WHERE type = 'LF'")
    fun getAllLongFormVideoStats(): List<VideoViewStats>?

}
