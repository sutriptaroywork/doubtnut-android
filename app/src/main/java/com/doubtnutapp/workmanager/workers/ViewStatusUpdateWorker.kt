package com.doubtnutapp.workmanager.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.Features
import com.doubtnutapp.FeaturesManager
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.workmanager.WorkManagerHelper
import javax.inject.Inject

class ViewStatusUpdateWorker @Inject constructor(context: Context,
                                                 workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    companion object {
        const val TAG = "ViewStatusUpdateWorker"
    }

    override fun doWork(): Result {

        DoubtnutApp.INSTANCE.daggerAppComponent
                .forceUnWrap()
                .inject(this)

        if (!FeaturesManager.isFeatureEnabled(applicationContext, Features.OFFLINE_VIDEOS))
            return Result.success()

        val viewTackerDao = DoubtnutApp.INSTANCE.getDatabase()?.videoStatusTrackDao()
        viewTackerDao?.deleteCompletedTask()
        viewTackerDao?.getNonViewIdEntity()?.forEach {
            WorkManagerHelper(applicationContext).assignViewIdTrackWork(it.id, it.questionId, it.videoTime, it.engageTime, it.page, it.studentId, "")
        }

        viewTackerDao?.getViewIdEntityWithStatus(false)?.forEach {
            WorkManagerHelper(applicationContext).assignUpdateVideoStatsWork(it.viewId.orEmpty(), "0", it.videoTime, it.engageTime, "")
        }

        return Result.success()

    }

}