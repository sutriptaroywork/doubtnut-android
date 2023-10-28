package com.doubtnut.core

import android.content.Context
import android.os.StrictMode
import androidx.multidex.MultiDexApplication
import com.doubtnut.core.analytics.ITracker
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.data.local.IBaseDatabase
import com.doubtnut.core.helpers.RxBus
import com.doubtnut.core.utils.AnrUtils
import com.doubtnut.core.widgets.IWidgetLayoutAdapter
import dagger.android.HasAndroidInjector
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Common Application
 */
abstract class CoreApplication : MultiDexApplication(), HasAndroidInjector, CoroutineScope {

    companion object {
        lateinit var INSTANCE: CoreApplication

        var pendingDeeplink: String? = null
    }

    private val bus: RxBus? by lazy { RxBus() }

    private var job = SupervisorJob()

    private val handler = CoroutineExceptionHandler { _, throwable ->
        if (throwable is Exception) {
            // other error can be there a well NoClassDefFoundError / OutOfMemoryError
            throwable.printStackTrace()
        }
        job = SupervisorJob()
    }

    override fun onCreate() {
//      initStrictMode()
        super.onCreate()
        INSTANCE = this
        AnrUtils.initAnrWatchDog()
    }

    private fun initStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            // For now, focusing on Thread policy violation only
//            StrictMode.setVmPolicy(
//                StrictMode.VmPolicy.Builder()
//                    .detectAll()
//                    .penaltyLog()
//                    .build()
//            )
        }
    }

    fun runOnDifferentThread(block: () -> Unit) {
        launch {
            block()
        }
    }

    fun runOnDifferentThread(block: () -> Unit, callback: () -> Unit) {
        launch {
            block()
            callback()
        }
    }

    fun runOnMainThread(block: () -> Unit) {
        launch(Dispatchers.Main) {
            block()
        }
    }

    fun runOnMainThread(block: () -> Unit, callback: () -> Unit = {}) {
        launch(Dispatchers.Main) {
            block()
            callback()
        }
    }

    fun bus(): RxBus? {
        return bus
    }

    abstract fun getWidgetLayoutAdapter(
        context: Context,
    ): IWidgetLayoutAdapter

    abstract fun getWidgetLayoutAdapter(
        context: Context,
        actionPerformer: ActionPerformer? = null,
        source: String? = null
    ): IWidgetLayoutAdapter

    abstract fun getEventTracker(): ITracker

    abstract fun getDatabase() : IBaseDatabase?

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job + handler
}