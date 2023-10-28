package com.doubtnutapp

import com.doubtnut.core.constant.CoreConstants

object Constants {
    const val FRESHCHAT_RESTORE_ID = "restore_id"
    const val OPEN_PROFILE_FROM_BOTTOM = "open_profile"
    const val OPEN_FORUM_FROM_BOTTOM = "open_forum"
    const val OPEN_LIBRARY_FROM_BOTTOM = "open_library"
    const val SUBJECT = "subject"
    const val VERSION_CODE: String = "version_code"
    const val VERSION_NAME: String = "version_name"
    const val PAGE_HISTORY = "VIDEO_HISTORY"
    const val PREF_KEY_CAMERA_SCREEN_SHOWN_COUNT = "camera_screen_shown_count"
    const val NAVIGATE_ON_BOARDING = "navigate_on_boarding"
    const val XAUTH_HEADER_TOKEN = "x-auth-token"
    const val XAUTH_HEADER_REFRESH_TOKEN = "x-auth-refresh-token"
    const val RESPONSE_XAUTH_HEADER_TOKEN = "dn-x-auth-token"
    const val RESPONSE_XAUTH_HEADER_REFRESH_TOKEN = "dn-x-auth-refresh-token"
    const val GAMES_PARAM_SUB = "sub"
    const val NAVIGATE_COURSE_DETAIL = "navigate_course_detail"
    const val NAVIGATE_VIEW_PLAYLIST = "navigate_view_playlist"
    const val NAVIGATE_HOME = "navigate_home"
    const val NAVIGATE_LIBRARY = "navigate_library"
    const val NAVIGATE_PROFILE = "navigate_profiles"
    const val NAVIGATE_DOUBT_FEED = "navigate_doubt_feed"
    const val NAVIGATE_FORUM_FEED = "navigate_forum_feed"
    const val NAVIGATE_FEED = "navigate_feed"
    const val COUNT = "count"
    const val NAVIGATE_COURSE = "navigate_course"
    const val NAVIGATE_COURSE_DETAIL_NOTIFICATION = "navigate_course_detail_notification"
    const val NAVIGATE_FROM_CLASS_CHANGE = "navigate_from_class_change"
    const val STUDENT_ID = CoreConstants.STUDENT_ID
    const val SEEN_NOTIFICATIONS_LIST = "seen_notifications_list"
    const val SID = "sid"
    const val SOLUTION_RESOURCE_TYPE = "resource_type"
    const val CLASS = "class"
    const val EXAM = "exam"
    const val COURSE = "course"
    const val CHAPTER = "chapter"
    const val MICROCONCEPT = "microconcept"
    const val ONBOARDING_VIDEO = "onboardingVideo"
    const val STUDENT_CLASS = CoreConstants.STUDENT_CLASS
    const val CCM_ID = "selected_exam_ccm_id"
    const val LAST_MOE_NOTIFICATION_ID = "last_moe_notification_id"
    const val UNREAD_NOTIFICATION_COUNT = "unread_notification_count"
    const val STUDENT_CLASS_DISPLAY_NCERT = "student_class_display_ncert"
    const val STUDENT_CLASS_NCERT = "student_class_ncert"
    const val STUDENT_LANGUAGE_CODE = "student_language_code"
    const val USER_SELECTED_BOARD = "user_selected_board"
    const val USER_SELECTED_EXAMS = "user_selected_exams"
    const val STUDENT_COURSE = "student_course"
    const val STUDENT_BOARD = "student_board"
    const val STUDENT_EXAM = "student_exam"
    const val CHAPTER_LIST = "chapter_list"
    const val PARENT_LIST = "parent_list"
    const val QUESTION_ID = "question_id"
    const val ASK_QUE_URI = "ask_que_uri"
    const val ASK_QUE_URL = "ask_que_url"
    const val UPLOADED_IMAGE_QUESTION_ID = "uploaded_image_question_id"
    const val ASK_IMAGE_OCR = "typed_question_text"
    const val OCR_NOTIFICATION_ID = "ocr_notification_id"
    const val VIDEO_TAG_NAME = "video_tag_name"
    const val ITEM = "item"
    const val Q_ID = "qid"
    const val VIDEO_START_POSITION = "video_start_position"
    const val ID = "id"
    const val PARENT_ID = "parent_id"
    const val OCR_TEXT = "ocr_text"
    const val HTML = "html"
    const val DEEPLINK = "DEEPLINK"
    const val ONBOARDING_COMPLETED = CoreConstants.ON_BOARDING_COMPLETED
    const val SESSION_ID = "session_id"
    const val GCM_REG_ID = "gcm_reg_id"
    const val PHONE_NUMBER = "phone_number"
    const val RECENT_DICTIONARY_SEARCHES = "recent_dictionary_searches"
    const val COUNTRY_CODE = "country_code"
    const val COUNTRY_CODE_INDIA: String = "91"
    const val PLAYLIST_ID = "playlist_id"
    const val TAB_TITLE = "tab_title"
    const val MC_CLASS = "mc_class"
    const val PLAYLIST_TITLE = "playlist_title"
    const val IS_LAST = "is_last"
    const val CDN_PATH = "cdn_path"
    const val COLOR_INDEX = "color_index"
    const val PAGE = "page"
    const val PARENT_PAGE = "parent_page"
    const val PAGE_CC = "CC"
    const val PAGE_SC = "SC"
    const val PAGE_SRP = "SRP"
    const val PAGE_P2P = "P2P"
    const val PAGE_MPVP = "MPVP"
    const val PAGE_MPVP_BOTTOM_SHEET = "MPVP_BOTTOM_SHEET"
    const val PAGE_WOLFRAM = "WOLFRAM"
    const val PAGE_YT_ASK = "YT_ASK"
    const val PAGE_LIBRARY = "LIBRARY"
    const val PAGE_BROWSE = "BROWSE"
    const val PAGE_SIMILAR = "SIMILAR"
    const val PAGE_SEARCH_SRP = "SEARCH_SRP"
    const val PAGE_TOPIC_BOOSTER_GAME = "TOPIC_BOOSTER_GAME"
    const val PAGE_REWARDS = "REWARDS"
    const val PAGE_ADV_SEARCH = "ADV_SEARCH"
    const val PAGE_PRACTICE_CORNER = "PRACTICE_CORNER"
    const val IMAGE_SOURCE = "image_source"
    const val NCERT_PLAYLIST_ID = "NCERT"
    const val IS_FROM_VIDEO_TAG = "IS_FROM_VIDEO_TAG"
    const val HIDE_TOOLBAR = "hide_toolbar"
    const val JEE_ADVANCE = "JEE_ADVANCE"
    const val JEE_MAIN = "JEE_MAIN"
    const val BOARDS_12 = "BOARDS_12"
    const val BOARDS_10 = "BOARDS_10"
    const val QUESTION_SHORT_URL_ERROR_BRANCH_TOAST =
        "Error while getting the Question link to share"
    const val PAGE_NAME = "page_name"
    const val REFFERING_PARAMS = "referring_params"
    const val STUDENT_LANGUAGE_NAME = "student_language_name"
    const val STUDENT_USER_NAME = "student_user_name"
    const val URL = "url"
    const val PDF_PACKAGE = "pdf_package"
    const val LEVEL_ONE = "level_one"
    const val SMART_CONTENT = "smart_content"

