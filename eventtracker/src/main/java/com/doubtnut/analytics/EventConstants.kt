package com.doubtnut.analytics

import com.doubtnut.core.constant.CoreEventConstants

object EventConstants {
    const val EVENT_NOTIFICATION_VIEW = "view_notification"
    const val EVENT_NOTIFICATION_CLICK = "click_notification"

    const val EVENT_NAME_SCREEN_STATE_SPLASH = "SplashStateScreen"
    const val EVENT_NAME_GET_LANGUAGE_FAILURE = "OnBoardingGetLanguageApiFailure"
    const val EVENT_NAME_GET_LANGUAGE_API_ERROR = "OnBoardingGetLanguageApiError"
    const val EVENT_NAME_GET_LANGUAGE_BAD_REQUEST = "OnBoardingGetLanguageBadRequest"
    const val EVENT_NAME_UPDATE_LANGUAGE_FAILURE = "OnBoardingUpdateLanguageApiFailure"
    const val EVENT_NAME_UPDATE_LANGUAGE_API_ERROR = "OnBoardingUpdateLanguageApiError"
    const val EVENT_NAME_UPDATE_LANGUAGE_BAD_REQUEST = "OnBoardingUpdateLanguageBadRequest"
    const val EVENT_NAME_SCREEN_STATE_LANGUAGE = "LanguageStateScreen"
    const val EVENT_NAME_SCREEN_STATE_LANGUAGE_CLICK = "LanguageItemClick"
    const val EVENT_NAME_SCREEN_STATE_CLASS = "ClassStateScreen"

    const val EVENT_NAME_GET_CLASS_FAILURE = "OnBoardingGetClassApiFailure"
    const val EVENT_NAME_GET_CLASS_API_ERROR = "OnBoardingGetClassApiError"
    const val EVENT_NAME_GET_CLASS_Bad_Request = "OnBoardingGetClassBadRequest"

    const val EVENT_NAME_CLASS_CLICK = "ClassItemClick"
    const val EVENT_NAME_CLASS_SSC_CLICK = "ClassSSCItemClick"
    const val EVENT_NAME_CLASS_CHANGE_EDIT_BIO = "class_change_on_edit_bio"

    const val EVENT_NAME_BTN_SEND_OTP_CLICK = "BtnSendOtp"
    const val EVENT_NAME_BTN_VERIFY_OTP_CLICK = "BtnVerifyOtp"
    const val EVENT_NAME_LOGIN_DONE = "LoginDone"
    const val EVENT_NAME_SCREEN_ADAPTER_ATTACH = "AdapterAttach"
    const val EVENT_NAME_SCREEN_ADAPTER_DETACH = "AdapterDetach"
    const val EVENT_NAME_ITEM_ENTITY = "ItemEntity"

    const val EVENT_NAME_SCREEN_STATE_MAIN_ACTIVITY_PAGE = "MainActivityPage"
    const val EVENT_IN_APP_FACET_CLICKED = "inappsearch_facet_clicked"
    const val EVENT_NAME_HOME_IN_BOTTOM_CLICK = "HomeInBottomClick"
    const val EVENT_NAME_APP_SESSION_STARTED = "AppSessionStarted"
    const val BACK_FROM_CAMERA = "back_from_camera_to_home"
    const val AUDIO_PLAYED = "audio_played"
    const val AUDIO_MUTED = "audio_muted"

    const val EVENT_NAME_ASK_IN_BOTTOM_CLICK = "AskInBottomClick"
    const val EVENT_NAME_TOPICS_IN_BOTTOM_CLICK = "TopicsInBottomClick"
    const val EVENT_NAME_LIBRARY_IN_BOTTOM_CLICK = "LibraryInBottomClick"
    const val EVENT_NAME_FORUM_IN_BOTTOM_CLICK = "ForumInBottomClick"
    const val EVENT_NAME_PROFILE_IN_BOTTOM_CLICK = "ProfileInBottomClick"

    const val EVENT_CAMERA_HISTORY_ICON_CLICKED = "CameraHistoryIconClicked"
    const val EVENT_SELFIE_IMAGE = "SelfieImage"

    // question ask history page
    const val EVENT_HISTORY_WATCH_AGAIN = "HistoryWatchAgain"
    const val EVENT_HISTORY_SEARCH_NOW = "HistorySearchNow"

    const val EVENT_NAME_TEXT_SEARCH_BUTTON_CLICKED = "TextSearchButtonClicked"
    const val EVENT_NAME_CAMERA_DUMMY_BUTTON_CLICKED = "CameraDummyButtonClicked"
    const val EVENT_NAME_CAPTURE_IMAGE_BUTTON_CLICKED = "CaptureImageButtonClicked"
    const val EVENT_NAME_CAMERA_CLOSE_BUTTON_CLICKED = "CameraCloseButtonClicked"
    const val EVENT_NAME_CAMERA_FLASH_TURN_ON = "CameraFlashTurnOn"
    const val EVENT_NAME_CAMERA_FLASH_TURN_OFF = "CameraFlashTurnOff"
    const val EVENT_NAME_BACK_FROM_CAMERA = "BackFromCameraPage"
    const val EVENT_NAME_CROP_PAGE = "CropPage"
    const val EVENT_NAME_SUBMIT_FROM_CROP = "SubmitFromCrop"
    const val EVENT_NAME_CLOSE_FROM_CROP = "CloseFromCrop"
    const val EVENT_NAME_FIND_SOLUTION_CLICK = "FindSolutionButtonClick"
    const val EVENT_NAME_FIND_SOLUTION_CLICK_9_13 = "FindSolutionButtonClick_9_13"
    const val EVENT_NAME_FIND_SOLUTION_CLICK_IIT = "FindSolutionButtonClick_IIT"
    const val EVENT_NAME_FIND_SOLUTION_CLICK_NEET = "FindSolutionButtonClick_NEET"
    const val EVENT_NAME_FLASH_SWITCHING = "FlashSwitching"
    const val EVENT_NAME_MATCH_PAGE_VIEW = "MatchPageView"
    const val EVENT_NAME_NO_MATCH_FOUND = "NoMatchFound"
    const val EVENT_NAME_POST_IN_COMMUNITY_CLICK = "FabPostInCommunityClick"
    const val EVENT_NAME_REACH_ASK_PAGE = "ReachAskPage"
    const val EVENT_NAME_DEMO_QUESTION_CLICKED = "DemoQuestionClicked"
    const val EVENT_NAME_DEMO_VIDEO_CLICKED = "DemoVideoClicked"
    const val EVENT_NAME_TITLE = "title"
    const val EVENT_NAME_TAB = "tab"
    const val EVENT_NAME_ID = "id"
    const val EVENT_UDID = "udid"
    const val EVENT_NAME_CAMERA_VERSION = "camera_version"
    const val EVENT_NAME_CAMERA_V2 = "camera_v2"
    const val EVENT_NAME_CROPPER_USED = "CropperUsed"

    const val EVENT_NAME_SETTING_BUTTON_CLICK = "SettingButtonClick"
    const val EVENT_NAME_PAYMENT_HISTORY_BUTTON_CLICK = "dnvip_payment_history_view"
    const val EVENT_NAME_EDIT_PROFILE_CLICK = "EditProfileClick"
    const val EVENT_NAME_PROFILE_IMAGE_CLICK = "clickonprofileImage"
    const val EVENT_NAME_QUIZ = "Quiz"
    const val EVENT_NAME_INVITE_FRIEND = "InviteFriends"

    // SettingPage
    const val EVENT_NAME_SETTING_PAGE = "SettingPage"
    const val EVENT_NAME_RATE_US = "RateUs"
    const val EVENT_NAME_CONTACT_US = "ContactUs"
    const val EVENT_NAME_HOW_TO_USE_DOUBTNUT_YOUTUBE = "HowToUseDoubtnutYoutube"
    const val EVENT_NAME_ABOUT_US = "AboutUs"
    const val EVENT_NAME_TERMS_N_CONDITION = "Terms_N_Conditions"
    const val EVENT_NAME_PRIVACY_POLICY = "PrivacyPolicy"
    const val EVENT_NAME_LOGOUT = "Logout"
    const val EVENT_NAME_LOGIN = "Login"

    // VideoViewPage
    const val EVENT_NAME_QUESTION_DETAILS_COLLAPSE_EXPAND = "QuestionDetailsCollapseExpands"
    const val EVENT_NAME_BACK_FROM_VIDEO_VIEW = "BackFromVideoView"
    const val EVENT_NAME_VIDEO_PLAY_END = "VideoPlayEnd"
    const val EVENT_NAME_VIDEO_PLAY_START = "VideoPlayStart"
    const val EVENT_NAME_VIDEO_PLAY_PAUSE = "VideoPlayPause"
    const val EVENT_NAME_VIDEO_PLAY_BUFFERING = "VideoPlayBuffering"
    const val EVENT_NAME_VIDEO_FULL_SCREEN_BTN = "VideoFullScreenButton"
    const val EVENT_NAME_VIDEO_EXIT_FULL_SCREEN_BTN = "ExitVideoFullScreenButton"

    const val EVENT_NAME_VIDEO_PAGE = "VideoPage"
    const val EVENT_USER_HAS_WATCHED_VIDEO = "UserWatchedVideo"

    // Video Image Summary
    const val EVENT_VIDEO_IMAGE_SUMMARY_VIEW = "video_image_summary_view"
    const val EVENT_VIDEO_IMAGE_SUMMARY_CLICKED = "video_image_summary_clicked"

    // contestActivity , leaderboard page
    const val PAGE_CONTEST_LIST = "ContestListPage"
    const val EVENT_NAME_CONTEST_ITEM_CLICK = "ContestItemClick"
    const val EVENT_NAME_BACK_FROM_CONTEST = "BackFromContestList"

    // chapterPage
    const val PAGE_CHAPTER_FRAGMENT = "ChapterFragmentPage"
    const val EVENT_NAME_CHAPTER_ITEM_CLICK = "ChapterItemClick"
    const val EVENT_NAME_SHOW_CLASS_SHEET = "ShowClassSheet"
    const val EVENT_NAME_SHOW_COURSE_SHEET = "ShowCourseSheet"
    const val PAGE_CHAPTER_ADAPTER = "ChapterAdapter"
    const val PAGE_SELECT_CLASS_DIALOG = "SelectClassDialog"
    const val PAGE_SELECT_COURSE_DIALOG = "SelectCourseDialog"
    const val EVENT_NAME_COURSE_CLICK = "CourseClick"

    // chapterDetailsPage
    const val PAGE_CHAPTER_DETAILS_FRAGMENT = "ChapterDetailsPage"
    const val PAGE_SUBTOPIC_ADAPTER = "SubtopicAdapter"
    const val EVENT_NAME_CHAPTER_DETAILS_FRAGMENT = "EventChapterDetailsPage"

    // MicroConceptsActivity
    const val PAGE_MICRO_CONCEPTS_ACTIVITY = "MicroConceptsActivity"
    const val EVENT_NAME_MICRO_CONCEPTS_PAGE = "MicroConceptPage"
    const val MICRO_CONCEPTS_LIST_ADAPTER = "MicroConceptListAdapter"
    const val EVENT_NAME_MICRO_CONCEPT_ITEM_CLICK = "MicroConceptItemClick"
    const val PAGE_MICRO_CONCEPTS_ADAPTER = "MicroConceptsAdapter"
    const val EVENT_NAME_ITEM_VIEW_ALL_CLICK = "ItemViewAllClick"
    const val EVENT_NAME_MICROCONCEPT_CHIP_CLICK = "MicroConceptChipClick"

    // DailyPrizeActivity
    const val PAGE_DAILY_PRIZE_ACTIVITY = "DailyPrizeActivityPage"

    const val EVENT_NAME_BACK_FROM_DAILY_PRIZE = "BackFromDailyPrize"
    const val EVENT_NAME_RIGHT_SCROLL_LASTDAY = "RightScrollLastDayWinner"
    const val EVENT_NAME_LEFT_SCROLL_LASTDAY = "LeftScrollLastDayWinner"
    const val EVENT_NAME_RIGHT_SCROLL_TODAY = "RightScrollToDayWinner"
    const val EVENT_NAME_LEFT_SCROLL_TODAY = "LeftScrollToDayWinner"
    const val PAGE_TODAY_WINNER_ADAPTER = "TodayWinnerAdapter"
    const val PAGE_LASTDAY_WINNER_ADAPTER = "LastDayWinnerAdapter"

    // Notification
    const val PAGE_NOTIFICATION = "Notification"
    const val NOTIFICATION_RECEIVED = "NotificationReceived"
    const val NOTIFICATION_OPENED = "NotificationOpened"
    const val PAYLOAD_DATA = "PayloadData"

    // LIB PAGE
    const val EVENT_NAME_LIBRARY_HOME_PAGE = "LibraryHomePage"
    const val EVENT_NAME_CLASS_CHANGE_FROM_LIBRARY = "classchangefromlibrary"
    const val EVENT_NAME_SEARCH_ICON_CLICK_LIBRARY = "search_icon_click_library"
    const val EVENT_NAME_SEARCH_ICON_CLICK_IN_LIBRARY = "search_icon_click_in_library"
    const val EVENT_NAME_OPEN_CLASS_PAGE_LIBRARY = "openClasspageFromLibrary"

    // Download n share
    const val EVENT_PRAMA_CLICKED_ITEM_NAME = "ClickedItemName"
    const val PAGE_DOWNLOAD_N_SHARE_ACTIVITY = "DownloadNShareActivityPage"
    const val EVENT_NAME_BOOK_ITEM_CLICK = "BookItemClick"
    const val EVENT_PRAMA_CLOSE_BUTTON_CLICK = "CloseButtonClick"
    const val EVENT_NAME_CLICK_FOR_DOWNLOAD_PDF = "ClickForDownloadPdf"

    const val EVENT_NAME_BOOK_ITEM_NEXT_LEVEL_CLICK = "BookItemNextLevelClick"
    const val PAGE_DOWNLOAD_N_SHARE_ADAPTER = "DownloadNShareAdapter"
    const val PAGE_DOWNLOAD_N_SHARE_LEVEL_ONE = "DownloadNShareLevelOneActivityPage"
    const val PAGE_DOWNLOAD_N_SHARE_LEVEL_TWO = "DownloadNShareLevelTwoActivityPage"

    // Forum Page
    const val PAGE_FEED = "PageFeed"
    const val PAGE_HOME_FEED = "PageHomeFeed"
    const val FEED_ENGAGEMENT = "FeedEngagement"
    const val ENGAGEMENT_TIME = "engagement_time"
    const val MAX_SEEK_TIME = "max_seek_time"
    const val PAGE_PAID_CONTENT_FEED = "PAID_CONTENT_FEED"
    const val EVENT_PAID_CONTENT_BUY_NOW = "feedPaidContentBuyNow"
    const val EVENT_NO_PAID_CONTENT = "NoPaidContentFound"
    const val FEED_PINNED_VIDEO_AUTO_PLAYED = "feed_pinned_video_auto_played"

    // CommentActivity
    const val PAGE_COMMENT_ACTIVITY = "PageCommentActivity"
    const val EVENT_NAME_CHOOSE_IMAGE_FROM_CAMERA = "SelectImageFromCamera"
    const val EVENT_NAME_CHOOSE_IMAGE_FROM_GALLERY = "SelectImageFromGallery"
    const val EVENT_NAME_POST_COMMENT_CLICK = "PostCommentClick"
    const val EVENT_NAME_COMMENT_ICON_LONG_CLICK = "CommentItemLongClick"
    const val EVENT_NAME_COMMENT_ITEM_FULL_VIEW = "CommentItemFullView"
    const val FEED_TYPE = "type"
    const val EVENT_NAME_GET_COMMENT_SUCCESS = "GetCommentsApiSuccess"
    const val EVENT_NAME_GET_COMMENT_BAD_REQUEST = "GetCommentsApiBadRequest"
    const val EVENT_NAME_GET_COMMENT_API_ERROR = "GetCommentsApiError"
    const val EVENT_NAME_GET_COMMENT_API_FAILURE = "GetCommentsApiFailure"

    const val EVENT_NAME_COMMENT_POST_SUCCESSFULL = "CommentPostSuccessFull"
    const val EVENT_NAME_COMMENT_POST_BAD_REQUEST = "CommentPostApiBadRequest"
    const val EVENT_NAME_COMMENT_POST_API_ERROR = "CommentPostApiError"
    const val EVENT_NAME_COMMENT_POST_FAILURE = "CommentPostApiFailure"
    const val EVENT_NAME_COMMENT_MESSAGE_REPORT = "CommentMessageReport"
    const val IS_REPLY = "is_reply"

    // PlayListPage
    const val EVENT_NAME_VIEW_PLAY_LIST_PAGE = "ViewPlayListPage"

    // FeedbackFragment
    const val PAGE_FEEDBACK_FRAGMENT = "FeedbackDialogFragment"
    const val EVENT_NAME_SUBMIT_FEEDBACK = "SubmitFeedback"
    const val EVENT_NAME_CLOSE_FROM_FEEDBACK = "CloseFromFeedback"

    // NETWORK ERROR
    const val EVENT_NAME_NETWORK_ERROR_BTN = "NetworkErrorButton"
    const val PAGE_NETWORK_ERROR_DIALOG = "NetworkErrorDialog"

    // AskQuestionFragment
    const val EVENT_NAME_SUCCESS = "ConvertImageToStringSuccess"

    // TextSearchpage
    const val EVENT_NAME_TEXT_SEARCH_AUTOCOMPLETE_ADAPTER = "TextSearchAutoCompleteAdapter"
    const val PAGE_TEXT_SEARCH = "TextSearchPage"
    const val EVENT_NAME_TEXT_SEARCH_SUGGESTION_CLICK = "TextSearchSuggestionClick"
    const val EVENT_NAME_TEXT_SEARCH_CLOSE_CLICK = "TextSearchCloselick"
    const val EVENT_NAME_TEXT_SEARCH_FIND__SOLUTION_CLICK = "TextSearchFindSolutionlick"

    // most common events
    const val EVENT_NAME_SHARE_CLICK = "ShareClicked"
    const val EVENT_NAME_PLAY_VIDEO_CLICK = "PlayVideoClick"
    const val EVENT_NAME_ADD_VIDEO_CLICK = "AddVideoClick"
    const val EVENT_NAME_DISLIKE_CLICK = "DislikeClick"
    const val EVENT_NAME_LIKE_CLICK = "LikeClick"
    const val EVENT_NAME_REMOVE_FROM_DISLIKE_CLICK = "RemovedFromDislike"
    const val EVENT_NAME_COMMENT_ICON_CLICK = "CommentIconClick"

    // end

    //region EventParameter Key
    const val EVENT_PRAMA_KEY_STUDENT_ID = "StudentId"
    const val EVENT_PARAM_KEY_STUDENT_NAME = "StudentUsername"
    const val EVENT_PRAMA_KEY_NETWORK_CONNECTED = "NetworkState"
    const val EVENT_PRAMA_KEY_SCREEN_NAME = "ScreenName"
    const val EVENT_PRAMA_KEY_CLICKED_ITEM_NAME = "ClickedItemName"

    const val STATE_OPEN = "OPEN"
    const val STATE_CLOSE = "CLOSE"
    const val EVENT_PRAMA_SCREEN_STATE = "ScreenState"
    const val EVENT_PRAMA_API_RESPONSE_STATE = "API_RESPONSE_STATE"
    const val EVENT_PRAMA_API_RESPONSE_FAILLED = "RequestFailed"
    const val EVENT_PRAMA_API_RESPONSE_API_ERROR = "RequestApiError"
    const val EVENT_PRAMA_API_RESPONSE_BAD_REQUEST = "RequestBadRequest"
    const val EVENT_PRAMA_ADAPTER_SCREEN_ITEM = "AdapterItem"

    //endregion

    //region page
    const val PAGE_SPLASH = "SplashPage"
    const val PAGE_LANGUAGE = "LanguagePage"
    const val PAGE_LANGUAGE_ADAPTER = "LanguagePageAdapter"

    const val PAGE_CLASS_ADAPTER = "ClassPageAdapter"

    const val PAGE_CLASS = "ClassPage"
    const val PAGE_MAIN_ACTIVITY = "MainActivityPage"
    const val PAGE_INAPP_SEARCH_ACTIVITY = "PAGE_INAPP_SEARCH_ACTIVITY"
    const val PAGE_VIDEO_VIEW_ACTIVITY = "ViewAnswerActivityPage"

    const val PAGE_MAIN_TOPICS_PAGE = "TopicsPage"
    const val PAGE_MAIN_LIBRARY_PAGE = "LibraryPage"
    const val PAGE_MAIN_ASK_PAGE = "AskPage"
    const val PAGE_MAIN_PROFILE_PAGE = "ProfilePage"
    const val PAGE_FORUM_FEED_PAGE = "ForumFeedPage"
    const val PAGE_MATCH_ACTIVITY = "MatchActivityPage"

    const val NOTIFICATION_OPEN = "NotificationOpen"
    const val PAGE_DEEPLINK_CLICK = "DeeplinkClick"
    const val PAGE_EXTERNAL_URL = "ExternalUrl"

    const val NOTIFICATION_ACTION_CLICK = "NotificationActionClick"
    const val ACTION = "action"
    //endregion

    const val NOTIFICATION_OPEN_TAGGED = "NOTgd"
    const val NOTIFICATION_RECEIVED_TAGGED = "NRTgd"
    const val NOTIFICATION = "notification"
    const val RESOURCE_TYPE = "resource_type"

    const val EVENT_NAME_BANNER_CLICK_WITH_ID_ = "Banner_click_with_id_"

    //region Copy Text event constants
    const val FEED_ENTITY_COPY_TEXT = "FeedEntityCopyText"
    const val FEED_ENTITY_COPY_TEXT_COMMENTS = "FeedEntityCopyText_comments"
    const val PAGE_ERROR_DIALOG = "ErrorDialog"

    // end region

    //region PDF action event name
    const val SCREEN_NAME_MY_PDF = "MyPDFScreen"
    const val EVENT_NAME_MY_PDF_OPEN = "MyPDFOpen"
    const val EVENT_NAME_PDF_SHARE = "PDFShare"
    const val PDF_NAME_AS_PARAMS = "pdfNameAsParams"
    const val VIEW_PDF_FROM_DOWNLOAD_URL = "viewPdfFromDownloadUrl"
    const val VIEW_PDF_FROM_FILE_PATH = "viewPdfFromFilePath"
    const val EVENT_NAME_VIEW_PDF_FROM_NOTIFICATION = "ViewPdfFromNotification"

