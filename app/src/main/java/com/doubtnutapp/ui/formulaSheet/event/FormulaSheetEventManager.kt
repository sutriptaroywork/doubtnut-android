package com.doubtnutapp.ui.formulaSheet.event

import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.ADD_TO_CHEAT_SHEET_CLICKED
import com.doubtnut.analytics.EventConstants.ADD_TO_CHEAT_SHEET_SUCCESS
import com.doubtnut.analytics.EventConstants.FORMULA_SUBJECT_SELECTED
import com.doubtnut.analytics.EventConstants.FORMULA_TOPIC_SELECTED
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-07-20.
 */
class FormulaSheetEventManager @Inject constructor(private val analyticsPublisher: AnalyticsPublisher) {

    fun onFormulaSubjectSelected(subject: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(FORMULA_SUBJECT_SELECTED, hashMapOf<String, Any>().apply {
            put(EventConstants.TOPIC_NAME, subject)
        }, ignoreSnowplow = true))
    }

    fun onFormulaTopicSelected(topic: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(FORMULA_TOPIC_SELECTED, hashMapOf<String, Any>().apply {
            put(EventConstants.TOPIC_NAME, topic)
        }, ignoreSnowplow = true))
    }

    fun eventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf(), ignoreSnowplow = ignoreSnowplow))
    }

    fun onAddToCheatSheetClick(cheatSheetName: String, type: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(ADD_TO_CHEAT_SHEET_CLICKED, hashMapOf<String, Any>().apply {
            put(EventConstants.CHEAT_SHEET_NAME, cheatSheetName)
            put(EventConstants.CHEAT_SHEET_TYPE, type)
        }, ignoreSnowplow = true))
    }

    fun onAddToCheatSheetSuccessFull() {
        analyticsPublisher.publishEvent(AnalyticsEvent(ADD_TO_CHEAT_SHEET_SUCCESS, hashMapOf()))
    }

}