package com.doubtnutapp.data.common

import android.content.SharedPreferences
import androidx.core.content.edit
import com.doubtnut.core.constant.CoreConstants
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.DefaultDataStoreImpl
import com.doubtnut.core.worker.ISyncConfigDataWorker
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.domain.login.entity.IntroEntity
import com.doubtnutapp.domain.newlibrary.entities.LibraryHistoryEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class UserPreferenceImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val defaultDataStore: DefaultDataStore
) : UserPreference {

    companion object {
        const val PREF_KEY_PROFILE_BUTTON_CLICKED = "profile_button_clicked"
        const val PREF_KEY_LIBRARY_BUTTON_CLICKED = "library_button_clicked"
        const val PREF_KEY_FORUM_BUTTON_CLICKED = "forum_button_clicked"
        const val GCM_REG_ID = "gcm_reg_id"
        const val XAUTH_HEADER_TOKEN = "x-auth-token"
        const val STUDENT_ID = "student_id"
        const val ONBOARDING_VIDEO = "onboardingVideo"
        const val TYPE_INTRO = "intro"
        const val TYPE_COMMUNITY = "community"
        const val TYPE_INTRO_URL = "type_intro_url"
        const val TYPE_INTRO_QID = "type_intro_qid"
        const val TYPE_COMMUNITY_URL = "type_community_url"
        const val TYPE_COMMUNITY_QID = "type_community_qid"
        const val ONBOARDING_COMPLETED = "onboarding_completed"
        const val OLD_STUDENT_ID = "old_student_id"
        const val IS_LOGOUT = "is_logout"
        const val LAST_SHOWN_IN_APP_UPDATE = "last_shown_in_app_update"
        const val USER_HAS_WATCHED_VIDEO = "user_has_watched_video"
        const val VIDEO_WATCHED_COUNT = "video_watched_count"
        const val VIDEO_WATCHED_ON_DAY0 = "video_watched_on_day0"
        const val VIDEO_WATCHED_AFTER_DAY2 = "video_watched_after_day2"
        const val THREE_VIDEOS_WATCHED_IN_2DAYS = "three_videos_watched_in_2days"
        const val USER_SELECTED_EXAMS = "user_selected_exams"
        const val USER_SELECTED_BOARD = CoreConstants.USER_SELECTED_BOARD
        const val CCM_ID = "selected_exam_ccm_id"
        const val USER_WATCHED_ETOOS_VIDEO = "user_watched_etoos_video"
        const val IS_EMULATOR = "is_emulator"
        const val HAS_PLAY_SERVICE = "has_play_service"
        const val LAST_DAY_BRANCH_EVENT_SEND = "last_day_branch_event_send"
        const val LAST_PAYMENT_INITIATE_TIME = "last_payment_initiate_time"
        const val APP_EXIT_DIALOG_SHOWN_IN_CURRENT_SESSION =
            "app_exit_dialog_shown_in_current_session"
        const val CAMERA_SCRREN_NAVIGATION_DATA_FETCHED_IN_CURRENT_SESSION =
            "camera_screen_navigation_data_fetched_in_current_session"

        // Doubt P2p V2
        private const val IS_DOUBT_P2P_ENABLED = "is_doubt_p2p_enabled"
        private const val DOUBT_P2P_TITLE = "doubt_p2p_title"
        private const val DOUBT_P2P_IMAGE = "doubt_p2p_image"
        private const val DOUBT_P2P_DEEPLINK = "doubt_p2p_deeplink"
        private const val IS_DOUBT_P2P_NOTIFICATION_ENABLED = "is_doubt_p2p_notification_enabled"

        // Khelo Aur jeeto V2
        private const val IS_KHELO_AUR_JEETO_ENABLED = "is_khelo_aur_jeeto_enabled"
        private const val KHELO_AUR_JEETO_TITLE = "khelo_aur_jeeto_title"
        private const val KHELO_AUR_JEETO_IMAGE = "khelo_aur_jeeto_image"
        private const val KHELO_AUR_JEETO_DEEPLINK = "khelo_aur_jeetop_deeplink"

        // Doubt feed 2
        private const val IS_DOUBT_FEED_2_ENABLED = "is_doubt_feed_2_enabled"
        private const val DOUBT_FEED_2_TITLE = "doubt_feed_2_title"
        private const val DOUBT_FEED_2_IMAGE = "doubt_feed_2_image"
        private const val DOUBT_FEED_2_DEEPLINK = "doubt_feed_2_deeplink"

        // Study Group
        private const val STUDY_GROUP_NOTIFICATION_MUTE_STATUS =
            "study_group_notification_mute_status"

        // Revision Corner
        private const val IS_REVISION_CORNER_ENABLED = "is_revision_corner_enabled"
        private const val REVISION_CORNER_TITLE = "revision_corner_title"
        private const val REVISION_CORNER_IMAGE = "revision_corner_image"
        private const val REVISION_CORNER_DEEPLINK = "revision_corner_deeplink"

        // Revision Corner
        private const val DNR_TITLE = "dnr_title"
        private const val DNR_TITLE1 = "dnr_title_1"
        private const val DNR_IMAGE = "dnr_image"
        private const val DNR_DEEPLINK = "dnr_deeplink"
        private const val DNR_DESCRIPTION = "dnr_deeplink"
        private const val DNR_CTA_TEXT = "dnr_cta_text"

        private const val QUESTION_ASK_COUNT = "question_ask_count"

        private const val GAID = "gaid"

        // User Journey
        private const val USER_JOURNEY = "user_journey"
    }

    private val PREF_STRING_VALUE_DEFAULT = ""

    private val PREF_KEY_IS_CAMERA_SCREEN_SHOWN_FIRST = "camera_screen_shown_first"

    private val PREF_KEY_IS_TRICKY_QUESTION_BUTTON_SHOWN = "tricky_question_button_shown"

    private val PREF_KEY_IS_USER_SEEN_BADGE_SCREEN_SHOWN = "is_user_seen_badge_screen_once"

    private val STUDENT_ID = "student_id"

    private val STUDENT_CLASS = "student_class"

    private val BOTTOM_SHEET_COUNT = "bottom_sheet_count"

    private val SHOW_BOTTOM_SHEET = "show_bottom_sheet"

    private val ONBOARDING_COMPLETED = "onboarding_completed"

    private val STUDENT_COURSE = "student_course"

    private val STUDENT_SCHOOL = "student_school"

    private val STUDENT_EMAIL = "student_email"

    private val STUDENT_DOB: String = "student_dob"

    private val STUDENT_PINCODE: String = "student_pincode"

    private val STUDENT_COACHING_NAME: String = "student_coaching_name"

    private val STUDENT_CLASS_DISPLAY = "student_class_display"

    private val STUDENT_LANGUAGE_CODE = "student_language_code"

    private val STUDENT_LANGUAGE_NAME = "student_language_name"

    private val STUDENT_LANGUAGE_NAME_DISPLAY: String = "student_language_name_display"

    private val STUDENT_USER_NAME = "student_user_name"

    private val STUDENT_LOGIN = "student_login"

    private val PREF_KEY_DAILY_STREAK = "daily_streak"

    private val STUDENT_IMAGE_URL = "image_url"

    private val KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER = "firebase_reg_id_updated_on_server"

    private val LIBRARY_HISTORY = "library_history"

    private val MATCH_PAGE_FEEDBACK_DIALOG_COUNT = "match_page_feedback_dialog_count"

    private val MATCH_PAGE_FEEDBACK_DIALOG_CLOSE_COUNT = "match_page_feedback_dialog_close_count"

    private val IS_PIN_SET = "IS_PIN_SET"

    private val PIN = "PIN"

    private val CURRENT_DAY = "current_day"

    private val CURRENT_LEVEL = "current_level"

    private val SHOW_SCRATCH_CARD_REMINDER = "show_scratch_card_reminder"

    private val CURRENT_LEVEL_UNKNOWN = -1
    private val CURRENT_DAY_UNKNOWN = -1
    private val FIRST_PAGE_DEEPLINK = "first_page_deeplink"

    // Study Dost
    private val STUDY_DOST_DESCRIPTION = "study_dost_description"
    private val STUDY_DOST_UNREAD_COUNT = "study_dost_unread_count"
    private val STUDY_DOST_IMAGE = "study_dost_image"
    private val STUDY_DOST_LEVEL = "study_dost_level"
    private val STUDY_DOST_DEEPLINK = "study_dost_deeplink"
    private val STUDY_DOST_CTA_TEXT = "study_dost_cta_text"

    // Study Group
    private val IS_STUDY_GROUP_FEATURE_ENABLED = "is_study_group_feature_enabled"
    private val STUDY_GROUP_TITLE = "study_group_title"
    private val STUDY_GROUP_IMAGE = "study_group_image"
    private val STUDY_GROUP_DEEPLINK = "study_group_deeplink"

    private val IS_DOUBT_FEED_AVAILABLE = "is_doubt_feed_available"

    private val IS_DOUBT_P2P_HOME_WIDGET_VISIBLE = "is_doubt_p2p_home_widget_visible"

    override fun isTrickyQuestionButtonShown() =
        sharedPreferences.getBoolean(PREF_KEY_IS_TRICKY_QUESTION_BUTTON_SHOWN, false)

    override fun isUserSeenBadgeScreenOnce(): Boolean =
        sharedPreferences.getBoolean(PREF_KEY_IS_USER_SEEN_BADGE_SCREEN_SHOWN, false)

    override fun getUserStudentId() =
        sharedPreferences.getString(STUDENT_ID, PREF_STRING_VALUE_DEFAULT)
            ?: PREF_STRING_VALUE_DEFAULT

    override fun setIsCameraScreenShownToTrue() {
        sharedPreferences.edit().putBoolean(PREF_KEY_IS_CAMERA_SCREEN_SHOWN_FIRST, true).apply()
    }

    override fun setIsTrickyQuestionButtonToTrue() {
        sharedPreferences.edit().putBoolean(PREF_KEY_IS_TRICKY_QUESTION_BUTTON_SHOWN, true).apply()
    }

    override fun getUserClass() =
        sharedPreferences.getString(STUDENT_CLASS, PREF_STRING_VALUE_DEFAULT)
            ?: PREF_STRING_VALUE_DEFAULT

    override fun getStudentName(): String =
        sharedPreferences.getString(STUDENT_USER_NAME, PREF_STRING_VALUE_DEFAULT)
            ?: PREF_STRING_VALUE_DEFAULT

    override fun updateStudentName(name: String) {
        sharedPreferences.edit().putString(STUDENT_USER_NAME, name).apply()
    }

    override fun getUserCourse() =
        sharedPreferences.getString(STUDENT_COURSE, PREF_STRING_VALUE_DEFAULT)
            ?: PREF_STRING_VALUE_DEFAULT

    override fun getSelectedLanguage() =
        sharedPreferences.getString(STUDENT_LANGUAGE_CODE, PREF_STRING_VALUE_DEFAULT)
            ?: PREF_STRING_VALUE_DEFAULT

    override fun updateLanguage(languageCode: String, title: String) {
        sharedPreferences.edit().putString(STUDENT_LANGUAGE_CODE, languageCode).apply()
        sharedPreferences.edit().putString(STUDENT_LANGUAGE_NAME_DISPLAY, title).apply()
    }

    override fun getSelectedDisplayLanguage() =
        sharedPreferences.getString(STUDENT_LANGUAGE_NAME_DISPLAY, PREF_STRING_VALUE_DEFAULT)
            ?: PREF_STRING_VALUE_DEFAULT

    override fun setUserBadgeScreenOnceToTrue() {
        sharedPreferences.edit().putBoolean(PREF_KEY_IS_USER_SEEN_BADGE_SCREEN_SHOWN, true).apply()
    }

    override fun updateClass(className: String, classDisplay: String) {
        sharedPreferences.edit().run {
            putString(STUDENT_CLASS, className)
            putString(STUDENT_CLASS_DISPLAY, classDisplay)
            apply()
        }
    }

    override fun getUserProfileData(): HashMap<String, String> {
        val name =
            sharedPreferences.getString(STUDENT_USER_NAME, PREF_STRING_VALUE_DEFAULT).orEmpty()
        val school =
            sharedPreferences.getString(STUDENT_SCHOOL, PREF_STRING_VALUE_DEFAULT).orEmpty()
        val email = sharedPreferences.getString(STUDENT_EMAIL, PREF_STRING_VALUE_DEFAULT).orEmpty()
        val dob = sharedPreferences.getString(STUDENT_DOB, PREF_STRING_VALUE_DEFAULT).orEmpty()
        val pinCode =
            sharedPreferences.getString(STUDENT_PINCODE, PREF_STRING_VALUE_DEFAULT).orEmpty()
        val coachingName =
            sharedPreferences.getString(STUDENT_COACHING_NAME, PREF_STRING_VALUE_DEFAULT).orEmpty()
        val imageUrl =
            sharedPreferences.getString(STUDENT_IMAGE_URL, PREF_STRING_VALUE_DEFAULT).orEmpty()

        return hashMapOf(
            STUDENT_USER_NAME to name,
            STUDENT_SCHOOL to school,
            STUDENT_EMAIL to email,
            STUDENT_DOB to dob,
            STUDENT_PINCODE to pinCode,
            STUDENT_COACHING_NAME to coachingName,
            STUDENT_IMAGE_URL to imageUrl
        )
    }

    override fun getUserProfileDataAndOldStudentId(): Pair<HashMap<String, String>, String> {
        return getUserProfileData() to sharedPreferences.getString(
            OLD_STUDENT_ID,
            PREF_STRING_VALUE_DEFAULT
        ).orEmpty()
    }

    override fun updateUserProfileData(
        userName: String,
        email: String,
        school: String,
        pincode: String,
        coaching: String,
        dob: String,
        imageUrl: String?
    ) {
        sharedPreferences.edit().apply {
            putString(STUDENT_USER_NAME, userName)
            putString(STUDENT_EMAIL, email)
            putString(STUDENT_SCHOOL, school)
            putString(STUDENT_PINCODE, pincode)
            putString(STUDENT_COACHING_NAME, coaching)
            putString(STUDENT_DOB, dob)
            putString(STUDENT_IMAGE_URL, imageUrl ?: "")
        }.apply()
    }

    override fun getUserLoggedIn(): Boolean =
        sharedPreferences.getString(STUDENT_LOGIN, "false") == "true"

    override fun logOutUser() {
        sharedPreferences.edit().apply {
            putString(STUDENT_LOGIN, "false")
            putBoolean(ONBOARDING_COMPLETED, false)
            putString(STUDENT_CLASS, "")
            putInt(BOTTOM_SHEET_COUNT, 0)
            putBoolean(SHOW_BOTTOM_SHEET, true)
            putLong("camera_screen_shown_count", 0)
            putLong("camera_s_v_c", 0)
            putLong("ias_back_dialog_slp_count", 0)
            putLong("ias_back_dialog_srp_count", 0)
            putLong(MATCH_PAGE_FEEDBACK_DIALOG_COUNT, 0)
            putLong(MATCH_PAGE_FEEDBACK_DIALOG_CLOSE_COUNT, 0)
            putString(XAUTH_HEADER_TOKEN, "")
            putString(FIRST_PAGE_DEEPLINK, "")
            putInt("anonymous_login_type", 2)
            putBoolean("guest_login", false)
            putString("default_online_class_tab_tag", "")

            // This will sync the shared reference from config api.
            ISyncConfigDataWorker.syncConfigData = true

        }.apply()

        runBlocking {
            defaultDataStore.set(DefaultDataStoreImpl.DNR_NEW_USER_REWARD, false)
        }
    }

    override fun getDailyStreakRegisterEvent(): DailyStreakDate {
        val dailyStreakDefaultDate = "01-01-1990"
        val savedDate = sharedPreferences.getString(PREF_KEY_DAILY_STREAK, dailyStreakDefaultDate)
            ?: dailyStreakDefaultDate
        return DailyStreakDate(savedDate)
    }

    override fun setDailyStreakRegisterEvent(dailyStreakDate: DailyStreakDate) {
        sharedPreferences.edit().putString(PREF_KEY_DAILY_STREAK, dailyStreakDate.date).apply()
    }

    override fun isBottomNavigationButtonClicked(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    override fun setBottomNavigationButtonClicked(key: String) {
        sharedPreferences.edit().putBoolean(key, true).apply()
    }

    override fun getGcmRegistrationId(): String {
        return sharedPreferences.getString(GCM_REG_ID, "") ?: ""
    }

    override fun getCameraScreenVisitCount(): Long {
        return sharedPreferences.getLong("camera_s_v_c", 0L)
    }

    override fun putUserData(
        studentId: String,
        onBoardingVideoId: String,
        introList: List<IntroEntity>,
        studentUserName: String
    ) {
        sharedPreferences.edit().apply {
            putString(STUDENT_ID, studentId)
            putString(ONBOARDING_VIDEO, onBoardingVideoId)
            putString(STUDENT_USER_NAME, studentUserName)
            putString(STUDENT_LOGIN, "true")
        }.apply()

        for (i in introList.indices) {
            if (introList[i].type == TYPE_INTRO) {
                sharedPreferences.edit().apply {
                    putString(TYPE_INTRO_QID, introList[i].questionId)
                    putString(TYPE_INTRO_URL, introList[i].video)
                }.apply()
            } else if (introList[i].type == TYPE_COMMUNITY) {
                sharedPreferences.edit().apply {
                    putString(TYPE_COMMUNITY_QID, introList[i].questionId)
                    putString(TYPE_COMMUNITY_URL, introList[i].video)
                }.apply()
            }
        }
    }

    override fun getIsOnBoardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(ONBOARDING_COMPLETED, false)
    }

    override fun putOnBoardingCompleted() {
        sharedPreferences.edit().putBoolean(ONBOARDING_COMPLETED, true).apply()
    }

    override fun putFcmTokenUpdatedOnServerStatus(status: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER, status).apply()
    }

    override fun onCleverTapSetUp(studentId: String) {
        sharedPreferences.edit().apply {
            putString(OLD_STUDENT_ID, studentId)
            putBoolean(IS_LOGOUT, false)
        }.apply()
    }

    override fun putLibraryHistory(libraryHistoryEntity: LibraryHistoryEntity) {
        sharedPreferences.edit().putString(LIBRARY_HISTORY, Gson().toJson(libraryHistoryEntity))
            .apply()
    }

    override fun getLibraryHistory(): LibraryHistoryEntity? {
        val historyEntityString = sharedPreferences.getString(LIBRARY_HISTORY, null)
        return if (historyEntityString == null || historyEntityString.isBlank()) {
            null
        } else {
            getLibraryHistoryEntity(historyEntityString)
        }
    }

    override fun getLastShownInAppPopUp(): Long {
        return sharedPreferences.getLong(LAST_SHOWN_IN_APP_UPDATE, 0)
    }

    override fun setLastShownInAppPopUp(timeInMillis: Long) {
        sharedPreferences.edit().putLong(LAST_SHOWN_IN_APP_UPDATE, timeInMillis).apply()
    }

    override fun updateOnBoardingData(apiOnBoardingStatus: ApiOnBoardingStatus) {
        sharedPreferences.edit()
            .putBoolean(ONBOARDING_COMPLETED, apiOnBoardingStatus.isOnboardingCompleted).apply()
        if (apiOnBoardingStatus.isOnboardingCompleted) {
            apiOnBoardingStatus.studentLanguage?.let {
                sharedPreferences.edit().putString(STUDENT_LANGUAGE_CODE, it.code).apply()
                sharedPreferences.edit().putString(STUDENT_LANGUAGE_NAME, it.name).apply()
                sharedPreferences.edit().putString(STUDENT_LANGUAGE_NAME_DISPLAY, it.display)
                    .apply()
            }

            apiOnBoardingStatus.studentClass?.let {
                sharedPreferences.edit().putString(STUDENT_CLASS, it.code.toString()).apply()
                sharedPreferences.edit().putString(STUDENT_CLASS_DISPLAY, it.display).apply()
            }
        }
    }

    override fun updateClassAndLanguage(apiOnBoardingStatus: ApiOnBoardingStatus) {
        apiOnBoardingStatus.studentLanguage?.let {
            sharedPreferences.edit().putString(STUDENT_LANGUAGE_CODE, it.code).apply()
            sharedPreferences.edit().putString(STUDENT_LANGUAGE_NAME, it.name).apply()
            sharedPreferences.edit().putString(STUDENT_LANGUAGE_NAME_DISPLAY, it.display).apply()
        }

        apiOnBoardingStatus.studentClass?.let {
            sharedPreferences.edit().putString(STUDENT_CLASS, it.code.toString()).apply()
            sharedPreferences.edit().putString(STUDENT_CLASS_DISPLAY, it.display).apply()
        }

        apiOnBoardingStatus.pinExist?.let {
            sharedPreferences.edit().putBoolean(IS_PIN_SET, it).apply()
        }

        apiOnBoardingStatus.pin?.let {
            try {
                val encryptedPin = AESHelper.encrypt(it)
                sharedPreferences.edit().putString(PIN, encryptedPin).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun updateStudyDostData(apiStudyDost: ApiOnBoardingStatus.ApiStudyDost?) {
        sharedPreferences.edit().putString(STUDY_DOST_IMAGE, apiStudyDost?.image).apply()
        sharedPreferences.edit().putString(STUDY_DOST_DESCRIPTION, apiStudyDost?.description)
            .apply()
        sharedPreferences.edit().putInt(STUDY_DOST_UNREAD_COUNT, apiStudyDost?.unreadCount ?: 0)
            .apply()
        sharedPreferences.edit().putString(STUDY_DOST_DEEPLINK, apiStudyDost?.deeplink).apply()
        sharedPreferences.edit().putString(STUDY_DOST_CTA_TEXT, apiStudyDost?.ctaText).apply()
        sharedPreferences.edit().putInt(STUDY_DOST_LEVEL, apiStudyDost?.level ?: -1).apply()
    }

    override fun updateStudyGroupData(apiStudyGroup: ApiOnBoardingStatus.ApiStudyGroup?) {
        sharedPreferences.edit().putBoolean(IS_STUDY_GROUP_FEATURE_ENABLED, apiStudyGroup != null)
            .apply()
        apiStudyGroup?.let { groupInfo ->
            sharedPreferences.edit().putString(STUDY_GROUP_TITLE, groupInfo.title).apply()
            sharedPreferences.edit().putString(STUDY_GROUP_IMAGE, groupInfo.image).apply()
            sharedPreferences.edit().putString(STUDY_GROUP_DEEPLINK, groupInfo.deeplink).apply()
            setStudyGroupNotificationMuteStatus(apiStudyGroup.isMute)
        }
    }

    override fun getStudyGroupData(): ApiOnBoardingStatus.ApiStudyGroup? {
        val isStudyGroupEnabled =
            sharedPreferences.getBoolean(IS_STUDY_GROUP_FEATURE_ENABLED, false)
        if (isStudyGroupEnabled.not()) return null
        val studyGroupTitle =
            sharedPreferences.getString(STUDY_GROUP_TITLE, PREF_STRING_VALUE_DEFAULT)
                ?: PREF_STRING_VALUE_DEFAULT
        val studyGroupImage =
            sharedPreferences.getString(STUDY_GROUP_IMAGE, PREF_STRING_VALUE_DEFAULT)
                ?: PREF_STRING_VALUE_DEFAULT
        val studyGroupDeeplink =
            sharedPreferences.getString(STUDY_GROUP_DEEPLINK, PREF_STRING_VALUE_DEFAULT)
                ?: PREF_STRING_VALUE_DEFAULT
        val isMute = sharedPreferences.getBoolean(STUDY_GROUP_NOTIFICATION_MUTE_STATUS, false)
        return ApiOnBoardingStatus.ApiStudyGroup(
            title = studyGroupTitle,
            image = studyGroupImage,
            deeplink = studyGroupDeeplink,
            isMute = isMute
        )
    }

    // Start - Doubt P2p V2
    override fun updateDoubtP2pData(apiDoubtP2p: ApiOnBoardingStatus.ApiDoubtP2p?) {
        sharedPreferences.edit().putBoolean(IS_DOUBT_P2P_ENABLED, apiDoubtP2p != null).apply()
        apiDoubtP2p?.let { doubtP2p ->
            sharedPreferences.edit().putString(DOUBT_P2P_TITLE, doubtP2p.title).apply()
            sharedPreferences.edit().putString(DOUBT_P2P_IMAGE, doubtP2p.image).apply()
            sharedPreferences.edit().putString(DOUBT_P2P_DEEPLINK, doubtP2p.deeplink).apply()
        }
    }

    override fun getDoubtP2pData(): ApiOnBoardingStatus.ApiDoubtP2p? {
        val isDoubtP2pEnabled = sharedPreferences.getBoolean(IS_DOUBT_P2P_ENABLED, false)
        if (isDoubtP2pEnabled.not()) return null
        return ApiOnBoardingStatus.ApiDoubtP2p(
            title = sharedPreferences.getString(DOUBT_P2P_TITLE, "") ?: "",
            image = sharedPreferences.getString(DOUBT_P2P_IMAGE, "") ?: "",
            deeplink = sharedPreferences.getString(DOUBT_P2P_DEEPLINK, "") ?: ""
        )
    }

    override fun setDoubtP2pNotificationEnabled(enable: Boolean) {
        sharedPreferences.edit().putBoolean(IS_DOUBT_P2P_NOTIFICATION_ENABLED, enable).apply()
    }

    override fun isDoubtP2pNotificationEnabled(): Boolean =
        sharedPreferences.getBoolean(IS_DOUBT_P2P_NOTIFICATION_ENABLED, true)
    // End - Doubt P2p V2

    // Start - Khelo Aur Jeeto V2
    override fun updateKheloAurJeetoData(apiKheloAurJeeto: ApiOnBoardingStatus.ApiKheloAurJeeto?) {
        sharedPreferences.edit().putBoolean(IS_KHELO_AUR_JEETO_ENABLED, apiKheloAurJeeto != null)
            .apply()
        apiKheloAurJeeto?.let { doubtP2p ->
            sharedPreferences.edit().putString(KHELO_AUR_JEETO_TITLE, doubtP2p.title).apply()
            sharedPreferences.edit().putString(KHELO_AUR_JEETO_IMAGE, doubtP2p.image).apply()
            sharedPreferences.edit().putString(KHELO_AUR_JEETO_DEEPLINK, doubtP2p.deeplink).apply()
        }
    }

    override fun getKheloAurJeetoData(): ApiOnBoardingStatus.ApiKheloAurJeeto? {
        val isDoubtP2pEnabled = sharedPreferences.getBoolean(IS_KHELO_AUR_JEETO_ENABLED, false)
        if (isDoubtP2pEnabled.not()) return null
        return ApiOnBoardingStatus.ApiKheloAurJeeto(
            title = sharedPreferences.getString(KHELO_AUR_JEETO_TITLE, "") ?: "",
            image = sharedPreferences.getString(KHELO_AUR_JEETO_IMAGE, "") ?: "",
            deeplink = sharedPreferences.getString(KHELO_AUR_JEETO_DEEPLINK, "") ?: ""
        )
    }
    // End - Khelo Aur Jeeto V2

    // region Doubt Feed 2
    override fun updateDoubtFeed2Data(apiDoubtFeed2: ApiOnBoardingStatus.ApiDoubtFeed2?) {
        sharedPreferences.edit().putBoolean(IS_DOUBT_FEED_2_ENABLED, apiDoubtFeed2 != null).apply()
        apiDoubtFeed2?.let { df ->
            sharedPreferences.edit().putString(DOUBT_FEED_2_TITLE, df.title).apply()
            sharedPreferences.edit().putString(DOUBT_FEED_2_IMAGE, df.image).apply()
            sharedPreferences.edit().putString(DOUBT_FEED_2_DEEPLINK, df.deeplink).apply()
        }
    }

    override fun getDoubtFeed2Data(): ApiOnBoardingStatus.ApiDoubtFeed2? {
        val isDoubtFeedEnabled = sharedPreferences.getBoolean(IS_DOUBT_FEED_2_ENABLED, false)
        if (isDoubtFeedEnabled.not()) return null
        return ApiOnBoardingStatus.ApiDoubtFeed2(
            title = sharedPreferences.getString(DOUBT_FEED_2_TITLE, "") ?: "",
            image = sharedPreferences.getString(DOUBT_FEED_2_IMAGE, "") ?: "",
            deeplink = sharedPreferences.getString(DOUBT_FEED_2_DEEPLINK, "") ?: ""
        )
    }
    //endregion

    // Start - Study Group
    override fun setStudyGroupNotificationMuteStatus(isMute: Boolean) {
        sharedPreferences.edit().putBoolean(STUDY_GROUP_NOTIFICATION_MUTE_STATUS, isMute).apply()
    }

    override fun isStudyGroupNotificationMuted(): Boolean =
        sharedPreferences.getBoolean(STUDY_GROUP_NOTIFICATION_MUTE_STATUS, true)
    // End - Study Group

    // Start - Revision Corner
    override fun updateRevisionCornerData(apiRevisionCorner: ApiOnBoardingStatus.ApiRevisionCorner?) {
        sharedPreferences.edit().putBoolean(IS_REVISION_CORNER_ENABLED, apiRevisionCorner != null)
            .apply()
        apiRevisionCorner?.let { rc ->
            sharedPreferences.edit().putString(REVISION_CORNER_TITLE, rc.title).apply()
            sharedPreferences.edit().putString(REVISION_CORNER_IMAGE, rc.image).apply()
            sharedPreferences.edit().putString(REVISION_CORNER_DEEPLINK, rc.deeplink).apply()
        }
    }

    override fun getRevisionCornerData(): ApiOnBoardingStatus.ApiRevisionCorner? {
        val isRevisionCornerEnabled =
            sharedPreferences.getBoolean(IS_REVISION_CORNER_ENABLED, false)
        if (isRevisionCornerEnabled.not()) return null
        return ApiOnBoardingStatus.ApiRevisionCorner(
            title = sharedPreferences.getString(REVISION_CORNER_TITLE, "") ?: "",
            image = sharedPreferences.getString(REVISION_CORNER_IMAGE, "") ?: "",
            deeplink = sharedPreferences.getString(REVISION_CORNER_DEEPLINK, "") ?: ""
        )
    }
    // End - Revision Corner

    // Start - Doubtnut Rupya
    override fun updateDnrData(apiDnr: ApiOnBoardingStatus.ApiDnr?) {
        apiDnr?.let { dnrData ->
            sharedPreferences.edit {
                putString(DNR_TITLE, dnrData.title).apply()
                putString(DNR_TITLE1, dnrData.title1).apply()
                putString(DNR_IMAGE, dnrData.image).apply()
                putString(DNR_DESCRIPTION, dnrData.description).apply()
                putString(DNR_CTA_TEXT, dnrData.ctaText).apply()
                putString(DNR_DEEPLINK, dnrData.deeplink).apply()
            }
        }
    }

    override fun getDnrData(): ApiOnBoardingStatus.ApiDnr {
        return ApiOnBoardingStatus.ApiDnr(
            title = sharedPreferences.getString(DNR_TITLE, "") ?: "",
            title1 = sharedPreferences.getString(DNR_TITLE1, "") ?: "",
            image = sharedPreferences.getString(DNR_IMAGE, "") ?: "",
            deeplink = sharedPreferences.getString(DNR_DEEPLINK, "") ?: "",
            description = sharedPreferences.getString(DNR_DESCRIPTION, "") ?: "",
            ctaText = sharedPreferences.getString(DNR_CTA_TEXT, "") ?: "",
            lfEngagementTime = -1,
            srpSfEngagementTime = -1,
            nonSrpSfEngagementTime = -1
        )
    }
    // End - Doubtnut Rupya
    private fun getLibraryHistoryEntity(data: String): LibraryHistoryEntity? =
        Gson().fromJson(data, object : TypeToken<LibraryHistoryEntity>() {}.type)

    override fun putUserHasWatchedVideo(state: Boolean) {
        sharedPreferences.edit().putBoolean(USER_HAS_WATCHED_VIDEO, state).apply()
    }

    override fun getUserHasWatchedVideo(): Boolean {
        return sharedPreferences.getBoolean(USER_HAS_WATCHED_VIDEO, false)
    }

    override fun putVideoWatchedCount(count: Int) {
        sharedPreferences.edit().putInt(VIDEO_WATCHED_COUNT, count).apply()
    }

    override fun getVideoWatchedCount(): Int {
        return sharedPreferences.getInt(VIDEO_WATCHED_COUNT, 0)
    }

    override fun putVideoWatchedOnDayZero(state: Boolean) {
        sharedPreferences.edit().putBoolean(VIDEO_WATCHED_ON_DAY0, state).apply()
    }

    override fun getVideoWatchedOnDayZero(): Boolean {
        return sharedPreferences.getBoolean(VIDEO_WATCHED_ON_DAY0, false)
    }

    override fun putVideoWatchedAfterDayTwo(state: Boolean) {
        sharedPreferences.edit().putBoolean(VIDEO_WATCHED_AFTER_DAY2, state).apply()
    }

    override fun getVideoWatchedAfterDayTwo(): Boolean {
        return sharedPreferences.getBoolean(VIDEO_WATCHED_AFTER_DAY2, false)
    }

    override fun putThreeVideosWatchedInTwoDays(state: Boolean) {
        sharedPreferences.edit().putBoolean(THREE_VIDEOS_WATCHED_IN_2DAYS, state).apply()
    }

    override fun getThreeVideosWatchedInTwoDays(): Boolean {
        return sharedPreferences.getBoolean(THREE_VIDEOS_WATCHED_IN_2DAYS, false)
    }

    override fun putUserSelectedExams(exams: String) {
        sharedPreferences.edit().putString(USER_SELECTED_EXAMS, exams).apply()
    }

    override fun getUserSelectedExams(): String {
        return sharedPreferences.getString(USER_SELECTED_EXAMS, "") ?: ""
    }

    override fun putUserSelectedBoard(board: String) {
        sharedPreferences.edit().putString(USER_SELECTED_BOARD, board).apply()
    }

    override fun putCcmId(ccmId: String) {
        sharedPreferences.edit().putString(CCM_ID, ccmId).apply()
    }

    override fun getCcmId() {
        sharedPreferences.getString(CCM_ID, "")
    }

    override fun getUserSelectedBoard(): String {
        return sharedPreferences.getString(USER_SELECTED_BOARD, "") ?: ""
    }

    override fun putUserHasWatchedEtoosVideo(state: Boolean) {
        sharedPreferences.edit().putBoolean(USER_WATCHED_ETOOS_VIDEO, state).apply()
    }

    override fun getUserHasWatchedEtoosVideo(): Boolean {
        return sharedPreferences.getBoolean(USER_WATCHED_ETOOS_VIDEO, false)
    }

    override fun hasPlayService(): Boolean {
        return sharedPreferences.getBoolean(HAS_PLAY_SERVICE, false)
    }

    override fun isEmulator(): Boolean {
        return sharedPreferences.getBoolean(IS_EMULATOR, false)
    }

    override fun putLastDayBranchEventSend(day: Int) {
        sharedPreferences.edit().putInt(LAST_DAY_BRANCH_EVENT_SEND, day).apply()
    }

    override fun getLastDayBranchEventSend(): Int {
        return sharedPreferences.getInt(LAST_DAY_BRANCH_EVENT_SEND, 0)
    }

    override fun putLastPaymentInitiateTime(time: Long) {
        sharedPreferences.edit().putLong(LAST_PAYMENT_INITIATE_TIME, time).apply()
    }

    override fun getLastPaymentInitiateTime(): Long {
        return sharedPreferences.getLong(LAST_PAYMENT_INITIATE_TIME, 0)
    }

    override fun getAccessToken(): String {
        return sharedPreferences.getString(XAUTH_HEADER_TOKEN, "") ?: ""
    }

    override fun setAppExitDialogShownInCurrentSession(shown: Boolean) {
        sharedPreferences.edit().putBoolean(APP_EXIT_DIALOG_SHOWN_IN_CURRENT_SESSION, shown).apply()
    }

    override fun getAppExitDialogShownInCurrentSession(): Boolean {
        return sharedPreferences.getBoolean(APP_EXIT_DIALOG_SHOWN_IN_CURRENT_SESSION, false)
    }

    override fun setCameraScreenNavigationDataFetchedInCurrentSession(isFetched: Boolean) {
        sharedPreferences.edit()
            .putBoolean(CAMERA_SCRREN_NAVIGATION_DATA_FETCHED_IN_CURRENT_SESSION, isFetched).apply()
    }

    override fun getCameraScreenNavigationDataFetchedInCurrentSession(): Boolean {
        return sharedPreferences.getBoolean(
            CAMERA_SCRREN_NAVIGATION_DATA_FETCHED_IN_CURRENT_SESSION,
            false
        )
    }

    override fun getUserImageUrl(): String {
        return sharedPreferences.getString(STUDENT_IMAGE_URL, PREF_STRING_VALUE_DEFAULT).orEmpty()
    }

    override fun getRewardSystemCurrentLevel(): Int {
        return sharedPreferences.getInt(CURRENT_LEVEL, CURRENT_LEVEL_UNKNOWN)
    }

    override fun getRewardSystemCurrentDay(): Int {
        return sharedPreferences.getInt(CURRENT_LEVEL, CURRENT_DAY_UNKNOWN)
    }

    override fun putRewardSystemCurrentLevel(currentLevel: Int) {
        sharedPreferences.edit().putInt(CURRENT_LEVEL, currentLevel).apply()
    }

    override fun putRewardSystemCurrentDay(currentDay: Int) {
        sharedPreferences.edit().putInt(CURRENT_DAY, currentDay).apply()
    }

    override fun putShowRewardSystemScratchCardReminder(scratchCardStatus: Boolean) {
        sharedPreferences.edit().putBoolean(SHOW_SCRATCH_CARD_REMINDER, scratchCardStatus).apply()
    }

    override fun getShowRewardSystemScratchCardReminder(): Boolean {
        return sharedPreferences.getBoolean(SHOW_SCRATCH_CARD_REMINDER, true)
    }

    override fun setDoubtFeedAvailable(isAvailable: Boolean) {
        sharedPreferences.edit().putBoolean(IS_DOUBT_FEED_AVAILABLE, isAvailable).apply()
    }

    override fun isDoubtFeedAvailable(): Boolean {
        return sharedPreferences.getBoolean(IS_DOUBT_FEED_AVAILABLE, false)
    }

    override fun setDoubtP2PHomeWidgetVisibility(visibility: Boolean) {
        sharedPreferences.edit().putBoolean(IS_DOUBT_P2P_HOME_WIDGET_VISIBLE, visibility).apply()
    }

    override fun isDoubtP2PHomeWidgetVisibility(): Boolean {
        return sharedPreferences.getBoolean(IS_DOUBT_P2P_HOME_WIDGET_VISIBLE, true)
    }

    override fun getQuestionAskCount(): Long {
        return sharedPreferences.getLong(QUESTION_ASK_COUNT, 0)
    }

    override fun getGaid(): String {
        return sharedPreferences.getString(GAID, "") ?: ""
    }

    override fun setJourneyCountDataAsString(journeyCountMap: Map<String, Int>?) {
        sharedPreferences.edit().putString(USER_JOURNEY, journeyCountMap?.toString()).apply()
    }

    override fun getJourneyCountDataAsString(): String {
        return sharedPreferences.getString(USER_JOURNEY, PREF_STRING_VALUE_DEFAULT) ?: PREF_STRING_VALUE_DEFAULT
    }

    override fun setUserJourneyCountForKey(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    override fun getUserJourneyCountForKey(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }
}
