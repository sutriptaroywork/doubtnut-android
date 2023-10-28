package com.doubtnutapp.networkstats.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.doubtnutapp.networkstats.models.VideoStatsData
import kotlinx.coroutines.flow.Flow

/**
 * Created by Raghav Aggarwal on 04/02/22.
 */
@Dao
interface VideoNetworkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveData(data: VideoStatsData)

    @Query("SELECT * from video_network_data")
    fun getData(): Flow<List<VideoStatsData>>

    @Query("DELETE FROM video_network_data")
    fun deleteData()
}