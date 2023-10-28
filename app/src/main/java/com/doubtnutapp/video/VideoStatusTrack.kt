package com.doubtnutapp.video

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "video_status_track")
data class VideoStatusTrack(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long = 0,
        @ColumnInfo(name = "question_id")
        val questionId: String,
        @ColumnInfo(name = "video_time")
        val videoTime: String,
        @ColumnInfo(name = "engage_time")
        val engageTime: String,
        @ColumnInfo(name = "page")
        val page: String,
        @ColumnInfo(name = "student_id")
        val studentId: String,
        @ColumnInfo(name = "view_id")
        val viewId: String? = null,
        @ColumnInfo(name = "is_view_tracked")
        val isViewTracked: Boolean = false
)