package com.doubtnutapp.ui.pdfviewer.event

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-20.
 */
class PdfViewerEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun pdfItemClick() {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.PDF_CLICKED, hashMapOf()))
    }

    fun eventWith(eventName: String, params: HashMap<String, Any>, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}