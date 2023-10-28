package com.doubtnutapp.utils

import android.content.Context
import android.net.Uri
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.widgets.entities.ActionData
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.addEventNames
import com.doubtnutapp.data.remote.models.BannerActionData
import com.doubtnutapp.data.remote.models.BannerModel
import com.doubtnutapp.deeplink.AppActions
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.utils.UserUtil.getStudentId
import javax.inject.Inject

object BannerActionUtils {


    @set:Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    fun navigateToPage(context: Context, actionActivity: String, actionData: BannerActionData?, eventTracker: Tracker) {
        val bannerModel = BannerModel(null, null, null, actionActivity, actionData, null, null, null, null, null)
        navigateToPage(context, bannerModel, eventTracker)
    }

    fun navigateToPage(context: Context, bannerData: BannerModel, eventTracker: Tracker) {

        sendEventForPage(EventConstants.EVENT_NAME_BANNER_CLICK_WITH_ID_ + bannerData?.id, context, eventTracker)

        val actionData = bannerData.actionData
        val appAction = AppActions.fromString(bannerData.actionActivity)

        if (appAction != null) {
            var deeplink = AppActions.getDeeplink(appAction)
            val deeplinkUriBuilder = Uri.parse(deeplink).buildUpon()
            if (actionData != null) {
                if (actionData.page != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.PAGE, actionData.page)
                if (actionData.qid != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.Q_ID, actionData.qid)
                if (actionData.playListId != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.PLAYLIST_ID, actionData.playListId)
                if (actionData.playlistTitle != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.PLAYLIST_TITLE, actionData.playlistTitle)
                if (actionData.isLast != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.IS_LAST, actionData.isLast.toString())
                if (actionData.type != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.TYPE, actionData.type)
                if (actionData.studentClass != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.CLASS, actionData.studentClass)
                if (actionData.course != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.COURSE, actionData.course)
                if (actionData.chapter != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.CHAPTER, actionData.chapter)
                if (actionData.externalUrl != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.URL, actionData.externalUrl)
                if (actionData.contestId != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.CONTEST_ID, actionData.contestId)
                if (actionData.pdfPackage != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.PDF_PACKAGE, actionData.pdfPackage)
                if (actionData.levelOne != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.LEVEL_ONE, actionData.levelOne)
                if (actionData.tagKey != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.INTENT_EXTRA_TAG_KEY, actionData.tagKey)
                if (actionData.tagValue != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.INTENT_TAG_VALUE, actionData.tagValue)
                if (actionData.pdfUrl != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.INTENT_EXTRA_PDF_URL, actionData.pdfUrl)
                if (actionData.chapterId != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.CHAPTER_ID, actionData.chapterId)
                if (actionData.exerciseName != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.EXCERCISE_NAME, actionData.exerciseName)
                if (actionData.facultyId != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.FACULTY_ID, actionData.facultyId.toString())
                if (actionData.ecmId != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.ECM_ID, actionData.ecmId.toString())
                if (actionData.subject != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.SUBJECT, actionData.subject)
                if (actionData.id != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.ID, actionData.id)
            }
            deeplink = deeplinkUriBuilder.build().toString()

            deeplinkAction.performAction(context, deeplink, bannerData.actionData?.page ?: "")
        }
    }

    fun navigateToPage(context: Context, actionActivity: String?, actionData: ActionData?, eventTracker: Tracker) {

        sendEventForPage(EventConstants.EVENT_NAME_BANNER_CLICK_WITH_ID_ + actionData?.id, context, eventTracker)

        val appAction = AppActions.fromString(actionActivity)

        if (appAction != null) {
            var deeplink = AppActions.getDeeplink(appAction)
            val deeplinkUriBuilder = Uri.parse(deeplink).buildUpon()
            if (actionData != null) {
                if (actionData.page != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.PAGE, actionData.page)
                if (actionData.qid != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.Q_ID, actionData.qid)
                if (actionData.playListId != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.PLAYLIST_ID, actionData.playListId)
                if (actionData.playlistTitle != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.PLAYLIST_TITLE, actionData.playlistTitle)
                if (actionData.isLast != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.IS_LAST, actionData.isLast.toString())
                if (actionData.type != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.TYPE, actionData.type)
                if (actionData.studentClass != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.CLASS, actionData.studentClass)
                if (actionData.course != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.COURSE, actionData.course)
                if (actionData.chapter != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.CHAPTER, actionData.chapter)
                if (actionData.externalUrl != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.URL, actionData.externalUrl)
                if (actionData.contestId != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.CONTEST_ID, actionData.contestId)
                if (actionData.pdfPackage != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.PDF_PACKAGE, actionData.pdfPackage)
                if (actionData.levelOne != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.LEVEL_ONE, actionData.levelOne)
                if (actionData.tagKey != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.INTENT_EXTRA_TAG_KEY, actionData.tagKey)
                if (actionData.tagValue != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.INTENT_TAG_VALUE, actionData.tagValue)
                if (actionData.pdfUrl != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.INTENT_EXTRA_PDF_URL, actionData.pdfUrl)
                if (actionData.chapterId != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.CHAPTER_ID, actionData.chapterId)
                if (actionData.exerciseName != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.EXCERCISE_NAME, actionData.exerciseName)
                if (actionData.facultyId != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.FACULTY_ID, actionData.facultyId.toString())
                if (actionData.ecmId != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.ECM_ID, actionData.ecmId.toString())
                if (actionData.subject != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.SUBJECT, actionData.subject)
                if (actionData.id != null)
                    deeplinkUriBuilder.appendQueryParameter(Constants.ID, actionData.id)
            }
            deeplink = deeplinkUriBuilder.build().toString()

            deeplinkAction.performAction(context, deeplink, actionData?.page ?: "")
        }
    }

    fun performAction(context: Context, actionActivity: String, actionData: ActionData?) {
        navigateToPage(context, actionActivity,
                actionData?.run {
                    ActionData(pdfUrl, pdfPackage, page, qid, playListId, playlistTitle, isLast, chapterId, exerciseName,
                            id, type, studentClass, course, chapter, externalUrl, contestId, levelOne, tagKey, tagValue,
                            facultyId, ecmId, subject
                    )
                },
                DoubtnutApp.INSTANCE.getEventTracker())
    }
}

private fun sendEventForPage(eventName: String, context: Context, eventTracker: Tracker) {
    eventTracker.addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(context).toString())
            .addStudentId(getStudentId())
            .track()
}
