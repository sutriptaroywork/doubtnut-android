package com.doubtnutapp.conviva

import android.content.Context
import com.doubtnutapp.Constants
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.orDefaultValue

object ConvivaHelper {

    private const val NA = "NA"
    private const val YES = "Yes"
    private const val NO = "No"

    fun init(context: Context) {
        /*if (BuildConfig.DEBUG) {
            val settings: MutableMap<String, Any> = HashMap()
            val gatewayUrl = BuildConfig.CONVIVA_GATEWAY_URL
            settings[ConvivaSdkConstants.GATEWAY_URL] = gatewayUrl
            settings[ConvivaSdkConstants.LOG_LEVEL] = ConvivaSdkConstants.LogLevel.DEBUG
            ConvivaAnalytics.init(context, BuildConfig.CONVIVA_TEST_CUSTOMER_KEY, settings)
        } else {
            // production release
            ConvivaAnalytics.init(context, BuildConfig.CONVIVA_PRODUCTION_CUSTOMER_KEY)
        }*/
    }

/*    fun initTracker(context: Context): TrackerController {
        return ConvivaAppAnalytics.createTracker(context,
            BuildConfig.CONVIVA_PRODUCTION_CUSTOMER_KEY,
            BuildConfig.APPLICATION_ID
        )
    }*/

    /**
     * ConvivaSdkConstants.FRAMEWORK_NAME - Autocollected from ExoPlayer.
     * ConvivaSdkConstants.FRAMEWORK_VERSION - Autocollected from ExoPlayer.
     * ConvivaSdkConstants.DURATION - Autocollected from ExoPlayer.
     * @param videoUrl - video resource exoplayer wants to play
     * @param assetName - unique identifier to video resource
     * @param isLive - is this live stream or not
     * @param encodedFrameRate - frames per second
     * @return - content info map containing key/value pair
     */
    fun getContentInfo(
        videoUrl: String,
        assetName: String,
        isLive: Boolean,
        encodedFrameRate: Int?
    ): Map<String, Any> {
        val contentInfo = hashMapOf<String, Any>()
/*
        // The manifest URL of the video stream. (string)
        contentInfo[ConvivaSdkConstants.STREAM_URL] = videoUrl

        // Use unique name for each stream/video asset. (string)
        contentInfo[ConvivaSdkConstants.ASSET_NAME] = assetName

        // Denotes whether the content is video on-demand or a live stream. (boolean)
        contentInfo[ConvivaSdkConstants.IS_LIVE] = isLive

        // A string value used to distinguish video players (applications). (string)
        contentInfo[ConvivaSdkConstants.PLAYER_NAME] = "DoubtnutAndroid"

        // Video server resource the stream is played from.
        // Set this field when the video server resource cannot be inferred from the STREAM_URL. (string)
//        contentInfo[ConvivaSdkConstants.DEFAULT_RESOURCE] = NULL

        // Encoded frame rate of the video stream in frames per second. (integer)
        contentInfo[ConvivaSdkConstants.ENCODED_FRAMERATE] = encodedFrameRate ?: NA

        // Application build version. Shall have the same value for both ads and video. (string)
        contentInfo["c3.app.version"] = BuildConfig.VERSION_NAME

        // A unique identifier to distinguish individual viewers or devices through Conviva's Viewers Module. (string)
        contentInfo[ConvivaSdkConstants.VIEWER_ID] =
            defaultPrefs().getString(Constants.STUDENT_ID, NA).orDefaultValue(NA)*/
        return contentInfo
    }

