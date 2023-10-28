package com.doubtnutapp.downloadedVideos

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "offline_video")
data class OfflineMediaItem(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long = 0,

    @ColumnInfo(name = "videoUrl")
    val videoUrl: String,

    @ColumnInfo(name = "questionId")
    val questionId: Int,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "drm_licence_url")
    val drmLicenseUrl: String?,

    @ColumnInfo(name = "thumb_url")
    val thumbUrl: String?,

    @ColumnInfo(name = "drm_licence_expire_date")
    val licenceExpireDate: Long,

    @ColumnInfo(name = "media_type")
    val mediaType: String?,

    @ColumnInfo(name = "aspect_ratio")
    val aspectRatio: String?,

    @ColumnInfo(name = "subscription_expire_date")
    val subscriptionExpireDate: Long,

    @ColumnInfo(name = "status")
    val status: OfflineMediaStatus = OfflineMediaStatus.INITIAL,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date()

) {
    var isSelected: Boolean = false
}

enum class OfflineMediaStatus(val code: Int) {
    DOWNLOADED(1),
    FAILURE(-1),
    DOWNLOADING(0),
    EXPIRED(2),
    INITIAL(3)
}
