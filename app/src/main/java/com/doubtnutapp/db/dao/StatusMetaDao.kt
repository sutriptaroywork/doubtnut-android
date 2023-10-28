package com.doubtnutapp.db.dao

import androidx.annotation.Keep
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.doubtnutapp.db.entity.StatusMeta

@Keep
@Dao
interface StatusMetaDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertViewedStatus(StatusMeta: StatusMeta): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLikedStatus(StatusMeta: StatusMeta): Long

    @Query("DELETE FROM statusMeta WHERE addedAt < :expiryTime")
    fun deleteOutdated(expiryTime: Long)

    @Query("SELECT status_id FROM statusMeta WHERE viewed = :isViewed")
    fun getViewedStatus(isViewed: Boolean): List<String>

    @Query("SELECT status_id FROM statusMeta WHERE liked  = :isLiked")
    fun getLikedStatus(isLiked: Boolean): List<String>
}
