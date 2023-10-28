package com.doubtnutapp.fallbackquiz.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.fallbackquiz.db.FallbackQuizRepository
import com.doubtnutapp.forceUnWrap
import javax.inject.Inject

class FallbackQuizWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    companion object {
        const val FALLBACK_WORKER_TAG = "fallback_worker_tag"
    }

    @Inject
    lateinit var fallbackQuizRepository: FallbackQuizRepository

    override suspend fun doWork(): Result {
        //Check if feature is enabled, populate local DB
        fallbackQuizRepository.setAllFallbackQuizData()
        return Result.success()
    }
}