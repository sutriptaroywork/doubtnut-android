package com.doubtnutapp.analytics.model

import androidx.annotation.Keep
import com.doubtnut.core.entitiy.AnalyticsEvent

@Keep
data class ConvivaEvent(
    val eventName: String,
    val eventParams: HashMap<String, Any> = hashMapOf()
) : AnalyticsEvent(eventName, eventParams)