    //region Branch Deep Link
    const val BRANCH_HOST = "doubtnut.app.link"

    const val VIDEO_CHANNEL = "app_video_share"

    const val TIME_SPENT_IN_MIN = "dn_t_s_in_min"
    const val NAVIGATE_TO_FREE_TRIAL_COURSE = "navigate_to_free_trial_course"

    //bottom nav icons keys
    const val KEY_HOME = "navigate_home"
    const val KEY_ONLINE_CLASS = "navigate_live_class"
    const val KEY_FRIENDS = "navigate_feed"
    const val KEY_PROFILE = "navigate_profiles"
    const val KEY_ONLINE_CLASS_TAB = "navigate_online_class_tab"
    const val NAVIGATE = "navigate"

    /**
     * This constant is still here because we want the back portability for old users. For new users
     * we use [FORUM_SHARED_FEED_DETAIL_FEATURE]
     * */
    const val TIMELINE_FEED_FEATURE = "timeline_feed"

    const val FORUM_SHARED_FEED_DETAIL_FEATURE = "forum_shared_detail_feed"

    const val DOWNLOAD_PDF = "downloadpdf"

    const val INVITE_FRIEND_FEATURE = "invite_friend"
    const val INVITE_FRIEND_CHANNEL = "invite_friend"
    const val CONTROL_PARAM_KEY_INVITOR_ID: String = "invitorId"
    const val CONTROL_PARAM_KEY_REFERRER_STUDENT_ID: String = "referrer_student_id"
    //endregion

    const val VIEW_ID = CoreConstants.VIEW_ID
    const val STUDENT_LOGIN = CoreConstants.STUDENT_LOGIN
    const val NAVIGATE_LOGOUT = CoreConstants.NAVIGATE_LOGOUT
    const val NAVIGATE_FROM = CoreConstants.NAVIGATE_FROM
    const val NAVIGATE_FROM_DEEPLINK = CoreConstants.NAVIGATE_FROM_DEEPLINK

    const val CAMERA = "camera"
    const val RECENT_SEARCHES = "recent_searches"
    const val RECENT_SEARCHES_ICON_URL = "recent_searches_icon_url"

    const val INTENT_EXTRA_TAG_KEY = "tag_key"
    const val INTENT_TAG_VALUE = "tag_value"
    const val INTENT_EXTRA_INVITOR_ID = "invitor_id"
    const val INTENT_EXTRA_FORUM_FILTER = "filter"
    const val FILTER_FRAGMENT = "filter_fragment"
    const val FILTER_OPTION_FRAGMENT = "filter_option_fragment"

    //region SharedPreference Extra Key
    const val SHARED_PREF_EXTRA_INVITATION_DATA_SENT = "invitation_data_sent"
    //endregion

    const val VIDEO_NOT_WATCHED_COUNTER = "video_not_watched_counter"
    const val FEEDBACK_TYPE_ASKED_NOT_WATCHED = "asked_but_not_watched"
    const val FEEDBACK_TYPE_NO_MATCH_WATCHED = "no_matches"
    const val FEEDBACK_NPS = "nps"
    const val FEEDBACK_OBJECT = "feedback_object"

    const val PARENT_QUESTION_ID = "prarent_question_id"
    const val NEW_SESSION = "new_session"
    const val NOTIFICATION_SETTING = "notification_setting"

    const val VIRAL_VIDEO_SOURCE: String = "android"
    const val VIRAL_PAGE: String = "ONBOARDING"
    const val STUDENT_COURSE_LIST: String = "student_course_list"
    const val LOCAL_BROADCAST_REQUEST_ACCEPT: String = "local_broadcast_request_accept"
    const val LOCAL_BROADCAST_REQUEST_DATA: String = "local_broadcast_request_data"
    const val ASK_FRAGMENT: String = "ask_fragment"
    const val LIBRARY_FRAGMENT: String = "library_fragment"
    const val FORUM_FRAGMENT: String = "forum_fragment"
    const val PROFILE_FRAGMENT: String = "profile_fragment"
    const val PROFILE = "profile"
    const val SELECT_CLASS_FRAGMENT: String = "select_class_fragment"
    const val PAGE_COMMUNITY: String = "COMMUNITY"

    const val TYPE_INTRO = "intro"
    const val TYPE_COMMUNITY = "community"
    const val TYPE_INTRO_URL = "type_intro_url"
    const val TYPE_INTRO_QID = "type_intro_qid"
    const val TYPE_COMMUNITY_URL = "type_community_url"
    const val TYPE_COMMUNITY_QID = "type_community_qid"
    const val CLASSAPIHIT: String = "classapihit"

    const val PAGE_SUGGESTIONS: String = "SUGGESTIONS"
    const val GOOGLE: String = "Google"
    const val ONLINE_CLASSES = "Online Classes"
    const val VIP = "VIP"
    const val CYMATH: String = "Cymath"
    const val YOUTUBE: String = "Youtube"
    const val DOUBTNUT: String = "Doubtnut"
    const val USER: String = "User"
    const val MATCH: String = "MatchPage"
    const val SUPPORTED_CHARACTER_ENCODING = "UTF-8"

    const val NCERT_SLV = "Solved Examples "
    const val NCERT_MEX = "Miscellaneous Exercise "
    const val NCERT_EX = "Exercise "
    const val NCERT_QN = "|Question No. "