    const val PDF_VIEWER_ENGAGEMENT_TOTAL_TIME_AS_PARAMS = "pdfViewerEngagementTotalTimeAsParams"
    const val PDF_VIEWER_ENGAGEMENT_TOTAL_TIME = "pdfViewerEngagementTotalTime"

    //endregion

    const val EVENT_NAME_SPLASH_LANGUAGE_STATE_SCREEN = "SplashLanguageStateScreen"
    const val EVENT_NAME_SPLASH_LANGUAGE_ITEM_CLICK = "SplashLanguageItemClick"

    const val EVENT_NAME_SPLASH_CLASS_OPEN = "SplashClassOpen"
    const val EVENT_NAME_SPLASH_LANGUAGE_OPEN = "SplashLanguageOpen"
    const val EVENT_NAME_SPLASH_CLASS_ITEM_CLICK = "SplashClassItemClick"

    //region ViewAnwserActivity events name
    const val HLS_VIDEO_PLAYED = "VideoPlayedHLS"
    const val BLOB_VIDEO_PLAYED = "VideoPlayedBlob"
    const val HLS_FAILURE_VIDEO_PLAYED = "VideoPlayedHLSFailure"
    const val BLOB_FAILURE_BLOB_FAILURE = "VideoPlayedBlobFailure"
    //endregion

    //region WebViewFragment event name
    const val EVENT_NAME_MATCHES_PAGE_CLICK =
        "Matches?PageClick" // here ? is used as place holder here
    const val EVENT_NAME_MATCHES_PAGE = "Matches?Page" // here ? is used as place holder here

    //endregion

    // CAMERA GUIDE ACTIVITY
    const val EVENT_NAME_CAMERA_GUIDE_CLICKED = "IButtonClick"
    const val PAGE_CAMERA_GUIDE_ACTIVITY = "CameraGuideActivity"
    const val EVENT_NAME_GET_CAMERA_GUIDE_FAILURE = "GetCameraGuideFailure"
    const val EVENT_NAME_GET_CAMERA_GUIDE_API_ERROR = "GetCameraGuideApiError"
    const val EVENT_NAME_GET_CAMERA_GUIDE_BAD_REQUEST = "GetCameraGuideBadRequest"
    const val EVENT_NAME_GET_CAMERA_GUIDE_SUCCESS = "GetCameraGuideSuccess"
    const val EVENT_NAME_ASK_DOUBT_FROM_CAMERA_GUIDE = "ask_doubt_from_camera_guide"

    // feed engagement in activities
    const val FEED_COMMENT_PAGE_ENGAGEMENT = "feed_comment_page_engagement"
    const val EVENT_NAME_CLOSE_CAMERA_GUIDE_PAGE = "close_camera_guide_page"
    const val LIVE_CHAT_ENGAGEMENT = "live_chat_engagement"

    // FORMULA SHEET
    const val EVENT_NAME_FORMULA_SHEET_CLICK = "formulaSheetClick"
    const val EVENT_NAME_GET_FORMULA_HOME_CALL_FAILURE = "getFormulaHomeCallFailure"
    const val EVENT_NAME_GET_FORMULA_HOME_CALL_SUCCESS = "getFormulaHomeCallSuccess"
    const val EVENT_NAME_GET_FORMULA_HOME_CALL_API_ERROR = "getFormulaHomeCallApiError"

    const val PAGE_FORMULA_SHEET_GLOBAL_SEARCH_ACTIVITY = "formulaSheetGlobalSearchActivity"

    const val EVENT_NAME_GET_CHAPTER_CALL_FAILURE = "getChapterCallFailure"
    const val EVENT_NAME_GET_CHAPTER_CALL_SUCCESS = "getChapterCallSuccess"
    const val EVENT_NAME_GET_CHAPTER_CALL_API_ERROR = "getChapterCallApiError"

    const val EVENT_NAME_GET_FORMULAS_CALL_FAILURE = "getFormulasFailure"
    const val EVENT_NAME_GET_FORMULAS_CALL_SUCCESS = "getFormulasCallSuccess"
    const val EVENT_NAME_GET_FORMULAS_CALL_API_ERROR = "getFormulasCallApiError"

    const val PAGE_FORMULA_SHEET_HOME_ACTIVITY = "formulaSheetHomeActivity"
    const val PAGE_FORMULA_SHEET_CHAPTER_ACTIVITY = "formulaSheetChapterActivity"
    const val PAGE_FORMULA_SHEET_FORMULAS_ACTIVITY = "formulaSheetFormulasActivity"
    const val EVENT_NAME_FORMULA_SHEET_SUBJECT_CLICK = "formulaSheetSubjectClick"
    const val EVENT_NAME_FORMULA_CHAPTER_CLICK = "formulaSheetChapterClick"
    const val FORMULA_PAGE_ENGAGEMENT_TOTAL_TIME = "formulaPageEngagementTotalTime"
    const val FORMULA_PAGE_ENGAGEMENT_TOTAL_TIME_AS_PARAMS =
        "formulaSheetFormulasEngagementTimeAsParams"

    const val EVENT_NAME_VIEW_MORE_FROM_HAND_WRITTEN_DIALOG = "viewMoreFromHandWrittenDialog"
    const val EVENT_NAME_ASK_AGAIN_FROM_HAND_WRITTEN_DIALOG = "viewMoreFromHandWrittenDialog"
    const val PAGE_HAND_WRITTEN_ERROR_DIALOG = "HandWrittenDialogError"
    const val PAGE_GLOBAL_SEARCH_FORMULAS_ACTIVITY = "SearchFormulasActivity"
    const val EVENT_NAME_ASK_QUESTION_API_FAILURE = "AskQuestionApiFailure"
    const val EVENT_NAME_ASK_QUESTION_API_ERROR = "AskQuestionApiError"

    const val EVENT_NAME_BLUR_DIALOG_SHOWN = "blur_dialog_shown"
    const val EVENT_NAME_BLUR_TAKE_NEW_IMAGE = "blur_take_new_image"
    const val EVENT_NAME_BLUR_VIEW_DEMO = "blur_view_demo"

    // GROUPCHAT
    const val EVENT_NAME_GROUP_CHAT_FROM_NOTIFICATION = "GroupChatFromNotification"
    const val PAGE_GROUP_CHAT_ACTIVITY = "pageGroupChatActivity"
    const val EVENT_NAME_GROUP_CLICK_TO_CHAT = "groupClickToChat"
    const val EVENT_NAME_BACK_FROM_GROUP_CHAT = "backFromGroupChat"
    const val GROUP_CHAT = "groupchat"
    const val CONTENT_TYPE = "content_type"

    const val LIVE_CHAT = "livechat"
    const val EVENT_NAME_POST_COMMENT_IN_LIVE_CHAT_CLICK = "commentInLiveChatClick"
    const val EVENT_NAME_COMMENT_POST_IN_LIVE_CHAT_SUCCESSFULL = "CommentPostInLiveChatSuccessFull"
    const val EVENT_NAME_COMMENT_POST_IN_LIVE_CHAT_BAD_REQUEST =
        "CommentPostInLiveChatApiBadRequest"
    const val EVENT_NAME_COMMENT_POST_IN_LIVE_CHAT_API_ERROR = "CommentPostInLiveChatApiError"
    const val EVENT_NAME_COMMENT_POST_IN_LIVE_CHAT_FAILURE = "CommentPostInLiveChatApiFailure"
    const val PAGE_LIVE_CHAT_ACTIVITY = "pageLiveChatActivity"
    const val EVENT_NAME_BACK_FROM_LIVE_CHAT = "backFromLiveChat"
    const val EVENT_NAME_QUIZ_ALERT_VIEW = "QuizAlertView"
    const val EVENT_NAME_QUIZ_ALERT_SKIP = "QuizAlertSkip"
    const val EVENT_NAME_QUIZ_ALERT_GOTO = "QuizAlertGoTo"

    // STICKERS
    const val EVENT_NAME_DOUBTNUT_STICKERS = "doubtnutStickers"

    // MOCKTEST
    const val EVENT_NAME_MOCK_TEST = "mockTest"

    // Homepage
    const val EVENT_HOMEPAGE_SHARE_PDF = "homepageSharePdf"
    const val EVENT_HOMEPAGE_CRASH_COURSE = "CrashCourse_Gotocourse"
    const val EVENT_HOMEPAGE_CRASH_COURSE_INTRO_VIDEO = "CrashCourse_introvideoclick"

    // QA Widget
    const val EVENT_ITEMS_SHOWN = "items_shown"
    const val EVENT_IMAGE_CLICK = "image_click"
    const val EVENT_DIALOG_CLOSE_CLICK = "dialog_close_click"

    //common - any page opened
    const val PAGE_OPENED = "page_opened"

    // new Home
    const val EVENT_NAME_NEW_HOME_PAGE = "newHomePage"
    const val EVENT_NEW_HOME_ASK_QUESTION_CLICK = "newHomeAskQuestionClick"
    const val EVENT_NEW_HOME_PAGE_CLICK_ACTION = "newHomeClickAction"
    const val EVENT_NEW_HOME_VIEW_ALL = "newHomeViewAll"
    const val EVENT_NEW_HOME_SHARE_CLICK = "newHomeShareClick"
    const val EVENT_NEW_HOME_SCROLL_TO_TOP_CLICK = "newHomeScrollToTopClick"
    const val EVENT_NAME_CLICK_FOR_SELECT_SHARE_ALL_PDF = "clickForSelectShareAllPdf"
    const val EVENT_NAME_CLICK_FOR_SHARE_ALL_PDF = "clickForShareAllPdf"
    const val EVENT_NAME_SELECT_PDF_SHARE_ITEM = "selectPdfShareItem"
    const val EVENT_NAME_UNSELECT_PDF_SHARE_ITEM = "UnSelectPdfShareItem"
    const val EVENT_NAME_TOTAL_PDF_TO_SHARE_COUNT = "totalPdfToShareCount"
    const val EVENT_NAME_SHARE_ALL_PDF = "shareAllPdf"
    const val EVENT_NEW_HOME_ASK_QUESTION_CLICK_WITH_FULL_ACTION_BAR =
        "newHomeAskQuestionClickWithFullActionBar"
    const val EVENT_NAME_HOME_PAGE_CLICK_ACTION = "homeClick_"
    const val EVENT_NAME_HOME_PAGE_CLICK = "homeClick"
    const val EVENT_HOME_FEED_API_ERROR = "homeFeedApiError"
    const val EVENT_BANNER_CLICK = "banner_click"
    const val EVENT_HOME = "home"
    const val EVENT_LIBRARY = "library"
    const val EVENT_LIBRARY_LISTING = "library_listing"

    // CLEVER TAP EVENT
    const val EVENT_NAME_LANGUAGE_PAGE = "language_page"
    const val PARAM_SHOW = "show"
    const val PARAM_SUCCESS = "success"
    const val PARAM_LANGUAGE = "language"
    const val PARAM_CLASS = "class"
    const val PARAM_ALLOW = "allow"

    const val EVENT_NAME_CLICK_LANGUAGE = "click_language"
    //

    const val EVENT_NAME_CLASS_PAGE = "class_page"
    const val EVENT_NAME_CLICK_CLASS = "click_class"

    const val EVENT_NAME_HOME_PAGE_VIEW = "home_page_view"
    const val PARAM_IS_USER_CLICK = "is_user_click"
    const val PARAM_NAME = "name"
    const val EVENT_NAME_HOME_PAGE_BOTTOM_SHEET = "home_page_bottom_sheet"
    const val SOURCE_BOTTOM_NAV = "bottom_nav"

    const val PARAM_IS_EXPANDED = "is_expanded"

    const val EVENT_NAME_PDF_OPEN = "pdf_open"
    const val EVENT_NAME_PDF_SHARE_CLEVERTAP = "pdf_share"
    const val EVENT_NAME_PATH = "path_name"
    const val EVENT_NAME_PDF_DOWNLOAD = "pdf_download"
    const val EVENT_NAME_PACKAGE = "package_name"
    const val EVENT_NAME_ASK_QUESTION_CAMERA_BUTTON = "ask_question_camera_button"
    const val PARAM_TIMESTAMP = "timeStamp"
    const val EVENT_NAME_GALLERY_RETRIEVAL = "gallery_image_retrieval"
    const val EVENT_NAME_ASK_QUESTION_TEXT_BUTTON = "ask_question_text_button"
    const val EVENT_NAME_ASK_QUESTION_TEXT_SUBMIT = "ask_question_text_submit"
    const val EVENT_NAME_ASK_QUESTION_SUBMIT_BUTTON_CLICK = "ask_question_submit_button_click"

    const val EVENT_NAME_FIND_SOLUTION_CLICK_CLEVER_TAP = "find_solution_button click"
    const val EVENT_NAME_CAMERA_PERMISSION = "camera_permission"
    const val PARAM_PARENT_QUESTION_ID = "parent_question_id"
    const val EVENT_NAME_REACHED_MATCH_PAGE = "reached_match_page"

    const val EVENT_NAME_SEARCH_ICON_CLICK = "search_icon_click"
    const val EVENT_NAME_PLAY_FROM_AUTO = "play_from_auto"

    const val EVENT_NAME_CLOSE_FROM_NO_MATCH = "close_from_no_match"
    const val FEEDBACK_LIKE_CLICKED = "feedback_like_click"
    const val EVENT_NAME_ASK_AGAIN_FROM_FEEDBACK = "askAgainFromFeedback"
    const val EVENT_NAME_ASK_IMAGE_VIEW_FULL = "askImageFullView"
    const val EVENT_NAME_OPEN_CLASS_PAGE_HOME = "openClasspageFromHome"
    const val EVENT_NAME_PLAY_VIDEO_FROM_SIMILAR = "playVideoClickedFromSimilar"
    const val EVENT_PROFILE_SUBMIT = "Profile_save_click"
    const val EVENT_NAME_USER_BADGE_CLICK = "userBadgeClick"

    const val EVENT_NAME_USER_ACHIEIVEMENT_PAGE = "UserAchievementFragment"
    const val EVENT_NAME_DAILY_STREAK_CLICK = "dailyStreakClick"
    const val EVENT_NAME_LEADERBOARD_CLICK = "leaderBoardClick"
    const val EVENT_NAME_MY_WALLET_CLICK = "MywalletClick"
    const val EVENT_NAME_POINTS_EARNED_CLICK = "pointsToEarnedClick"
    const val EVENT_NAME_LOGIN_BUTTON_CLICKED = "LoginButtonClicked"
    const val EVENT_NAME_BADGE_SHARE_CLICKED = "badgeShareClick"
    const val EVENT_NAME_BADGE_SCREEN = "badgeScreen"
    const val EVENT_NAME_OTHERS_BADGE_SCREEN = "othersBadgeScreen"
    const val EVENT_NAME_POINTS_SCREEN = "pointsScreen"
    const val EVENT_NAME_OTHERS_PROFILE_CLICK = "othersProfileClick"

    const val EVENT_NAME_GAMIFICATION_POP_CLICK_VIEW_NOW = "gamificationPopupClickViewNow"
    const val BOTTOM_BAR = "BottomBar"
    const val SETTING_MENU_ITEM_CLICKED = "SettingMenuItemClicked"
    const val SEARCH_CLICK_EVENT = "inappsearch_clicksuggestion"
    const val SEARCH_KEYBOARD_EVENT = "inappsearch_click_keyboardsearch"
    const val SEARCH_VOICE_EVENT = "inappsearch_voice"
    const val SEARCH_QUERY_EVENT = "onChangedSearchQuery"
    const val TRENDING_CLICK_EVENT = "inappsearch_trending_history"
    const val TRENDING_CLICK_RECENT = "inappsearch_recent_search"
    const val NO_DATA_EVENT = "inappsearch_noresultfound"
    const val SEARCH_NEW_LANDING_RECENT_SEARCH_CLICKED = "inappsearch_nlpage_recentsearch"
    const val SEARCH_NEW_LANDING_TRENDING_SEARCH_CLICKED = "inappsearch_nlpage_trendingsearch"
    const val SEARCH_NEW_LANDING_MOST_RECENT_DOUBT_CLICKED = "inappsearch_nlpage_recentdoubt"
    const val SEARCH_NEW_LANDING_MOST_WATCHED_DOUBT_CLICKED = "inappsearch_nlpage_mostwatched"
    const val SEARCH_NEW_LANDING_BOOK_CLICKED = "inappsearch_nlpage_books"
    const val SEARCH_NEW_LANDING_PDF_CLICKED = "inappsearch_nlpage_pdfs"
    const val SEARCH_NEW_LANDING_SUBJECT_CLICKED = "inappsearch_toptagclick"
    const val SEARCH_NEW_LANDING_EXAM_PAPER_CLICKED = "inappsearch_nlpage_exampaper"

    const val SEARCH_TRIGGER_TOP_TAGS = "top_tags_clicked"
    const val SEARCH_TRIGGER_TOP_RESULT_SEE_ALL = "top_result_see_all_clicked"
    const val SEARCH_TRIGGER_TOP_RESULT = "top_result_clicked"
    const val SEARCH_TRIGGER_TRENDING_TOPICS = "trending_topics_clicked"
    const val SEARCH_TRIGGER_RECENT_SEARCH = "recent_search_clicked"
    const val SEARCH_TRIGGER_VOICE_SEARCH = "voice_search_clicked"
    const val SEARCH_TRIGGER_DIRECT_SEARCH = "direct_search_clicked"
    const val SEARCH_TRIGGER_SUGGESTION_RECOMMENDATION = "suggestion_recommendation_clicked"
    const val SEARCH_RESULTS_TAB_SWITCH = "result_tab_switched"
    const val SEARCH_RESULT_CLICKED = "search_result_clicked"
    const val EVENT_TRENDING_BOOK_CLICKED = "ias_trending_book_clicked"
    const val EVENT_TRENDING_POPULAR_ON_DOUBTNUT_CLICKED =
        "ias_trending_popular_on_doubtnut_clicked"
    const val EVENT_TRENDING_COURSES_CLICKED = "ias_trending_courses_clicked"
    const val EVENT_MOST_RECENT_DOUBT_CLICKED = "ias_most_recent_doubt_clicked"
    const val SEARCH_TRIGGER_FILTER = " Filter_Sorting_Applied"
    const val EVENT_IAS_ALL_CHAPTER_CLICK = "ias_all_chapter_click"
    const val RESULT_PAGE = "result_page"
    const val HOST_STUDENT_ID = "host_student_id"
    const val OPPONENT_ID = "opponent_id"
    const val RESULT = "result"
    const val QUESTIONS_ATTEMPTED = "questions_attempted"
    const val CORRECT_ANSWERS = "correct_answers"
    const val SEARCH_COURSE_BANNER_CLICKED = "search_course_banner_clicked"

    const val SOURCE = CoreEventConstants.SOURCE
    const val FLAG_ID = "flag_id"

    const val WIDGET = CoreEventConstants.WIDGET
    const val SEARCHED_ITEM = "searched_item"
    const val CLICKED_ITEM = "clicked_item"
    const val CLICKED_POSITION = "clicked_position"
    const val CLICKED_ITEM_TYPE = "clicked_item_type"
    const val CLICKED_ITEM_ID = "clicked_item_id"
    const val SUGGESTER_VARIANT_ID = "suggester_variant_id"

    const val ITEM_POSITION = CoreEventConstants.ITEM_POSITION
    const val TYPE = CoreEventConstants.TYPE
    const val SECTION = "section"
    const val FACET = "facet"
    const val SEARCH_ID = "search_id"
    const val MENU = "menu"
    const val CHANGE_CLASS_FROM_HOME = "classchangefromHome"
    const val CHANGE_CLASS_FROM_LIBRARY = "classchangefromLibrary"
    const val CHANGE_CLASS_FROM_PROFILE = "classchangefromProfile"
    const val CLASS_SELECTED = "ClassSelected"
    const val TOP_ICON_CLICK = "TopIconClick"
    const val ITEM = "item"
    const val CAMERA_BUTTON = "CameraButton"
    const val VIEW_SOURCE = "viewSource"
    const val LIBRARY_TOP_MENU_CLICKED = "LibraryTopMenuClicked"
    const val LIBRARY_PLAYLIST_TOP_MENU_CLICKED = "LibraryPLaylistTopMenuClicked"
    const val MENU_ITEM = "menu_item"
    const val CAMERA = "camera"
    const val GALLERY = "gallery"
    const val SETTING_ITEM_CLICK = "SettingItemClick"
    const val MY_PDF_CLICK = "MyPDFClick"
    const val MY_PLAYLIST_CLICK = "MyplaylistClick"
    const val WATCH_LATER_CLICK = "WatchLaterClick"
    const val VIDEO_WATCHED_CLICK = "VideoWatchedclick"
    const val CONTEST_ITEM_CLICK = "ContestItemClick"
    const val CHANGE_LANGUAGE_CLICK = "Changelanguageclick"
    const val SHARE_AND_SAVE_CLICKED = "ShareAndSaveclick"
    const val SEARCH_QUERY = "SearchQuery"
    const val GLOBAL_SEARCH = "GlobalSearch"
    const val SEARCH_CLICKED = "SearchClicked"
    const val LIKE_VIDEO = "LikeVideo"
    const val DISLIKE_VIDEO = "DisLikeVideo"
    const val COMMENT_VIDEO_CLICK = "CommentVideoClick"
    const val SHARE_VIDEO_CLICK = "ShareVideoClick"
    const val QUESTION_ID = "QuestionId"
    const val ROTATION_ANGLE = "rotation_angle"
    const val QUESTION_ID2 = "question_id"
    const val VIDEO_VIEW_ID = "view_id"
    const val QUESTION_CHAPTER = "QuestionChapter"
    const val QUESTION_CLASS = "QuestionClass"
    const val QUESTION_RESOURCE_TYPE = "QuestionResourceType"
    const val QUESTION_SUBJECT_NAME = "QuestionSubjectName"
    const val CREATE_NEW_PLAYLIST = "CreateNewPlayList"
    const val ADD_TO_PLAYLIST = "AddToPlayList"
    const val ADD_TO_PLAYLIST_CLICK = "AddToPlayListClick"
    const val LIBRARY_ITEM_CLICKED = "LibraryItemClicked"
    const val TOPIC_NAME = "TopicName"
    const val BUTTON = "button"
    const val TOPIC = "topic"
    const val SETTING_PLAYLIST_SELECTION = "SettingPlaylistSelection"
    const val FORMULA_SUBJECT_SELECTED = "FormulaSubjectSelected"
    const val FORMULA_TOPIC_SELECTED = "FormulaTopicSelected"
    const val VIEW_EXISTING_CHEAT_SHEETS = "ViewExistingCheatSheets"
    const val ADD_TO_CHEAT_SHEET_CLICKED = "AddToCheatSheetClicked"
    const val ADD_TO_CHEAT_SHEET_SUCCESS = "AddToCheatSheetSuccessfull"
    const val CHEAT_SHEET_TYPE = "CheatSheetType"
    const val NEW = "new"
    const val EXISTING = "existing"
    const val CHEAT_SHEET_NAME = "cheatSheetName"
    const val SCHOLARSHIP_TEST_ID = "scholarship_test_id"
    const val CHANGE_TEST = "change_test"
    const val TEST_ID = "TestId"
    const val PROGRESS = "progress"
    const val RANK = "rank"
    const val MARKS = "marks"
    const val TOTAL_MARKS = "total_marks"
    const val TIME_STAMP = "time_stamp"
    const val DAILY_QUIZ_TOPIC_SELECTION = "DailyQuizTopicSelection"
    const val SUBMIT_QUIZ = "SubmitQuiz"
    const val VIEW_ANSWERS = "ViewAnswers"
    const val TEST_NAME = "TestName"
    const val LIBRARY_MOCK_TEST_SELECTED = "LibraryMockTestSelected"
    const val PROFILE_MOCK_TEST_SELECTED = "ProfileMockTestSelected"
    const val MOCK_TEST_TOPIC_SELECTED = "MockTestTopicSelected"
    const val SUBJECT = "Subject"
    const val CHAPTER = "Chapter"
    const val VIEW_WINNER = "ViewWinners"
    const val TEST_TYPE = "TestType"
    const val VIEW_ALL_CLICK = "ViewAllClicked"
    const val PDF_LEVEL_SELECTED = "PDFLevelSelected"
    const val PDF_NAME = "PDFName"
    const val PDF_CLICKED = "PDFClicked"
    const val PDF_DOWNLOAD_CLICK = "download_pdf_click"
    const val OTP_SUCCESS = "OtpSuccess"
    const val SOURCE_FEED = "Feed"
    const val RECENT_TOPIC = "recent_topic"
    const val RANDOM_TOPIC = "random_topic"
    const val SPECIFIC_TOPIC = "specific_topic"

