package com.doubtnutapp.downloadedVideos

class DownloadedVideosRepository constructor(private val offlineMediaDao: OfflineMediaDao) {

    fun getDownloadedVideos() = offlineMediaDao.getDownloadedVideos()

    fun deleteDownloadedVideo(videoUrl: String) {
        offlineMediaDao.deleteOfflineVideo(videoUrl)
    }

    fun deleteDownloadedVideo(videoUrls: List<String>) {
        offlineMediaDao.deleteOfflineVideo(videoUrls)
    }
}
