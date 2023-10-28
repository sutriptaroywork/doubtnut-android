package com.doubtnutapp

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.edit
import com.doubtnut.noticeboard.NoticeBoardConstants
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject

object FeaturesManager {

    var isFeatureConfigUpdated = false

    @SuppressLint("CheckResult")
    fun updateFeatureConfig() {
        isFeatureConfigUpdated = false

        // if user is not logged in then don;'t update feature config for now
        // this will be called again after user has logged in
        if (getStudentId().isEmpty()) {
            return
        }
        getFeatureConfig().subscribeOn(Schedulers.io()).subscribe({
            saveFeatureConfig(DoubtnutApp.INSTANCE.applicationContext, it)
            isFeatureConfigUpdated = true
        }, {
            it?.printStackTrace()
        })
    }

    fun getFeatureConfig(): Single<HashMap<String, Any>> {
        return DataHandler.INSTANCE.appConfigRepository.getFeatureConfig(
            hashMapOf<String, Any>().apply {
                put("capabilities", Features.capabilities)
                put("timeout", 3000)
            }
        )
    }

    fun saveFeatureConfig(context: Context, featureConfig: HashMap<String, Any>) {

        // save all the feature related info that we need, we are deliberately not adding
        // a generic way to store all the info in shared preferences, only store what will
        // be need and remember to cleanup after feature A/B is completed

        setFeatureVariants(context, featureConfig)

        val playerBufferEnabled =
            getFeatureConfigEnabled(featureConfig, Features.PLAYER_BUFFER_TIME)
        setIsFeatureEnabled(context, Features.PLAYER_BUFFER_TIME, playerBufferEnabled)
        val playerBufferPayload = getPayloadForFeature(featureConfig, Features.PLAYER_BUFFER_TIME)
        if (playerBufferPayload != null) {
            setFeaturePayload(context, Features.PLAYER_BUFFER_TIME, playerBufferPayload)
        }

        val ncertWebViewerPayload = getPayloadForFeature(featureConfig, Features.NCERT_WEBVIEWER)
        if (ncertWebViewerPayload != null) {
            setIsFeatureEnabled(
                context,
                Features.NCERT_WEBVIEWER,
                ncertWebViewerPayload["enabled"] as Boolean
            )
        }

        val pdfDownloadPayload = getPayloadForFeature(featureConfig, Features.PDF_DOWNLOAD)
        if (pdfDownloadPayload != null) {
            setIsFeatureEnabled(
                context,
                Features.PDF_DOWNLOAD,
                pdfDownloadPayload["enabled"] as Boolean
            )
        }

        val freshchatChatPayload = getPayloadForFeature(featureConfig, Features.FRESHCHAT_CHAT)
        if (freshchatChatPayload != null) {
            setIsFeatureEnabled(
                context,
                Features.FRESHCHAT_CHAT,
                freshchatChatPayload["enabled"] as Boolean
            )
        }

        val dictionaryPayload = getPayloadForFeature(featureConfig, Features.DICTIONARY)
        if (dictionaryPayload != null) {
            setIsFeatureEnabled(
                context,
                Features.DICTIONARY,
                dictionaryPayload["enabled"] as Boolean
            )
        }

        val freshChatHamburgerPayload =
            getPayloadForFeature(featureConfig, Features.FRESHCHAT_CHAT_HAMBURGER)
        if (freshChatHamburgerPayload != null) {
            setIsFeatureEnabled(
                context,
                Features.FRESHCHAT_CHAT_HAMBURGER,
                freshChatHamburgerPayload["enabled"] as Boolean
            )
        }

        val appUpdateEnabled = getFeatureConfigEnabled(featureConfig, Features.APP_UPDATE_FREQUENCY)
        setIsFeatureEnabled(context, Features.APP_UPDATE_FREQUENCY, appUpdateEnabled)
        val appUpdatePayload = getPayloadForFeature(featureConfig, Features.APP_UPDATE_FREQUENCY)
        if (appUpdatePayload != null) {
            setFeaturePayload(context, Features.APP_UPDATE_FREQUENCY, appUpdatePayload)
        }

        val autoSuggesterPayload = getPayloadForFeature(featureConfig, Features.IAS_AUTO_SUGGESTER)
        if (autoSuggesterPayload != null) {
            setIsFeatureEnabled(
                context,
                Features.IAS_AUTO_SUGGESTER,
                autoSuggesterPayload["enabled"] as Boolean
            )
            setFeaturePayload(context, Features.IAS_AUTO_SUGGESTER, autoSuggesterPayload)
        }

        val videoDownloadPayload =
            getPayloadForFeature(featureConfig, Features.VIDEO_CONTENT_SHARING)
        if (videoDownloadPayload != null) {
            setIsFeatureEnabled(
                context,
                Features.VIDEO_CONTENT_SHARING,
                videoDownloadPayload["enabled"] as Boolean
            )
        }

        val homeCarouselsShowMorePayload =
            getPayloadForFeature(featureConfig, Features.HOME_CAROUSELS_SHOW_MORE)
        if (homeCarouselsShowMorePayload != null) {
            setIsFeatureEnabled(
                context,
                Features.HOME_CAROUSELS_SHOW_MORE,
                homeCarouselsShowMorePayload["enabled"] as Boolean
            )
        }

        val dnGameFeedPayload = getPayloadForFeature(featureConfig, Features.DN_GAME_FEED)
        if (dnGameFeedPayload != null) {
            setIsFeatureEnabled(
                context,
                Features.DN_GAME_FEED,
                dnGameFeedPayload["enabled"] as Boolean
            )
        }

        val tabbedHomeCarouselPayload =
            getPayloadForFeature(featureConfig, Features.TABBED_HOME_CAROUSEL)
        if (tabbedHomeCarouselPayload != null) {
            setIsFeatureEnabled(
                context,
                Features.TABBED_HOME_CAROUSEL,
                tabbedHomeCarouselPayload["enabled"] as Boolean
            )
        }

        val clubFeedNotificationPayload =
            getPayloadForFeature(featureConfig, Features.CLUB_FEED_NOTIFICATION_V2)
        if (clubFeedNotificationPayload != null) {
            setIsFeatureEnabled(
                context,
                Features.CLUB_FEED_NOTIFICATION_V2,
                clubFeedNotificationPayload["enabled"] as Boolean
            )
        }

        val recentStatusPayload = getPayloadForFeature(featureConfig, Features.RECENT_STATUS)
        if (recentStatusPayload != null) {
            setIsFeatureEnabled(
                context,
                Features.RECENT_STATUS,
                recentStatusPayload["enabled"] as Boolean
            )
        }

        val feedStatusAdsPayload = getPayloadForFeature(featureConfig, Features.FEED_STATUS_ADS)
        if (feedStatusAdsPayload != null) {
            setIsFeatureEnabled(
                context,
                Features.FEED_STATUS_ADS,
                feedStatusAdsPayload["enabled"] as Boolean
            )
        }

        val liveClassBottomIconEnabled =
            getFeatureConfigEnabled(featureConfig, Features.LIVE_CLASS_BOTTOM_ICON_NAME)
        setIsFeatureEnabled(
            context,
            Features.LIVE_CLASS_BOTTOM_ICON_NAME,
            liveClassBottomIconEnabled
        )
        val liveClassBottomIconPayload =
            getPayloadForFeature(featureConfig, Features.LIVE_CLASS_BOTTOM_ICON_NAME)
        if (liveClassBottomIconPayload != null) {
            setFeaturePayload(
                context,
                Features.LIVE_CLASS_BOTTOM_ICON_NAME,
                liveClassBottomIconPayload
            )
        }

        val doubtPayWallPayload =
            getPayloadForFeature(featureConfig, Features.DOUBT_PAYWALL)
        if (doubtPayWallPayload != null) {
            setIsFeatureEnabled(
                context,
                Features.DOUBT_PAYWALL,
                doubtPayWallPayload["enabled"] as Boolean
            )
        }

        val commentTabOrder =
            getPayloadForFeature(featureConfig, Features.COMMENT_TAB_ORDER)
        if (commentTabOrder != null) {
            setIsFeatureEnabled(
                context,
                Features.COMMENT_TAB_ORDER,
                commentTabOrder["enabled"] as Boolean
            )
        }

        val videoOffsetPayload =
            getPayloadForFeature(featureConfig, Features.VIDEO_OFFSET)
        if (videoOffsetPayload != null) {
            setIsFeatureEnabled(
                context,
                Features.VIDEO_OFFSET,
                videoOffsetPayload["enabled"] as Boolean
            )
        }

        val quizPopupPayLoad =
            getPayloadForFeature(featureConfig, Features.QUIZ_POPUP)
        if (quizPopupPayLoad != null) {
            setIsFeatureEnabled(
                context,
                Features.QUIZ_POPUP,
                quizPopupPayLoad["enabled"] as Boolean
            )
        }

        val fallbackPushPayLoad =
            getPayloadForFeature(featureConfig, Features.FALLBACK_PUSH_NOTIFICATION)
        if (fallbackPushPayLoad != null) {
            setIsFeatureEnabled(
                context,
                Features.FALLBACK_PUSH_NOTIFICATION,
                fallbackPushPayLoad["enabled"] as Boolean
            )
        }

        val similarVideoLocaleThumbnail =
            getPayloadForFeature(featureConfig, Features.SIMILAR_VIDEO_THUMBNAIL)
        if (similarVideoLocaleThumbnail != null) {
            setIsFeatureEnabled(
                context,
                Features.SIMILAR_VIDEO_THUMBNAIL,
                similarVideoLocaleThumbnail["enabled"] as Boolean
            )
        }

        val bottomIconLiveClass =
            getPayloadForFeature(featureConfig, Features.BOTTOM_ICON_LIVE_CLASS)
        if (bottomIconLiveClass != null) {
            setIsFeatureEnabled(
                context,
                Features.BOTTOM_ICON_LIVE_CLASS,
                bottomIconLiveClass["enabled"] as Boolean
            )
        }

        getPayloadForFeature(featureConfig, Features.OFFLINE_VIDEOS)?.let {
            setIsFeatureEnabled(context, Features.OFFLINE_VIDEOS, it["enabled"] as Boolean)
        }

        getPayloadForFeature(featureConfig, Features.NUDGE_POP_UP)?.let {
            setFeaturePayload(context, Features.NUDGE_POP_UP, it)
        }

        getPayloadForFeature(featureConfig, Features.JOIN_NEW_STUDY_GROUP)?.let {
            setFeaturePayload(context, Features.JOIN_NEW_STUDY_GROUP, it)
        }

        getPayloadForFeature(featureConfig, Features.DNS_CACHING)?.let {
            setIsFeatureEnabled(context, Features.DNS_CACHING, it["enabled"] as Boolean)
        }

        getPayloadForFeature(featureConfig, Features.IAS_SERVICE)?.let {
            setFeaturePayload(context, Features.IAS_SERVICE, it)
        }

        getPayloadForFeature(featureConfig, Features.WALLET_PAGE_RESOURCES)?.let {
            setIsFeatureEnabled(context, Features.WALLET_PAGE_RESOURCES, it["enabled"] as Boolean)
        }

        getPayloadForFeature(featureConfig, Features.OFFLINE_OCR_IF_NO_INTERNET)?.let {
            setIsFeatureEnabled(
                context,
                Features.OFFLINE_OCR_IF_NO_INTERNET,
                it["enabled"] as Boolean
            )
        }

        getPayloadForFeature(featureConfig, Features.DOUBT_FEED)?.let {
            setIsFeatureEnabled(context, Features.DOUBT_FEED, it["enabled"] as Boolean)
        }

        getPayloadForFeature(featureConfig, Features.NOTICE_BOARD)?.let {
            setIsFeatureEnabled(context, Features.NOTICE_BOARD, it["enabled"] as? Boolean? ?: false)
            defaultPrefs(context).edit {
                putBoolean(
                    NoticeBoardConstants.NB_HOME_ENABLED,
                    it["home_enabled"] as? Boolean? ?: false
                )
            }
            defaultPrefs(context).edit {
                putBoolean(
                    NoticeBoardConstants.NB_LIVE_CLASS_ENABLED,
                    it["live_class_enabled"] as? Boolean? ?: false
                )
            }
            defaultPrefs(context).edit {
                putBoolean(
                    NoticeBoardConstants.NB_FEED_ENABLED,
                    it["feed_enabled"] as? Boolean? ?: false
                )
            }
            defaultPrefs(context).edit {
                putBoolean(
                    NoticeBoardConstants.NB_PROFILE_ENABLED,
                    it["profile_enabled"] as? Boolean? ?: false
                )
            }
        }

//        getPayloadForFeature(featureConfig, Features.EVERNOTE_ANDROID_JOB)?.let {
//            setIsFeatureEnabled(context, Features.EVERNOTE_ANDROID_JOB, it["enabled"] as? Boolean? ?: true)
//        }

        setIsFeatureEnabled(context, Features.EVERNOTE_ANDROID_JOB, true)

        getPayloadForFeature(featureConfig, Features.DISABLE_WEBVIEW_ON_VIDEO_ERROR)?.let {
            setIsFeatureEnabled(
                context,
                Features.DISABLE_WEBVIEW_ON_VIDEO_ERROR,
                it["enabled"] as Boolean
            )
        }

        getPayloadForFeature(featureConfig, Features.FORCE_VIDEO_WEB_VIEW)?.let {
            setIsFeatureEnabled(context, Features.FORCE_VIDEO_WEB_VIEW, it["enabled"] as Boolean)
        }

        getPayloadForFeature(featureConfig, Features.DAILY_GOAL_HOME_PAGE_BOTTOM_SHEET)?.let {
            setIsFeatureEnabled(
                context,
                Features.DAILY_GOAL_HOME_PAGE_BOTTOM_SHEET,
                it["enabled"] as Boolean
            )
        }

        getPayloadForFeature(featureConfig, Features.REDEEM_DNR_TIME_OUT)?.let {
            setIsFeatureEnabled(context, Features.REDEEM_DNR_TIME_OUT, it["enabled"] as Boolean)
            setFeaturePayload(context, Features.REDEEM_DNR_TIME_OUT, it)
        }

        getPayloadForFeature(featureConfig, Features.MP_FREE_CLASS_TAB_TEXT)?.let {
            setIsFeatureEnabled(context, Features.MP_FREE_CLASS_TAB_TEXT, it["enabled"] as Boolean)
        }

        getPayloadForFeature(featureConfig, Features.HIDE_TEACHERS_TAB)?.let {
            setIsFeatureEnabled(context, Features.HIDE_TEACHERS_TAB, it["enabled"] as Boolean)
        }

        getPayloadForFeature(featureConfig, Features.STUDY_GROUP_AS_FRESH_CHAT)?.let {
            setIsFeatureEnabled(context, Features.STUDY_GROUP_AS_FRESH_CHAT, it["enabled"] as Boolean)
        }

        getPayloadForFeature(featureConfig, Features.CONTINUE_WATCHING_WIDGET)?.let {
            setIsFeatureEnabled(
                context,
                Features.CONTINUE_WATCHING_WIDGET,
                it["enabled"] as Boolean
            )
        }

        getPayloadForFeature(featureConfig, Features.NETWORK_STATS_OPTION)?.let {
            setIsFeatureEnabled(context, Features.NETWORK_STATS_OPTION, it["enabled"] as Boolean)
        }

        getPayloadForFeature(featureConfig, Features.D0_AND_REFERRAL_EXPERIMENT)?.let {
            setIsFeatureEnabled(context, Features.D0_AND_REFERRAL_EXPERIMENT, it["enabled"] as Boolean)
            setFeaturePayload(context, Features.D0_AND_REFERRAL_EXPERIMENT, it)
        }
    }

