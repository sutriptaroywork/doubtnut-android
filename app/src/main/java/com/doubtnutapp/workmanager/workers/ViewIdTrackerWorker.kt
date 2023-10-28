package com.doubtnutapp.workmanager.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.Log
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.domain.videoPage.interactor.PublishViewOnboarding
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.workmanager.WorkManagerHelper
import retrofit2.HttpException
import javax.inject.Inject

class ViewIdTrackerWorker @Inject constructor(context: Context,
                                              workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    companion object {
        const val TAG = "VideoStatusTrackerWorker"
        const val QUESTION_ID = "questionId"
        const val MAX_SEEK_TIME = "maxSeekTime"
        const val PAGE = "page"
        const val ENGAGEMENT_TIME = "engagementTime"
        const val STUDENT_ID = "studentId"
        const val ENTITY_ID = "id"
        const val VIDEO_LOCK_UNLOCK_LOGS_DATA = "video_lock_unlock_logs_data"

    }

    @Inject
    lateinit var publishViewOnboarding: PublishViewOnboarding

    override fun doWork(): Result {
        val retryCount: Int = runAttemptCount
        if (retryCount >= 2)
            return Result.success()

        DoubtnutApp.INSTANCE.daggerAppComponent
                .forceUnWrap()
                .inject(this)

        val videoTrackDao = DoubtnutApp.INSTANCE.getDatabase()?.videoStatusTrackDao()

        var result = Result.retry()
        val id = inputData.getLong(ENTITY_ID, 0)
        val page = inputData.getString(PAGE).orEmpty()
        val questionId = inputData.getString(QUESTION_ID).orEmpty()
        val maxSeekTime = inputData.getString(MAX_SEEK_TIME).orEmpty()
        val engagementTime = inputData.getString(ENGAGEMENT_TIME).orEmpty()
        val studentId = inputData.getString(STUDENT_ID).orEmpty()
        val videoLockUnlockLogs = inputData.getString(UpdateVideoStatsWorker.VIDEO_LOCK_UNLOCK_LOGS_DATA).orEmpty()

        if (questionId.isBlank() || page.isBlank()) {
            videoTrackDao?.deleteById(id)
            return Result.success()
        }

        publishViewOnboarding.execute(PublishViewOnboarding.RequestValues(questionId = questionId, videoTime = maxSeekTime, engagementTime = engagementTime, page = page, studentId = studentId))
                .subscribeToSingle(
                        success = {
                            videoTrackDao?.getVideoTrackerById(id)?.copy(viewId = it.viewId)?.let { item ->
                                videoTrackDao.updateEntity(item)
                            }
                            WorkManagerHelper(applicationContext).assignUpdateVideoStatsWork(it.viewId, "0", maxSeekTime, engagementTime, videoLockUnlockLogs)
                            result = Result.success()
                        },
                        error = { error ->
                            videoTrackDao?.deleteByQuestionID(questionId)
                            try {
                                if (error is HttpException) {
                                    val code = error.response()?.code()
                                    if (code != null && code >= 400 && code < 500) {
                                        result = Result.failure()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(e, TAG)
                            }
                        }
                )

        return result
    }


}