    const val INVITE_SHARE_IMAGE =
        "https://d10lpgp6xz60nq.cloudfront.net/marketing/invite_share_image.png"
    const val className: String = "CLASS:"
    const val CHECK_LANGUAGE_SELECTED: String = "check_language_selected"
    const val NAVIGATE_LANGUAGE_FRAGMENT_FROM_EDITPROFILE: String = "navigate_language_fragment"
    const val NAVIGATE_LANGUAGE_FRAGMENT_FROM_NAV: String = "navigate_language_fragment_from_nav"
    const val NAVIGATE_CLASS_FRAGMENT_FROM_NAV: String = "navigate_class_fragment_from_nav"

    const val MY_PERMISSIONS_REQUEST_CAMERA: Int = 10001
    const val MY_PERMISSIONS_REQUEST_LOCATION: Int = 80

    const val CAMERA_GALLERY_DIALOG_PROFILE: String = "camera_gallery_dialog_profile"
    val CAMERA_GALLERY_DIALOG_COMMENT: String = "camera_gallery_dialog_comment"

    const val INTENT_EXTRA_COMMENT_COUNT: String = "comment_count"
    const val INTENT_EXTRA_ENTITY_ID = "entityId"
    const val INTENT_EXTRA_ENTITY_TYPE = "entityKey"
    const val INTENT_EXTRA_FEED_POSITION = "feedPosition"
    const val INTENT_EXTRA_PARENT_COMMENT = "parentComment"
    const val INTENT_EXTRA_BATCH_ID = "batch_id"

    const val CONTEST_ID: String = "contest_id"
    const val SESSION_FROM_INVITE: String = "session_from_invitee"
    const val APP_VERSION: String = "app_version"
    const val APP_VERSION_NCERT: String = "app_version_ncert"
    const val STUDENT_LANGUAGE_NAME_DISPLAY: String = "student_language_name_display"
    const val FILTER_PACKAGE: String = "package"
    const val FILTER_LEVEL_ONE: String = "level1"
    const val FILTER_LEVEL_TWO: String = "level2"
    const val STUDENT_CLASS_DISPLAY: String = "student_class_display"
    const val PAGE_SSC: String = "SSC"

    //region KEYS
    const val KEY_UDID = "udid"
    const val KEY_NAME = "name"
    const val KEY_APP_VERSION = "app_version"
    const val KEY_EMAIL = "email"
    const val KEY_GCM_REG_ID = "gcm_reg_id"
    const val KEY_GCM_ID = "gcm_id"
    const val KEY_STUDENT_ID = "student_id"
    const val KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER = "firebase_reg_id_updated_on_server"
    const val KEY_IS_VERFIED = "is_verified"
    const val KEY_VISITED_PROFILE_ID = "visited_profile_id"
    const val DOUBTNUT_PROFILE_ID = "98"

    //endregion

    //region onboarding networkcall error
    const val GET_LANGUAGE: String = "get_language"
    const val UPDATE_LANGUAGE: String = "update_language"
    const val ERROR_MSG: String = "error_msg"
    const val FAILED_IN: String = "failed_in"
    const val LANG_CODE: String = "lang_code"
    const val ADD_PUBLIC_USER: String = "add_public_user"
    const val CLASS_SSC: String = "class_ssc"

    const val NAVIGATE_LANGUAGE_FRAGMENT_FROM_PROFILE = "navigate_language_fragment_from_profile"
    const val DEVICE_NAME = "DeviceName"
    const val DEVICE_LATITUDE = "DeviceLatitude"
    const val DEVICE_LONGITUDE = "DeviceLongitude"
    const val APP_LAUNCH_TIME = "app_launch_time"

    const val AES_KEY = CoreConstants.AES_KEY
    const val IV_KEY = CoreConstants.IV_KEY
    const val YOUTUBE_API_KEY = "AIzaSyD4Os4iXuGWAfJySVk4IW_2KLoe5DtVI2k"
    const val TRACK_UX_CAM = "track_ux_cam"
    const val NAVIGATE_CAMERA_SCREEN = "navigate_camera_screen"

    const val REQUEST_CODE_COMMENT_ACTIVITY: Int = 10005
    const val INVALID_ITEM_POSITION = -1
    const val TAKE_PHOTO_REQUEST = 101
    const val GALLERY_REQUEST = 102

    const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    const val REQUEST_STORAGE_PERMISSION = 201
    const val POST_ID = "post_id"
    const val INVITE_PAGE_FEATURE = "invite"
    const val FROM_NOTIFICATION_VIDEO = "from_notification_video"
    const val VIDEO_FIREBASE_EVENT_TAG = "video_firebase_event_tag"

    // Test
    const val TEST_DETAILS_OBJECT: String = "testDetails"
    const val USER_CANNOT_ATTEMPT_TEST: String = "user_cannpt_attempt_test"
    const val TEST_OVER: String = "test_over"
    const val TEST_UPCOMING: String = "test_upcoming"
    const val TEST_ACTIVE: String = "test_active"
    const val TEST_TRUE_TIME_FLAG: String = "test_true_time_flag"
    const val TEST_START_TIME: String = "test_start_time"
    const val TEST_ID: String = "test_id"
    const val TEST_SUBSCRIPTION_ID: String = "test_subscription_id"
    const val SYLLABUS_TITLE: String = "syllabus_title"
    const val TEST_QUESTION_LIST: String = "test_question_list"
    const val TEST_QUESTION_INDEX: String = "test_question_index"
    const val TEST_SIZE: String = "test_list_size"
    const val TEST_REPORT_LIST_FLAG = "test_report_list_flag"
    const val TEST_SINGLE_ACTION_TYPE_OPTION = "SINGLE"
    const val TEST_MULTIPLE_ACTION_TYPE_OPTION = "MULTI"
    const val TEST_MATRIX_ACTION_TYPE_OPTION = "MATRIX"
    const val TEST_TEXT_ACTION_TYPE_OPTION = "TEXT"

    const val TEST_REPORT_LIST = "test_report_list"

    const val FORMULA_SUBJECT_ID = "subjectId"
    const val CHAPTER_ID = "chapterId"
    const val CHAPTER_TITLE = "chapter_title"
    const val ASSORTMENT_ID = "assortment_id"
    const val VARIANT_ID = "variant_id"
    const val TITLE = "title"
    const val IS_TRIAL = "is_trial"
    const val FORMULA_SUBJECT_NAME = "formula_subject_name"
    const val FORMULA_SUBJECT_ICON = "formula_subject_icon"
    const val SUPER_CHAPTER_NAME = "superChapterName"
    const val PDF_VIEWER = "pdf_viewer"
    const val FEATURE_TYPE_FEED_POST = "feed_post"
    const val INTENT_EXTRA_PDF_URL = "pdf_url"
    const val CHEAT_SHEET = "Cheatsheet"
    const val SEARCH_TYPE = "searchType"
    const val CLICKED_ITEM_NAME = "clickedItemName"
    const val FORMULAS = "formulas"
    const val TOPICS = "topics"
    const val CHAPTERS = "chapters"
    const val FORMULA_ITEM_ID = "formula_item_id"
    const val APP_INDEXING = "APP_INDEXING"

