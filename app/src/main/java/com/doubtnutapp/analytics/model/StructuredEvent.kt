package com.doubtnutapp.analytics.model

import androidx.annotation.Keep
import com.doubtnut.core.entitiy.AnalyticsEvent

@Keep
data class StructuredEvent(
    val category: String,
    val action: String,
    val label: String? = null,
    val property: String? = null,
    val value: Double? = null,
    val eventParams: HashMap<String, Any> = hashMapOf()
) : AnalyticsEvent(action, eventParams)