    fun getVideoContentMetaData(contentType: String?, assetId: String): Map<String, Any> {
        val metaDataInfo = hashMapOf<String, Any>()
/*
        // Advanced content delivery methods along with Live and VOD. Acceptable values: "Live", "Live-Linear", "DVR", "Catchup", "VOD".
        metaDataInfo["c3.cm.contentType"] = contentType ?: NA

        // The channel on which the content is consumed.
        metaDataInfo["c3.cm.channel"] = NA

        // The name of the brand to which the content belongs.
        metaDataInfo["c3.cm.brand"] = NA

        // Affiliate or MVPD name for TV Everywhere authenticated services.
        metaDataInfo["c3.cm.affiliate"] = NA

        // Content business categories of interest.
        metaDataInfo["c3.cm.categoryType"] = NA

        // Name of CMS Provider.
        metaDataInfo["c3.cm.name"] = NA

        // Unique asset identifier to query CMS system to gather additional asset metadata information for a specific asset.
        metaDataInfo["c3.cm.id"] = assetId

        // The name of Series. Set the value only if the metadata cannot be gathered from CMS System. Null if not applicable
        metaDataInfo["c3.cm.seriesName"] = NA

        // The Season number. Set the value only if the details cannot be inferred from Asset Provider Server. Null if not applicable
        metaDataInfo["c3.cm.seasonNumber"] = NA

        // The name of the Episode or Show Title. Set the value only if the details cannot be inferred from Asset Provider Server. Null if not applicable.
        metaDataInfo["c3.cm.showTitle"] = NA

        // The Episode number. Set the value only if the details cannot be inferred from Asset Provider Server. Null if not applicable.
        metaDataInfo["c3.cm.episodeNumber"] = NA

        // The Primary content genre. Set the value only if the details cannot be inferred from Asset Provider Server. Null if not applicable
        metaDataInfo["c3.cm.genre"] = NA

        // The list of the applicable content genre. Set the values in a comma separated list only if the details cannot be inferred from Asset Provider Server. Null if not applicable.
        metaDataInfo["c3.cm.genreList"] = NA

        // Provide the complete generated UTM URL / Link to track the effectiveness of the online marketing campaign across traffic sources and publishing media.
        metaDataInfo["c3.cm.utmTrackingUrl"] = NA*/
        return metaDataInfo
    }

    fun getCustomTags(
        subject: String?,
        chapter: String?,
        subscription: Boolean?,
        videoFormat: String?,
        typeOfContent: String?,
        facultyId: String?,
        facultyName: String?,
        medium: String?,
        assortmentId: String?,
        batchId: String?,
        videoUrl: String,
        isVip: Boolean?,
        subscriptionStart: String?,
        subscriptionEnd: String?,
        courseId: String?,
        courseTitle: String?
    ): Map<String, Any> {

        val customTagInfo = hashMapOf<String, Any>()
/*
        customTagInfo["subject"] = subject ?: NA
        customTagInfo["chapter"] = chapter ?: NA
        customTagInfo["subscription"] = if (subscription == true) YES else NO
        customTagInfo["videoFormat"] = videoFormat ?: NA
        customTagInfo["typeOfContent"] = typeOfContent ?: NA
        customTagInfo["facultyId"] = facultyId ?: NA
        customTagInfo["facultyName"] = facultyName ?: NA
        customTagInfo["medium"] = medium ?: NA
        customTagInfo["assortmentId"] = assortmentId ?: NA
        customTagInfo["batchId"] = batchId ?: NA
        customTagInfo["courseId"] = courseId ?: NA
        customTagInfo["courseTitle"] = courseTitle ?: NA
        customTagInfo["videoUrl"] = videoUrl
        customTagInfo["ccm"] = defaultPrefs().getString(Constants.CCM_ID, "").orDefaultValue(NA)
        customTagInfo["isVip"] = if (isVip == true) YES else NO
        customTagInfo["subscriptionStart"] = subscriptionStart ?: NA
        customTagInfo["subscriptionEnd"] = subscriptionEnd ?: NA*/
        return customTagInfo
    }

    fun getUserProperties(): Map<String, Any> {
        val userProperties = hashMapOf<String, Any>()

        userProperties["gradeLevel"] =
            defaultPrefs().getString(Constants.STUDENT_CLASS, "").orDefaultValue(NA)
        userProperties["language"] =
            defaultPrefs().getString(Constants.STUDENT_LANGUAGE_CODE, "").orDefaultValue(NA)
        userProperties["board"] =
            defaultPrefs().getString(Constants.USER_SELECTED_BOARD, "").orDefaultValue(NA)
        userProperties["examType"] =
            defaultPrefs().getString(Constants.USER_SELECTED_EXAMS, "").orDefaultValue(NA)
        return userProperties
    }
}
