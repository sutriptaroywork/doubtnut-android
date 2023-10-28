package com.doubtnutapp.db.entity

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.doubtnutapp.utils.DateUtils.isDayBeforeYesterday
import com.doubtnutapp.utils.DateUtils.isYesterday
import java.util.*

@Keep
@Entity(tableName = "video_view_stats")
data class VideoViewStats(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "video_time")
    val videoTime: Long,

    @ColumnInfo(name = "engage_time")
    val engageTime: Long,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()
) {
    fun isVideoViewedYesterday(): Boolean {
        return createdAt.isYesterday()
    }

    fun isVideoViewedDayBeforeYesterday(): Boolean {
        return createdAt.isDayBeforeYesterday()
    }
}