    const val IN_DEEP_LINK = "inDeepLink"
    const val EXCERCISE_NAME = "excercise_name"
    const val INTENT_EXTRA_GROUP_NAME = "group_name"
    const val GROUP_CHAT = "group_chat"
    const val IS_AUTO_PLAY = "is_auto_play"
    const val QUESTION_IDS = "question_ids"
    const val MOCK_TEST_LIST = "mock_test_list"
    const val MOCK_TEST_TOOLBAR_TEXT = "mock_test_tite"
    const val EVENT_NAME_ASK_QUESTION_FROM_GALLERY = "ask_question_gallery_image"

    // Pdf Screen constant
    const val PDF_ACTION_ACTIVITY = "pdf_action_activity"

    const val PDF_ACTION_DATA_URL = "pdf_action_data_url"
    const val PDF_ACTION_DATA_PACKAGE = "pdf_action_data_package"
    const val PDF_ACTION_ACTIVITY_VIEW = "pdf_viewer"
    const val PDF_ACTION_ACTIVITY_LEVEL_ONE = "downloadpdf_level_one"
    const val PDF_ACTION_ACTIVITY_LEVEL_TWO = "downloadpdf_level_two"

    const val NAVIGATE_TO_SELECT_CLASS = "navigate_to_select_class"

    //
    const val STRING_COURSE_GOVT_EXAM = "GOVT_EXAM"
    const val STRING_COURSE_NCERT = "NCERT"
    const val LIBRARY_FRAGMENT_LEVEL_PAGE = "library_fragment_level_page"
    const val LIBRARY_FRAGMENT_LEVEL_PLAYLIST_ID = "library_fragment_level_playlist_Id"
    const val LIBRARY_FRAGMENT_LEVEL_PLAYLIST_TITLE = "library_fragment_level_playlist_Title"
    const val LIBRARY_FRAGMENT_LEVEL = "library_fragment_level"

    // match page
    const val GOOGLE_URL = "https://www.google.com/search?q="
    const val YOUTUBE_URL: String = "https://www.youtube.com/search?q="
    const val SCALE_DOWN_IMAGE_QUALITY = 70
    const val SCALE_DOWN_IMAGE_AREA = 300000.0
    const val UPLOAD_IMAGE_TIMER_AT_10 = 10
    const val UPLOAD_IMAGE_TIMER_AT_15 = 15
    const val UPLOAD_IMAGE_TIMER_AT_30 = 30
    const val UPLOAD_IMAGE_TIMER_AT_45 = 45

    // internal or external teacher
    const val INTERNAL_TEACHER = "internal"

    //    const val YOUTUBE_URL: String = "https://www.youtube.com/results?search_query="
    const val CYMATH_URL: String = "https://www.cymath.com/answer?q="

    const val SG_SCALE_DOWN_IMAGE_AREA = 600000.0
    const val CREATE_POST_SCALE_DOWN_IMAGE_AREA = 600000.0

    // PROFILE
    const val NAVIGATE_TO_SELECT_CLASS_FROM_PROFILE = "navigate_class_from_profile"

    const val ITEM_POSITION = "item_position"

    const val EXTERNAL_URL = "external_url"

    const val PACKAGE_NAME: String = "com.doubtnutapp"
    const val WHATSAPP_PACKAGE_NAME = "com.whatsapp"
    const val WHATSAPP_FOR_BUSINESS_PACKAGE_NAME = "com.whatsapp.w4b"
    const val TRUECALLER_PACKAGE_NAME = "com.truecaller"

    //region Notification
    const val NOTIFICATION_EVENT = "event"
    const val NOTIFICATION_QID = "qid"
    const val NOTIFICATION_PAGE = "page"
    const val NOTIFICATION_RESOURCE_TYPE = "resource_type"
    //endregion

    const val FROM_LIBRARY = "from_library"

    const val SHOW_TRUE_CALLER_LOGIN = "show_true_caller_login"
    const val SHOW_TRUE_CALLER_MISSED_CALLED_LOGIN = "show_truecaller_missed_call_login"

    const val NOTIFICATION_DATA = "notification_data"
    const val WHATSAPP_LOGIN = "whatsapp_login"

    const val CLASS_CAMERA_DATA = "class_camera_data"

    const val GAID: String = "gaid"
    const val VIDEO_ID = "video_id"

    const val LIBRARY_SCREEN_SELECTED_TAB: String = "library_screen_selected_Tab"
    const val COURSE_ID: String = "course_id"

    const val BADGE_ID: String = "badge_id"
    const val NUDE_DESCRIPTION: String = "nude_description"
    const val IMAGE_URL: String = "image_url"
    const val FEATURE_TYPE: String = "feature_type"
    const val SHARING_MESSAGE: String = "sharing_message"
    const val ACTION_PAGE: String = "action_page"
    const val CHANGE_LANGUAGE = "change_language"
    const val CHANGE_CLASS = "change_class"
    const val UXCAM_PERCENT = 3