    const val EVENT_WHATSAPP_CARD_CLICK = "whatsappCardClick_"
    const val SCREEN_SIMILAR_VIDEO_FRAGMENT = "similarVideoFragment"
    const val HOME_PAGE_TOP = "HomepageTop"
    const val PROFILE_SHARE_CLICK = "Profileshareclick"
    const val LOGIN_CLICK_PROFILE = "LoginClickProfile"
    const val PROFILE = "Profile"
    const val POP_UP_PC_CLICK = "popup_PClock_click"
    const val PC_BANNER_CLICK = "pc_bannerclick"
    const val ONE_TAP_LOGIN = "onetaplogin"
    const val WHATSAPP_OPTIN_CLICK = "whatsappOptinClick"
    const val TRUECALLER = "Truecaller"
    const val WHATSAPP = "Whatsapp"
    const val MOBILE_NO = "Mobile_No"
    const val FIREBASE = "Firebase"
    const val CHECKED = "checked"
    const val USER_CONSENT_CLICK = "user_consent_click"
    const val SMS = "sms"

    const val PAGE_LOGIN = "page_login"

    const val RECEIVED_TRUECALLER_TOKEN = "received_trucaller_token"
    const val RECEIVED_WHATSAPP_TOKEN = "received_whatsapp_token"
    const val GUPSHUP_AGREE_BUTTON_CLICK = "gupshup_agree_button_click"
    const val GUPSHUP_ENGAGEMENT = "gupshupEngagement"
    const val PAGE_GUPSHUP = "gupshup_page"
    const val GUPSHUP_ENGAGEMENT_TOTAL_TIME = "gupshup_engagement_time"

    // quiz
    const val EVENT_NAME_QUIZ_SELECTED = "quiz_selected_"
    const val PAGE_QUIZ_LIST = "pageQuizList"
    const val EVENT_NAME_START_QUIZ = "startQuiz"
    const val PAGE_QUIZ = "pageQuiz"

    //
    const val PARENT = "parent"
    const val LIBRARY_FILTER_CLICK = "library_filter_click"

    const val ASK_SIGNED_URL_FAILURE = "Ask_SignedUrlFailure"
    const val ASK_SIGNED_URL_SUCCESS = "Ask_SignedUrlSuccess"
    const val IMAGE_UPLOAD_SUCCESS = "Image_upload_Success"
    const val FILE_NAME = "file_name"
    const val FILE_SIZE = "file_size"
    const val ASK_RANDOM_IMAGE = "Ask_RandomImage"
    const val ASK_IMAGE_UPLOAD_FAIL = "Ask_ImageUploadFail"
    const val SIZE = "size"
    const val ASK_TAB_CLICK = "Ask_TabClick"

    // Store
    const val STORE_ITEM_BUY_CLICK = "Buy"
    const val STORE_SCREEN_TAB_SELECTED = "RedeemStore"
    const val CONVERT_DN_CASH_CLICK = "ConvertDNcashClick"
    const val MY_ORDER_ITEM_CLICK = "Click"

    // Dictionary
    const val DC_PAGE_VISIT = "dc_page_visit"
    const val DC_BANNER_CLICK = "dc_banner_click"
    const val DC_PROFILE_ICON_CLICK = "dc_profile_icon_click"
    const val DC_WORD_TYPING_START = "dc_word_typing_start"
    const val DC_WORD_SEARCHED = "dc_word_searched"
    const val DC_LANGUAGE_CHANGE_DROPDOWN_CLICK = "dc_language_change_dropdown_click"
    const val DC_LANGUAGE_CHANGED = "dc_language_changed"
    const val DC_RECENT_WORD_CLICKED = "dc_recent_word_clicked"
    const val DC_PRONUNCIATION_ICON_CLICK = "dc_pronunciation_icon_click"
    const val DC_NO_RESULT_FOUND = "dc_no_result_found"

    const val SELECTED_WORD = "selected_word"

    const val EVENT_NAME_VIEWLEVEL_INFO_TOP_CLICK = "ViewLevelInfoTopClick"
    const val EVENT_NAME_POINT_ACTION_CLICK = "PointActionClick"
    const val EVENT_NAME_VIEW_LEVEL_INFO_BOTTOM = "viewLevelInfoBottom"
    const val EVENT_NAME_VIEW_LEVEL_INFO = "viewLevelInfo"
    const val EVENT_NAME_OTHERS_POINT_CLICK = "OthersPointClick"
    const val EVENT_NAME_OTHERS_BADGE_CLICK = "OthersBadgeClick"
    const val EVENT_NAME_OTHERS_DAILY_ATTENDANCE_CLICK = "OthersDailyStreakClick"
    const val EVENT_NAME_LEADER_BOARD_OVERALL_CLICK = "LeaderBoardOverallClick"
    const val EVENT_NAME_VIEW_POINT_HISTORY = "ViewPointHistory"
    const val EVENT_NAME_BUY_STORE_ITEM = "Buy"
    const val EVENT_NAME_OPEN_STORE_ITEM = "Click"
    const val EVENT_NAME_NOT_ENOUGH_DN_CASH_POPUP_VIEW = "NotEnoughDNCashPopupView"
    const val EVENT_NAME_NOT_OTHERS_MY_ACTIVITY_CLICK = "OthersMyActivityClick"
    const val EVENT_NAME_MY_ORDER_CLICK = "MyOrdersClick"
    const val EVENT_NAME_BADGE_SHARE_CLICK = "BadgeShareClick"
    const val EVENT_NAME_MY_ORDER_PROFILE_CLICK = "MyOrdersProfileClick"
    const val EVENT_NAME_EARN_BADGE_CLICK = "ClickEarnBadge"
    const val PROFILE_CLICK = "Profile_click_home_page"
    const val LANGUAGE_CHANGE_SUCCESS = "LanguageChangedSuccess"
    const val PROFILE_MY_BIO_CLICK = "Profile_MyBio_click"
    const val CHANGE_CLASS_FROM_DRAWER = "ClassChangeFromDrawer"
    const val EVENT_NAME_EXAM_SUBMIT_CLICK = "exam_submitted"
    const val EVENT_NAME_BOARD_SUBMIT_CLICK = "board_submitted"
    const val EVENT_NAME_HAMBURGER_CLICK = "hamburger_click"
    const val EVENT_RATING_WIDGET = "viewRatingWidget"
    const val EVENT_RATING_WIDGET_VISIBLE = "RatingWidgetVisible"
    const val PAGE = CoreEventConstants.PAGE
    const val EXAM = "exam"
    const val BOARD = "board"
    const val CRASH_COURSE_SCORE_BANNER_CLICK = "CrashCourse_scorebanner_click"
    const val EVENT_NAME_STUDENT_IIT_CLOSE_BUTTON_CLICK = "scholarship_close_click"
    const val EVENT_NAME_PAY_NOW = "doubt_pass_plan_paynow"
    const val EVENT_NAME_R_PAY_NOW = "doubt_pass_rplan_paynow"
    const val EVENT_NAME_PAY_SUCCESS = "doubt_pass_plan_payment_success"
    const val EVENT_NAME_R_PAY_SUCCESS = "doubt_pass_rplan_payment_success"
    const val EVENT_NAME_PAY_FAILURE = "doubt_pass_plan_payment_failed"
    const val EVENT_NAME_R_PAY_FAILURE = "doubt_pass_rplan_payment_failed"
    const val EVENT_NAME_PLAN_VIEW = "doubt_pass_plan_view"
    const val EVENT_NAME_DN_PLAN_VIEW = "dnvip_plan_view"
    const val EVENT_NAME_HAMBURGER_MENU = "hamburger_menu"
    const val EVENT_NAME_CAMERA_COUNTER = "camera_counter"
    const val EVENT_NAME_CAMERA_VIP = "doubt_camera_vip"
    const val EVENT_NAME_VIDEO_TAG_CLICK = "tag_click"
    const val EVENT_NAME_IN_APP_SEARCH_VOICE = "inappsearch_click_voicesearch"
    const val EVENT_NAME_IN_APP_SEARCH_CAMERA_ICON_CLICK = "inappsearch_camreaiconclick"
    const val EVENT_NAME_ASK_QUESTION_ZOOM_OUT = "Ask_Question_Zoom_out"
    const val EVENT_NAME_APP_OPEN_DN_CATEGORY = "app_open_dn-category"
    const val EVENT_NAME_APP_OPEN_DN = "app_open_dn"
    const val EVENT_NAME_APP_EXIT_DN = "app_exit_dn"
    const val EVENT_NAME_DEFAULT = "default"
    const val EVENT_NAME_DEEPLINK = "deeplink"
    const val EVENT_NAME_RELATED_CONCEPT_CLICK = "related_concept_click"
    const val EVENT_NAME_DN_VIP_FAQ = "doubt_pass_faq"
    const val EVENT_NAME_DN_VIP_FEEDBACK = "doubt_pass_feedback"
    const val EVENT_NAME_DN_VIP_FEEDBACK_SUBMIT = "doubt_pass_feedback_submit"
    const val EVENT_NAME_SUCCESS_PAGE = "success_page"
    const val EVENT_NAME_FAILURE_PAGE = "failure_page"
    const val EVENT_NAME_PLAN_VIEWV2 = "doubt_pass_plan_view_non_vip"
    const val EVENT_NAME_CURRENT_PLAN_VIEW = "doubt_pass_current_plan_view"
    const val EVENT_NAME_CURRENT_PLAN_VIEW_RENEWAL = "doubt_pass_current_plan_view_renewal"
    const val EVENT_NAME_STATUS = "status"
    const val EVENT_NAME_POP_UP_LOCK = "doubt_pass_camera_popup_lock"
    const val EVENT_NAME_POP_UP_ONE_TIME = "doubt_pass_camera_popup_onetime"
    const val EVENT_NAME_CAMERA_MESSAGE_LIMIT = "doubt_pass_camera_message_doubt_left"
    const val EVENT_NAME_CAMERA_MESSAGE_RENEW = "doubt_pass_camera_message_renew"
    const val EVENT_IN_APP_SERACH_EMPTY_BACK = "inappsearch_emptyback"
    const val EVENT_IN_APP_SEARCH_BACK = "inappsearch_back"
    const val EVENT_IN_APP_SERACH_CROSS_ICON = "inappsearch_crossicon"
    const val EVENT_NAME_VIP_PLAN_SELECTION = "vip_plan_selection"
    const val TOPIC_BOOSTER_VISIBLE = "topic_booster_visible"
    const val TOPIC_BOOSTER_ANSWER_SELECTED = "topic_booster_answer_selected"
    const val TOPIC_BOOSTER_VIEW_SOLUTION_TAP = "topic_booster_view_solution_tap"
    const val TOPIC_BOOSTER_ANSWER_CORRECT = "topic_booster_answer_correct"
    const val TOPIC_BOOSTER_SOLUTION_PAGE_LAND = "topic_booster_solution_page_land"
    const val RELATED_CONCEPT_BAR_VISIBLE = "related_concept_bar_visible"
    const val RELATED_CONCEPT_VIDEO_PLAYED = "related_concept_video_played"
    const val RELATED_CONCEPT_TABS_VISIBLE = "related_concept_tabs_visible"
    const val RELATED_CONCEPT_TABS_SELECTED = "related_concept_tab_selected"
    const val RELATED_CONCEPT_DROP_DOWN_OPEN = "related_concept_drop_down_open"
    const val RELATED_CONCEPT_DROP_DOWN_CLOSE = "related_concept_drop_down_close"
    const val RELATED_CONCEPT_SCROLL = "related_concept_scroll"
    const val ASK_FIRST_DOUBT_CLICK = "Ask_first_doubt_click"
    const val PREVENT_MATCH_PAGE_EXIT_VISIBLE = "prevent_match_page_exit_visible"
    const val PREVENT_MATCH_PAGE_EXIT_YES = "prevent_match_page_exit_yes"
    const val PREVENT_MATCH_PAGE_EXIT_NO = "prevent_match_page_exit_no"
    const val ASK_QUESTION_API_ERROR = "ask_question_api_error"
    const val ASK_QUESTION_BACK_PRESS_BEFORE_MATCHES = "ask_question_back_press_before_matches"
    const val ASK_QUESTION_BACK_EXIT_BEFORE_MATCHES = "ask_question_app_exit_before_matches"
    const val ASK_QUESTION_0_5_SEC = "ask_question_0_5_secs"
    const val ASK_QUESTION_5_10_SEC = "ask_question_5_10_secs"
    const val ASK_QUESTION_10_15_SEC = "ask_question_10_15_secs"
    const val ASK_QUESTION_15_20_SEC = "ask_question_15_20_secs"
    const val ASK_QUESTION_MORE_THAN_20_SEC = "ask_question_more_than_20_secs"
    const val APP_OPEN_IMAGE_SHARE = "app_open_image_share"
    const val IMAGE_SHARE_CROP_ACTIVITY = "image_share_crop_activity"
    const val IMAGE_SHARE_LOGIN_STUCK = "image_share_login_stuck"
    const val EVENT_NAME_IN_APP_SEARCH_SUGGESTION_CLICK = "inappsearch_suggestion_click"
    const val EVENT_NAME_IN_APP_TYD_BUTTON_CLICK = "inappsearch_tyd_findsolutionclick"
    const val EVENT_NAME_IN_APP_SEARCH_VERSION = "inappsearch_version"
    const val EVENT_NAME_IN_APP_SEARCH_ICON_CLICK = "inappsearch_arrow_icon_click"
    const val IN_APP_SEARCH_ACTIVITY_STARTED = "InAppSearchActivity_Started"
    const val IN_APP_SEARCH_ACTIVITY_EXIT = "InAppSearchActivity_Exit"
    const val IN_APP_SEARCH_ADVANCED_FILTER_APPLIED = "advanced_filter_applied"
    const val IN_APP_SEARCH_POPULAR_ON_DOUBTNUT_CLICKED = "ias_popular_on_doubtnut_clicked"
    const val CLASS_FILTER = "class_filter"
    const val SUBJECT_FILTER = "subject_filter"
    const val CHAPTER_FILTER = "chapter_filter"
    const val BOOK_NAME_FILTER = "book_name_filter"
    const val AUTHOR_FILTER = "author_filter"
    const val PUBLICATION_FILTER = "publication_filter"
    const val BOARD_FILTER = "board_filter"
    const val EXAM_FILTER = "exam_filter"
    const val TEACHER_FILTER = "teacher_filter"
    const val NEWER_SEARCH_FLOW = "NEWER_SEARCH_FLOW"
    const val OLDER_SEARCH_FLOW = "OLDER_SEARCH_FLOW"
    const val INAPP_SEARCH_ETOOS_RESULT_DISPLAY = "Inappsearch_Etoos_result_display"
    const val INAPP_SEARCH_CLICK_ETOOS_RESULT = "Inappsearch_click_etoos_result"
    const val COUNT_OF_ETOOS_CONTENT = "count_of_etoos_content"
    const val ETOOS_CONTENT_TITLE = "etoos_content_title"

    const val COURSES_FREE_CLICK = "courses_free_click"
    const val COURSES_TEACHER_CLICK = "courses_teacher_click"
    const val COURSES_CARD_FREE_CLICK = "courses_card_free_click"
    const val PLAY_VIDEO_CLICK = "PlayVideoClick"
    const val VIP_PAYMENT_SUCCESS = "vip_payment_success"
    const val VIP_PAYMENT_FAILURE = "vip_payment_failure"
    const val QR_PAYMENT_PAGE_OPEN = "qr_payment_page_open"
    const val QR_RETRY_CLICK = "qr_retry_click"
    const val QR_PAYMENT_SUCCESS = "qr_payment_success"
    const val VIP_PAYMENT_HELP_CLICK = "vip_payment_help_click"
    const val VIP_PAGE_OPEN = "vip_page_open"
    const val HORIZONTAL_VIEW_ENABLED = "horizontal_view_enabled"
    const val HORIZONTAL_SIMILAR_SCROLL = "horizontal_similar_scroll"
    const val HORIZONTAL_SIMILAR_CLICK = "horizontal_similar_click"
    const val HORIZONTAL_SIMILAR_DRAG = "horizontal_similar_drag"

    const val IS_REUPLOAD = "is_reupload"
    const val VIDEO_SCREEN_EXIT_BEFORE_PLAYING = "video_screen_exit_before_playing"
    const val EVENT_NAME_VIDEO_ANALYTICS_V1 = "video_analytics_v1"
    const val VIDEO_ANALYTICS_TOTAL_INIT_TIME_V1 = "initialize_time_v1"
    const val VIDEO_ANALYTICS_BUFFERING_TIME_V1 = "buffering_time_v1"
    const val VIDEO_ANALYTICS_PLAYING_TIME_V1 = "playing_time_v1"
    const val VIDEO_ANALYTICS_TOTAL_PAUSE_TIME_V1 = "pause_time_v1"
    const val VIDEO_ANALYTICS_TOTAL_SPENT_TIME_V1 = "total_spent_time_v1"
    const val VIDEO_ANALYTICS_ACTUAL_DURATION_V1 = "actual_duration_v1"
    const val VIDEO_ANALYTICS_MEDIA_SOURCE_TYPE_V1 = "media_source_type_v1"
    const val VIDEO_ANALYTICS_LAG_RATIO_V1 = "lag_ratio_v1"
    const val URL_INSIDE_TAB_CLICK = "url_inside_tab_click"
    const val BACK_PRESS_INSIDE_TAB_CLICK = "back_press_inside_tab_click"
    const val URL_INSIDE_TAB = "url"
    const val SCREEN_TIME = "screen_time"
    const val API_RESPONSE_TIME = "api_response_time"

    const val NEW_USER_REGISTERED = "new_user_registered"
    const val REACHED_CAMERA_SCREEN = "reached_camera_screen"
    const val REACHED_CROP_SCREEN = "reached_crop_screen"

    const val MATCH_VIDEO_WATCH = "match_video_watch"
    const val QUESTION_ASK = "question_ask"
    const val SELECTED_ROTATED_IMAGE = "selected_rotated_image"
    const val REGISTRATION_LOGIN_COMPLETE = "registration_login_complete"
    const val REGISTRATION_LOGIN_COMPLETION = "registration_login_completion"
    const val SESSION_START = "session_start_dn"
    const val APP_INSTALL = "app_install_dn"

    const val NCERT_RE_ENTRY_HOME = "ncert_re_entry_home"
    const val NCERT_RE_ENTRY_CAMERA = "ncert_re_entry_camera"
    const val NCERT_RE_ENTRY_SIMILAR = "ncert_re_entry_similar"

    const val PURCHASE_P1 = "pur_p1"
    const val PURCHASE_P3 = "pur_p3"

    // Events for Snowplow
    const val SNOWPLOW_VIDEO_VIEW = "video_view"
    const val TRANSACTION_SUCCESS = "transaction_success"
    const val MOCK_TEST_PAGE_VIEW = "mock_test_page_view"
    const val MOCK_TEST_COMPLETION = "mock_test_completion"
    const val LIBRARY_PAGE_VIEW = "library_page_view"
    const val DAILY_QUIZ_PAGE_VIEW = "daily_quiz_page_view"
    const val COURSE_PAGE_VIEW = "course_page_view"
    const val GUPSHUP_PAGE_VIEW = "gupshup_page_view"
    const val GUPSHUP_POST = "gupshup_post"

    const val EVENT_HTTP_ERROR = "http_error"
    const val EVENT_API_REQUEST = "api_request"
    const val EVENT_NETWORK_ERROR = "network_error"
    const val EVENT_UNKNOWN_ERROR = "unknown_error"

    const val REQUEST_TO_REMOVE_BAN_CLICK = "request_to_remove_ban_click"
    const val SUBMIT_FOR_UNBAN_REVIEW_CLICK = "submit_for_unban_review_click"

