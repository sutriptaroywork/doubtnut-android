package com.doubtnutapp.ui.downloadPdf.event

import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-20.
 */
class DownloadPdfEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun onPDFLevelSelected(pdfName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.PDF_LEVEL_SELECTED, hashMapOf<String, Any>().apply {
            put(EventConstants.PDF_NAME, pdfName)
        }, ignoreSnowplow = true))
    }

    fun eventWith(eventName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf()))
    }
}