    private fun getPayloadForFeature(
        featureConfig: HashMap<String, Any>,
        feature: String
    ): LinkedTreeMap<String, Any>? {
        return if (featureConfig.containsKey(feature)) {
            ((featureConfig[feature] as? LinkedTreeMap<String, Any>)?.get("payload") as? LinkedTreeMap<String, Any>)
        } else null
    }

    fun getFeatureConfigEnabled(featureConfig: HashMap<String, Any>, feature: String): Boolean {
        if (featureConfig.containsKey(feature)) {
            return ((featureConfig[feature] as? LinkedTreeMap<String, Any>)?.get("enabled") as? Boolean
                ?: false)
        } else return false
    }

    private fun getFeatureKey(feature: String): String {
        return "flagr-${feature}-enabled"
    }

    private fun getFeaturePayloadKey(feature: String): String {
        return "flagr-${feature}-payload"
    }

    private fun getFeaturePlacementKey(feature: String): String {
        return "flagr-${feature}-placement"
    }

    private fun getFeatureVariantKey(feature: String): String {
        return "flagr-${feature}-variant"
    }

    private fun getFeatureVariantNumber(feature: String): String {
        return "flagr-${feature}-variant-number"
    }

    private fun setIsFeatureEnabled(context: Context, feature: String, enabled: Boolean) {
        defaultPrefs(context).edit {
            putBoolean(getFeatureKey(feature), enabled)
        }
    }