    const val POST_CREATE_CLICK = "post_create_click"
    const val POST_PAYLOAD_SELECTED = "post_payload_selected"
    const val POST_UPLOAD_INITIATED = "post_upload_initiated"
    const val POST_UPLOAD_SUCCESS = "post_upload_success"
    const val POST_UPLOAD_FAILURE = "post_upload_failure"
    const val SELF_PROFILE_VISIT = "self_profile_visit"
    const val SELF_PROFILE_FAVORITES_VISIT = "self_profile_favorites_visit"
    const val OTHERS_PROFILE_FAVORITES_VISIT = "others_profile_favorites_visit"
    const val SELF_PROFILE_EDIT_BIO_CLICK = "self_profile_edit_bio_click"
    const val OTHERS_PROFILE_VISIT = "others_profile_visit"
    const val OTHERS_PROFILE_FOLLOW_CLICK = "others_profile_follow_click"
    const val OTHERS_PROFILE_UNFOLLOW_CLICK = "others_profile_unfollow_click"
    const val EVENT_NAME_FEED_POST_SHOWN = "feed_post_shown"
    const val EVENT_NAME_FEED_POST_HIDDEN = "feed_post_hidden"
    const val EVENT_NAME_FEED_POST_IMPRESSION = "feed_post_impression"
    const val EVENT_NAME_FEED_POST_VIEW_IMAGE = "feed_post_view_image"
    const val EVENT_NAME_FEED_POST_VIEW_VIDEO = "feed_post_view_video"
    const val EVENT_NAME_FEED_POST_CREATION = "feed_post_creation"
    const val EVENT_NAME_FEED_POST_COMMUNITY_GUIDELINE = "feed_community_guideline"
    const val EVENT_NAME_FEED_POST_LIKE = "feed_post_like"
    const val EVENT_NAME_FEED_POST_UNLIKE = "feed_post_unlike"
    const val EVENT_NAME_FEED_POST_STAR = "feed_post_star"
    const val EVENT_NAME_FEED_POST_UNSTAR = "feed_post_unstar"
    const val EVENT_NAME_FEED_POST_SHARE = "feed_post_share"
    const val EVENT_NAME_FEED_POST_COMMENT_VIEW = "feed_post_comment_view"
    const val EVENT_NAME_FEED_POST_REPORT = "feed_post_report"
    const val EVENT_NAME_FEED_POST_MUTE = "feed_post_mute"
    const val EVENT_NAME_FEED_POST_DELETE = "feed_post_delete"
    const val EVENT_NAME_FEED_POST_FOLLOW = "feed_post_follow"
    const val EVENT_NAME_FEED_POST_UNFOLLOW = "feed_post_unfollow"
    const val EVENT_NAME_FEED_POLL_SUBMIT = "feed_poll_submit"
    const val EVENT_NAME_FEED_POST_IMAGE_OPEN = "feed_post_image_open"
    const val EVENT_NAME_FEED_POST_LINK_CLICK = "feed_post_link_click"
    const val EVENT_NAME_FEED_POST_VIDEO_PLAY = "feed_post_video_play"
    const val EVENT_NAME_FEED_POST_PDF_OPEN = "feed_post_pdf_open"
    const val EVENT_NAME_FEED_POST_PROFILE_CLICK = "feed_post_profile_click"
    const val EVENT_NAME_FEED_POST_TOPIC_CLICK = "feed_post_topic_click"
    const val EVENT_NAME_WIDGET_CLICK = "widget_click"

    const val EVENT_NAME_COMMON_WIDGET_CLICK = CoreEventConstants.EVENT_NAME_COMMON_WIDGET_CLICK

    const val CATEGORY_FEED = "feed"
    const val CATEGORY_WIDGETS = "widgets"

    const val EVENT_FEATURE_CONFIG_STATUS = "feature_config_status"

    const val IN_APP_SEARCH_LANDING_VERSION = "in_app_search_version_landing"
    const val DRAWER_CONTEST_CLICK = "drawer_contest_click"

    const val EVENT_JANE_KAISE_CLICKED = "Jane_Kaise_Clicked"
    const val EVENT_JANE_KAISE_CROSSED = "Jane_Kaise_Crossed"
    const val EVENT_DEMO_QUESTION_CLICKED = "Demo_Question_Clicked"
    const val EVENT_DEMO_ANIMATION_POP_UP_CLICKED = "Demo_Animation_Pop_Up_Clicked"
    const val EVENT_DEMO_ANIMATION_POP_UP_CLOSED = "Demo_Animation_Pop_Up_Closed"
    const val NCERT_RE_ENTRY_HOME_CLICK = "ncert_re_entry_home_click"
    const val NCERT_RE_ENTRY_CAMERA_CLICK = "ncert_re_entry_camera_click"
    const val NCERT_RE_ENTRY_SIMILAR_CLICK = "ncert_re_entry_similar_click"

    const val LOGIN_SUCCESSFUL = "login_successful"
    const val LOGIN_FAIL = "login_fail"
    const val INITIATE_VIP_PLAN_PAGE = "initiate_vip_plan_page"
    const val PAYMENT_CARD = "payment_card"
    const val VARIANT_ID = "variant_id"
    const val MUlTIPLE_PACKAGE = "multiple_package"
    const val EVENT_NAME_MATCH_PAGE_NOTIFICATION_CLICK = "match_page_notification_click"
    const val EVENT_CAMERA_BACK_PRESS_POP_UP_VISIBLE = "back_press_pop_up_visible"
    const val EVENT_CAMERA_BACK_PRESS_POP_UP_AGREED = "back_press_pop_up_agreed"
    const val EVENT_CAMERA_BACK_PRESS_POP_UP_DENIED = "back_press_pop_up_denied"
    const val EVENT_CAMERA_ERROR = "camera_error"
    const val INAPP_SEARCH_CLICK_SEE_ALL = "inappsearch_click_seeall"

    const val EVENT_EASY_READER_WEBVIEWER_OPEN = "easyreader_webview_click"

    const val AMOUNT = "amount"
    const val PACKAGE_ID = "package_id"
    const val EVENT_REGISTRATION_11to13 = "E_Reg_11to13"
    const val DN_REG_JEE_11 = "dn_reg_jee_11"
    const val DN_REG_JEE_12 = "dn_reg_jee_12"
    const val DN_REG_JEE_13 = "dn_reg_jee_13"

    const val MATCH_NOTIFICATION_SHOWN = "match_notification_shown"
    const val CAMERA_CHOOSE_IMAGE_ORIENTATION_DIALOG = "choose_image_orientation_dialog"
    const val WALK_THROUGH_PAGE_OPENED = "walk_through_page_opened"
    const val DURATION_BETWEEN_SEND_AND_RESEND_OTP = "duration_between_send_and_resend_otp"
    const val RESEND_OTP_CLICK = "resend_otp_click"
    const val ASK_API_RESPONSE = "ask_api_response"
    const val BOOK_FEEDBACK_VISIBLE = "book_feedback_visible"
    const val BOOK_FEEDBACK_SUBMITTED = "book_feedback_submitted"
    const val BOOK_FUZZY_RESULTS_SHOWN = "book_fuzzy_results_shown"
    const val BOOK_FUZZY_SUGGESTION_CLICKED = "book_fuzzy_suggestion_clicked"
    const val EVENT_NAME_BOOK_FEEDBACK_CLOSE = "book_feedback_close"
    const val RANDOM_IMAGE_POP_UP_VISIBLE = "random_image_pop_up_visible"
    const val RANDOM_IMAGE_POP_UP_CLICKED_DEMO = "random_image_pop_up_clicked_demo"
    const val RANDOM_IMAGE_POP_UP_CLICKED_ASK_NEW = "random_image_pop_up_clicked_ask_new"
    const val ITEM_COUNT = "count"
    const val DV3D3 = "DV3D3"
    const val D0V7 = "D0V7"
    const val VV_USER = "vv_user"
    const val VV_USER_D3 = "event_v_urer"
    const val DV3D3_APP_OPEN_AFTER_DAY2 = "DV3D3_APP_Open>2d"
    const val EVENT_NAME_MY_BOOKS_CLICK = "my_books_click"

    const val EVENT_NAME_TRUECALLER_VERIFICATION_STATUS = "truecaller_verification_status"
    const val EVENT_NAME_TRUECALLER_NAME_VERIFICATION_STARTED =
        "truecaller_name_verification_started"
    const val EVENT_NAME_TRUECALLER_NAME_POPUP_DISPLAYED = "truecaller_name_pop_up_displayed"
    const val EVENT_NAME_TRUECALLER_NAME_ENTERED = "truecaller_name_entered"
    const val EVENT_NAME_TRUECALLER_LOGIN_POPUP = "show_truecaller_login_popup"
    const val NAME = "name"
    const val EVENT_NAME_TRUECALLER_NAME_SUBMITTED = "truecaller_name_submitted"
    const val STATUS = "status"
    const val EVENT_NAME_CHANGE_NUMBER_CLICKED = "change_number_clicked"
    const val EVENT_NAME_TRUE_CALLER_PROFILE_SHARED_SUCCESS = "truecaller_profile_shared_success"
    const val EVENT_NAME_TRUE_CALLER_PROFILE_SHARED_ERROR = "truecaller_profile_shared_error"
    const val EVENT_NAME_TRUE_CALLER_PERMISSION_ALLOWED = "truecaller_permission_allowed"
    const val EVENT_NAME_TRUE_CALLER_PERMISSION_DENIED = "truecaller_permission_denied"
    const val EVENT_NAME_TRUE_CALLER_NAME_BACK_PRESS = "truecaller_name_back_press"
    const val EVENT_NAME_TRUE_CALLER_CALL_TIME_OUT = "truecaller_call_time_out"
    const val EVENT_NAME_FIREBASE_OTP_TRIGGERED = "firebase_otp_triggered"
    const val EVENT_NAME_TRUECALLER_FAIL_OTP = "truecaller_fail_otp_initiated"
    const val EVENT_NAME_ONBOARDING_COMPLETED = "onboarding_completed"
    const val EVENT_NAME_GUEST_LOGIN_INITIATED = "guest_login_initiated"
    const val EVENT_NAME_LOGIN_INITIATED = "login_initiated"
    const val ERROR_TYPE = "error_type"

    const val WIDGET_TYPE = CoreEventConstants.WIDGET_TYPE

    const val USC_ID = "uscId"
    const val ID = CoreEventConstants.ID
    const val ITEM_ID = "itemId"
    const val CREATED_AT = "createdAt"
    const val EVENT_TYPE = "eventType"
    const val IS_CLICKED = "is_clicked"
    const val SEARCHED_TEXT = "search_text"
    const val IS_SEARCHED = "is_searched"
    const val IS_MATCHED = "is_matched"
    const val USC_LOGGING_VERSION = "logging_version"
    const val USC_TAB_POSITION = "tab_position"
    const val USC_TAB_NAME = "tab_name"
    const val USC_ITEM_POSITION = "item_position"
    const val CLICKED_UNIQUE_IDS = "clicked_unique_ids"

    const val EVENT_VIDEO_CONTENT_SHARE_CLICKED_ON_LINK_SHARE = "clicked_on_link_share"
    const val EVENT_VIDEO_CONTENT_SHARE_CLICKED_ON_DOWNLOAD_AND_SHARE =
        "clicked_on_download_and_share"

    const val EVENT_GAME_SECTION_VIEW = "games_view"
    const val EVENT_GAME_CLICK = "games_click"

    const val EVENT_GAME_ENGAGEMENT = "games_engagement_time"
    const val EVENT_GAME_ENGAGEMENT_TOTAL_TIME = "games_engagement_total_time"
    const val EVENT_GAME_SCREEN = "game"
    const val EVENT_GAME = "game"
    const val EVENT_FILTER_SELECTED = "filter_selected"
    const val EVENT_FILTER_AVAILABLE = "filter_available"
    const val EVENT_ALL_FILTERS_SELECTED = "all_filters_selected"
    const val FACET_TYPE = "facet_type"
    const val EVENT_FACET_SELECTED = "selected_facet"
    const val EVENT_IN_APP_MATCH_DIALOG_VISIBLE = "in_app_match_dialog_visible"
    const val EVENT_IN_APP_MATCH_DIALOG_CLICKED = "in_app_match_dialog_clicked"

    const val EVENT_FIRST_LOGIN = "first"
    const val EVENT_REPEAT_LOGIN = "repeat"
    const val EVENT_PAYMENT = "payment"
    const val LOGIN_STATUS = "login_status"
    const val PAYMENT_FAILED = "pay_failed"
    const val PAYMENT_SUCCESS = "pay_success"
    const val PAYMENT_METHOD = "payment_method"
    const val PAYMENT_STATUS = "payment_status"
    const val PAYMENT_METHOD_IS_RAZERPAY = "is_razorpay"
    const val PAYMENT_METHOD_IS_PAYTM = "is_paytm"
    const val APP_INSTALL_DATE = "app_install_date"
    const val EVENT_VIDEO_WATCHED = "video_watched"
    const val EVENT_LIVE_VIDEO_WATCHED = "live_video_watched"
    const val EVENT_LIVE_VIDEO_REPLAY_WATCHED = "live_video_replay_watched"
    const val VIDEO_CONTENT_TYPE = "content_type"
    const val PAID_VIDEO_CONTENT = "paid_content"
    const val FREE_VIDEO_CONTENT = "free_content"
    const val MOCK_TEST_STARTED = "mock_test_started"
    const val PURCHASE_COUNT = "purchase_count"
    const val TRAIL_STATUS = "trail_status"
    const val TRAIL_ACTIVE_STATUS = "trial_active_status"

    const val LIVE_FREE_VIDEO_CLICK = "live_free_video_click"
    const val REMINDER_CARD_CLICK = "reminder_card"
    const val LIVE_CLASS_TAB_FILTER_CLICK = "live_class_tab_filter_click"
    const val LIVE_CLASS_GO_TO_COURSE_CLICK = "live_class_go_to_course_click"
    const val LIVE_CLASS_FILTER_CLICK = "live_class_filter_click"
    const val LIVE_CLASS_PAGE_OPEN = "live_class_page_open"
    const val NOTIFICATION_ICON_CLICKED = "notification_icon_clicked"
    const val NOTIFICATION_ITEM_CLICKED = "notification_item_clicked"
    const val EXPERIMENT_LOGIN_V3 = "experiment_login3"
    const val LOGIN_PAGE_BOTTOM_SHEET_OPENED = "login_page_bottom_sheet_open"
    const val LOGIN_PAGE_BOTTOM_SHEET_CLOSED = "login_page_bottom_sheet_close"
    const val LANGUAGE_PAGE_OPENED = "language_page_open"
    const val LANGUAGE_ITEM_CLICK = "language_item_click"
    const val COURSE_FILTER_ITEM_CLICK = "course_filter_item_click"
    const val PLAYLIST_ITEM_CLICK = "playlist_item_click"
    const val ALL_COURSE_ITEM_CLICK = "course_all_course_item_click"
    const val IS_LIVE = "is_live"
    const val LIVE_CLASS_EXIT = "live_class_exit"
    const val LIVE_CLASS_POLL = "live_class_poll"
    const val LIVE_CLASS_POLL_CLOSE = "live_class_poll_close"
    const val DURATION = "duration"
    const val DURATION_ACTUAL = "duration_actual"

    const val IN_APP_SEARCH_POPUP_SLP = "inappsearch_popupslp"
    const val IN_APP_SEARCH_POPUP_SLP_RESULT_CLICK = "inappsearch_popupslp_clickresult"
    const val IN_APP_SEARCH_POPUP_SLP_CLOSE = "inappsearch_popupslp_clickcross"

    const val IN_APP_SEARCH_POPUP_SRP = "inappsearch_popupsrp"
    const val IN_APP_SEARCH_POPUP_SRP_RESULT_CLICK = "inappsearch_popupsrp_clickresult"
    const val IN_APP_SEARCH_POPUP_SRP_CLOSE = "inappsearch_popupsrp_clickcross"

    // Onboarding Screen
    const val BACK_PRESS_FROM_ONBOARDING = "back_press_from_onboarding"
    const val ONBOARDING_ITEM_CLICK = "onboarding_item_click"
    const val ONBOARDING_TYPE = "onboarding_type"
    const val ONBOARDING_ITEM_TYPE = "type"
    const val ONBOARDING_ITEM_CODE = "code"
    const val ONBOARDING_ITEM_TITLE = "title"

    // TopperStudyPlan Screen
    const val CATEGORY_PERSONALIZATION = "personalization"
    const val EVENT_NAME_PERSONALIZATION_STUDY_PLAN = "personalization_study_plan"
    const val EVENT_NAME_CHAPTER_DETAIL_PAGE = "ChapterDetailActivity"
    const val ONBOARDING_OTHER_OPTION_CLICK = "onboarding_other_option_click"
    const val TEACHER_NAME = "teacher_name"

    const val SENT_COUNT = "sent_count"
    const val COURSE_NAME = "course_name"

    const val EVENT_NAME_VERIFY_PROFILE_VISIT = "verify_profile_visit"
    const val EVENT_NAME_VERIFY_PROFILE_SUBMITTED = "verify_profile_submitted"
    const val EVENT_NAME_FEED_LIVE_POST_BUTTON_CLICK = "livepost_button_click"
    const val IMAGE_URL = "image_url"

    const val EVENT_MATCH_PAGE_ERROR = "match_page_error"
    const val ERROR_MESSAGE = "error_message"

    // Camera orientation
    const val ORIENTATION = "orientation"

    const val COURSE_FILTER_EXAM_CLICK = "course_filter_exam_click"
    const val COURSE_TYPE = "course_type"
    const val COURSE_CLASS = "course_class"
    const val COURSE_WHY_VIP_PAGE_OPEN = "course_why_vip_page_open"

    // Video Sticky Notification
    const val VIDEO_STICKY_SELECTED = "video_sticky_selected"
    const val VIDEO_STICKY_DESELECTED = "video_sticky_deselected"
    const val VIDEO_STICKY_NOTIFICATION_SHOWN = "video_sticky_notification_shown"
    const val EVENT_VIDEO_STICKY_CLICKED = "video_sticky_clicked"
    const val EVENT_VIDEO_STICKY_SETTINGS_CLICKED = "video_sticky_settings_clicked"

    const val EVENT_HOME_CAROUSELS_SHOW_MORE = "home_carousels_show_more"

    const val PLAN_TYPE_CLICK = "plan_type_click"
    const val PAYMENT_COURSE_TYPE_FILTER_ITEM_CLICK = "payment_course_type_filter_item_click"

    // Match page - youtube results
    const val EVENT_SHOW_MORE_CLICKED = "show_more_clicked"
    const val EVENT_YT_VIDEO_CLICKED = "yt_video_clicked_match_page"
    const val EVENT_YT_RESULTS_VIEWED = "YT_Results_viewed_match_page"
    const val EVENT_YT_RESULTS_NOT_FOUND = "YT_Results_not_found_match_page"
    const val QUESTION_ASKED_ID = "question_asked_id"
    const val STICKY_LINK_CLICK = "sticky_link_click"

    // Match page - Remote Config
    const val EVENT_RETRY_ASK_FLOW_COUNT = "retry_ask_flow_count"
    const val EVENT_ASK_FLOW_TIME_OUT = "ask_flow_time_out"
    const val EVENT_VARIANT_VALUE = "variant_value"

    // Type your doubt events
    const val EVENT_TYD_PAGE_OPEN = "tyd_page_open"
    const val EVENT_TYD_TYPED = "tyd_typed"
    const val EVENT_TYD_SUGGESTION_CLICKED = "tyd_suggestion_clicked"
    const val EVENT_TYD_SUBMIT = "tyd_submit"

    const val EVENT_COMMUNITY_GUIDELINE_CLICK = "community_guideline_click"
    const val EVENT_REPORT_USER_CLICK = "report_user_click"
    const val EVENT_REPORT_USER_CONFIRM_CLICK = "report_user_confirm_click"
    const val EVENT_FOLLOWERS_CLICK = "followers_click"
    const val EVENT_FOLLOWING_CLICK = "following_click"
    const val EVENT_REMOVE_FOLLOWER_CLICK = "remove_follower_click"
    const val EVENT_USER_FOLLOW = "user_follow"
    const val EVENT_USER_UNFOLLOW = "user_unfollow"
    const val APPLICATION_STATE = "ApplicationState"
    const val APPLICATION_ENTERED_FOREGROUND = "entered_foreground"
    const val APPLICATION_ENTERED_BACKGROUND = "entered_background"
    const val APPLICATION_ENTERED_FOREGROUND_ACTION = "EnteredForeground"
    const val APPLICATION_ENTERED_BACKGROUND_ACTION = "EnteredBackground"

    const val QUIZ_ACTIVITY_STATE = "QUIZ_ACTIVITY_STATE"
    const val QUIZ_ACTIVITY_ENTERED_FOREGROUND = "quiz_activity_entered_foreground"
    const val QUIZ_ACTIVITY_ENTERED_BACKGROUND = "quiz_activity_entered_background"
    const val QUIZ_ACTIVITY_ENTERED_FOREGROUND_ACTION = "QuizActivityEnteredForeground"
    const val QUIZ_ACTIVITY_ENTERED_BACKGROUND_ACTION = "QuizActivityEnteredBackground"

    const val VIDEO_TYPE_LIVE_VIDEO = "live_video"
    const val SOURCE_ID = "source_id"
    const val IS_PREMIUM = "is_premium"
    const val LIVE_VIDEO_WATCHED = "live_video_watched"

    const val LIVE_CLASS_QUIZ_SUBMITTED = "live_class_quiz_submitted"
    const val LIVE_CLASS_QUIZ_VIEW = "live_class_quiz_view"
    const val LIVE_CLASS_QUIZ_SUBMIT_STATUS = "live_class_quiz_submit_status"

    // Match page - Autoplay results
    const val MATCH_AUTO_PLAYED = "match_auto_played"
    const val LIVE_CLASS_AUTO_PLAYED = "live_class_auto_played"
    const val ANSWER_ID = "answer_id"
    const val CONTINUE_WATCHING_BUTTON_VISIBLE = "continue_watching_button_visible"
    const val ANSWER_VIDEO = "answer_video"
    const val AUTO_PLAY_TIME = "auto_play_time"
    const val MATCH_AUTO_PLAY_CLICKED = "match_auto_play_clicked"
    const val MATCH_PAGE_AUTO_PLAY_ENABLED = "match_page_auto_play_enabled"
    const val MATCH_PAGE_AUTO_PLAY_SHOWN = "match_page_auto_play_shown"
    const val MATCH_PAGE_AUTO_PLAY_TOGGLE = "match_page_auto_play_toggle"
    const val AUTO_PLAY_TOGGLE_ENABLE = "auto_play_toggle_enabled"
    const val DN_VERSION_CODE = "dn_version_code"
    const val DN_VERSION_NAME = "dn_version_name"
    const val IAS_BOUNCE = "ias_bounce_30s"
    const val IAS_NOT_MATCHED = "ias_not_matched"
    const val IAS_140S = "ias_140s"
    const val IAS_80PER = "ias_80percent"
    const val IAS_SIMILAR_WIDGET_SEARCH = "inappsearch_widgetsearchperformed"

    const val LIVE_CLASS_ALERT = "live_class_alert"

    const val LIVE_CLASS_ALERT_ACTIVITY_STATE = "LIVE_CLASS_ALERT_ACTIVITY_STATE"
    const val LIVE_CLASS_ALERT_ACTIVITY_ENTERED_FOREGROUND =
        "live_class_alert_activity_entered_foreground"
    const val LIVE_CLASS_ALERT_ACTIVITY_ENTERED_BACKGROUND =
        "live_class_alert_activity_entered_background"
    const val LIVE_CLASS_ALERT_ACTIVITY_ENTERED_FOREGROUND_ACTION =
        "LiveClassAlertActivityEnteredForeground"
    const val LIVE_CLASS_ALERT_ACTIVITY_ENTERED_BACKGROUND_ACTION =
        "LiveClassAlertActivityEnteredBackground"

