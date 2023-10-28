package com.doubtnutapp.networkstats.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Raghav Aggarwal on 04/02/22.
 */
@Entity(tableName = "video_network_data")
data class VideoStatsData(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,

    @ColumnInfo(name = "question_id")
    var questionId: String = "",

    @ColumnInfo(name = "video_name")
    var videoName: String = "Title Not Available",

    @ColumnInfo(name = "video_bytes")
    var videoBytes: Long = 0,

    @ColumnInfo(name = "video_url")
    var videoUrl: String = "",

    @ColumnInfo(name = "date")
    var date: Long = 0,

    @ColumnInfo(name = "engagement_time")
    var engagementTime: Long = 0,

    @ColumnInfo(name = "seek_time")
    var seekTime: Long = 0,

    @ColumnInfo(name = "content_type")
    var contentType: String = ""
)