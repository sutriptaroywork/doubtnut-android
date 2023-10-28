
package com.doubtnutapp.utils.extension

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.doubtnutapp.Log
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.EventObserver

inline fun <T> LiveData<T>.observeNonNull(
    owner: LifecycleOwner,
    crossinline observer: (t: T) -> Unit
) {
    this.observe(owner) {
        it?.let(observer)
    }
}

inline fun <T> LiveData<T>.observeNonNullSafely(
    owner: LifecycleOwner,
    crossinline observer: (t: T) -> Unit
) {
    this.observe(owner) {
        try {
            it?.let(observer)
        } catch (e: Exception) {
            Log.e(e)
        }
    }
}

fun <T> LiveData<Event<T>>.observeEvent(owner: LifecycleOwner, onChanged: (T) -> Unit) {
    observe(owner, EventObserver(onChanged))
}