    const val LIVE_CLASS_REMINDER_PAGE_OPEN = "live_class_reminder_page_open"
    const val LIVE_CLASS_REMINDER_JOIN_CLICK = "live_class_reminder_join_click"
    const val LIVE_CLASS_REMINDER_CLOSE_CLICK = "live_class_reminder_close_click"
    const val LIVE_CLASS_REMINDER_OUTSIDE_CLICK = "live_class_reminder_outside_click"

    const val IS_STARTED = "is_started"

    const val VIDEO_PAGE_VIDEO_PAUSE_VERTICAL = "video_pause_vertical"
    const val VIDEO_PAGE_VIDEO_PAUSE_HORIZONTAL = "video_pause_horizontal"
    const val IAS_TOPPERS_CHOICE = "is_toppers_choice"

    const val EVENT_SOURCE_HOME = "home"
    const val SUFFIX_SOURCE_FEED = "feed"
    const val EVENT_SOURCE_ALL_GAME_SCREEN = "allGame"

    const val NCERT_CONTINUE_WATCHING = "ncert_continue_watching"

    // No Internet  Activity
    const val EVENT_NO_INTERNET_RETRY = "no_intenet_retry_button_click"
    const val EVENT_NO_INTERNET_BACK_PRESS = "no_intenet_back_press"
    const val REMINDER = "Reminder"

    const val EVENT_ITEM_CLICK = "ItemClick"
    const val EVENT_SCREEN_PREFIX = "screen_"
    const val EVENT_PARENT_SCREEN_PREFIX = "parent_screen_"
    const val COURSE_ID = "course_id"
    const val ASSORTMENT_ID = "assortment_id"
    const val CATEGORY_ID = "category_id"
    const val PAGE_VIEW = "page_view_"
    const val RECORDED = "recorded"
    const val PLAYER_TYPE = "player_type"
    const val RESOURCE_ID = "resource_id"
    const val SELECT_LIBRARY_TOP_TAB = "select_library_top_tab"
    const val WIDGET_TITLE = "widget_title"
    const val TAB_ID = "tab_id"
    const val TAB_TITLE = "tab_title"
    const val FILTER_TAB = "FilterTab"

    // Match page language personalisation
    const val LANGUAGE_BAR_CLICKED = "language_bar_clicked"

    const val GOOGLE_IN_APP_REVIEW_VIEW = "google_in_app_review_view"
    const val GOOGLE_IN_APP_REVIEW_COMPLETION = "google_in_app_review_completion"
    const val QUIZ_RECEIVED_REAL_TIME = "quiz_received_real_time"
    const val RESOURCE_DETAIL_ID = "resource_detail_id"

    const val STATE = "state"
    const val COURSE = "course"
    const val TEST = "test"
    const val LIVE_CLASS_ANNOUNCEMENT = "live_class_announcement"
    const val LIVE_CLASS_POLL_OPTION_CLICKED = "live_class_poll_option_clicked"
    const val LIVE_CLASS_POLL_RESULT = "live_class_poll_result"

    const val SIGN_IN = "sign_in"

    const val AUTOPLAY_TOGGLE_STATE = "autoplay_toggle_state"
    const val EVENT_FEEDBACK_VIEW = "feedback_view"
    const val DETAIL_ID = "detail_id"
    const val FEEDBACK_SUBMIT = "feedback_submit"
    const val EVENT_HOUR = "event_hour"
    const val EVENT_LOGIN_BACK_PRESS_DIALOG_OPEN = "login_back_press_dialog_open"

    const val SIMILAR_VIDEO_CARD_PLAYED = "similar_video_card_played"
    const val MATCH_VIDEO_FROM_VIDEO_PAGE_WATCHED = "match_video_from_video_page_watched"
    const val LIVE_CLASS_VIDEO_PLAYED = "live_class_video_played"
    const val VIEW_ALL_TOP_CLICK_LC_MPVP = "view_all_top_click_lc_mpvp"
    const val VIEW_ALL_BOTTOM_CLICK_LC_MPVP = "view_all_bottom_click_lc_mpvp"
    const val SHOW_MORE_SIMILAR_VIDEOS = "show_more_similar_videos"
    const val SIMILAR_ITEM_CLICK = "similar_video_click"
    const val SIMILAR_LIST_SCROLLED = "similar_list_scrolled"
    const val LAST_VISIBLE_ITEM = "last_visible_item"

    const val ADD_COMMENTS_LANDSCAPE = "add_comment_landscape"
    const val PREDEFINED_COMMENTS_ClICK_LANDSCAPE = "predefined_comment_click_landscape"

    // Camera gallery events
    const val GALLERY_TAPPED_OPEN = "gallery_tapped_open"
    const val GALLERY_TAPPED_CLOSE = "gallery_tapped_close"
    const val GALLERY_IMAGES_VIEWED_ON_CAMERA = "gallery_images_viewed_on_camera"
    const val GALLERY_IMAGE_ON_CAMERA_CLICKED = "gallery_image_on_camera_clicked"
    const val GALLERY_IMAGES_VIEW_ALL_CLICKED = "gallery_images_view_all_clicked"
    const val GALLERY_READ_STORAGE_PERMISSION_GRANTED = "gallery_read_storage_permission_granted"
    const val GALLERY_READ_STORAGE_PERMISSION_DENIED = "gallery_read_storage_permission_denied"
    const val GALLERY_BUTTON_CLICKED_NEW = "gallery_button_clicked_new"
    const val PHONE_GALLERY_OPENED = "phone_gallery_opened"
    const val NO_IMAGES_IN_GALLERY = "no_images_in_gallery"
    const val GALLERY_HOW_TO_CLICKED = "gallery_how_to_clicked"
    const val GALLERY_SWIPED_UP = "gallery_swiped_up"
    const val GALLERY_DEMO_CLICKED = "gallery_demo_clicked"

    const val TIME_TABLE_PAGE_VIEW = "lc_timetable_page_view"
    const val TIME_TABLE_SCROLL = "lc_timetable_scroll"
    const val TIME_TABLE_TODAY_CLICK = "lc_timetable_today_click"
    const val TIME_TABLE_BACK = "lc_timetable_back"
    const val EXPLORE_PAGE_VIEW = "lc_explore_page_view"
    const val EXPLORE_BACK = "lc_explore_back"
    const val EXPLORE_CAROUSEL_ITEM_CLICK = "lc_explore_carousel_item_click"
    const val BUNDLE_PAGE_VIEW = "lc_bundle_page_view"
    const val ASSORTMENT_IDS = "assortment_ids"
    const val BUNDLE_CLICK_EMI = "lc_bundle_click_emi"
    const val BUNDLE_CLICK_BUY_NOW = "lc_bundle_click_buy_now"
    const val BUNDLE_CLICK_KNOW_MORE = "lc_bundle_know_more"
    const val BUNDLE_CLICK_KNOW_LESS = "lc_bundle_know_less"
    const val BUNDLE_BACK = "lc_bundle_back"
    const val PAYMENT_PAGE_VIEW = "lc_payment_page_view"
    const val PAYMENT_COUPON_ENTERED_VALUE = "lc_payment_coupon_pasted_value"
    const val PAYMENT_PAY_NOW = "lc_payment_pay_now"
    const val ITEM_PARENT_POSITION = "parent_position"
    const val PARENT_TITLE = "parent_title"
    const val PARENT_ID = "parent_id"
    const val PARENT_TAB_KEY = "parent_tab_key"
    const val EXPLORE_CAROUSEL = "lc_explore_carousel"
    const val EXPLORE_SEE_MORE = "lc_explore_see_more"
    const val WIDGET_ITEM_CLICK = "item_click"
    const val UNDERSCORE = "_"
    const val EXPLORE_GO_TO_COURSE = "lc_explore_gt_course"
    const val COURSE_CALL_CLICK = "lc_call_click"
    const val COURSE_TRIAL_CLICK = "lc_trial_click"
    const val BUY_NOW_CLICK = "lc_buy_now_click"
    const val COURSE_INTRO_VIDEO_PLAY = "lc_course_intro_play"
    const val SHARE_PAYMENT_LINK_CLICK = "share_payment_link_click"
    const val PAYMENT_LINK_SHARED = "payment_link_shared"
    const val PHONE_NUMBER = "phone_number"

    const val COURSE_CLICK_EMI = "lc_course_click_emi"
    const val COURSE_CLICK_BUY_NOW = "lc_course_click_buy_now"
    const val COURSE_CLICK_KNOW_MORE = "lc_course_know_more"
    const val DEVICE_TIME_STAMP = "device_time_stamp"
    const val EVENT_TIME = "event_time"
    const val EVENT_TIME2 = "event_time2"
    const val TIME_TABLE_TEST = "time_table_test"
    const val TIME_TABLE_PDF = "time_table_pdf"
    const val MY_PLAN_VIEW_CLICK = "my_plan_view_click"
    const val PROMO_BANNER_VIEW = "promo_banner_view"
    const val PROMO_BANNER_CLICK = "promo_banner_click"

    const val CAROUSEL_BANNER_VIEW = "carousel_banner_view"
    const val CAROUSEL_BANNER_CLICK = "carousel_banner_click"

    const val TOP_OPTION_CLICK = "top_option_click"
    const val LIVE_CLASS_CHAT = "live_class_chat"
    const val BUY_NOW = "buy_now"
    const val SEND_MSG = "send_message"
    const val EVENT_CROP_AGAIN_CLICKED = "crop_again_clicked"

    const val LANGUAGE_CHANGE_BUTTON_CLICKED = "language_change_button_clicked"
    const val LANGUAGE_CHANGE_DONE = "language_change_done"
    const val LANGUAGE_CHANGE_POP_UP_OPEN = "language_change_pop_up_open"
    const val LANGUAGE_CHANGE_POP_UP_CLOSE = "language_change_pop_up_close"
    const val VIDEO_PLAY_LIST_FAILED = "VIDEO_PLAY_LIST_FAILED"
    const val VIDEO_RESOURCE = "VIDEO_RESOURCE"
    const val VIDEO_MEDIA_TYPE = "MEDIA_TYPE"

    const val LOGIN_PIN_SET = "login_pin_set"
    const val VERIFY_PIN_DIALOG_OPEN = "verify_pin_dialog_open"
    const val OTP_OVER_CALL_DIALOG_OPEN = "otp_over_call_dialog_open"
    const val OTP_OVER_CALL_BUTTON_CLICKED = "otp_over_call_button_clicked"
    const val OTP_OVER_MESSAGE_BUTTON_CLICKED = "otp_over_message_button_clicked"
    const val OTP_OVER_CLOSE_BUTTON_CLICKED = "otp_over_close_button_clicked"
    const val NEW_USER_REGISTERED_LANGUAGE = "new_user_registered_language"

    // Nudges
    const val NUDGE_ID = "nudge_id"
    const val NUDGE_TYPE = "nudge_type"
    const val EVENT_NUDGE_CLICK = "lc_nudge_click"
    const val EVENT_NUDGE_CLOSED = "lc_nudge_close"
    const val EVENT_NUDGE_SCRATCH = "lc_nudge_scratch"
    const val EVENT_NUDGE_VIEW = "lc_nudge_view"
    const val EVENT_TYD_CROSS_CLICKED = "TYD_Cross_clicked"
    const val EVENT_TYD_BACK_PRESS = "TYD_back_press"
    const val VOICE_SEARCH_CAMERA_BUTTON_TAPPED = "voice_search_camera_button_tapped"
    const val VOICE_SEARCH_BUTTON_TAPPED = "voice_search_button_tapped"
    const val VOICE_INPUT_ENTERED = "voice_input_entered"
    const val VOICE_SUGGESTION_CLICKED = "voice_suggestion_clicked"

    // Offline videos
    const val EVENT_OFFLINE_GO_TO_DOWNLOADS = "go_to_my_downloads"
    const val EVENT_OFFLINE_DOWNLOAD_START = "offline_download_button_click"
    const val EVENT_OFFLINE_DOWNLOAD_ITEM_CLICK = "offline_download_item_click"
    const val EVENT_OFFLINE_DOWNLOAD_DELETE = "offline_download_delete"
    const val EVENT_OFFLINE_MY_DOWNLOADS_CLICK = "my_downloads_click"

    const val NOTES_FILTER_CLICKED = "notes_filter_clicked"

    // EMI Reminder
    const val EMI_REMINDER_PAYMENT_CLICK = "lc_emi_reminder_pay_now"
    const val EMI_REMINDER_VIEW = "lc_emi_reminder_view"
    const val EMI_REMINDER_CLOSE = "lc_emi_reminder_close"

    const val VIDEO_WATCH_IIT_11_TO_13 = "dn_video_watch_iit_11_to_13"
    const val VIDEO_WATCH_9_TO_12 = "dn_video_watch_9_to_12"
    const val VIDEO_WATCH_IIT = "dn_video_watch_iit"
    const val VIDEO_WATCH_NEET = "dn_video_watch_neet"

    // OCR Edit
    const val OCR_EDIT_BUTTON_VISIBLE = "ocr_edit_button_visible"
    const val OCR_EDIT_BUTTON_CLICKED = "ocr_edit_button_clicked"
    const val OCR_EDIT_RESEARCH_DONE = "ocr_edit_research_done"
    const val OCR_EDIT_CROSS_PRESSED = "ocr_edit_cross_pressed"
    const val OCR_EDIT_POP_UP_DISPLAYED = "ocr_edit_pop_up_displayed"
    const val OCR_EDIT_VOICE_SEARCH = "ocr_edit_voice_search"
    const val OCR_EDIT_POP_UP_CLOSED = "ocr_edit_pop_up_closed"
    const val OCR_EDIT_TEXT_CHANGED = "ocr_edit_text_changed"
    const val OCR_EDIT_SUGGESTION_CLICKED = "ocr_edit_suggestion_clicked"

    const val DRAWER_WALLET_CLICK = "drawer_wallet_click"
    const val WALLET_HELP_CLICK = "wallet_help_click"

    const val WALLET_PAYMENT_SUCCESS = "wallet_payment_success"
    const val WALLET_PAGE_OPEN = "wallet_page_open"

    // Status Events
    const val CATEGORY_STATUS = "status"

    const val STATUS_SOURCE_HEADER = "status_header"
    const val STATUS_SOURCE_RECENT_WIDGET_HOME = "status_widget_home"
    const val STATUS_SOURCE_RECENT_WIDGET_FEED = "status_widget_feed"

    const val ADD_STATUS_CLICKED = "add_status_clicked"
    const val ADD_STATUS_CANCELLED = "add_status_cancelled"

    const val STATUS_UPLOAD_INITIATED = "status_upload_initiated"
    const val STATUS_UPLOAD_FAILURE = "status_upload_failure"
    const val STATUS_UPLOAD_SUCCESS = "status_upload_success"

    const val STATUS_CREATION_LIMIT_REACHED = "status_creation_limit_reached"

    const val MY_STATUS_VIEW = "my_status_view"
    const val OTHERS_STATUS_VIEW = "others_status_view"
    const val STATUS_AD_VIEW = "status_ad_view"
    const val STATUS_AD_CTA_CLICKED = "status_ad_cta_clicked"
    const val STATUS_LIKED = "status_liked"
    const val STATUS_DELETED = "status_deleted"
    const val STATUS_REPORTED = "status_reported"
    const val STATUS_FOLLOW_CLICK = "status_follow_click"

    const val STATUS_BOTTOMSHEET_OPEN = "status_bottomsheet_open"
    const val BOTTOM_SHEET_OPEN_FOR_CONTINUE_WATCHING_WIDGET =
        "continue_watching_bottom_sheet_opened"

    const val CREATOR = "creator"
    const val VIEWER = "viewer"
    const val REPORTER = "reporter"
    const val VIEW_COUNT = "view_count"
    const val LIKE_COUNT = "like_count"
    const val HEADER = "header"
    const val UPLOAD_URL = "upload_url"
    const val IMAGE_SOURCE = "image_source"
    const val ROTATE = "rotate"
    const val CROPPED = "cropped"

    const val EVENT_STATUS_ENGAGEMENT = "StatusEngagement"
    const val SOURCE_STATUS_ENGAGEMENT_CREATE_STATUS = "CreateStatus"
    const val SOURCE_STATUS_ENGAGEMENT_VIEW_STATUS = "ViewStatus"

    // Video Page Picture-in-Picture mode
    const val PIP_MODE_ENABLED = "pip_mode_enabled"
    const val PIP_MODE_CLOSED = "pip_mode_closed"
    const val PIP_MODE_VIEWED = "pip_mode_viewed"
    const val PIP_MODE_EXPANDED = "pip_mode_expanded"
    const val PIP_MODE_PLAY = "pip_mode_play"
    const val PIP_MODE_PAUSE = "pip_mode_pause"
    const val PIP_MODE_NEXT = "pip_mode_next"
    const val PIP_MODE_PERMISSION_DENIED = "pip_mode_permission_denied"
    const val PIP_MODE_PERMISSION_ALLOWED = "pip_mode_permission_allowed"
    const val PIP_MODE_BUTTON_CLICKED = "pip_mode_button_clicked"
    const val PIP_MODE_MAIN_SETTINGS_CLICKED = "pip_mode_main_settings_clicked"
    const val PIP_MODE_MAIN_SETTING_DISABLED = "pip_mode_main_setting_disabled"
    const val PIP_MODE_MAIN_SETTING_REENABLED = "pip_mode_main_setting_reenabled"

    const val PAID_ASSORTMENT_ID_PREFIX = "paid_assortment_id_"

    const val PINCH_TO_ZOOM = "pinch_to_zoom"

    const val NO_VIDEO_PLAYED_IN_LIST = "no_video_played_in_list"

    // MultiSelectWidget
    const val CATEGORY = "category"
    const val MASTER_FILTER_CLICKED = "lc_filter_master_click"
    const val CHILD_FILTER_CLICKED = "lc_filter_child_click"
    const val MASTER_FILTER_NAME = "master_filter_name"
    const val CHILD_FILTER_ID = "child_filter_id"

    // MultiSelectWidgetV2
    const val CHILD_FILTER_IDS = "child_filter_ids"
    const val EXAM_SELECTION_BOTTOM_SHEET = "exam_selection_bottom_sheet"

    // PDF Download In Video Page
    const val PDF_BANNER_VIEW = "pdf_banner_view"
    const val PDF_BANNER_CLICK = "pdf_banner_click"
    const val PDF_DOWNLOADED_BY_USER = "pdf_downloaded_by_user"
    const val PDF_URL_NULL = "pdf_url_null"
    const val PDF_AUTOPLAY_VIDEO_CLICKED = "pdf_autoplay_video_clicked"
    const val VIDEO_PLAYED_FROM_SRP_PDF = "video_played_from_srp_pdf"

    // Airtel Payment Bank
    const val NEAREST_STORE_CLICKED = "apb_find_location"
    const val PERMISSION_ALLOW = "apb_permission_allow"
    const val PERMISSION_DENY = "apb_permission_deny"
    const val APB_SEARCH_CLICKED = "apb_location_search"
    const val SHOP_NUMBER_CLICKED = "apb_call_store"
    const val SHOP_PHONE_NUMBER = "apb_phone_number"
    const val APB_WALLET_CLICKED = "apb_landing_wallet"
    const val APB_CHECKOUT_CLICKED = "apb_landing_checkout"
    const val SEND_SMS_CLICKED = "apb_click_sms"
    const val NO_RESULT_SEND_SMS_CLICKED = "apb_click_sms_no_result"
    const val NO_RESULT_REDIRECT_AIRTEL_WEBSITE_CLICKED = "apb_click_website_redirect"

    // App Exit Dialog
    const val POP_UP_DISPLAYED_FRAGMENT = "pop_up_displayed_fragment_"
    const val POP_UP_CLICKED_FRAGMENT = "pop_up_clicked_fragment_"
    const val POP_UP_SHOWN = "pop_up_shown"
    const val POP_UP_CLICKED = "pop_up_clicked"
    const val POP_UP_CLOSED = "pop_up_closed"
    const val HW_SUBMITTED = "hw_submitted"
    const val STUDENT_ID = CoreEventConstants.STUDENT_ID

    // Homework
    const val HW_LIST_VIEW = "hw_list_view"
    const val HW_LIST_CLICK = "hw_list_click"
    const val HW_HOME_LIST_CLICK = "hw_home_list_click"
    const val HW_VIEW = "hw_view"
    const val HOME_FEED_TOP_CLICK = "home_feed_top_click"

    // LCS Search
    const val SEARCH_BAR_CLICKED = "lcs_top_tag_clicked"
    const val POPULAR_SUGGESTION_CLICKED = "lcs_popular_suggestion_clicked"
    const val KEYBOARD_SEARCH_CLICKED = "lcs_keyboard_search_clicked"
    const val VOICE_SEARCH_CLICKED = "lcs_voice_search_clicked"
    const val VOICE_SEARCH_EXECUTED = "lcs_voice_search_executed"
    const val BACK_CLICKED = "lcs_back_clicked"
    const val LCS_CROSS_CLICKED = "lcs_cross_clicked"
    const val LCS_EMPTY_BACK = "lcs_empty_back"
    const val SUGGESTION_RESULTS_CLICKED = "lcs_suggestion_results_clicked"
    const val LCS_COURSE_RESULTS_CLICKED = "lcs_course_results_clicked"
    const val STUDENT_CLASS = "class"
    const val SOURCE_PAGE = "source_page"

    //region Camera Screen Navigation
    const val CAMERA_BOTTOM_NAVIGATION_VISIBLE = "camera_bottom_navigation_visible"
    const val CAMERA_BOTTOM_NAVIGATION_CLICKED = "camera_bottom_navigation_clicked"
    const val CAMERA_NAVIGATION_ICONS_BOTTOM_SHEET_VISIBLE =
        "camera_navigation_icons_bottom_sheet_visible"
    const val CAMERA_NAVIGATION_ICONS_SWIPE_UP = "camera_navigation_icons_swipe_up"
    const val CAMERA_NAVIGATION_ICONS_SWIPE_DOWN = "camera_navigation_icons_swipe_down"
    const val CAMERA_NAVIGATION_ICON_CLICK = "camera_navigation_icon_click"
    //endregion

    // referral events
    const val REFERRAL_SHARE_FEED = "referral_share_feed"
    const val REFERRAL_SHARE_WHATSAPP = "referral_share_whatsapp"
    const val REFERRAL_COPY = "referral_copy"
    const val REFERRAL_SHARE_CLICK = "referral_share_click"
    const val PAYMENT_PAGE = "Payment_page"
    const val HAMBURGER = "hamburger"

