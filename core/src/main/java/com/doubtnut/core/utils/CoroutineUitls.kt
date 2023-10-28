package com.doubtnut.core.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

object CoroutineUtils {

    fun AppCompatActivity.executeInCoroutine(
        lifecycle: Lifecycle.State = Lifecycle.State.STARTED,
        block: suspend () -> Unit
    ) {
        lifecycleScope.launch {
            repeatOnLifecycle(lifecycle) {
                block()
            }
        }
    }
}