    private fun setFeatureVariants(context: Context, featureConfig: HashMap<String, Any>) {
        featureConfig.entries.forEach {
            defaultPrefs(context).edit {
                val feature = it.key
                val variantId =
                    (it.value as? LinkedTreeMap<String, Any>)?.get("variantId") as? Double
                        ?: -1.0
                val payload =
                    (it.value as? LinkedTreeMap<String, Any>)?.get("payload") as? LinkedTreeMap<String, Any>
                val variantNumber = payload?.get("variantNumber") as? Double ?: -1.0
                putInt(getFeatureVariantKey(feature), variantId.toInt())
                putInt(getFeatureVariantNumber(feature), variantNumber.toInt())
            }
        }
    }

    private fun setFeaturePlacement(context: Context, feature: String, placement: String) {
        defaultPrefs(context).edit {
            putString(getFeaturePlacementKey(feature), placement)
        }
    }

    fun isFeatureEnabled(context: Context, feature: String): Boolean {
        return defaultPrefs(context).getBoolean(getFeatureKey(feature), false)
    }

    fun isFeatureEnabled(context: Context, feature: String, defaultStatus: Boolean): Boolean {
        return defaultPrefs(context).getBoolean(getFeatureKey(feature), defaultStatus)
    }

    fun getVariantId(context: Context, feature: String): Int {
        return defaultPrefs(context).getInt(getFeatureVariantKey(feature), -1)
    }

