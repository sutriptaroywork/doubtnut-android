package com.doubtnutapp

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnutapp.Constants.CONTROL_PARAM_KEY_REFERRER_STUDENT_ID
import com.doubtnutapp.Constants.ID
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.deeplink.AppActions
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import org.json.JSONObject

class BranchDeeplinkHandler {

    companion object {

        fun getParsedDeeplink(context: Context, referringParams: String, eventTracker: Tracker, analyticsPublisher: AnalyticsPublisher): String? {
            if (JSONObject(referringParams).optBoolean("+clicked_branch_link", false)) {
                defaultPrefs(context).edit {
                    putBoolean(Constants.SESSION_FROM_INVITE, JSONObject(referringParams).optBoolean("+is_first_session", false))
                }
            }
            return parseJSONObject(JSONObject(referringParams), context, eventTracker, analyticsPublisher)
        }

        private fun parseJSONObject(
            referringParams: JSONObject,
            context: Context,
            eventTracker: Tracker,
            analyticsPublisher: AnalyticsPublisher,
        ): String? {

            if (referringParams.optBoolean("+clicked_branch_link", false)) {
                var feature = ""
                if (referringParams.has("~feature")) {
                    feature = referringParams.getString("~feature")
                }

                // override ~feature with dn_feature, ~feature is reserved keyword cannot be used to create ad-deeplink
                if (referringParams.has("dn_feature")) {
                    feature = referringParams.getString("dn_feature")
                }

                if (feature == Constants.INVITE_FRIEND_FEATURE) {
                    defaultPrefs(context).edit {
                        putString(Constants.INTENT_EXTRA_INVITOR_ID, referringParams.getString(Constants.CONTROL_PARAM_KEY_INVITOR_ID))
                    }
                    eventTracker.addEventNames(EventConstants.EVENT_NAME_INVITE_FRIEND)
                        .addNetworkState(NetworkUtils.isConnected(context).toString())
                        .addStudentId(getStudentId())
                        .addScreenName(EventConstants.PAGE_DEEPLINK_CLICK)
                        .addEventParameter(Constants.INTENT_EXTRA_INVITOR_ID, referringParams.getString(Constants.CONTROL_PARAM_KEY_INVITOR_ID))
                        .track()
                    return null
                } else if (feature == Constants.INVITE_PAGE_FEATURE) {
                    defaultPrefs(context).edit {
                        putString(Constants.INTENT_EXTRA_INVITOR_ID, referringParams.getString(Constants.CONTROL_PARAM_KEY_INVITOR_ID))
                    }
                } else if (feature == Constants.COURSE_DETAILS &&
                    referringParams.has(CONTROL_PARAM_KEY_REFERRER_STUDENT_ID)
                ) {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.SHARE_COURSE_URL,
                            hashMapOf(
                                CONTROL_PARAM_KEY_REFERRER_STUDENT_ID to referringParams.getString(CONTROL_PARAM_KEY_REFERRER_STUDENT_ID),
                                EventConstants.ASSORTMENT_ID to referringParams.getString(ID),
                                EventConstants.STUDENT_ID to getStudentId()
                            )
                        )
                    )

                    analyticsPublisher.publishBranchIoEvent(
                        AnalyticsEvent(
                            EventConstants.SHARE_COURSE_URL,
                            hashMapOf(
                                CONTROL_PARAM_KEY_REFERRER_STUDENT_ID to referringParams.getString(CONTROL_PARAM_KEY_REFERRER_STUDENT_ID),
                                EventConstants.ASSORTMENT_ID to referringParams.getString(ID),
                                EventConstants.STUDENT_ID to getStudentId()
                            )
                        )
                    )
                }

                val appAction = AppActions.fromString(feature)

                if (appAction != null) {
                    val deeplink = AppActions.getDeeplink(appAction)
                    val deeplinkUriBuilder = Uri.parse(deeplink).buildUpon()
                    if (referringParams.has("path") &&
                        referringParams.getString("path").isNullOrBlank().not()
                    ) {
                        deeplinkUriBuilder.appendPath(referringParams.getString("path"))
                    }
                    referringParams.keys().forEach {
                        // ignore page param from branch
                        if (it == "page") return@forEach
                        deeplinkUriBuilder.appendQueryParameter(it, referringParams.getString(it))
                    }

                    if (referringParams.has("override_page") &&
                        referringParams.has("page") &&
                        referringParams.getString("override_page") == "1"
                    ) {
                        deeplinkUriBuilder.appendQueryParameter("page", referringParams.getString("page"))
                    } else {
                        deeplinkUriBuilder.appendQueryParameter("page", Constants.DEEPLINK)
                    }

                    return deeplinkUriBuilder.build().toString()
                }

                return null
            } else {
                return null
            }
        }
    }
}
