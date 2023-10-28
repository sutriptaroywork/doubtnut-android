package com.doubtnutapp.networkstats.utils

import android.content.Context
import androidx.work.*
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.DefaultDataStoreImpl
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.networkstats.ui.NetworkStatsActivity
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Created by Raghav Aggarwal on 31/01/22.
 */

class NetworkUsageStatsWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(
    context,
    workerParams
) {
    @Inject
    lateinit var defaultDataStore: DefaultDataStore

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override suspend fun doWork(): Result {
        saveImageData()
        return Result.success()

    }

    companion object {
        const val TAG = "NetworkUsageStatsWorker"

        fun enqueue(context: Context) {
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    TAG,
                    ExistingWorkPolicy.REPLACE,
                    OneTimeWorkRequestBuilder<NetworkUsageStatsWorker>()
                        .build()
                )
        }
    }

    private suspend fun saveImageData() {
        val previousImageData = defaultDataStore.imageData.firstOrNull() ?: 0.0F
        val totalData = NetworkStatsActivity.sessionImageData + previousImageData
        defaultDataStore.set(DefaultDataStoreImpl.IMAGE_NETWORK_DATA_CONSUMED, totalData)
    }

}


