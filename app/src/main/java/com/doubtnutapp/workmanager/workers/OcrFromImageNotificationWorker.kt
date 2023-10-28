package com.doubtnutapp.workmanager.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.workmanager.OcrFromImageNotificationManager

/**
 * Created by Anand Gaurav on 24/09/20.
 */
class OcrFromImageNotificationWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    companion object {
        const val TAG = "OcrFromImageNotificationWorker"
    }

    override fun doWork(): Result {

        val latestOcrRow = DoubtnutApp.INSTANCE.getDatabase()?.offlineOcrDao()?.getLatestQuestion()
                ?: return Result.success()

        OcrFromImageNotificationManager.handleNotification(
                context = DoubtnutApp.INSTANCE,
                ocr = latestOcrRow.ocr,
                notificationId = latestOcrRow.ts
        )

        return Result.success()
    }
}