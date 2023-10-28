package com.doubtnut.core.observer

import androidx.lifecycle.Observer
import com.doubtnut.core.entitiy.SingleEvent

class SingleEventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) :
    Observer<SingleEvent<T>> {
    override fun onChanged(event: SingleEvent<T>?) {
        event?.getContentIfNotHandled()?.let { value ->
            onEventUnhandledContent(value)
        }
    }
}