    const val BOTTOM_SHEET_COUNT = "bottom_sheet_count"
    const val SHOW_BOTTOM_SHEET = "show_bottom_sheet"
    const val CAMERA_INFO_COUNT = "camera_info_count"
    const val IS_MOENGAGE_UNIQUE_ID_SET = "is_moengage_unique_id_set"
    const val MATCH_PAGE_SOURCE_BACK_PRESS = "back_press"
    const val MATCH_PAGE_SOURCE_HOME = "home"
    const val MATCH_PAGE_SOURCE_LIBRARY = "library"
    const val MATCH_PAGE_SOURCE_FORUM = "forum"
    const val MATCH_PAGE_SOURCE_PROFILE = "profile"
    const val MATCH_PAGE_SOURCE_OTHER = "other"
    const val VIDEO_DISLIKE_SCREEN_TEXT = "text"
    const val VIDEO_DISLIKE_SCREEN_VIDEO = "video"
    const val STICKY_NOTIFICATION = "sticky_notification"
    const val FIREBASE_NOTIFICATION = "firebase_notification"
    const val MATCH_PAGE_NOTIFICATION = "match_page_notif"
    const val MOENGAGE_NOTIFICATION = "moengage_notification"
    const val NOTIFICATION = "notification"
    const val TYPE = "type"
    const val IN_APP_SEARCH = "in-app-search"
    const val SEARCH_ID = "search_id"
    const val SEARCH_QUERY = "search_query"
    const val VOICE_SEARCH = "voice_search"
    const val IN_APP_SEARCH_MIC = "in-app-search-mic"
    const val QUICK_SEARCH = "quick-search"
    const val QUICK_SEARCH_SETTING = "quick-search-setting"
    const val IS_FROM_TOPIC_BOOSTER_SOLUTION = "is_from_topic_booster_solution"
    const val MOENGAGE = "MOENGAGE"
    const val COURSE_DETAILS = "course_details"
    const val FACULTY_ID = "faculty_id"
    const val ECM_ID = "ecm_id"

    const val CLEAR_TASK = "clear_task"
    const val CLEAR_TOP = "clear_top"
    const val DEEPLINK_URI = "deeplink_uri"
    const val COURSE_VIDEO = "course_video"
    const val TYPE_HOMEPAGE_CONTINUE_WATCHING_WIDGET = "homepage_continue_watching"

    const val ADMIN_API_URL = "admin_api_url"
    const val SOCKET_BASE_URL = "socket_base_url"
    const val ADMIN_API_MICRO_URL = "admin_api_micro_url"
    const val SOURCE = "source"
    const val TAG = "tag"
    const val DATE = "date"
    const val CROP_IMAGE_URL = "crop_image_url"
    const val ENABLE_DEBUG_LOG = "enable_debug_log"
    const val MATCH_NOTIFICATION = "match_notification"
    const val MATCH_OCR_NOTIFICATION = "match_ocr_notification"
    const val VIDEO_PAGE_BANNER = "video_page_banner"

    // Counter in shared preference to track first and seventh video of the user
    const val USER_VIDEO_WATCH_COUNTER = "USER_VIDEO_WATCH_COUNTER"

    // Etoos content constants
    const val ETOOS_VIDEO = "video"
    const val ETOOS_FACULTY = "etoos_faculty"
    const val ETOOS_COURSE = "etoos_course"
    const val ETOOS_CHAPTER = "etoos_chapter"
    const val PDF = "pdf"
    const val BOOK = "book"
    const val VIDEO = "video"
    const val TAB_VIDEO = "tab_Video"
    const val ANNOUNCEMENT = "announcement"
    const val IS_INSTALL_EVENT_SENT = "is_install_event_sent"
    const val IS_SUBSCRIBED = "isSubscribed"
    const val DOUBT = "doubt"
    const val IS_VIP = "isVip"
    const val RESOURCE_DATA = "resource_data"
    const val RESOURCE_TYPE = "resource_type"
    const val PLAYER_TYPE = "player_type"
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
    const val AUDIO = "AUDIO"
    const val DYNAMIC_TEXT = "dynamic_text"
    const val PACKAGE_ID = "package_id"
    const val GROUP_ID = "group_id"
    const val ALL_COURSES = "all_courses"
    const val PACKAGE_DETAIL_ID = "package_detail_id"

    const val TRUE_CALLER_WAIT_TIME = "true_caller_wait_time"

    const val GAME_TITLE = "game_title"
    const val GAME_URL = "game_url"
    const val GAME_ID = "game_id"

    const val EXCLUDE_BYJU_VEDANTU_RESULTS_STRING = " -byju's -vedantu -unacademy"
    const val ADD_DOUBTNUT_STRING = " doubtnut"
    const val IS_EMULATOR = "is_emulator"
    const val HAS_PLAY_SERVICE = "has_play_service"
    const val GAME_TOKEN = "game_token"

    const val VIDEO_DATA = "video_data"
    const val PRICE = "price"

    const val IN_APP_MATCH_DIALOG_COUNT = "in_app_match_dialog_count"
    const val MOCK_TEST_SECTION_LIST = "mock_test_section_list"
    const val VARIANT_ID_NEW_SUGGESTER = 412
    const val LIVE_CLASS = "liveclass"

    const val IS_VOD = "is_vod"

    const val CHROME_CUSTOM_TAB = "chrome_custom_tab"

    const val LOGIN_VARIANT = "login_variant"
    const val LOGIN_STUDENT_IMAGES = "login_student_images"
    const val ENABLE_TRUECALLER_VERIFICATION = "enable_truecaller_verification"
    const val ENABLE_MISSED_CALL_VERIFICATION = "enable_missed_call_verification"

    const val LCS_LOGGING_VERSION = "v1"
    const val USC_LOGGING_VERSION = "v7"
    const val NA = "NA"

    const val IAS_BACK_DIALOG_SLP_COUNT = "ias_back_dialog_slp_count"
    const val IAS_BACK_DIALOG_SRP_COUNT = "ias_back_dialog_srp_count"
    const val IAS_BACK_DIALOG_LAST_DATE = "ias_back_dialog_count"
    const val ASKED_QUESTION_ID = "asked_question_id"

    const val LIVE_STATUS_LIVE = 1
    const val LIVE_STATUS_SCHEDULED = 2
    const val LIVE_STATUS_ENDED = 3

    const val ITEMS = "items"

    const val USER_COMMUNITY_BAN = "user_community_ban"
    const val USER_UNABN_REQUEST_STATE = "user_unban_request_state"

    const val SELECTED_CLASS_NAME = "selected_class_name"
    const val SELECTED_CLASS_NO = "selected_class_no"
    const val EVENT_NAME = "event_name"

    const val VIDEO_TIME = "video_time"
    const val VIDEO_ENGAGEMENT_TIME = "video_engagement_time"
    const val RETRY_DELAY_TIME = 1000L
    const val REQUEST_CODE_UPLOAD_ASK_IMAGE = 1000
    const val REQUEST_CODE_ASK_IMAGE_SEARCH = 1001
    const val REQUEST_CODE_ASK_TEXT_SEARCH = 1002
    const val REQUEST_CODE_GET_SIGNED_URL = 1003
    const val REQUEST_CODE_OCR_EDIT = 1004
    const val REQUEST_CODE_FILTER_RESULT = 1005

    // region Match page language personalisation
    const val LANGUAGES = "languages"
    //endregion

    const val SIGN_UP = "sign_up"