    //endregion

    //region ad event
    const val AD_SKIP_CLICK = "ad_skip_click"
    const val AD_START = "ad_start"
    const val AD_CTA_DISPLAY = "ad_cta_display"
    const val AD_CTA_CLICK = "ad_cta_click"
    const val AD_BANNER_CLICK = "ad_banner_click"
    const val AD_ID = "ad_id"
    const val AD_DURATION = "ad_duration"
    //endregion

    //region Home Page V2
    const val HOME_PAGE_CAROUSEL_SHOWN = "home_page_carousel_shown"
    const val HOME_PAGE_CAROUSEL_HIDDEN = "home_page_carousel_hidden"
    const val HOME_PAGE_CAROUSEL_CLICKED = "home_page_carousel_clicked"
    const val HOME_PAGE_CAROUSEL_TAB_VISIBLE = "home_page_carousel_tab_visible"
    const val HOME_PAGE_CAROUSEL_TAB_CLICKED = "home_page_carousel_tab_clicked"
    const val TAB_WIDGET_BOTTOM_CTA_CLICK = "tab_widget_bottom_cta_click"
    const val TAB_WIDGET_LINK_TEXT_CLICK = "tab_widget_link_text_click"
    const val CLICKED_BUTTON_NAME = "clicked_button_name"
    const val HOME_PAGE_CAROUSEL_SWIPED = "home_page_carousel_swiped"
    const val HOME_PAGE_AUTOPLAY_STOP_REASON_SLOW_INTERNET =
        "home_page_autoplay_stop_reason_slow_internet"
    //endregion

    const val EVENT_NAME_MATCHES_TABS = "MatchPageTab?" // here ? is used as place holder here

    // 8 pm quiz
    const val QUIZ_NOTIFICATION_TYPE = "old_or_new_quiz"
    const val CLICK_QUIZ_NOTIFICATION = "QuizAlertClick"
    const val CLICK_QUIZ_TYPE = "click_btn"
    const val QUIZ_NOTIFICATION_VERSION = "quiz_version"
    const val QUIZ_NOTIFICATION_TITLE = "quiz_title"
    const val QUIZ_NOTIFICATION_STATUS = "quiz_status"
    const val QUIZ_STUDENT_ID = "quiz_student_id"
    const val QUIZ_NOTIFICATION_REASON = "quiz_notification_reason"

    // 8pm quiz v2
    const val QUIZ_CLICKED_TYPE = "quiz_clicked"
    const val CLICK_QUIZ_EVENT_V2 = "quizalertviewed_v2"

    const val EVENT_NAME_QUIZ_ALERT_VIEW_NEW = "QuizAlertViewNew"
    const val EVENT_NAME_QUIZ_NEW = "Quiz"
    const val QUIZ_TO_BE_SHOWN = "Quiz_notification_to_be_shown"
    const val QUIZ_NOTIFICATION_SHOWN = "Quiz_notification_shown"
    const val QUIZ_NOTIFICATION_CLICKED = "Quiz_notification_clicked"

    const val THUMBNAIL_TYPE_LOCALE = "locale_thumbnail"
    const val THUMBNAIL_TYPE_ENGLISH = "english_thumbnail"
    const val THUMBNAIL_TYPE_OCR = "ocr_text"

    //region FollowerWidget
    const val FOLLOWER_WIDGET_PROFILE_CLICKED = "follower_widget_profile_clicked"
    const val FOLLOWER_WIDGET_FOLLOW_CLICKED = "follower_widget_follow_clicked"

    // wallet events
    const val BUY_NOW_CLICKED = "wallet_buy_now"
    const val ADD_WALLET_MONEY = "wallet_add_money"

    const val COURSE_SUBJECT_EXPAND = "lc_subject_expand_collapse"
    const val COURSE_DETAILS_EXPAND = "lc_course_details_expand_collapse"
    const val FAQ_PLAY_VIDEO = "lc_faq_play_video"
    const val TOGGLE = "toggle"
    const val COURSE_PAGE_POST_PURCHASE = "course_page_post_purchase"
    const val COURSE_PAGE_PRE_PURCHASE = "course_page_pre_purchase"

    //endregion

    const val ONBOARDING_VIEW = "onboarding_view"
    const val ONBOARDING_MUTE = "Onboarding_speaker_mute"
    const val ONBOARDING_CLICK = "Onboarding_next_click"

    const val D3E40 = "D3E40"
    const val D5E80 = "D5E80"

    // Match Page ocr from image
    const val IMAGE_TO_OCR_CONVERTED = "image_to_ocr_converted"
    const val OCR_TEXT = "ocr_text"
    //endregion

    // Similar Video Locale thumbnail
    const val EVENT_SIMILAR_VIDEO_THUMBNAIL = "SimilarVideoThumbnailType"
    const val VIDEO_THUMBNAIL_FEATURE_STATUS = "thumbnail_user_locale_eligible "
    const val VIDEO_THUMBNAIL_SHOWN = "thumbnail_user_locale_shown_final"
    const val LOCALE_VIDEO_THUMBNAIL_STATUS = "locale_thumbnail_status"
    const val VIDEO_THUMBNAIL_STATUS = "thumbnail_status"

    const val SHARE_COURSE_BUTTON_CLICK = "share_course_button_click"
    const val SHARE_COURSE_URL = "share_course_url"

    // Match page back press pop up
    const val MATCH_PAGE_BACK_PRESS_POPUP_VIEWED = "match_page_back_press_popup_viewed"
    const val MATCH_PAGE_BACK_PRESS_POPUP_VIDEO_PLAYED = "match_page_back_press_popup_video_played"
    const val MATCH_PAGE_BACK_PRESS_POPUP_CROSS_CLICKED =
        "Match_page_back_press_popup_cross_clicked"
    const val MATCH_PAGE_BACK_PRESS_POPUP_NOTIFY_CLICKED =
        "Match_page_back_press_popup_notify_clicked"

    //region Topic Booster Gamification
    const val TOPIC_BOOSTER_GAME_PLAYED = "topic_booster_game_played"
    const val TOPIC_BOOSTER_GAME_WON = "topic_booster_game_won"
    const val TOPIC_BOOSTER_GAME_PLAY_AGAIN_CLICKED = "topic_booster_game_play_again_clicked"
    const val TOPIC_BOOSTER_GAME_QUESTION_ASK_CLICKED = "topic_booster_game_question_ask_clicked"
    const val TOPIC_BOOSTER_GAME_GO_HOME_CLICKED = "topic_booster_game_go_home_clicked"
    const val TOPIC_BOOSTER_GAME_QUIT = "topic_booster_game_quit"
    const val TOPIC_BOOSTER_GAME_USER_STATS_CLICK = "topic_booster_game_user_stats_click"
    const val TOPIC_BOOSTER_GAME_OPEN_SOURCE_PROFILE = "topic_booster_game_open_source_profile"

    const val TOPIC_BOOSTER_GAME_SUBJECT_CLICKED = "tbg_subject_clicked"
    const val TOPIC_BOOSTER_GAME_PERFORMANCE_HISTORY_CLICKED = "tbg_performance_history_clicked"
    const val TOPIC_BOOSTER_GAME_LEVEL_CLICK = "tbg_level_click"
    const val TOPIC_BOOSTER_GAME_LEVEL_INFO_CLICK = "tbg_level_info_click"
    const val TOPIC_BOOSTER_GAME_FAQ_CLICK = "tbg_faq_click"
    const val TBG_FAQ_CLICKED = "tbg_FAQ_video_clicked"
    const val TOPIC_BOOSTER_GAME_CHAT_MSG_SENT = "tbg_chat_msg_sent"
    const val TOPIC_BOOSTER_GAME_EMOJI_SENT = "tbg_emoji_sent"
    const val TOPIC_BOOSTER_GAME_VIEW_RANK_CLICK = "tbg_view_rank_click"
    const val TOPIC_BOOSTER_GAME_PLAY_WITH_OPPONENT_CLICK = "tbg_play_with_opponent_click"
    const val TOPIC_BOOSTER_GAME_INVITE_SENT = "tbg_invite_sent"
    const val TOPIC_BOOSTER_GAME_HOME_PAGE_VISITED = "tbg_home_page_visited"
    const val TOPIC_BOOSTER_GAME_PLAY_CLICKED = "tbg_play_clicked"
    const val TOPIC_BOOSTER_GAME_GO_HOME = "tbg_go home"
    const val TOPIC_BOOSTER_GAME_PLAY_WITH_FRIEND_CLICKED = "tbg_play_with_friend_clicked"
    const val TOPIC_BOOSTER_GAME_FIND_OPPONENT_CLICKED = "tbg_find_opponent-clicked"
    const val TOPIC_BOOSTER_GAME_FEATURE_UNAVAILABLE = "tbg_feature_unavailable"
    const val TOPIC_BOOSTER_GAME_PLAYED_V2 = "tbg_played_v2"
    const val TOPIC_BOOSTER_GAME_PROFILE_VISIT_LEADERBOARD = "tbg_profile_visited_from_leaderboard"
    const val TOPIC_BOOSTER_GAME_CHECK_COURSE_CLICKED = "tbg_check_course_clicked"
    const val HOME_PAGE_BANNER = "homepage_banner"

    //endregion

    const val CCM_ID = "selected_exam_ccm_id"

    // region No internet activity - match page
    const val NO_NETWORK_SCREEN = "no_network_screen"
    const val CLIENT_SIDE = "client_side"
    const val SERVER_SIDE = "server_side"
    const val FUNCTION = "function"

    const val CROP_WINDOW_RECT_INIT = "crop_window_rect_init"
    const val ENTITY_ID = "entity_id"
    const val ENTITY_TYPE = "entity_type"
    const val TOP_DOUBT_EXPAND_COLLAPSE = "top_doubt_expand_collapse"
    const val ORIENTATION_LANDSCAPE = "orientation_landscape"
    const val ORIENTATION_PORTRAIT = "orientation_portrait"
    const val COLLAPSE = "collapse"
    const val EXPAND = "expand"
    const val COMMENT_FILTER_CLICK = "comments_filter_click"
    const val TOP_DOUBT_VIEW_APPEAR = "top_doubt_view_appear"
    const val WIDGET_VIEW_SOLUTION_CLICK = "td_view_solution_click"
    const val COMMENT_VIEW_SOLUTION_CLICK = "td_comment_view_solution_click"
    const val COMMENT_DOUBT_SUBMITTED = "comment_doubt_submitted"

    // FAQ
    const val FAQ_PAGE_OPEN = "faq_page_open"
    const val FAQ_ITEM_EXPAND_COLLAPSE = "faq_item_expand_collapse"
    const val FAQ_ITEM_VIDEO_PLAY = "faq_item_video_play"
    const val PRIORITY = "PRIORITY"

    // checkout
    const val PAYMENT_CHECKOUT_COLLECT_UPI_METHOD_CLICKED =
        "payment_checkout_collect_upi_method_clicked"
    const val PAYMENT_CHECKOUT_NETBANKING_SELECT_METHOD_CLICKED =
        "payment_checkout_netbanking_select_method_clicked"
    const val PAYMENT_CHECKOUT_METHOD_CLICKED = "payment_checkout_method_clicked"
    const val PAYMENT_CHECKOUT_CUSTOM_START = "payment_checkout_custom_start"
    const val PAYMENT_CHECOUT_BACKPRESSED = "payment_custom_back_press"
    const val PAYMENT_FOR = "payment_for"
    const val COUPON_INVALID = "lc_coupon_invalid"
    const val COUPON_VALID = "lc_coupon_valid"
    const val COUPON_VIEW = "lc_coupon_view"

    const val BUNDLE_SHEET_VIEW = "LC_bottom_sheet_view"
    const val BUNDLE_SHEET_ITEM_CLICK = "LC_sheet_click_buy_now"

    // DISCOUNT ON PAYMENT WIDGET
    const val PAYMENT_CHECKOUT_DISCOUNT_WIDGET_SHOWN = "rzp_backpress_off_widget_shown"
    const val PAYMENT_CHECKOUT_DISCOUNT_WIDGET_CLOSED = "rzp_backpress_off_widget_closed"
    const val PAYMENT_CHECKOUT_DISCOUNT_WIDGET_CONTINUE_BUYING_CLICKED =
        "rzp_backpress_off_widget_continued"
    const val SOURCE_RZP_BACKPRESS_WIDGET = "rzp_backpress_widget"

    // region Retry image upload - match page
    const val REQUEST_PARALLEL_IMAGE_UPLOAD = "request_parallel_image_upload"
    const val IS_RETRIED = "is_retried"
    const val MATCH_PAGE_API_RESPONSE_TIME = "mp_api_response_time"
    const val MATCH_PAGE_SIGNED_URL_API = "mp_signed_url_api"
    const val MATCH_PAGE_IMAGE_UPLOAD_API = "mp_image_upload"
    const val MATCH_PAGE_IMAGE_RESULT_API = "mp_image_result_api"
    const val MATCH_PAGE_TEXT_RESULT_API = "mp_text_result_api"

    const val PAYMENT_COMPLETE_CLICKED = "lc_complete_payment_click"
    const val COMPLETE_PAYMENT_VIEW = "lc_complete_payment_view"
    //endregion

    const val EVENT_VIDEO_WATCH = "video_watch"

    // reward attendance events
    const val ATTENDANCE_CARD_DISPLAYED = "attendance_card_displayed"
    const val ATTENDANCE_CARD_EXPLORE_CLICK = "attendance_card_explore_clicked"
    const val ATTENDANCE_CARD_CROSS_CLICK = "attendance_card_cross_clicked"
    const val ATTENDANCE_CARD_IGNORE_CLICK = "attendance_card_ignore_clicked"
    const val ATTENDANCE_MANUALLY_MARKED = "attendance_card_manually_marked"
    const val ATTENDANCE_MANUALLY_MARKED_REWARD_PAGE = "attendance_card_manually_marked_reward_page"
    const val ATTENDANCE_AUTOMATICALLY_MARKED = "attendance_card_automatically_marked"
    const val ATTENDANCE_REMINDER_SENT = "attendance_card_reminder_sent"
    const val ATTENDANCE_REMINDER_CLICK = "attendance_card_reminder_click"
    const val ATTENDANCE_CARD_MARKED_GET_IT_NOW = "attendance_card_marked_get_it_now"
    const val ATTENDANCE_CARD_KNOW_MORE_CLICK_PROFILE = "attendance_card_know_more_click_profile"

    const val REWARD_PAGE_OPEN = "reward_page_open"
    const val REWARD_PAGE_SCRATCH_CARD_UNLOCKED_CARD_TAPPED =
        "reward_page_scratch_card_unlocked_card_tapped"
    const val REWARD_PAGE_SCRATCH_CARD_LOCKED_CARD_TAPPED = "reward_page_locked_card_tapped"
    const val REWARD_PAGE_SCRATCH_CARD_SCRATCHED = "reward_page_scratch_card_scratched"
    const val REWARD_PAGE_SCRATCH_CARD_CTA_CLICKED = "reward_page_scratch_card_cta_clicked"
    const val REWARD_PAGE_SCRATCH_CARD_SHARE_CLICKED = "reward_page_scratch_card_share_clicked"
    const val REWARD_PAGE_CODE_COPIED = "reward_page_code_copied"
    const val REWARD_PAGE_REMINDER_SET = "reward_page_reminder_set"
    const val REWARD_PAGE_REMINDER_CLEARED = "reward_page_reminder_cleared"
    const val REWARD_PAGE_KNOW_MORE_CLICKED = "reward_page_know_more_clicked"
    const val REWARD_PAGE_OPEN_SOURCE_PROFILE = "reward_page_open_source_profile"
    const val REWARD_PAGE_OPEN_SOURCE_HAMBURGER = "reward_page_open_source_hamburger"
    const val REWARD_PAGE_BACKPRESS_POPUP_SHOWN = "reward_page_backpress_popup_shown"
    const val REWARD_PAGE_BACKPRESS_POPUP_REMIDER_CLICK =
        "reward_page_backpress_popup_reminder_click"
    const val REWARD_PAGE_BACKPRESS_POPUP_CROSS_CLICK = "reward_page_backpress_popup_cross_click"
    const val REWARD_PAGE_BACKPRESS_POPUP_CLOSE_BACKPRESS =
        "reward_page_backpress_popup_close_backpress"
    const val REWARD_PAGE_BACKPRESS_POPUP_DISMISSED = "reward_page_backpress_popup_dismissed"

    const val STREAK_BREAK_ATTENDANCE_MISSED = "streak_break_attendance_missed"
    const val STREAK_CONTINUE_ACHIEVED = "streak_continue_achieved"

    const val REWARD_NOTIFICATION_SENT = "reward_notification_sent"
    const val REWARD_NOTIFICATION_CLICKED = "reward_notification_clicked"

    // POST PURCHASE COURSE
    const val LC_COURSE_PAGE_CARD_CLICK = "Lc_course_page_card_click"
    const val LC_COURSE_SUBJECT_CARD_CLICK = "Lc_course_subject_card_click"
    const val CARD_ID = "card_id"
    const val CARD_STATE = "card_state"
    const val CARD_ORDER = "card_order"
    const val COURSE_TRIAL_BUY_NOW_CLICK = "lc_trial_to_buy_click"
    const val COURSE_VALIDITY_BUY_NOW_CLICK = "lc_course_validity_buy_now_click"
    const val ALL_CLASSESS_EXPAND = "Lc_course_all_classes_item_expand_collapse"
    const val ALL_CLASSESS_ITEM_ACTION = "Lc_course_all_classes_item_action"
    const val ALL_CLASSESS_ViDEO_CLICK = "Lc_course_all_classes_video_click"
    const val ALL_CLASSESS_TOPIC_CLICK = "Lc_course_all_classes_topic_click"
    const val EXPANDED = "expanded"
    const val COMPLETED = "completed"
    const val DISPLAY_NAME = "display_name"
    const val COURSE_NOTES_FILTER_SELECT = "Lc_course_notes_filter_select"
    const val LC_COURSE_TEST_ITEM_CLICK = "Lc_course_test_item_click"
    const val LC_COURSE_TIMELINE_SCROLL = "Lc_course_timeline_scroll"
    const val LC_COURSE_TIMELINE_MONTH_SELECT = "Lc_course_timeline_month_select"
    const val MONTH = "month"
    const val LC_COURSE_PAGE_VIEW = "Lc_course_page_view"
    const val LC_COURSE_CAROUSAL_ITEM_CLICK = "Lc_course_carousal_item_click"
    const val LC_SUBJECT_VIEW_ALL_CLICK = "Lc_subject_view_all_click"
    const val LC_COURSE_VIDEO_PLAY_ITEM = "Lc_course_video_play_item"
    const val COURSE_DROPDOWN_SELECT = "Lc_course_dropdown_select"
    const val SUPPORT_BUTTON_CLICK = "lc_contact_us_dropdown_click"
    const val CHAT_STARTED = "chat_started"

    // region NCERT Video page
    const val NCERT_NEW_PAGE_OPEN = "ncert_new_page_open"
    const val EXERCISE_TAPPED = "exercise_tapped"
    const val NCERT_SIMILAR_VIDEO_CLICKED = "ncert_similar_video_clicked"
    const val NCERT_NEXT_BOOK_AUTO_PLAYED = "ncert_next_book_auto_played"
    const val NCERT_NEXT_BOOK_CLICKED = "ncert_next_book_clicked"
    const val NCERT_SIMILAR_AUTO_PLAYED = "ncert_similar_auto_played"
    const val BACK_FROM_NCERT_PAGE = "back_from_ncert_page"
    const val PLAYLIST_ID = "playlist_id"
    const val PLAYLIST_TYPE = "playlist_type"
    //endregion

    // Course Sticky Notification
    const val COURSE_NOTIFICATION_BUTTON_CLICK = "lc_sticky_buy_now_button"
    const val COURSE_NOTIFICATION_BANNER_CLICK = "lc_sticky_click"
    const val COURSE_NOTIFICATION_DISPLAY = "lc_sticky_display"

    // Generic Sticky Notification with Timer
    const val STICKY_WITH_TIMER_NOTIFICATION_CLICK = "sticky_with_timer_click"
    const val STICKY_WITH_TIMER_NOTIFICATION_DISPLAY = "sticky_with_timer_display"

    // Popular course widget
    const val POPULAR_COURSE_BUTTON_CLICK = "pc_cta_click"
    const val POPULAR_COURSE_BANNER_CLICK = "pc_thumb_click"
    const val POPULAR_COURSE_VIEW = "pc_view"

    // Homepage ask doubts
    const val ASK_DOUBTS_CLICK = "hp_ask_doubt_click"

    // Paid content
    const val PAID_CONTENT_SEARCH_EVENTS = "paid_content_search_events"
    const val CTA_VIEWED = "cta_viewed"
    const val CTA_CLICKED = CoreEventConstants.CTA_CLICKED
    const val PAID_USER = "is_vip"
    const val VIEW_FROM = "view_from"

    const val BBPS_CLICK = "bbps_click"
    const val PAYMENT = "PAYMENT"
    const val COURSE_DROPDOWN = "course_dropdown"

    // MPVP redesign (bottom sheet playlist)
    const val MPVP_BOTTOM_SHEET_VIDEO_AUTOPLAYED = "mpvp_bottom_sheet_video_autoplayed"
    const val MPVP_BOTTOM_SHEET_VIDEO_SELECTED = "mpvp_bottom_sheet_video_selected"
    const val MPVP_BOTTOM_SHEET_SCROLLED = "mpvp_bottom_sheet_scrolled"
    const val MPVP_BOTTOM_SHEET_EXPANDED = "mpvp_bottom_sheet_expanded"
    const val MPVP_BOTTOM_SHEET_COLLAPSED = "mpvp_bottom_sheet_collapsed"
    const val MPVP_BOTTOM_SHEET_SHOWN = "mpvp_bottom_sheet_shown"

    const val BOTTOM_SHEET_TYPE = "bottom_sheet_type"
    //endregion

    // Study Dost
    const val STUDY_DOST_WIDGET_SHOWN = "study_dost_widget_shown"
    const val LEVEL = "level"
    const val STUDY_DOST_REQUESTED = "study_dost_requested"
    const val STUDY_DOST_START_CHAT_CLICKED = "study_dost_start_chat_clicked"
    const val STUDY_DOST_BLOCK_USER = "study_dost_block_user"
    const val STUDY_DOST_MESSAGE_SENT = "study_dost_message_sent"
    const val STUDY_DOST_IMAGE_UPLOADED = "study_dost_image_uploaded"
    const val STUDY_DOST_IMAGE_SENT = "study_dost_image_sent"

