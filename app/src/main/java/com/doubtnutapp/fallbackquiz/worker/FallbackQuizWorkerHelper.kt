package com.doubtnutapp.fallbackquiz.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import javax.inject.Inject

class FallbackQuizWorkerHelper @Inject constructor(@ApplicationContext private val context: Context) {

    fun assignWorkForFallbackQuiz(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequestBuilder<FallbackQuizWorker>()
            .addTag(FallbackQuizWorker.FALLBACK_WORKER_TAG)
            .build()
        workManager.enqueueUniqueWork(
            FallbackQuizWorker.FALLBACK_WORKER_TAG,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}