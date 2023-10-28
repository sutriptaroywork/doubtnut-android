package com.doubtnut.core.view.audiotooltipview

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.doubtnut.core.view.ViewActionHandler

/**
Created by Sachin Saxena on 12/07/22.
 */
class ViewLifecycleObserver : DefaultLifecycleObserver {

    private lateinit var actionHandler: ViewActionHandler

    fun registerActionHandler(handler: ViewActionHandler) {
        actionHandler = handler
    }

    fun registerLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }

    private fun deRegisterLifecycle(lifecycle: Lifecycle) {
        lifecycle.removeObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        actionHandler.onCreate()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        actionHandler.onStart()
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        actionHandler.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        actionHandler.onPause()
    }

    override fun onStop(owner: LifecycleOwner) {
        actionHandler.onStop()
        super.onStop(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        actionHandler.onDestroy()
        deRegisterLifecycle(owner.lifecycle)
        super.onDestroy(owner)
    }
}