    fun getVariantNumber(context: Context, feature: String): Int {
        return defaultPrefs(context).getInt(getFeatureVariantNumber(feature), -1)
    }

    fun getFeaturePlacement(context: Context, feature: String): String? {
        return defaultPrefs(context).getString(getFeaturePlacementKey(feature), null)
    }

    private fun setFeaturePayload(
        context: Context,
        feature: String,
        payload: LinkedTreeMap<String, Any>
    ) {
        defaultPrefs(context).edit {
            putString(getFeaturePayloadKey(feature), JSONObject(payload).toString())
        }
    }

    fun getFeaturePayload(context: Context, feature: String): HashMap<String, Any>? {
        defaultPrefs(context).getString(getFeaturePayloadKey(feature), null)?.let {
            return Gson().fromJson(it, HashMap::class.java) as HashMap<String, Any>
        }
        return null
    }
}

object Features {

    // when adding new feature here, remember to add put it capabilities below also
    const val NCERT_WEBVIEWER = "NCERT_Easy_Read_Webviewer"
    const val PLAYER_BUFFER_TIME = "video-buffer-time"
    const val APP_UPDATE_FREQUENCY = "app_update_frequency"
    const val PDF_DOWNLOAD = "pdf_download"
    const val FRESHCHAT_CHAT = "freshchat_sales_flow_v1"
    const val DICTIONARY = "dictionary"
    const val FRESHCHAT_CHAT_HAMBURGER = "chat_support"
    const val VIDEO_CONTENT_SHARING = "video_content_share"
    const val IAS_AUTO_SUGGESTER = "ias_suggester"
    const val IAS_SERVICE = "ias_service"
    const val HOME_CAROUSELS_SHOW_MORE = "home_carousels_show_more"
    const val DN_GAME_FEED = "dn_game_feed"
    const val TABBED_HOME_CAROUSEL = "tabbed_home_carousel"
    const val OFFLINE_VIDEOS = "offline-videos"
    const val CLUB_FEED_NOTIFICATION_V2 = "club_feed_notification_v2"
    const val RECENT_STATUS = "recent_status"
    const val FEED_STATUS_ADS = "feed_status_ads"
    const val DOUBT_PAYWALL = "doubt_paywall"
    const val NUDGE_POP_UP = "nudge-pop-up-experiment"
    const val VIDEO_OFFSET = "video_offset"
    const val QUIZ_POPUP = "quizalertview_v3"
    const val SIMILAR_VIDEO_THUMBNAIL = "similar_thumbnail_user_language"
    const val BOTTOM_ICON_LIVE_CLASS = "bottom_icon_liveclass"
    const val JOIN_NEW_STUDY_GROUP = "join_new_study_group"
    const val DNS_CACHING = "dns_caching"
    const val FALLBACK_PUSH_NOTIFICATION = "quiz_display_as_alarm"
    const val WALLET_PAGE_RESOURCES = "wallet_page_resources"
    const val OFFLINE_OCR_IF_NO_INTERNET = "offline_ocr_if_no_internet"
    const val DOUBT_FEED = "doubt_feed_no_bottom_nav"
    const val COMMENT_TAB_ORDER = "comment_tab_order"
    const val LIVE_CLASS_BOTTOM_ICON_NAME = "liveclass_bottom_icon"
    const val NOTICE_BOARD = "notice_board"
    const val DISABLE_WEBVIEW_ON_VIDEO_ERROR = "disable_webview_on_video_error"
    const val EVERNOTE_ANDROID_JOB = "evernote_android_job"
    const val FORCE_VIDEO_WEB_VIEW = "force_video_web_view"
    const val DAILY_GOAL_HOME_PAGE_BOTTOM_SHEET = "daily_goal_home_page_bottom_sheet"
    const val REDEEM_DNR_TIME_OUT = "redeem_dnr_time_out"
    const val MP_FREE_CLASS_TAB_TEXT = "mp_free_classes_tab_text"
    const val HIDE_TEACHERS_TAB = "hide_teachers_tab"
    const val CONTINUE_WATCHING_WIDGET = "continue-watching-widget"
    const val NETWORK_STATS_OPTION = "network_stats_option"
    const val D0_AND_REFERRAL_EXPERIMENT = "d0_and_referral_exp"
    const val STUDY_GROUP_AS_FRESH_CHAT = "study_group_as_fresh_chat"