    const val CURRENT_STATE = "current_state"

    const val STUDY_PLAN = "study_plan"
    const val CHAPTER_DETAIL = "chapter_detail"
    const val TABBED_VIEW = "tab_viewed"
    const val NEXT_CHAPTER = "next_chapter"

    const val IS_LOGIN_BACK_PRESS_DIALOG_SHOWN = "IS_BACK_PRESS_DIALOG_SHOWN"

    const val READ_STORAGE_DENIED_ON_GALLERY_CLICK = "read_storage_denied_on_gallery_click"

    const val LANGUAGE_SCREEN = "LANGUAGE_SCREEN"
    const val DN_SHORTS_SCREEN = "DN_SHORTS_SCREEN"
    const val SCREEN_NAME = "screen_name"
    const val REQUEST_CODE_GET_LANGUAGE = 1005

    const val LOCALE = "locale"

    const val KNOW_MORE = "Know more"
    const val SHOW_LESS = "Show less"

    const val LIVE_CLASS_SIMILAR_VIDEO_PAGE = "live_class_similar_video_page"
    const val MPVP = "mpvp"

    const val LANGUAGE = "language"
    const val VIDEO_BOTTOM_VIEW_LIVE_CLASS = "LIVE_CLASS"

    const val IS_PIN_SET = "IS_PIN_SET"
    const val PIN = "PIN"
    const val NO_OF_CHANGE_PIN_DIALOG_SHOWN = "NO_OF_CHANGE_PIN_DIALOG_SHOWN"
    const val MAX_NO_OF_CHANGE_PIN_DIALOG = 3
    const val NUDGE_COURSE_COUNT = "nudge_course_count"
    const val NUDGE_BUNDLE_COUNT = "nudge_bundle_count"
    const val NUDGE_CHECKOUT_COUNT = "nudge_checkout_count"
    const val NUDGE_ID_BUNDLE = "nudge_id_bundle"
    const val NUDGE_ID_CHECKOUT = "nudge_id_checkout"
    const val NUDGE_ID_COURSE = "nudge_id_course"

    const val FEED_TRIGGER = "feed_trigger"
    const val CLUB_ACTION = "club_action"
    const val COMMENT_COUNT: String = "comment_count"
    const val DATA = "data"
    const val AD_DATA = "ad_data"
    const val MUTE_POST = "mute"
    const val NOTIFICATION_ID = "notification_id"
    const val COMMENT = "Comment"
    const val COMMENT_ID = "comment_id"
    const val MUTE = "Mute"

    const val IN_APP_SEARCH_SOURCE = "IAS"
    const val IN_APP_SEARCH_TAB_LIVE_CLASS = "live_class"
    const val IN_APP_SEARCH_TAB_VIP = "live_class_vip"
    const val DEMO_QUESTION_URL = "demo_question_url"

    const val CAUSE = "cause"
    const val MESSAGE = "message"
    const val IS_API_ERROR = "is_api_error"
    const val UNAUTHORIZED_USER_ERROR = "unauthorized_user_error"

    const val ORIENTATION = "orientation"

    // OCR Edit popup source
    const val EDIT_BUTTON = "edit_button"
    const val BACKPRESS = "backpress"
    const val BLUR = "blur"
    const val HANDWRITTEN = "handwritten"
    const val OCR_EDIT_COACHMARK_SHOWN = "ocr_edit_coachmark_shown"

    const val VIDEO_PLAY_TIME = "video_play_time"

    const val POSITION = "position"

    // App exit dialog
    const val ACTIVITY = "activity"
    const val APP_OPEN = "app_open"
    const val RESET = "reset"
    const val WIDGET_NAME = "widget_name"
    const val EXPERIMENT = "experiment"

    // Camera screen navigation
    const val MOVE_TO = "move_to"

    // SRP PDF Banner
    const val SRP_PDF = "srp_pdf"

    // Home Page V2
    const val TAB_KEY = "tab_key"

    const val WIDGET_TYPE = "widget_type"
    const val POST_PURCHASE_SESSION_COUNT = "post_purchase_session_count"
    const val FIRST_PAGE_DEEPLINK = "first_page_deeplink"
    const val POPUP_DEEPLINK = "popup_deeplink"
    const val DEFAULT_ONBOARDING_DEEPLINK = "default_onboarding_deeplink"
    const val PURCHASED_ASSORTMENT_ID = "purchased_assortment_id"
    const val SHOULD_SHOW_MY_COURSE = "should_show_my_course"
    const val SHOULD_SHOW_COURSE_SELECTION = "should_show_course_selection"
    const val IS_COURSE_SELECTION_SHOWN = "is_course_selection_shown"
    const val SESSION_COUNT = "session_count"
    const val TOOLTIP_ID = "id"

    const val CELEB = "celeb"

    // Tyd screen
    const val VOICE_SEARCH_FIRST = 1
    const val TEXT_SEARCH_FIRST = 2
    const val ONLY_TEXT_SEARCH = -1

    // Quiz popup 8 pm
    const val QUIZ_CURRENT_DAY = "quiz_current_day"
    const val QUIZ_LAST_SHOWN = "last_quiz_shown"

    // Topic Booster Gamification
    const val QUESTIONS_ATTEMPTED = "questions_attempted"
    const val TOTAL_QUESTIONS = "total_questions"
    const val USER_WON = "user_won"
    const val USER_CORRECT_ANSWERS = "user_correct_answers"
    const val GAME_VARIANT_ID = "game_variant_id"

    const val COMMENT_TYPE_REPLY = "comment_type_reply"
    const val COMMENT_TYPE_DOUBT = "comment_type_doubt"

    // Home page API data caching
    const val HOME_PAGE_DATA_INVALIDATED = "home_page_data_invalidated"
    const val HOME_PAGE_DATA = "home_page_data"
    const val LAST_BUILD_VERSION_CODE = "last_build_version_code"
    const val LAST_MARKED_DAY = "last_marked_attendance"
    const val REWARD_NOTIFICATION_TITLE = "reward_notification_title"
    const val REWARD_NOTIFICATION_DESCRIPTION = "reward_notification_description"
    const val REWARD_POPUP_DATA_AFTER_MARK_ATTENDANCE = "reward_popup_data_after_mark_attendance"
    const val UNSCRATCHED_CARD_COUNT = "unscratched_card_count"
    const val IS_FIRST_REWARD_SHOWN = "is_first_reward"
    const val IS_FIRST_UNSCRATCHED_SHOWN = "is_first_unscratch_reward"
    const val CURRENT_LEVEL = "current_level"
    const val CURRENT_DAY = "current_day"
    const val REWARD_LEVEL = "reward_level"
    const val REWARD_TYPE = "reward_type"
    const val ATTENDANCE_WIDGET_USER_INTERACTION_DONE = "attendance_widget_user_interaction_done"
    const val ATTENDANCE_MARKED_FROM_REWARD_PAGE = "attendance_marked_from_reward_page"

