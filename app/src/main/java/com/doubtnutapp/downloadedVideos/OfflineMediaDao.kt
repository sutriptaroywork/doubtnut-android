package com.doubtnutapp.downloadedVideos

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Observable

@Dao
interface OfflineMediaDao {

    @Query("select * from offline_video order by created_at desc")
    fun getDownloadedVideos(): Observable<List<OfflineMediaItem>>

    @Query("select * from offline_video")
    fun getAllVideosData(): List<OfflineMediaItem>

    @Query("select * from offline_video where status = :downloaded")
    fun getDownloadVideoList(downloaded: OfflineMediaStatus = OfflineMediaStatus.DOWNLOADED): List<OfflineMediaItem>

    @Query("select * from offline_video where status = :downloaded or status = :downloading order by created_at desc")
    fun getDownloadedNDownloadingVideos(downloading: OfflineMediaStatus = OfflineMediaStatus.DOWNLOADING, downloaded: OfflineMediaStatus = OfflineMediaStatus.DOWNLOADED): LiveData<List<OfflineMediaItem>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun addOfflineVideoData(mediaItem: OfflineMediaItem)

    @Query("update offline_video set status = :status where videoUrl = :url")
    fun updateVideoStatus(url: String, status: OfflineMediaStatus)

    @Update
    fun updateOfflineMedia(offlineMediaItem: OfflineMediaItem)

    @Query("delete from offline_video where videoUrl = :url")
    fun deleteOfflineVideo(url: String)

    @Query("delete from offline_video where videoUrl in (:videoUrlList)")
    fun deleteOfflineVideo(videoUrlList: List<String>)

    @Query("delete from offline_video")
    fun deleteAllVideos()

    @Query("select exists(select * from offline_video WHERE videoUrl = :url)")
    fun isVideoDataExist(url: String): Boolean
}
