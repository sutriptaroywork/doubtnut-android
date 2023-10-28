package com.doubtnutapp.downloadedVideos

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.data.remote.DataHandler
import io.reactivex.Single

class DownloadedVideoRefresherWorker(context: Context, workerParams: WorkerParameters) :
    RxWorker(context, workerParams) {

    companion object {
        const val TAG = "DownloadedVideoRefresherWorker"
    }

    override fun createWork(): Single<Result> {
        return Single.fromCallable {
            fetchDownloadedVideos()
            Result.success()
        }
    }

    private fun fetchDownloadedVideos() {
        DoubtnutApp.INSTANCE.getDatabase()?.offlineMediaDao()?.getAllVideosData()
            ?.forEach {
                if (it.status == OfflineMediaStatus.DOWNLOADED || it.status == OfflineMediaStatus.EXPIRED)
                    updateSubscription(it)
            }
    }

    private fun updateSubscription(offlineMediaItem: OfflineMediaItem) {
        val response = DataHandler.INSTANCE.videoDownloadRepository.checkValidity(offlineMediaItem.questionId.toString())
            .execute()
        if (response.isSuccessful && response.body() != null) {
            val subscriptionDate = response.body()
            val omi = offlineMediaItem.copy(subscriptionExpireDate = subscriptionDate?.time!!, status = OfflineMediaStatus.DOWNLOADED)
            DoubtnutApp.INSTANCE.getDatabase()?.offlineMediaDao()?.updateOfflineMedia(omi)
        } else {
            DoubtnutApp.INSTANCE.getDatabase()?.offlineMediaDao()?.updateVideoStatus(offlineMediaItem.videoUrl, OfflineMediaStatus.EXPIRED)
        }
    }
}
