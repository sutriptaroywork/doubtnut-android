package com.doubtnutapp

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.doubtnutapp.networkstats.utils.NetworkUsageStatsWorker

/**
 * Created by Raghav Aggarwal on 28/01/22.
 */

/**
 * This class is used to observe the lifecycle of the complete app
 */
class LifecycleListener(val appContext: Context) :
    DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        Log.d("Returning to foreground…", "DNLifecycle")
    }

    override fun onStop(owner: LifecycleOwner) {
        Log.d("Moving to background…", "DNLifecycle")
        DoubtnutApp.INSTANCE.runOnDifferentThread {
            NetworkUsageStatsWorker.enqueue(appContext)
        }
    }
}