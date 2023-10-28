package com.doubtnutapp.workmanager.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.domain.videoPage.interactor.UpdateVideoViewInteractor
import com.doubtnutapp.forceUnWrap
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 24/09/20.
 */
class UpdateVideoStatsWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    companion object {
        const val UPDATE_VIDEO_STATS = "update_video_stats"
        const val VIEW_ID = "viewId"
        const val IS_BACK = "isBack"
        const val MAX_SEEK_TIME = "maxSeekTime"
        const val ENGAGEMENT_TIME = "engagementTime"
        const val VIDEO_LOCK_UNLOCK_LOGS_DATA = "video_lock_unlock_logs_data"
    }

    @Inject
    lateinit var updateVideoViewUseCase: UpdateVideoViewInteractor

    override fun doWork(): Result {

        if (runAttemptCount >= 2)
            return Result.success()

        var result = Result.retry()
        val viewId = inputData.getString(VIEW_ID)
        val isBack = inputData.getString(IS_BACK).orEmpty()
        val maxSeekTime = inputData.getString(MAX_SEEK_TIME).orEmpty()
        val engagementTime = inputData.getString(ENGAGEMENT_TIME).orEmpty()
        val videoLockUnlockLogs = inputData.getString(VIDEO_LOCK_UNLOCK_LOGS_DATA).orEmpty()

        if (viewId == null) return Result.failure()

        DoubtnutApp.INSTANCE.daggerAppComponent
                .forceUnWrap()
                .inject(this)

        updateVideoViewUseCase.execute(
                UpdateVideoViewInteractor.Param(
                        viewId = viewId,
                        isBack = isBack,
                        maxSeekTime = maxSeekTime,
                        engagementTime = engagementTime,
                        lockUnlockLogs = videoLockUnlockLogs,
                        scheduled = true))
                .subscribeToCompletable(
                        success = {
                            DoubtnutApp.INSTANCE.getDatabase()?.videoStatusTrackDao()?.updateVideoTrackStatus(viewId)
                            result = Result.success()
                        },
                        error = { error ->
                            try {
                                if (error is HttpException) {
                                    val code = error.response()?.code()
                                    if (code != null && code >= 400 && code < 500) {
                                        result = Result.failure()
                                    }
                                } else {
                                    result = Result.retry()
                                }
                            } catch (e: Exception) {
                                result = Result.retry()
                            }
                        }
                )

        return result
    }
}