    private val defaultCapabilityPayload = hashMapOf<String, Any>().apply {
        val studentId = defaultPrefs(DoubtnutApp.INSTANCE).getString(Constants.STUDENT_ID, "") ?: ""
        if (studentId.isNotEmpty()) {
            put("student_id", studentId.toInt())
        }
    }

    // map of requested features for which feature flags will be fetched
    val capabilities: HashMap<String, Any> = hashMapOf<String, Any>().apply {
        put(NCERT_WEBVIEWER, "")
        put(PLAYER_BUFFER_TIME, "")
        put(APP_UPDATE_FREQUENCY, "")
        put(PDF_DOWNLOAD, "")
        put(FRESHCHAT_CHAT, "")
        put(DICTIONARY, "")
        put(FRESHCHAT_CHAT_HAMBURGER, "")
        put(VIDEO_CONTENT_SHARING, "")
        put(IAS_AUTO_SUGGESTER, "")
        put(IAS_SERVICE, "")
        put(HOME_CAROUSELS_SHOW_MORE, "")
        put(DN_GAME_FEED, "")
        put(TABBED_HOME_CAROUSEL, "")
        put(DOUBT_PAYWALL, "")
        put(OFFLINE_VIDEOS, "")
        put(CLUB_FEED_NOTIFICATION_V2, "")
        put(RECENT_STATUS, "")
        put(FEED_STATUS_ADS, "")
        put(NUDGE_POP_UP, "")
        put(VIDEO_OFFSET, "")
        put(QUIZ_POPUP, "")
        put(SIMILAR_VIDEO_THUMBNAIL, "")
        put(BOTTOM_ICON_LIVE_CLASS, "")
        put(JOIN_NEW_STUDY_GROUP, "")
        put(DNS_CACHING, "")
        put(FALLBACK_PUSH_NOTIFICATION, "")
        put(DOUBT_FEED, "")
        put(COMMENT_TAB_ORDER, "")
        put(LIVE_CLASS_BOTTOM_ICON_NAME, "")
        put(NOTICE_BOARD, "")
        put(DISABLE_WEBVIEW_ON_VIDEO_ERROR, "")
        put(FORCE_VIDEO_WEB_VIEW, "")
        put(DAILY_GOAL_HOME_PAGE_BOTTOM_SHEET, "")
        put(REDEEM_DNR_TIME_OUT, "")
        put(MP_FREE_CLASS_TAB_TEXT, "")
        put(HIDE_TEACHERS_TAB, "")
        put(CONTINUE_WATCHING_WIDGET, "")
        put(NETWORK_STATS_OPTION, "")
        put(D0_AND_REFERRAL_EXPERIMENT, "")
        put(STUDY_GROUP_AS_FRESH_CHAT, "")
    }
}