    const val TAB = "tab"
    const val PAID_ASSORTMENT_IDS = "paid_assortment_ids"
    const val COURSE_STICKY_NOTIFICATION_BUTTON = "course_sticky_notification_button"

    // Conviva
    const val NETWORK_STATE = "networkState"

    // Study Dost
    const val STUDY_DOST_LEVEL = "study_dost_level"
    const val STUDY_DOST_IMAGE = "study_dost_image"
    const val STUDY_DOST_DESCRIPTION = "study_dost_description"
    const val STUDY_DOST_CTA_TEXT = "study_dost_cta_text"
    const val STUDY_DOST_UNREAD_COUNT = "study_dost_unread_count"
    const val STUDY_DOST_DEEPLINK = "study_dost_deeplink"
    const val IGNORE_STUDY_DOST = "ignore_study_dost"
    const val IS_STUDY_DOST_CHAT_STARTED = "is_study_dost_chat_started"

    const val PREF_CHAT_START_TIME = "chat_start_time"
    const val CATEGORY_ID: String = "category_id"
    const val MY_COURSE: String = "my_courses_tab"
    const val SELECTED_CATEGORY_ID = "selected_category_id"
    const val SELECTED_ASSORTMENT_ID = "selected_assortment_id"

    const val GOAL_ID = "goal_id"
    const val GOAL_NUMBER = "goal_number"
    const val IS_DONE = "is_done"
    const val TOPIC = "topic"
    const val TOPICS_COUNT = "topics_count"
    const val IS_PREVIOUS = "is_previous"

    const val LIFECYCLE_ON_CREATE = "lifecycle_on_create"
    const val LIFECYCLE_ON_START = "lifecycle_on_start"
    const val LIFECYCLE_ON_RESUME = "lifecycle_on_resume"
    const val LIFECYCLE_ON_STOP = "lifecycle_on_stop"
    const val LIFECYCLE_ON_PAUSE = "lifecycle_on_pause"
    const val LIFECYCLE_ON_DESTROY = "lifecycle_on_destroy"

    // Study Group
    const val WIDGET_DISPLAY_NAME = "widget_display_name"
    const val STUDY_GROUP = "study_group"
    const val STUDY_CHAT = "study_chat"
    const val PAGE_STUDY_GROUP = "STUDYGROUP"
    const val SOURCE_STUDY_GROUP = "STUDY_GROUP"
    const val SG_PERSONAL_CHAT = "sg_personal_chat"
    const val SG_GROUP = "sg_group"

    const val DOUBT_FEED_REFRESH = "doubt_feed_refresh"
    const val STATE = "state"

    const val NUDGE_ID = "nudge_id"
    const val NUDGE_TYPE = "nudge_type"

    // Remove ClassBoardExamWidget from home
    const val REMOVE_CLASS_EXAM_BOARD_WIDGET = "remove_class_exam_board_widget"

    // MultiSelectFilterWidgetV2
    const val SHOW_MULTI_SELECT_FILTER_WIDGET_V2_SCROLL_ANIM =
        "show_multi_select_filter_widget_v_2_scroll_anim"

    // Bookmark Doubt
    const val IS_ACCESS_DOUBT_BOOKMARK_UI_SHOWN = "is_access_doubt_bookmark_ui_shown"

    // Course Change
    const val POPUP_TYPE = "type"
    const val SELECTED_ASSORTMENT = "selected_assortment"
    const val SOURCE_HOME = "home"
    const val SOURCE_ALL_COURSES = "all_courses"
    const val SOURCE_COURSE_CATEGORY = "course_category"
    const val SOURCE_COURSE_DETAIL = "course_detail"
    const val SOURCE_PAYMENT_FAILURE = "payment_failure"

    // PrePurchaseCallingCard
    const val TITLE_PROBLEM_SEARCH = "title_problem_search"
    const val SUBTITLE_PROBLEM_SEARCH = "subtitle_problem_search"
    const val TITLE_PROBLEM_PURCHASE = "title_problem_purchase"
    const val SUBTITLE_PROBLEM_PURCHASE = "subtitle_problem_purchase"
    const val TITLE_PAYMENT_FAILURE = "title_payment_failure"
    const val SUBTITLE_PAYMENT_FAILURE = "subtitle_payment_failure"

    const val CALLING_CARD_TITLE_TEXT_SIZE = "calling_card_title_text_size"
    const val CALLING_CARD_TITLE_TEXT_COLOR = "calling_card_title_text_color"
    const val CALLING_CARD_SUBTITLE_TEXT_SIZE = "calling_card_subtitle_text_size"
    const val CALLING_CARD_SUBTITLE_TEXT_COLOR = "calling_card_subtitle_text_color"
    const val CALLING_CARD_ACTION = "calling_card_action"
    const val CALLING_CARD_ACTION_TEXT_SIZE = "calling_card_action_text_size"
    const val CALLING_CARD_ACTION_TEXT_COLOR = "calling_card_action_text_color"
    const val CALLING_CARD_ACTION_IMAGE_URL = "calling_card_action_image_url"
    const val CALLING_CARD_IMAGE_URL = "calling_card_image_url"

    const val CHAT_DEEPLINK = "chat_deeplink"
    const val CALLBACK_DEEPLINK = "callback_deeplink"
    const val FLAG_ID = "flag_id"

    const val CALLBACK_BTN_SHOW = "callback_btn_show"
    const val CHAT_BTN_SHOW = "chat_btn_show"

    const val ANONYMOUS_LOGIN_TYPE = "anonymous_login_type"
    const val CR_LAST_VISITED_TIME = "cr_last_visit_time"
    const val CR_TOOLTIP_SHOWN_ONCE = "cr_tooltip_shown_once"
    const val QUESTION_ASK_COUNT = "question_ask_count"
    const val GUEST_LOGIN = "guest_login"
    const val ENABLE_DEEPLINK_GUEST_LOGIN = "enable_deeplink_guest_login"

