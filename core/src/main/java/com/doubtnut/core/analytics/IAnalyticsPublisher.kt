package com.doubtnut.core.analytics

import com.doubtnut.core.entitiy.CoreAnalyticsEvent

interface IAnalyticsPublisher {
    fun publishEvent(event: CoreAnalyticsEvent)
}