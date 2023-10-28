package com.doubtnutapp.config

import android.content.Context
import androidx.work.*
import com.doubtnut.core.worker.ISyncConfigDataWorker
import com.doubtnutapp.Constants
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.utils.ConfigUtils

class SyncConfigDataWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams), ISyncConfigDataWorker {

    override suspend fun doWork(): Result {
        val postPurchaseSessionCount =
            defaultPrefs().getInt(Constants.POST_PURCHASE_SESSION_COUNT, 0)
        val sessionCount = defaultPrefs().getInt(Constants.SESSION_COUNT, 1)
        DataHandler.INSTANCE.appConfigRepository.getConfigData(
            sessionCount,
            postPurchaseSessionCount
        )
            .applyIoToMainSchedulerOnSingle()
            .map {
                it.data
            }
            .subscribeToSingle({
                ConfigUtils.saveToPref(it)
            }, { })

        return Result.success()
    }

    companion object {
        const val TAG = "SyncConfigDataWorker"

        fun enqueue(context: Context) {
            if (ISyncConfigDataWorker.syncConfigData) {
                ISyncConfigDataWorker.syncConfigData = false
                WorkManager
                    .getInstance(context)
                    .beginUniqueWork(
                        TAG,
                        ExistingWorkPolicy.KEEP,
                        OneTimeWorkRequestBuilder<SyncConfigDataWorker>()
                            .build()

                    ).enqueue()
            }
        }
    }
}