    const val IS_INSTALL_REFERRER_LOGGED = "is_install_referrer_logged"

    // Doubt feed v2
    const val DG_CTA_AFTER_GOAL_COMPLETE = "cta_after_goal_complete"
    const val POPUP = "popup"
    const val HAMBURGER = "hamburger"
    const val DG_NEW_USER = "dg_new_user"
    const val BENEFITS_POPUP_SHOW_COUNT = "benefits_popup_show_count"
    const val MAX_BENEFITS_POPUP_SHOW_COUNT = 3
    const val BOTTOM_SHEET = "bottom_sheet"

    const val TYPE_SHARE = "SHARE"
    const val TYPE_DOWNLOAD = "DOWNLOAD"

    const val LAST_DOUBT_FEED_BOTTOM_SHEET_TOPIC = "last_doubt_feed_bottom_sheet_topic"
    const val HAMBURGER_WHATSAPP_TEXT = "hamburger_whatsapp_text"
    const val HAMBURGER_WHATSAPP_ICON_URL = "hamburger_whatsapp_icon_url"
    const val HAMBURGER_WHATSAPP_DEEPLINK = "hamburger_whatsapp_deeplink"
    const val PROFILE_WHATSAPP_TEXT = "profile_whatsapp_text"
    const val PROFILE_WHATSAPP_ICON_URL = "profile_whatsapp_icon_url"
    const val PROFILE_WHATSAPP_DEEPLINK = "profile_whatsapp_deeplink"

    const val DICTIONARY_DEEPLINK = "dictionary_deeplink"
    const val DICTIONARY_ICON_URL = "dictionary_icon_url"
    const val DICTIONARY_TEXT = "dictionary_text"

    const val DEFAULT_DICTIONARY_DEEPLINK = "doubtnutapp://dictionary"
    const val DEFAULT_DICTIONARY_ICON_URL =
        "https://d10lpgp6xz60nq.cloudfront.net/dictionary/dictionary-icon.webp"
    const val DEFAULT_DICTIONARY_TEXT = "Dictionary"

    const val WIDGET_ID = "widget_id"
    const val WIDGET_CLICK_SOURCE = "widget_click_source"
    const val WIDGET_PARENT_TOP_ICON_2 = "widget_parent_top_icon_2"
    const val WIDGET_TOP_100_QUESTIONS = "widget_top_100_questions"
    const val WIDGET_REVISION_CORNER_BANNER = "widget_revision_corner_banner"
    const val WIDGET_FORMULA_SHEET = "widget_formula_sheet"
    const val EXAM_TYPE = "exam_type"
    const val RULE_ID = "rule_id"
    const val DEFAULT_RULE_ID = -1
    const val REVISION_CORNER = "revision_corner"
    const val SHOULD_SHOW_FREE_CLASS = "should_show_free_class"
    const val SHOW_TIMETABLE = "show_timetable"
    const val HAS_UPI = "has_upi"

    const val TEACHER_ID = "teacher_id"
    const val TEACHER_TYPE = "teacher_type"
    const val CHANNEL_NAME = "channel_name"
    const val TAB_FILTER = "tab_filter"
    const val SUB_FILTER = "sub_filter"
    const val CONTENT_FILTER = "content_filter"

    const val IS_MATH_WEB_VIEW_WARMED_UP = "is_math_web_view_warmed_up"

    // Study timer
    const val MEDIA_PLAYER_STOPPED = 0
    const val MEDIA_PLAYER_PREPARING = 1
    const val MEDIA_PLAYER_PAUSED = 2
    const val MEDIA_PLAYER_PLAYING = 3

    const val TIMER_STATUS_STOPPED = 0
    const val TIMER_STATUS_RUNNING = 1
    const val TIMER_STATUS_PAUSED = 2
    const val TIMER_STATUS_COMPLETED = 3
    const val TIMER_STATUS_START_TIMER = 4

    const val ORIENTATION_TYPE_GRID = 1
    const val ORIENTATION_TYPE_HORIZONTAL_LIST = 2
    const val ORIENTATION_TYPE_VERTICAL_LIST = 3
    const val AUTH_PAGE_OPEN_COUNT = "auth_page_open_count"
    const val VIDEO_PAGE_EVENT_SENT_DAY = "video_page_event_sent_day"

    // Dnr Region start
    const val DNR_TOOLTIP_POPUP = "dnr_tooltip_popup"
    // Dnr Region end

    const val TYPE_DOUBTS = "doubts"

    //AUDIO TOOLTIP
    const val SCREEN_CAMERA = "camera"
    const val SCREEN_CROP = "crop"
    const val SCREEN_LOADING = "loading"
    const val SCREEN_MATCH_PAGE = "match_page"
    const val SCREEN_CAMERA_RETURN = "camera_return"

    const val SCREEN_CAMERA_SESSION = "camera_session"
    const val SCREEN_CROP_SESSION = "crop_session"
    const val SCREEN_LOADING_SESSION = "loading_session"
    const val SCREEN_MATCH_PAGE_SESSION = "match_page_session"
    const val SCREEN_CAMERA_RETURN_SESSION = "camera_return_session"

    const val CAMERA_AUDIO_TOOL_TIP_DATA = "camera_audio_tool_tip"
    const val USER_BACK_TO_CAMERA_PAGE = "is_back_to_camera"

    const val PROFILE_PRACTICE_ENGLISH_TEXT = "profile_practice_english_text"
    const val PROFILE_PRACTICE_ENGLISH_ICON_URL = "profile_practice_english_icon_url"
    const val PROFILE_PRACTICE_ENGLISH_DEEPLINK = "profile_practice_english_deeplink"

    const val AUDIO_URL = "audio_url"
    const val MUTE_TEXT = "mute_text"
    const val IMAGE_URL_MUTE = "image_url_mute"
    const val IMAGE_URL_UNMUTE = "image_url_unmute"

    const val DEFAULT_ONLINE_CLASS_TAB_TAG = "default_online_class_tab_tag"
    const val PDF_VIEW_GVIEW_URL = "https://docs.google.com/gview?embedded=true&url="
    const val USER_JOURNEY = "user_journey"
    const val SG_IMAGE_AUTO_DOWNLOAD = "sg_image_auto_download"
    const val P2P_IMAGE_AUTO_DOWNLOAD = "p2p_image_auto_download"
    const val GMAIL_VERIFIED = "gmail_verified"
    const val IS_PREMIUM = "is_premium"
    const val IS_RTMP = "is_rtmp"
}
