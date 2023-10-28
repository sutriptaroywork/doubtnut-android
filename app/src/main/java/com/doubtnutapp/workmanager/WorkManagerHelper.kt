package com.doubtnutapp.workmanager

import android.content.Context
import androidx.work.*
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import com.doubtnutapp.workmanager.workers.UpdateVideoStatsWorker
import com.doubtnutapp.workmanager.workers.ViewIdTrackerWorker
import com.doubtnutapp.workmanager.workers.ViewStatusUpdateWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 24/09/20.
 */
class WorkManagerHelper @Inject constructor(@ApplicationContext private val context: Context) {

    fun assignUpdateVideoStatsWork(viewId: String, isBack: String,
                                   maxSeekTime: String, engagementTime: String, lockUnlockLogs: String?) {
        val tag = UpdateVideoStatsWorker.UPDATE_VIDEO_STATS
        val request = OneTimeWorkRequestBuilder<UpdateVideoStatsWorker>()
                .addTag(tag)
                .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .setInputData(
                        Data.Builder()
                                .apply {
                                    putString(UpdateVideoStatsWorker.VIEW_ID, viewId)
                                    putString(UpdateVideoStatsWorker.IS_BACK, isBack)
                                    putString(UpdateVideoStatsWorker.MAX_SEEK_TIME, maxSeekTime)
                                    putString(UpdateVideoStatsWorker.ENGAGEMENT_TIME, engagementTime)
                                    putString(UpdateVideoStatsWorker.VIDEO_LOCK_UNLOCK_LOGS_DATA, lockUnlockLogs)
                                }
                                .build()
                )
                .setInitialDelay(30, TimeUnit.SECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
                .build()

        WorkManager.getInstance(context).beginUniqueWork(
                getUniqueWorkNameByTagAndId(tag, viewId),
                ExistingWorkPolicy.REPLACE, request).enqueue()
    }

    fun assignViewIdTrackWork(id: Long, questionId: String,
                              maxSeekTime: String, engagementTime: String,
                              page: String, studentId: String, lockUnlockLogs: String?) {
        val tag = ViewIdTrackerWorker.TAG
        val request = OneTimeWorkRequestBuilder<ViewIdTrackerWorker>()
                .addTag(tag)
                .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .setInputData(
                        Data.Builder()
                                .apply {
                                    putLong(ViewIdTrackerWorker.ENTITY_ID, id)
                                    putString(ViewIdTrackerWorker.QUESTION_ID, questionId)
                                    putString(ViewIdTrackerWorker.MAX_SEEK_TIME, maxSeekTime)
                                    putString(ViewIdTrackerWorker.ENGAGEMENT_TIME, engagementTime)
                                    putString(ViewIdTrackerWorker.PAGE, page)
                                    putString(ViewIdTrackerWorker.STUDENT_ID, studentId)
                                    putString(ViewIdTrackerWorker.VIDEO_LOCK_UNLOCK_LOGS_DATA, lockUnlockLogs)
                                }
                                .build()
                )
                .setInitialDelay(1, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
                .build()

        WorkManager.getInstance(context).beginUniqueWork(
                getUniqueWorkNameByTagAndId(tag, id.toString()),
                ExistingWorkPolicy.REPLACE, request).enqueue()
    }

    fun assignViewStatusUpdateWorker() {
        val request = PeriodicWorkRequestBuilder<ViewStatusUpdateWorker>(12, TimeUnit.HOURS)
                .addTag(ViewStatusUpdateWorker.TAG)
                .setInitialDelay(15, TimeUnit.MINUTES)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build())
                .build()

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(ViewStatusUpdateWorker.TAG, ExistingPeriodicWorkPolicy.REPLACE, request)
    }

    fun cancelUpdateVideoStatsUniqueWork(id: String) {
        cancelUniqueWork(getUniqueWorkNameByTagAndId(UpdateVideoStatsWorker.UPDATE_VIDEO_STATS, id))
    }

    private fun getUniqueWorkNameByTagAndId(tag: String, id: String) = tag + "_" + id

    private fun cancelUniqueWork(uniqueWorkName: String) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName)
    }

}