    // OCR from image notification
    const val OCR_FROM_IMAGE_NOTIFICATION_SHOWN = "ocr_from_image_notification_shown"
    const val IS_OCR_NOTIFICATION = "is_ocr_notification"
    const val OCR_FROM_IMAGE_STORED_IN_DB = "ocr_from_image_stored_in_db"
    const val EVENT_IN_APP_MATCH_OCR_DIALOG_VISIBLE = "in_app_match_ocr_dialog_visible"
    const val EVENT_IN_APP_MATCH_OCR_DIALOG_CLICKED = "in_app_match_ocr_dialog_clicked"

    // transaction history v2
    const val PAYMENT_HISTORY_INVOICE_CLICK = "payment_history_invoice_click"
    const val PAYMENT_HISTORY_DEEPLINK_CLICK = "payment_history_deeplink_click"
    const val PAYMENT_HISTORY_FAILED_CLICK = "payment_history_failed"

    const val REWARD_REMINDER = "reward_reminder"

    // Wallet recommeded courses
    const val WALLET_RECOMMENDED_COURSE_VISIBLE = "wallet_recommended_course_visible"
    const val WALLET_RECOMMENDED_COURSE_BUY_NOW_CLICK = "wallet_recommended_course_buy_now_click"

    // Match Page Exit
    const val MATCH_PAGE_EXIT = "match_page_exit"
    const val MATCH_RESULT_SHOWN = "match_result_shown"

    const val PDF_ACTIVITY_OPEN = "pdf_activity_open"
    const val PDF_ACTIVITY_CLOSE = "pdf_activity_close"
    const val PRE_PURCHASE = "pre_purchase"
    const val POST_PURCHASE = "post_purchase"
    const val CHAT_CLICK = "lc_chat_click"

    // Doubt P2P Events
    const val P2P_VISIBLE_ON_BACK_PRESS = "P2p_visible_on_back_press"
    const val P2P_VISIBLE_ON_BOTTOM_PAGE = "P2p_visible_on_bottom_page"
    const val P2P_CLICKED_ON_BACK_PRESS = "P2p_clicked_on_back_press"
    const val P2P_CLICKED_ON_BOTTOM_PAGE = "P2p_clicked_on_bottom_page"
    const val P2P_ROOM_OPEN = "p2p_room_open"
    const val P2P_NOTIFICATION_CLICKED = "P2p_notification_clicked"
    const val P2P_MESSAGE_SENT = "P2p_message_sent"
    const val P2P_IMAGE_SENT = "P2p_image_sent"
    const val P2P_IS_HOST = "host"
    const val P2P_MEMBER_JOINED = "P2p_member_joined"
    const val P2P_SOLUTION_FEEDBACK_NO = "P2p_solution_feedback_no"
    const val P2P_SOLUTION_FEEDBACK_YES = "P2p_solution_feedback_yes"
    const val P2P_FEEDBACK_SUBMITTED = "p2p_feedback_submitted"
    const val P2P_HELPER_JOINED_BUT_ROOM_CLOSED = "p2p_helper_joined_but_room_closed"
    const val REASON = "reason"
    const val P2P_NOTIFICATION = "p2p_notificatiton"
    const val ENABLE = "enable"
    const val P2P_CLICKED_ON_TOP_ICON = "p2p_cicked_on_top_icon"
    const val P2P_ICON_CLICKED = "p2p_icon_clicked"
    const val P2P_HELPER_SCREEN_VISIBLE = "p2p_helper_landing_on_explainer_screen"
    const val P2P_HOST_CONNECT_NOW = "p2p_host_clicked_connect_now"
    const val P2P_HELPER_CLICKED_SOLVE_NOW = "p2p_helper_clicked_solve_now"
    const val P2P_ASKER_LANDING_ON_EXPLAINER_SCREEN = "p2p_asker_landing_on_explainer_screen"
    const val P2P_WIDGET_CTA_CLICK = "p2p_widget_cta_click"
    const val P2P_COLLECTION_TAB = "p2p_collection_tab"
    const val P2P_IMAGE_AUTO_DOWNLOAD = "p2p_image_auto_download"

    // course select sheet
    const val COURSE_BOTTOM_SHEET_CLICK = "mc_bottom_sheet_select"
    const val COURSE_BOTTOM_SHEET_VIEW = "mc_bottom_sheet_view"
    const val BOTTOM_ICON = "bottom_icon"
    const val TOP_ICON = "top_icon"

    const val CC_HELP_CLICK = "custom_checkout_help_click"
    const val CC_HELP_METHOD_CLICK = "custom_checkout_help_method_click"

    // Doubt Feed Events
    const val DF_DAILY_GOAL_NOT_SET_STATE = "df_daily_goal_not_set_state"
    const val DF_DAILY_GOAL_SET_STATE = "df_daily_goal_set_state"
    const val DF_GOAL_COMPLETED = "df_goal_completed"
    const val DF_ALL_GOALS_COMPLETED_FOR_TOPIC_WITHIN_WINDOW =
        "df_all_goals_completed_for_topic_within_window"
    const val DF_TOPIC_CLICKED = "df_topic_clicked"
    const val DF_DOUBTS_WIDGET_CLICK = "df_widget_click"
    const val DF_NUMBER_OF_TOPICS_FOR_DAILY_GOALS = "df_number_of_topics_for_daily_goals"
    const val DF_BACK_PRESS_POPUP_SHOWN = "df_back_press_popup_shown"
    const val DF_BACK_PRESS_POPUP_ASK_QUESTION_CLICK = "df_back_press_popup_ask_question_click"
    const val DF_DAILY_GOAL_COMPLETED = "df_daily_goal_completed"
    const val DF_ASK_DOUBT_CLICK = "ask_doubt_click"
    const val DF_PAGE_VISITED = "df_page_visited"
    const val DF_OPEN_SOURCE_PROFILE = "df_open_source_profile"
    const val DF_OPEN_SOURCE_HAMBURGER = "df_open_source_hamburger"
    const val DF_MPVP_BANNER_VIEWED = "df_mpvp_banner_viewed"
    const val DF_MPVP_BANNER_CLICKED = "df_mpvp_banner_clicked"
    const val DF_HOME_BANNER_VIEWED = "df_home_banner_viewed"
    const val DF_HOME_BANNER_CLICKED = "df_home_banner_clicked"

    const val DG_ICON_CLICK = "dg_icon_click"
    const val DG_PAGE_VISITED = "dg_page_visited"
    const val DG_ASK_QUESTION_CLICK = "dg_ask_question_click"
    const val DG_SHOW_BENEFITS_CLICK = "dg_show_benefits_click"
    const val DG_INFO_ICON_CLICK = "dg_info_icon_click"
    const val DG_TASK_COMPLETED = "dg_task_completed"
    const val DG_GOAL_COMPLETED = "dg_goal_completed"
    const val DG_LEADERBOARD_VISITED = "dg_leaderboard_visited"
    const val DG_BACKPRESS_POPUP_SHOWN = "dg_backpress_popup_shown"
    const val DG_BACKPRESS_POPUP_ASK_CLICK = "dg_backpress_popup_ask_click"
    const val DG_BOTTOMSHEET_RESUME_CLICK = "dg_bottomsheet_resume_click"
    const val DG_VIEW_REWARDS_CLICK = "dg_view_rewards_click"
    const val DG_SCRATCHCARD_UNLOCKED = "dg_scratchcard_unlocked"
    const val DG_SCRATCHCARD_SCRATCHED = "dg_scratchcard_scratched"
    const val DG_SCRATCHCARD_CTA_CLICK = "dg_scratchcard_CTA_click"
    const val DG_SCRATCHCARD_SHARE_CLICK = "dg_scratchcard_share_click"
    const val DG_TOPIC_CLICK = "dg_topic_click"
    const val DG_PREVIOUS_DOUBT_VIEW_ALL_CLICK = "dg_previous_doubt_view_all_click"
    const val DG_PREVIOUS_DOUBT_TASK_DONE = "dg_previous_doubt_task_done"
    const val DG_TIME_LEFT_CLICK = "dg_time_left_click"
    const val DG_HOME_PAGE = "dg_home_page"
    const val DG_MPVP_BANNER_VIEWED = "dg_mpvp_banner_viewed"
    const val DG_MPVP_BANNER_CLICKED = "dg_mpvp_banner_clicked"
    const val DG_HOME_BANNER_VIEWED = "dg_home_banner_viewed"
    const val DG_HOME_BANNER_CLICKED = "dg_home_banner_clicked"
    const val DG_SHOW_BENEFITS_POPUP_SHOWN = "dg_show_benefits_popup_shown"
    const val DG_PROFILE_VISIT_LEADERBOARD = "dg_profile_visit_leaderboard"
    const val DG_HOME_PAGE_BOTTOMSHEET_SHOWN = "dg_home_page_bottomsheet_shown"
    const val DG_HOME_PAGE_BOTTOMSHEET_CLICKED = "dg_home_page_bottomsheet_clicked"

    // Coupon Banner event
    const val TIMER_PROMO_WIDGET_CLICK = "timer_promo_widget_click"
    const val TIMER_PROMO_WIDGET_SHOWN = "timer_promo_widget_shown"

    /**
     * MyCourseWidget
     */
    const val MY_COURSE_WIDGET_CLICKED = "my_course_widget_clicked"

    // Testimonial Widget
    const val REVIEW_CAROUSAL_VIDEO_CLICK = "review_carousel_video_click"
    const val REVIEW_CAROUSAL_SWIPE = "review_carousel_swiped"

    // Study Group
    const val SG_HAMBURGER_CLICK = "sg_hamburger_click"
    const val SG_FEED_CLICK = "sg_feed_click"
    const val SG_INVITE_SENT = "sg_invite_sent"
    const val SG_INTRO_PAGE_VISITED = "sg_intro_page_visited"
    const val SG_LIST_PAGE_VISITED = "sg_list_page_visited"
    const val SG_CREATE_GROUP_CLICKED = "sg_create_group_clicked"
    const val SG_CREATED = "sg_created"
    const val SG_MEMBER_JOINED = "sg_member_joined"
    const val SG_MESSAGE_SENT = "sg_message_sent"
    const val SG_INFO_CLICKED = "sg_info_clicked"
    const val SG_MEMBER_BLOCKED = "sg_member_blocked"
    const val SG_REPORTED_CONTAINER_REMOVE = "sg_reported_container_remove"
    const val SG_VIEW_PROFILE_CLICKED = "sg_view_profile_clicked"
    const val SG_LEAVE_GROUP_CLICKED = "sg_leave_group_clicked"
    const val SG_FAQ_CLICKED = "sg_faq_clicked"
    const val SG_KNOW_MORE_CLICKED = "sg_know_more_clicked"
    const val SG_UNABLE_TO_JOIN = "sg_unable_to_join"
    const val SG_UNABLE_TO_JOIN_CLICK = "sg_unable_to_join_click"
    const val SG_CTA_TEXT = "sg_cta_text"
    const val SG_DESCRIPTION = "sg_description"
    const val SG_PROFILE_ICON_CLICKED = "sg_profile_icon_clicked"
    const val ERROR_IN_UPLOADING = "error_in_uploading"
    const val SG_NOTIFICATION = "sg_notification"
    const val IS_MUTE = "is_mute"
    const val SG_INFO_EDIT_CLICK = "sg_info_edit_click"
    const val SG_INFO_GROUP_IMAGE_CLICK = "sg_info_group_image_click"
    const val SG_GROUP_IMAGE_UPDATED = "sg_group_image_updated"
    const val SG_GROUP_NAME_UPDATED = "sg_group_name_updated"
    const val SG_EXTRA_INFO_SCREEN = "sg_extra_info_screen"
    const val SG_CREATE_NEW_GROUP_CLICKED = "sg_create_new_group_click"
    const val SG_JOIN_GROUP_FAB_CLICKED = "sg_join_group_fab_clicked"
    const val SG_JOIN_GROUP_LIST_ITEM_CLICKED = "sg_join_group_list_item_clicked"
    const val SG_JOIN_GROUP_TOOLBAR_CTA_CLICKED = "sg_join_group_toolbar_cta_clicked"
    const val SG_JOIN_GROUP_TAB_OPTION_CLICKED = "sg_join_group_tab_option_clicked"
    const val SG_CREATE_NEW_CHAT_CLICKED = "sg_create_new_chat_click"
    const val SG_VIDEO_SHARE = "sg_video_share"
    const val SG_SETTING_NOTIFICATION_TOGGLE = "sg_setting_notification_toggle"
    const val SG_SETTING_SCREEN_OPEN = "sg_setting_screen_open"
    const val SG_SEARCH_QUERY = "sg_search_query"
    const val SG_REJECT_BLOCK_CHAT_REQUEST = "sg_reject_block_chat_request"
    const val SG_ACCEPT_CHAT_REQUEST = "sg_accept_chat_request"
    const val SG_EXTRA_INFO_PRIMARY_CTA_CLICK = "sg_extra_info_primary_cta_click"
    const val SG_EXTRA_INFO_SECONDARY_CTA_CLICK = "sg_extra_info_secondary_cta_click"
    const val GROUP_ID = "group_id"

    const val SG_SCREEN_STATE = "sg_screen_state"
    const val SCREEN_NAME = "screen_name"
    const val SG_SCREEN_ENTERED_FOREGROUND = "sg_screen_entered_foreground"
    const val SG_SCREEN_ENTERED_BACKGROUND = "sg_screen_entered_background"
    const val SG_SCREEN_ENTERED_FOREGROUND_ACTION = "action_sg_screen_entered_foreground"
    const val SG_SCREEN_ENTERED_BACKGROUND_ACTION = "action_sg_screen_entered_background"

    // Study Group - Report and Delete
    const val SG_DELETE_MESSAGE = "sg_delete_message"
    const val SG_REPORT_MESSAGE = "sg_report_message"
    const val IS_ADMIN = "is_admin"
    const val ADMIN_ID = "admin_id"
    const val DELETE_TYPE = "delete_type"
    const val MESSAGE_ID = "message_id"
    const val SENDER_ID = "sender_id"
    const val MILLIS = "millis"
    const val ROOM_ID = "room_id"
    const val REPORTED_STUDENT_ID = "reported_student_id"
    const val REPORTED_STUDENT_NAME = "reported_student_name"
    const val REPORT_TYPE = "report_type"
    const val REPORT_REASON = "report_reason"
    const val SG_DELETE_POPUP_OPEN = "sg_delete_popup_open"
    const val SG_BLOCK_POPUP_OPEN = "sg_block_popup_open"
    const val SG_REPORT_POPUP_OPEN = "sg_report_popup_open"
    const val SG_REPORT_OTHER_POPUP_OPEN = "sg_report_other_popup_open"
    const val SG_WARNING_DIALOG_OPEN = "sg_warning_dialog_open"
    const val SG_WARNING_DIALOG_CTA_CLICK = "sg_warning_dialog_cta_click"
    const val SG_WARNING_DIALOG_DISMISS = "sg_warning_dialog_dismiss"
    const val SG_WARNING_DIALOG_CROSS_CLICK = "sg_warning_dialog_cross_click"
    const val SG_ADMIN_DASHBOARD_OPEN = "sg_admin_dashboard_open"
    const val SG_USER_REPORTED_DASHBOARD_OPEN = "sg_user_reported_dashboard_open"
    const val SG_WARNING_STICKY_CLICK = "sg_warning_sticky_click"
    const val SG_REPORT_ICON_CLICK = "sg_report_icon_click"
    const val SG_CONTAINER_ID = "sg_container_id"
    const val SG_CONTAINER_TYPE = "sg_container_type"
    const val SG_IMAGE_AUTO_DOWNLOAD = "sg_image_auto_download"
    const val IS_ENABLED = "is_enabled"

    //  Study Group Notification
    const val SG_NOTIFICATION_DISPLAY = "sg_notification_display"

    // Notice Board
    const val PACKAGE_NAME = CoreEventConstants.PACKAGE_NAME
    const val APP_NAME = CoreEventConstants.APP_NAME
    const val CTA_TEXT = CoreEventConstants.CTA_TEXT
    const val CTA_TYPE = CoreEventConstants.CTA_TYPE
    const val DN_NB_ICON_VISIBLE = CoreEventConstants.DN_NB_ICON_VISIBLE
    const val DN_NB_ICON_CLICKED = CoreEventConstants.DN_NB_ICON_CLICKED
    const val DN_NB_STORY_VIEWED = CoreEventConstants.DN_NB_STORY_VIEWED
    const val DN_NB_CTA_CLICKED = CoreEventConstants.DN_NB_CTA_CLICKED
    const val DN_NB_NO_STORY_VISIBLE = CoreEventConstants.DN_NB_NO_STORY_VISIBLE
    const val DN_NB_PROFILE_SECTION_VISIBLE = CoreEventConstants.DN_NB_PROFILE_SECTION_VISIBLE
    const val DN_NB_PROFILE_ELEMENT_VIEWED = CoreEventConstants.DN_NB_PROFILE_ELEMENT_VIEWED
    const val DN_NB_PROFILE_ELEMENT_CLICKED = CoreEventConstants.DN_NB_PROFILE_ELEMENT_CLICKED

    const val URL = "url"
    const val HOME_WORK_PDF_SHARE = "home_work_pdf_share"
    const val HOME_WORK_PDF_DOWNLOAD = "home_work_pdf_download"

    const val INCREASE_VALIDITY_VIEW = "view_increase_validity"
    const val TEXT_SOLUTION_CTA_CLICK = "text_solution_cta_click"

    // Course Help
    const val COURSE_MENU_CLICK = "course_menu_click"
    const val SELECT_COURSE_DETAILS = "select_course_details_page_view"
    const val COURSE_DETAILS_CLASS_FILTER_APPLY = "select_course_details_filters_class_apply"
    const val COURSE_DETAILS_YEAR_FILTER_APPLY = "select_course_details_filters_year_apply"
    const val COURSE_DETAILS_EXAM_FILTER_APPLY = "select_course_details_filters_exam_apply"
    const val COURSE_DETAILS_FILTERS_MEDIUM_APPLY = "select_course_details_filters_medium"
    const val COURSE_CHANGE_POPUP_BUTTON_CLICK = "change_course_popup_button_click"
    const val COURSE_CHANGE_POPUP_VIEW = "change_course_popup_view"
    const val RECOMMENDED_COURSES_PAGE_VISIT = "recommended_courses"
    const val MEDIUM = "medium"
    const val CHANGE_COURSE_POPUP_CLOSED = "change_course_popup_closed"
    const val EXAM_YEAR = "exam_year"
    const val HELP_ICON_CLICK = "post_purchase_help_icon_click"

    // Pre Purchase Calling Card
    const val CALLING_CARD_CLICK = "calling_card_click"
    const val CARD_CHAT_CLICK = "card_chat_click"
    const val CARD_CALL_CLICK = "card_call_click"
    const val CARD_SHOWN = "card_shown"
    const val WEBVIEW_OPEN_ON_VIDEO_FAIL = "webview_open_on_video_fail"
    const val WIDGET_MEDIUM_SWITCH_CLOSE = "widget_medium_switch_close"

    // Ncert Video Page
    const val NCERT_BOOK_BOTTOM_SHEET_VISIBLE = "ncert_book_bottom_sheet_visible"

    // Anonymous Login
    const val ANONYMOUS_LOGIN = "anonymous_login"
    const val ANONYMOUS_LOGIN_PAGE_OPEN = "anonymous_login_page_open"
    const val ANONYMOUS_LOGIN_PAGE_SUCCESS = "anonymous_login_success"
    const val ANONYMOUS_LOGIN_PAGE_FAILURE = "anonymous_login_failure"
    const val ANONYMOUS_LOGIN_BLOCKER = "anonymous_login_blocker"
    const val ANONYMOUS_LOGIN_BLOCKER_LOGIN_CLICK = "anonymous_login_blocker_login_click"

    // Test Leaderboard
    const val TEST_LEADERBOARD_CARD_VIEW = "test_leaderboard_card_view"
    const val TEST_LEADERBOARD_CARD_CLICK = "test_leaderboard_card_click"
    const val TEST_LEADERBOARD_TAB_VIEW = "test_leaderboard_tab_view"
    const val TEST_LEADERBOARD_HELP_CLICK = "test_leaderboard_help_click"
    const val TEST_LEADERBOARD_GOTO_TESTS_CLICK = "test_leaderboard_goto_tests_click"
    const val RANK_WIDGET_CLICKED = "rank_widget_clicked"
    const val TEST_LEADERBOARD_USER_PROFILE_CLICKED = "test_leaderboard_user_profile_clicked"
    const val TAB_NAME = "tab_name"

    // Match page web results experiment
    const val GOOGLE_TAB_CLICK = "google_tab_click"

    const val COURSE_RECOMMENDATION_PAGE_VIEW = "course_recommendation_page_view"
    const val COURSE_RECOMMENDATION_CLOSE = "course_recommendation_close"
    const val COURSE_RECOMMENDATION_LAST_SHOWN_WIDGET = "course_recommendation_last_shown_widget"
    const val IS_BACK = "is_back"

    // course help
    const val COURSE_HELP_VIEW = "course_switch_view"
    const val COURSE_HELP_YES_CLICKED = "course_switch_yes_clicked"
    const val COURSE_HELP_NO_CLCKED = "course_switch_no_clicked"

    const val FEATURE = "feature"

    // Share Option Bottom Sheet
    const val SHARE_OPTIONS_BOTTOM_SHEET_SHOWN = "share_options_bottom_sheet_shown"
    const val SHARE_OPTIONS_WHATSAPP_CLICK = "share_options_whatsapp_click"
    const val SHARE_OPTIONS_SG_CLICK = "share_options_sg_click"
    const val SHARE_OPTIONS_DISMISS = "share_options_dismiss"
    const val MESSAGE_SHARED_TO_SG = "message_shared_to_sg"
    const val SHARED_CONTENT = "shared_content"

    const val EXO_PLAYER_FAILED = "player_failed"
    const val ERROR = "error"

    const val INSTALL_REFERRER_DATA = "install_referrer_data"
    const val INSTALL_REFERRER_URL = "install_referrer_url"

    const val CAMERA_BACK_PRESS_FRAGMENT_V2_PAGE_VIEW = "camera_back_press_fragment_v2_page_view"

    const val IMAGE = "image"

    const val PURCHASE_CATEGORY_TYPE = "purchase_category_type_"

    const val WHATSAPP_ADMIN_PAGE_OPEN = "whatsapp_admin_page_open"
    const val WHATSAPP_ADMIN_APPLY_CTA_CLICKED = "whatsapp_admin_apply_cta_clicked"
    const val WHATSAPP_ADMIN_FORM_SUBMIT = "whatsapp_admin_form_submit"

    // New PrePurchase Page
    const val NCP_DOWNLOAD_BROCHURE_TAPPED = "ncp_Download_brochure_tapped"
    const val NCP_SHARE_BROCHURE_TAPPED = "ncp_Share_brochure_tapped"
    const val NCP_SEATS_FILLING_FAST_TAPPED = "ncp_Seats_filling_fast_tapped"
    const val NCP_FAQ_TAPPED = "ncp_FAQs_front_tapped"
    const val NCP_BOOK_TRIAL_TAPPED = "ncp_Book_a_trial_tapped"
    const val NCP_COURSE_DURATION_TAPPED = "NCP_course_duration_tapped"
    const val NCP_COURSE_MEDIUM_TAPPED = "ncp_course_medium_tapped"
    const val NCP_CALL_TAPPED = "ncp_call_to_know_more_tapped"
    const val NCP_TAB_TAPPED = "ncp_tab_tapped"
    const val NCP_FREE_TRIAL_TAPPED = "ncp_one_day_free_trial_bottom_tapped"
    const val NCP_SYLLABUS_TAB_SUBJECT_TAPPED = "ncp_syllabus_tab_Subject_tag_tapped "
    const val NCP_SAMPLE_CONTENT_SUB_TAB_TAPPED = "ncp_sample_content_sub_tab_tapped"
    const val NCP_HELP_CLICK = "ncp_help_floated_clicked"
    const val PACKAGE_DETAIL_V4_ITEM_CLICKED = "package_detail_v4_item_clicked"
    const val BORDER_BUTTON_CLICKED = "border_button_clicked"
    const val NCP_VIEW_PLAN_TAPPED = "ncp_view_plan_tapped"
    const val NCP_CONTENT_FILTER_ITEM_CLICKED = "ncp_content_filter_item_clicked"
    const val NCP_CHOOSE_PLAN_ITEM_CLICKED = "ncp_choose_plan_item_clicked"

    const val COURSE_PAGE_EXIT = "course_page_exit"
    const val LIVE_CLASS_VIDEO_SHARE = "live_class_video_share"
    const val GENERIC_CARD_CLICK = "generic_card_click"
    const val FACULTY_CARD_CLICKED = "faculty_card_clicked"
    const val NCP_HELP_CLICKED = "ncp_help_button_tapped"
    const val NCP_TRIAL_BUTTON_TOP_CLICKED = "ncp_trial_button_top_clicked"

    // MPVP Popular courses and bottom sheet
    const val MPVP_COURSE_BANNER_SHOWN = "mpvp_course_banner_shown"
    const val MPVP_COURSE_BANNER_USER_SWIPE = "mpvp_course_banner_user_swipe"
    const val MPVP_COURSE_BANNER_TAPPED = "mpvp_course_banner_tapped"
    const val MPVP_COURSE_BOTTOMSHEET_SHOWN = "mpvp_course_bottomsheet_shown"
    const val MPVP_COURSE_BOTTOMSHEET_EXPANDED = "mpvp_course_bottomsheet_expanded"
    const val MPVP_COURSE_BOTTOMSHEET_TRIAL_CLICKED = "mpvp_course_bottomsheet_trial_clicked"
    const val MPVP_COURSE_BOTTOMSHEET_BUY_NOW_CLICKED = "mpvp_course_bottomsheet_buy_now_clicked"
    const val MPVP_COURSE_BOTTOMSHEET_CALL_US_CLICKED = "mpvp_course_bottomsheet_call_us_clicked"
    const val MPVP_COURSE_BOTTOMSHEET_SEATS_CLICKED = "mpvp_course_bottomsheet_seats_clicked"
    const val MPVP_COURSE_BOTTOMSHEET_INTRO_VIDEO_CLICKED =
        "mpvp_course_bottomsheet_intro_video_clicked"

    // (Kota class case)
    const val MPVP_COURSE_BOTTOMSHEET_NOT_DISPLAYED = "mpvp_course_bottomsheet_not_displayed"

    const val EXAM_CORNER_FRAGMENT_VIEW = "exam_corner_fragment_view"
    const val EXAM_CORNER_BOOKMARK_SCREEN_VIEW = "exam_corner_bookmark_page_view"
    const val EXAM_CORNER_BOOKMARK_CLICK = "exam_corner_click"
    const val VPA_WHATSAPP_SHARE = "vpa_whatsapp_share"

    // New Explore Page
    const val EXPLORE_PAGE_STRIP_PREVIEW_SHOWN = "explore_page_strip_preview_shown"
    const val EXPLORE_PAGE_STRIP_PREVIEW_BUY_NOW_CLICKED =
        "explore_page_strip_preview_buy_now_clicked"
    const val EXPLORE_PAGE_STRIP_PREVIEW_CALL_CLICKED = "explore_page_strip_preview_call_clicked"
    const val EXPLORE_PAGE_STRIP_PREVIEW_TRIAL_CLICKED = "explore_page_strip_preview_trial_clicked"
    const val CATEGORY_WIDGET_CLICKED = "category_widget_clicked"
    const val TESTIMONIAL_WIDGET_CLICKED = "testimonial_widget_clicked"
    const val POPULAR_COURSE_CLICKED = "popular_course_clicked"
    const val POPULAR_COURSE_CTA_CLICKED = "popular_course_cta_clicked"
    const val POPULAR_COURSE_TAG_CLICKED = "popular_course_tag_clicked"
    const val CTA_TITLE = "cta_title"
    const val COURSE_V4_CLICKED = "course_v4_clicked"
    const val COURSE_V4_CTA_CLICKED = "course_v4_cta_clicked"
    const val COURSE_V4_TAG_CLICKED = "course_v4_tag_clicked"
    const val COURSE_RECOMMENDATION_CTA_CLICKED = "course_recommendation_cta_clicked"
    const val COURSE_RESOURCE_CTA_CLICKED = "course_resource_cta_clicked"
    const val COURSE_RECOMMENDATION_CLICKED = "course_recommendation_clicked"
    const val RECOMMENDED_TEST_CLICKED = "recommended_test_clicked"
    const val RECOMMENDED_TEST_CTA_CLICKED = "recommended_test_cta_clicked"
    const val VIEWED = "viewed"
    const val CLICKED = "clicked"
    const val SCROLL = "scroll"
    const val CARD_VIEWED = "card_viewed"
    const val CARD_CLICKED = "card_clicked"
    const val MORE_CLICKED = "more_clicked"
    const val TAG_CLICKED = "tag_clicked"
    const val TAB_CLICKED = "tab_clicked"
    const val BUTTON_CLICKED = "button_clicked"
    const val CROSS_CLICKED = "cross_clicked"
    const val BACK_CLICKED2 = "back_clicked"
    const val DROP_DOWN_CLICKED = "drop_down_clicked"
    const val PROFILE_CLICKED = "profile_clicked"
    const val KNOW_MORE_CLICKED = "know_more_clicked"
    const val EXPLORE_PROMO_CLICKED = "explore_promo_clicked"
    const val CONTINUE_BUYING_CTA_CLICKED = "continue_buying_cta_clicked"
    const val VIDEO_WIDGET_CLICKED = "video_widget_clicked"
    const val WALLET_WIDGET_CLICKED = "wallet_widget_clicked"
    const val WALLET_WIDGET_CTA_CLICKED = "wallet_widget_cta_clicked"
    const val VIP_USER = "vip_user"
    const val WHATSAPP_ICON_CLICKED = "whatsapp_icon_clicked"
    const val DICTIONARY_ICON_CLICKED = "whatsapp_icon_clicked"
    const val RECOMMENDATION_WIDGET_CTA_CLICKED = "recommendation_widget_cta_clicked"

    const val NAVIGATE_FROM_CAMPAIGN = "navigate_from_campaign"
    const val VIEW_ID = "view_id"
    const val VIEWED_APPEND = "_viewed"
    const val REMOVED_APPEND = "_removed"
    const val VIEWED_DURATION_APPEND = "_viewed_duration"

    const val WATCH_NOW_BUTTON_CLICKED = "watch_now_button_clicked"

    // Revision Corner
    const val RC_DPP_BANNER_CLICK = "rc_dpp_banner_click"
    const val RC_SHORT_TEST_SUBJECT_CLICK = "rc_short_test_subject_click"
    const val RC_SHORT_TEST_CHAPTER_SELECTED = "rc_short_test_chapter_selected"
    const val RC_TOP_100_QUESTION_CLICK = "rc_top_100_question_click"
    const val RC_TOP_100_QUESTION_VIEW_ALL_CLICK = "rc_top_100_question_view_all_click"
    const val RC_INFO_CLICK = "rc_info_click"
    const val RC_FULL_TEST_CLICK = "rc_full_test_click"
    const val RC_BANNER_WIDGET_CLICK = "rc_banner_widget_click"
    const val RC_PERFORMANCE_REPORT_ICON_CLICK = "rc_performance_report_icon_click"
    const val RC_START_TEST_CLICK = "rc_start_test_click"
    const val RC_FORMULA_CLICK = "rc_formula_click"
    const val RC_FORMULA_VIEW_ALL_CLICK = "rc_formula_view_all_click"
    const val RC_TEST_SUBMITED = "rc_test_submited"
    const val RC_WATCH_SOLUTION_CLICK = "rc_watch_solution_click"
    const val RC_SOLUTION_VIDEO_CLICK = "rc_solution_video_click"
    const val RC_VIEW_ALL_PREVIOUS_SOLUTIONS = "rc_view_all_previous_solutions"
    const val RC_TEST_INSTRUCTION_PAGE_SHOWN = "rc_test_instruction_page_shown"
    const val RC_FULL_LENGHT_TEST = "rc_full_length_test"
    const val RC_PAGE_VISIT = "rc_page_visit"
    const val RC_ICON_CLICK = "rc_icon_click"
    const val COURSE_RECOMMENDATION_SEE_ALL_CLICKED = "course_recommendation_see_all_clicked"

    const val IS_SELF = "is_self"

    // Mock Test
    const val MOCK_TEST_ANALYSIS_VIEW = "mock_test_analysis_view"
    const val TEST_ANALYSIS_EXPAND_COLLAPSE = "test_analysis_expand_collapse"
    const val TEST_ANALYSIS_LIST_ITEM_CLICK = "test_analysis_list_item_click"
    const val TEST_ANALYSIS_BUTTON_CLICK = "test_analysis_button_click"
    const val REVIEW_BUTTON_CLICK = "mark_for_review_button_click"
    const val TEST_REVIEW_POPUP_VIEW = "test_review_pop_up_view"
    const val TEST_REVIEW_POPUP_CLICK = "test_review_popup_click"

    const val QUIZ_TFS_PAGE_VIEW = "quiz_tfs_page_view"
    const val QUIZ_TFS_CHAT_OPEN = "quiz_tfs_chat_open"
    const val QUIZ_TFS_QUESTION_SHOWN = "quiz_tfs_question_shown"
    const val QUIZ_TFS_ANSWER_SUBMITTED = "quiz_tfs_answer_submitted"
    const val IS_USER_CLICK = "is_user_click"

    const val VIDEO_WATCH_ENGAGEMENT = "video_watch_engagement"

    const val AWARDED_STUDENTS_WIDGET_CLICKED = "awarded_students_widget_clicked"
    const val VIDEO_PLAYLIST_CAMERA_ICON_CLICK = "video_playlist_camera_icon_click"

    // Teacher Channel
    const val TEACHER_CAROUSEL_VIEWED = "teacher_carousel_viewed"
    const val TEACHER_CAROUSEL_CLICKED = "teacher_carousel_clicked"
    const val TEACHER_CAROUSEL_CTA_TAPPED = "teacher_carousel_cta_tapped"
    const val SUBSCRIBED_TEACHER_CAROUSEL_CTA_CLICKED = "subscribed_teacher_carousel_cta_clicked"

    const val TEACHER_PAGE_OPEN = "teacher_page_open"
    const val TEACHER_PAGE_RESOURCE_CLICKED = "teacher_page_resource_clicked"
    const val TEACHER_PAGE_TAB_CLICKED = "teacher_page_tab_clicked"
    const val TEACHER_PAGE_CONTENT_TYPE_FILTER_CLICKED = "teacher_page_content_type_filter_clicked"
    const val TEACHER_PAGE_SUBTAB_FILTER_CLICKED = "teacher_page_subtab_filter_clicked"
    const val TEACHER_PAGE_SUBCRIBED_CLICKED = "teacher_page_subcribed_clicked"
    const val TEACHER_PROFILE_PAGE_OPEN = "teacher_profile_page_open"

    const val TEACHER_PAGE_SIMILAR_VIDEO_VIEWED = "teacher_page_similar_video_viewed"
    const val IS_SUBSCRIBED = "is_subscribed"
    const val WIDGET_SUBSCRIBED_TEACHER_CHANNEL = "WIDGET_SUBSCRIBED_TEACHER_CHANNEL"
    const val WIDGET_SUGGESTED_TEACHER_CHANNEL = "WIDGET_SUGGESTED_TEACHER_CHANNEL"

    // Study Timer
    const val ST_MUSIC_LIST_OPENED = "st_music_list_opened"
    const val ST_MUSIC_PLAYED = "st_music_played" // (param - song name)
    const val ST_MUSIC_CHANGED = "st_music_changed"
    const val ST_TIMER_STARTED = "st_timer_started"
    const val ST_BREAK_STARTED = "st_break_started"
    const val ST_TIMER_RESET_CLICK = "st_timer_reset_click"
    const val ST_CONTINUE_CLICK = "st_continue_click"
    const val ST_TIMER_RESUMED = "st_timer_resumed"
    const val ST_LABEL_EDITED = "st_label_edited"
    const val ST_STATS_OPENED = "st_stats_opened"
    const val ST_TIMER_OPENED = "st_timer_opened" // (source - topicon,hamburger,video_banner)
    const val ST_NOTIF_TIMER_PAUSED = "st_notif_timer_paused"
    const val ST_NOTIF_TIMER_RESET = "st_notif_timer_reset"
    const val ST_NOTIF_TIMER_RESUMED = "st_notif_timer_resumed"
    const val ST_STOP_POPUP = "st_stop_popup"
    const val ST_STOP_POPUP_CONTINUE_CLICK = "st_stop_popup_continue_click"
    const val ST_STOP_POPUP_STOPTIMER_CLICK = "st_stop_popup_stoptimer_click"
    const val ST_TIMER_TIME = "st_timer_time"
    const val ST_TIMER_STATE = "st_timer_state"

    const val GOOGLE_AUTH_PAGE_OPEN = "google_auth_page_open"
    const val GOOGLE_AUTH_SUCCESS = "google_auth_success"
    const val GOOGLE_AUTH_FAILURE = "google_auth_failure"
    const val GOOGLE_AUTH_CLICK = "google_auth_click"

    // Doubtnut Rupya
    const val REDEEM_ID = "redeem_id"
    const val VOUCHER_ID = "voucher_id"
    const val APP_BAR_DNR_ICON_CLICK = "app_bar_dnr_icon_click"
    const val DNR_INTRO_POPUP_CTA_CLICK = "dnr_intro_popup_cta_click"
    const val DNR_HOME_WIDGET_CLICK = "dnr_home_widget_click"
    const val DNR_HOME_PAGE_VISIT = "dnr_home_page_visit"
    const val DNR_FAQ_CLICKED = "dnr_faq_clicked"
    const val DNR_TC_CLICKED = "dnr_tc_clicked"
    const val DNR_VOUCHER_UNLOCK_CLICKED = "dnr_voucher_unlock_clicked"
    const val DNR_VOUCHER_DETAIL_OPEN = "dnr_voucher_detail_open"
    const val DNR_MYSTERY_BOX_OPEN = "dnr_mystery_box_open"
    const val DNR_SPINNER_OPEN = "dnr_spinner_open"
    const val DNR_REDEEM_NOW_CLICKED = "dnr_redeem_now_clicked"
    const val DNR_WIDGET_LIST_OPEN = "dnr_widget_list_open"
    const val DNR_REWARD_CLAIM = "dnr_reward_claim"
    const val FEED_DNR_ICON_CLICK = "feed_dnr_icon_click"
    const val IS_FAV = "is_fav"

    // Doubt Bookmark
    const val DOUBT_BOOKMARKED = "Doubt_bookmarked"
    const val DOUBT_UNBOOKMARKED = "doubt_unbookmarked"
    const val BOOKMARK_CHAPTER_EXPAND = "bookmark_chapter_expand"
    const val BOOKMARK_CHAPTER_COLLAPSE = "bookmark_chapter_collapse"
    const val BOOKMARK_VIEW_SOLUTION = "Bookmark_view_solution"
    const val BOOKMARK_GOTO_CLASS = "Bookmark_goto_class"
    const val SELF_DOUBT = "self_doubt"

    // Video V2
    const val VIDEO_TAB_CLICKED = "video_page_tab_click"
    const val VIDEO_PAGE_VERTICAL_TIMESTAMP_CLICK = "video_page_vertical_timestamp_click"
    const val VIDEO_PAGE_HORIZONTAL_TIMESTAMP_CLICK = "video_page_horizontal_timestamp_click"
    const val VIDEO_PAGE_ICON_CLICK = "video_page_icon_click"
    const val VIDEO_PAGE_PLAYLIST_CLICK = "video_page_playlist_click"
    const val VIDEO_PAGE_PLAYLIST_CLOSE = "video_page_playlist_close"
    const val VIDEO_PAGE_NEXT_CLICK = "video_page_next_click"

    const val EXPLORE_PROMO_VIEWED = "explore_promo_viewed"
    const val VIEW_INSIDE_PARENT_WIDGET = "view_inside_parent_widget"
    const val EVENT_VIEW_ADDED = "view_added"
    const val EVENT_VIEW_HIDDEN = "view_hidden"
    const val EVENT_VIEW_IMPRESSION = "view_impression"

    const val CHECKOUT_PAGE_OPEN = "checkout_page_open"
    const val SCREEN_VIEW = "screen_view"

    const val AUTO_REDIRECT = CoreEventConstants.AUTO_REDIRECT

    // Practice English
    const val PE_AUDIO_RECORD = "pe_audio_record"
    const val PE_AUDIO_PLAY = "pe_audio_play"
    const val PE_BUTTON_CLICK = "pe_button_click"
    const val PE_OPTION_CLICK = "pe_option_click"
    const val PE_PAGE_VIEW = "pe_page_view"
    const val QUESTION = "question"
    const val MCQ = "mcq"
    const val RESPONSE = "response"
    const val TRY_AGAIN = "try_again"
    const val CORRECT_ANSWER = "correct_answer"
    const val NEXT = "next"
    const val SUBMIT = "submit"
    const val PRACTICE_MORE = "practice_more"
    const val SESSION_REPEAT = "session_repeat"
    const val REMIND = "remind"
    const val SESSION_END = "session_end"
    const val WORD_BUBBLE = "word_bubble"
    const val AUDIO_ID = "audio_id"
    const val BANNER_WIDGET_CLICK = "banner_widget_click"
    const val PE_LAYOUT_CLICK = "pe_layout_click"

    const val TEACHER_FRAGMENT_PAGE_VIEW = "teacher_fragment_page_view"
    const val PRACTICE_ENGLISH_CLICKED = "practice_english_clicked"
    const val PRACTICE_ENGLISH_PAGE_VIEW = "practice_english_view"

    //free_course_list
    const val FREE_COURSE_LIST_OPENED = "free_course_list_page_opened"
    const val FREE_COURSE_CARD_CLICKED = "free_course_card_list"
    const val FREE_COURSE_CARD_CONFIRMATION_YES_CLICKED = "free_course_confirmation_yes_clicked"
    const val FREE_COURSE_CARD_CONFIRMATION_NO_CLICKED = "free_course_confirmation_no_clicked"
    const val FREE_COURSE_CARD_CONFIRMATION_CLOSE_CLICKED = "free_course_confirmation_close_clicked"
    const val FREE_COURSE_SUCCESS_DIALOG_OPENED = "free_course_activation_success_dialog_opened"
    const val FREE_COURSES_OPEN_COURSE_CLICKED = "free_courses_card_open_course_button_clicked"
    const val FREE_COURSE_ACTIVATE_WIDGET_CLICKED = "free_courses_activate_widget_clicked"
    const val FREE_CLASSES = "free_classes"

    const val TAB_CLICK = "tab_click"
    const val VIDEO_FAB_CLICK = "video_fab_click"
    const val NETWORK_STATS_ACCESSED = "network_stats_accessed"

    //video stats event
    const val LFET_20M_7d = "lfet_20m_7d"
    const val LFET_10M_3d = "lfet_10m_3d"
    const val LFET_5M_1d = "lfet_5m_1d"
    const val LFVV_4_7d = "lfvv_4_7d"
    const val SFVV_3_1d = "sfvv_3_1d"
    const val SFVV_6_2d = "sfvv_6_2d"
    const val LFVV_4_3d = "lfvv_4_3d"
    const val LFVV_2_1d = "lfvv_2_1d"
    const val LFVV_2_3d = "lfvv_2_3d"
    const val CONS_LF_2d = "cons_lf_2d"
    const val CONS_SF_3d = "cons_sf_3d"

    const val REFERRAL_STEPS_WIDGET = "referral_steps_widget_clicked"
    const val REFERRAL_LEVEL_WIDGET = "referral_level_widget_clicked"
    const val SHORTS_FRAGMENT_PAGE_VIEW = "shorts_fragment_page_view"

    const val CLICK = "click"
    const val LIKE = "like"
    const val COMMENT = "comment"
    const val SHARE = "share"
    const val BOOKMARK = "bookmark"
    const val IS_BOOKMARK = "is_bookmark"

    const val SHORTS_BOTTOM_NAV_HOME_CLICKED = "shorts_bottom_nav_home_clicked"
    const val SHORTS_BOTTOM_NAV_LIBRARY_CLICKED = "shorts_bottom_nav_library_clicked"
    const val SHORTS_BOTTOM_NAV_CAMERA_CLICKED = "shorts_bottom_nav_camera_clicked"
    const val SHORTS_BOTTOM_NAV_FEED_CLICKED = "shorts_bottom_nav_feed_clicked"
    const val SHORTS_BOTTOM_NAV_PROFILE_CLICKED = "shorts_bottom_nav_profile_clicked"
    const val SHORTS_EXIT = "shorts_exit"
    const val SHORTS_VIDEO_PLAYED_STATUS = "shorts_video_played_status"
    const val VIDEO_PLAYED = "video_played"
    const val VIDEO_PLAYED_COUNT = "video_played_count"
    const val VIDEO_VIEW_ON_SCREEN_COUNT = "video_view_on_screen_count"
    const val VIDEO_PERCENT_PLAYED = "video_percent_played"

    const val IS_NOTIFY = "is_notify"
    const val FEATURE_STUDY_GROUP = "study_group"
    const val FEATURE_FRESHWORKS = "fresh_works"
}
