package com.doubtnutapp.deeplink

enum class AppActions(private val host: String) {

    /*
    doubtnutapp://video?qid=141657785&page=HOME_FEED&playlist_id=BOOKS_80&video_start_position=234&create_new_instance=true
     */
    VIDEO("video"),

    /*
    doubtnutapp://vod_comment?qid=643948786&page=COURSE_LANDING&doubt_comment_id=608655d91cb2e211d9185ed2
     */
    VOD_COMMENT("vod_comment"),

    /*
    doubtnutapp://all_courses?ecm_id=4&subject=physics
     */
    ALL_COURSES("all_courses"),

    /*
    doubtnutapp://dictionary
     */
    DICTIONARY("dictionary"),

    /*
   doubtnutapp://feeds
   */
    FEEDS("feeds"),

    /*
    doubtnutapp://post_detail?post_id=5ec38bf9106abf4399043bda
     */
    POST_DETAIL("post_detail"),

    /*
    doubtnutapp://feed_post?post_id=5ec38bf9106abf4399043bda
     */
    FEED_POST("feed_post"),

    /*
    doubtnutapp://dialer?mobile=1234567890
     */
    DIALER("dialer"),

    STICKY_NOTIFICATION("sticky_notification"),

    /*
    doubtnutapp://match_page?uploaded_image_question_id=575998645&ask_que_url=https://d10lpgp6xz60nq.cloudfront.net/images/uploads_54894480_1609439405.png
    */
    MATCH_PAGE("match_page"),
    MATCH_NOTIFICATION("match_notification"),
    MATCH_OCR_NOTIFICATION("match_ocr_notification"),
    VIDEO_STICKY_NOTIFICATION("video_sticky_notification"),

    /*
    doubtnutapp://vip?assortment_id=42&variant_id=39&coupon_code=AQWDSHKL123&switch_assortment=""
     */
    VIP("vip"),

    /*
    doubtnutapp://my_plan
     */
    MY_PLAN("my_plan"),

    /*
    doubtnutapp://payment_upi_select?variant_id=39&upi_package=com.google.android.apps.nbu.paisa.user
     */
    PAYMENT_UPI_SELECT("payment_upi_select"),

    /*
    doubtnutapp://course_video?qid=10832&page=E_FACULTY
     */
    COURSE_VIDEO("course_video"),

    /*
    doubtnutapp://library_course
     */
    LIBRARY_COURSE("library_course"),

    /*
    doubtnutapp://time_table?course_id=24
     */
    TIME_TABLE("time_table"),

    /*
    doubtnutapp://redeem_store
     */
    REDEEM_STORE("redeem_store"),

    /*
    doubtnutapp://change_language
     */
    CHANGE_LANGUAGE("change_language"),

    /*
    doubtnutapp://change_class
     */
    CHANGE_CLASS("change_class"),

    @Deprecated("Moved to structured course")
    LIVE_CLASSES("live_classes"),

    /*
    Structured course detail
    doubtnutapp://detail_live_classes?course_id=9&course_detail_id=2677&subject=Chemistry
     */
    DETAIL_LIVE_CLASSES("detail_live_classes"),

    /*
    doubtnutapp://camera
     */
    CAMERA("camera"),

    /*
    doubtnutapp://test_series
     */
    TEST_SERIES("test_series"),

    /*
    doubtnutapp://doubtnut_stickers
     */
    DOUBTNUT_STICKERS("doubtnut_stickers"),

    /*
    doubtnutapp://group_chat
     */
    GROUP_CHAT("group_chat"),

    /*
    doubtnutapp://pdf_viewer?pdf_url=https://d10lpgp6xz60nq.cloudfront.net/pdf_download/JM_2019_ALL.pdf
     */
    PDF_VIEWER("pdf_viewer"),

    /*
    doubtnutapp://formula_sheet
     */
    FORMULA_SHEET("formula_sheet"),

    /*
    doubtnutapp://camera_guide
     */
    CAMERA_GUIDE("camera_guide"),

    /*
    doubtnutapp://quiz
     */
    QUIZ("quiz"),

    /*
    doubtnutapp://downloadpdf
     */
    DOWNLOADPDF("downloadpdf"),

    /*
    doubtnutapp://download_pdf
     */
    DOWNLOAD_PDF("download_pdf"),

    /*
    doubtnutapp://downloadpdf_level_one?pdf_package=FORMULA%20SHEET
     */
    DOWNLOADPDF_LEVEL_ONE("downloadpdf_level_one"),

    /*
    doubtnutapp://downloadpdf_level_two?pdf_package=&level_one=CHEMISTRY
     */
    DOWNLOADPDF_LEVEL_TWO("downloadpdf_level_two"),

    /*
    doubtnutapp://download_pdf_level_one?pdf_package=FORMULA%20SHEET
     */
    DOWNLOAD_PDF_LEVEL_ONE("download_pdf_level_one"),

    /*
   doubtnutapp://download_pdf_level_two?pdf_package=&level_one=CHEMISTRY
    */
    DOWNLOAD_PDF_LEVEL_TWO("download_pdf_level_two"),

    /*
    doubtnutapp://daily_contest
     */
    DAILY_CONTEST("daily_contest"),

    /*
    doubtnutapp://daily_contest_with_contest_id?contest_id=1
     */
    DAILY_CONTEST_CONTEST_ID("daily_contest_with_contest_id"),

    /*

     */
    TAG("tag"),

    /*
    doubtnutapp://learn_chapter?class=10&chapter=&course=test
     */
    LEARN_CHAPTER("learn_chapter"),

    /*
    doubtnutapp://course
     */
    COURSE("course"),

    /*
    doubtnutapp://community_question?qid=1000
     */
    @Deprecated("No longer supported")
    COUUMUNITY_QUESTION("community_question"),

    /*
    doubtnutapp://profile
    doubtnutapp://profile?student_id=123456&source=leaderboard
     */
    PROFILE("profile"),

    /*
    doubtnutapp://edit_profile?refresh_home_feed=true
     */
    EDIT_PROFILE("edit_profile"),

    /*
    doubtnutapp://playlist?playlist_id=119497&playlist_title=MHTCET%202020&is_last=0&page=SRP&package_details_id=LIBRARY_NEW_BOOK_-130_12_PHYSICS
     */
    PLAYLIST("playlist"),

    /*
    doubtnutapp://user_journey
     */
    USER_JOURNEY("user_journey"),

    /*
    doubtnutapp://external_url?url=google.com
     */
    EXTERNAL_URL("external_url"),

    /*
    doubtnutapp://topic?playlist_id=116733&playlist_title=Test
     */
    TOPIC("topic"),

    /*
    doubtnutapp://ncert?playlist_id=116733&playlist_title=Test&page=NCERT
    */
    NCERT("ncert"),

    /*
    doubtnutapp://books?playlist_id=116733&playlist_title=Test
    */
    BOOKS("books"),

    /*
    doubtnutapp://topic_parent?playlist_id=116733&playlist_title=Test
    */
    TOPIC_PARENT("topic_parent"),

    /*
    doubtnutapp://gamification_badge?sid=373774
     */
    GAMIFICATION_BADGE("gamification_badge"),

    /*
    doubtnutapp://daily_streak_badge
     */
    DAILY_STREAK_BADGE("daily_streak_badge"),

    /*
    doubtnutapp://live-voice-call?chatRoomId=5ea32449e5d4fd1470329b21&chatRoomName=Test&chatRoomColor=#32c5ff&chatRoomLanguage=English&chatRoomAdminId=7324203
     */
    LIVE_VOICE_CALL("live-voice-call"),

    /*
    doubtnutapp://feed_details?is_last=0&type=viral_videos&id=55515
     */
    @Deprecated("New feed should be used")
    OLD_FEED_DETAILS("feed_details"),

    /*
    doubtnutapp://library_tab?id=1
    doubtnutapp://library_tab?library_screen_selected_Tab=1
    doubtnutapp://library_tab?tag=mock_test

    tag has the highest preference
    doubtnutapp://library_tab?id=2&tag=mock_test
     */
    LIBRARY_TAB("library_tab"),

    /*
    doubtnutapp://dn_games
     */
    DN_GAMES("dn_games"),

    PERSONALIZE("personalize"),

    PERSONALIZE_CHAPTER("personalize_chapter"),

    /*
    doubtnutapp://live_class?id=1&page=similar&source_id=123
     */
    LIVE_CLASS("live_class"),

    /*
    doubtnutapp://live_class_home
     */
    LIVE_CLASS_HOME("live_class_home"),

    /*
    doubtnutapp://course_iit
     */
    COURSE_IIT("course_iit"),

    /*
    doubtnutapp://schedule
     */
    SCHEDULE("schedule"),

    /*
    doubtnutapp://course_neet
     */
    COURSE_NEET("course_neet"),

    /*
   doubtnutapp://course_details?id=9477&class=10&bottom_sheet=false
   doubtnutapp://course_details?id=scholarship_test_DNST19
    */
    COURSE_DETAILS("course_details"),

    /*
   doubtnutapp://resource?id=default?recorded=1
    */
    RESOURCE("resource"),

    /*
   doubtnutapp://mock_test_subscribe?id=1&source=xyz&exam_type=xyz
    */
    MOCK_TEST_SUBSCRIBE("mock_test_subscribe"),

    /*
   doubtnutapp://live_section?id=1?course_id=12
    */
    LIVE_SECTION("live_section"),

    /*
    doubtnutapp://games?game_title=Ohh1!&game_url=https://d10lpgp6xz60nq.cloudfront.net/dn-games/ohh1.zip
     */
    GAME("games"),

    /*
    doubtnutapp://feed_liveposts?type=upcoming
     */
    FEED_LIVE_POST("feed_liveposts"),

    /*
   doubtnutapp://go_live
    */
    GO_LIVE("go_live"),

    /*
   doubtnutapp://vip_detail
    */
    VIP_DETAIL("vip_detail"),

    /*
    doubtnutapp://web_view?chrome_custom_tab=true&url=https://www.google.com
     */
    WEB_VIEW("web_view"),

    /*
    * doubtnutapp://in_app_search?search_query=sample_search
    * */
    IN_APP_SEARCH("in_app_search"),

    /*
    * doubtnutapp://doubt_package
    * */
    DOUBT_PACKAGE("doubt_package"),

    /*
    * doubtnutapp://in_app_search_landing?selected_class_no=12&selected_class_name=Class 12
    * */
    IN_APP_SEARCH_LANDING("in_app_search_landing"),

    /*
   doubtnutapp://live_class_chat?assortment_id=12?variant_id=123?amount=120?title=Group Chat&room_type=study_dost?is_trial=true
    */
    LIVE_CLASS_CHAT("live_class_chat"),

    /*
    doubtnutapp://course_category?category_id=12?title=SomeTitle?filters=92
    */
    COURSE_CATEGORY("course_category"),

    /*
    doubtnutapp://wallet
    */
    WALLET("wallet"),

    /*
    doubtnutapp://resource_list?id=12?subject=maths
    */
    RESOURCE_LIST("resource_list"),

    /*
   doubtnutapp://apb_cash_payments
   */
    APB_CASH_PAYMENTS("apb_cash_payments"),

    /*
    doubtnutapp://daily_topper
    */
    DAILY_TOPPER("daily_topper"),

    /*
    doubtnutapp://my_downloads
    */
    MY_DOWNLOADS("my_downloads"),

    /*
    doubtnutapp://whatsapp?external_url=https://api.whatsapp.com/send?phone=918400400400&text=How%20to%20ask%20Doubt%3F
    */
    WHATSAPP("whatsapp"),

    /**
     * doubtnutapp://home?recreate=false
     *
     * recreate: true is required for "home" action
     * doubtnutapp://home?scroll_to_id=widget_attendance_id&recreate=true
     */
    HOME("home"),

    /*
    doubtnutapp://homework?qid=1234&is_video_page=true
    */
    HOME_WORK("homework"),

    /*
    doubtnutapp://homework_solution?qid=1234
    */
    HOME_WORK_SOLUTION("homework_solution"),

    /*
    doubtnutapp://homework_list
    */
    HOME_WORK_LIST("homework_list"),

    /*
    * doubtnutapp://referral_page
    */
    REFERRAL_PAGE("referral_page"),

    /*
    * doubtnutapp://referral
    */
    REFERRAL("referral"),

    /*
   * doubtnutapp://share_referral?share_message=Message&share_contact_batch_size=200
   */
    SHARE_REFERRAL("share_referral"),

    /*
    *  doubtnutapp://transaction_history
    */
    TRANSACTION_HISTORY("transaction_history"),

    /*

    *  doubtnutapp://faq?bucket=feature_explainer?priority=2&bucket=
    */
    FAQ("faq"),

    /*
    *  doubtnutapp://course_detail_info?assortment_id=12&tab=recent&subject=maths
    */
    COURSE_DETAIL_INFO("course_detail_info"),

    /*
    *  doubtnutapp://bundle_dialog?id=1234?source=page
    */
    BUNDLE_DIALOG("bundle_dialog"),

    /*
    *  doubtnutapp://course_select?source=""
    */
    COURSE_SELECTION_DIALOG("course_select"),

    /**
     * doubtnutapp://rewards
     */
    REWARDS("rewards"),

    /*
    *  doubtnutapp://video_dialog?question_id=1234&orientation=portrait&page=somePage
    */
    VIDEO_DIALOG("video_dialog"),

    /**
     * doubtnutapp://subject_detail?subject=Maths&assortment_id=1234
     */
    SUBJECT_DETAL("subject_detail"),

    /**
     * doubtnutapp://topic_booster_game?qid=12345
     */
    TOPIC_BOOSTER_GAME("topic_booster_game"),

    /*
    * doubtnutapp://study_group?is_study_group_exist=false
    */
    STUDY_GROUP("study_group"),

    /*
    doubtnutapp://study_group_chat?group_id=123&is_admin=true&is_faq=true&invitor=12345&invitee=456&is_support=false
    */
    STUDY_GROUP_CHAT("study_group_chat"),

    /*
    * doubtnutapp://study_group_v2/info?group_id=123
    * (Multiple deeplinks for different destinations using Navigation component)
    */
    STUDY_GROUP_V2("study_group_v2"),

    /*
    doubtnutapp://doubt_pe_charcha?room_id=4242&is_host=true&is_reply=true&is_message=true
    */
    DOUBT_PE_CHARCHA("doubt_pe_charcha"),

    /*
    doubtnutapp://doubt_p2p_collection?primary_tab_id=1&secondary_tab_id=1&subjects=MATHS,CHEMISTRY&languages="en"
    doubtnutapp://doubt_p2p_collection?primary_tab_id=1&secondary_tab_id=1
    */
    DOUBT_P2P_COLLECTION("doubt_p2p_collection"),

    /*
    doubtnutapp://dnr
    */
    DOUBTNUT_RUPYA("dnr"),

    /*
    doubtnutapp://community_guidelines?source=study_group
    */
    COMMUNITY_GUIDELINES("community_guidelines"),

    /**
     * doubtnutapp://full_screen_image?ask_que_uri=https://image
     * */
    FULL_SCREEN_IMAGE("full_screen_image"),

    /**
     * doubtnutapp://chat_support
     * */
    CHAT_SUPPORT("chat_support"),

    /**
     * doubtnutapp://app_survey?survey_id=12345
     */
    APP_SURVEY("app_survey"),

    /**
     * doubtnutapp://video_url?url=https://video
     */
    VIDEO_URL("video_url"),

    /**
     * doubtnutapp://doubt_feed
     */
    DOUBT_FEED("doubt_feed"),

    /**
     *doubtnutapp://action_web_view?url=https://www.google.com
     */
    ACTION_WEB_VIEW("action_web_view"),

    /**
     * doubtnutapp://nudge_popup?nudge_id=1234&nudge_type=""
     */
    NUDGE_POPUP("nudge_popup"),

    /**
     * doubtnutapp://course_change?type="wdkdw"&selected_assortment=""
     */
    COURSE_CHANGE("course_change"),

    /**
     * doubtnutapp://course_recommendation
     */
    COURSE_RECOMMENDATION("course_recommendation"),

    /**
     * doubtnutapp://course_explore
     */
    COURSE_EXPLORE("course_explore"),

    /**
     * doubtnutapp://course_change_option?assortment_id=""
     */
    COURSE_CHANGE_OPTION("course_change_option"),

    /**
     * doubtnutapp://khelo_jeeto
     * (Multiple deeplinks for different destinations using Navigation component)
     */
    KHELO_JEETO("khelo_jeeto"),

    /**
     * doubtnutapp://leaderboard?source=course&assortment_id=58145&type=paid_user_championship
     */
    LEADERBOARD("leaderboard"),

    /**
     * doubtnutapp://bottom_sheet_widget?id=1234&widget_type=select_medium&title=Select%20Medium&show_close_btn=false
     */
    BOTTOM_SHEET_WIDGET("bottom_sheet_widget"),

    /**
     * doubtnutapp://paginated_bottom_sheet_widget?id=courses&widget_type=home_explore_category&show_close_btn=true
     */
    PAGINATED_BOTTOM_SHEET_WIDGET("paginated_bottom_sheet_widget"),

    /**
     * doubtnutapp://dialog_widget?widget_type=paid_user_championship_profile&show_close_btn=true
     */
    DIALOG_WIDGET("dialog_widget"),

    /**
     * doubtnutapp://dialog_dismiss?tag=BaseWidgetDialogFragment
     */
    DIALOG_DISMISS("dismiss_dialog"),

    /**
     * doubtnutapp://submit_address_dialog?type=paid_user_championship_reward&id=12345
     */
    SUBMIT_ADDRESS_DIALOG("submit_address_dialog"),

    /**
     * doubtnutapp://doubt_feed_2
     * (Multiple deeplinks for different destinations using Navigation component)
     */
    DOUBT_FEED_2("doubt_feed_2"),

    /*
   doubtnutapp://exam_corner
    */
    EXAM_CORNER("exam_corner"),

    /*
   doubtnutapp://exam_corner_bookmark
    */
    EXAM_CORNER_BOOKMARK("exam_corner_bookmark"),

    /*
   doubtnutapp://whatsapp_admin_form?source=feed
    */
    WHATSAPP_ADMIN_FORM("whatsapp_admin_form"),

    /*
    doubtnutapp://quiz_tfs_selection
    */
    QUIZ_TFS_SELECTION("quiz_tfs_selection"),

    /*
    doubtnutapp://daily_practice?type=english_quiz
    */
    DAILY_PRACTICE("daily_practice"),

    /**
    doubtnutapp://quiz_tfs?class=12&subject=MATHS&language=en
     */
    QUIZ_TFS("quiz_tfs"),

    /**
    doubtnutapp://quiz_tfs_solution?id=12abc&date=12abc
     */
    QUIZ_TFS_SOLUTION("quiz_tfs_solution"),

    /**
    doubtnutapp://quiz_tfs_history
     */
    LIVE_QUESTIONS_HISTORY("quiz_tfs_history"),

    /**
    doubtnutapp://quiz_tfs_rewards?type=english_quiz
     */
    MY_REWARDS("quiz_tfs_rewards"),

    /**
    doubtnutapp://quiz_tfs_analysis?date= 06 Sep 21
     */
    QUIZ_TFS_ANALYSIS("quiz_tfs_analysis"),

    /**
     * doubtnutapp://course_details_bottom_sheet?ids=194562,101048,17,24&position=1
     */
    COURSE_DETAILS_BOTTOM_SHEET("course_details_bottom_sheet"),

    /**
     * doubtnutapp://book_call
     */
    BOOK_CALL("book_call"),

    /**
     * doubtnutapp://coupon_list?page=explore_view
     */
    COUPON_LIST("coupon_list"),

    /**
     * doubtnutapp://revision_corner
     */
    REVISION_CORNER("revision_corner"),

    /**
     * doubtnutapp://mock_test_list?course=
     */
    MOCK_TEST_LIST("mock_test_list"),

    /**
     * doubtnutapp://google_auth
     */
    GOOGLE_AUTH("google_auth"),

    /**
     * doubtnutapp://mock_test_analysis?testId=1234&subject=maths&source=quiz
     */
    MOCK_TEST_ANALYSIS("mock_test_analysis"),

    /**
     * doubtnutapp://teacher_channel?teacher_id=136072158&page=1&tab_filter=subject&subfilter=MATHS&contentfilter=videos
     */
    TEACHER_CHANNEL("teacher_channel"),

    /**
     * doubtnutapp://teacher_id=136072158
     */
    TEACHER_PROFILE("teacher_profile"),

    /**
     * doubtnutapp://library_previous_year_papers?exam_id=1234456
     */
    LIBRARY_PREVIOUS_YEAR_PAPERS("library_previous_year_papers"),

    /**
     * doubtnutapp://history
     */
    HISTORY("history"),

    /**
     *  doubtnutapp://olympiad
     */
    OLYMPIAD("olympiad"),

    /**
     *  doubtnutapp://olympiad-register
     */
    OLYMPIAD_REGISTER("olympiad-register"),

    /**
     *  doubtnutapp://olympiad-success
     */
    OLYMPIAD_SUCCESS("olympiad-success"),

    /**
     *  doubtnutapp://icons
     */
    ICONS("icons"),

    /**
     * doubtnutapp://practice_english?session_id=d31e1967-0ae0-4661-b9aa-1ee9ae2fe2a
     *
     * doubtnutapp://practice_english
     */
    PRACTICE_ENGLISH("practice_english"),

    /**
     * doubtnutapp://course_bottom_sheetv2?type=aaa&qid=123
     */
    COURSE_BOTTOM_SHEET_V2("course_bottom_sheet_v2"),

    /**
     *  doubtnutapp://doubts
     */
    DOUBTS("doubts"),

    /*
   doubtnutapp://share?message=Message&image_url=https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/0DD28355-D096-032F-DEAD-2A403AB22D6F.webp&app_name=Whatsapp&package_name=com.whatsapp&skip_branch=true
   */
    SHARE("share"),

    /**
     * doubtnutapp://scheduler_listing?subjects=PHYSICS,SCIENCE&slot=LIVECLASS_SCHEDULER::28-01-2022:16:Fri:136917
     */
    SCHEDULER_LISTING("scheduler_listing"),

    /**
     *  doubtnutapp://free_trial_course
     */

    FREE_TRIAL_COURSE("free_trial_course"),

    /**
     *  doubtnutapp://shorts?qid=123&type=shorts&nav_source=DEFAULT
     */
    SHORTS("shorts"),

    /**
     *  doubtnutapp://dn_app_settings
     */

    DN_APP_SETTINGS("dn_app_settings"),

    /**
     * doubtnutapp://top_icons
     */
    TOP_ICONS("top_icons"),

    /**
     *  doubtnutapp://email?email=shubham.agarwal@doubtnut.com&subject=Subject&message=Message
     */
    EMAIL("email"),

    /**
     * This deeplink is valid if coming on match page only - consuming parent activity ViewModel
     * doubtnutapp://match_page_book_feedback?source=srp
     */
    MATCH_PAGE_BOOK_FEEDBACK("match_page_book_feedback"),

    /**
     *  doubtnutapp://one_tap_posts_list?carousel_type=1
     */
    ONE_TAP_POST_LIST("one_tap_posts_list"),

    /**
     * doubtnutapp://clp_filter_bottom_sheet?source=CLP&assortment_id=42&filters={"chapter":["maths"],"subject":["PHYSICS","ENGLISH"]}
     * */
    CLP_FILTERS_BOTTOM_SHEET("clp_filter_bottom_sheet"),

    /**
     *  doubtnutapp://refer_and_earn
     */
    REFER_AND_EARN("refer_and_earn"),

    /**
     *  doubtnutapp://refer_and_earn_faq
     */
    REFER_AND_EARN_FAQ("refer_and_earn_faq"),

    /**
     *  doubtnutapp://refer_a_friend?referrer="1234"
     */
    REFERRAL_CODE_SHARE("refer_a_friend"),

    /**
     * doubtnutapp://common_popup?page=HOME_PAGE
     */
    COMMON_POP_UP("common_popup"),

    /**
     *  doubtnutapp://copy?text=1234&label=doubtnut_coupon_code&toast_message=
     */
    COPY("copy"),

    /**
     * doubtnutapp://doubt_pe_charcha_rewards
     */
    DOUBT_PE_CHARCHA_REWARDS("doubt_pe_charcha_rewards"),

    /**
     * doubtnutapp://dynamic_result_page?page=result&type=&source=
     */
    RESULT_PAGE("dynamic_result_page");

    override fun toString(): String {
        return host
    }

    companion object {
        private const val DOUBTNUT_DOMAIN = "doubtnutapp://"

        fun getDeeplink(appActions: AppActions): String {
            return DOUBTNUT_DOMAIN + appActions.toString()
        }

        fun fromString(host: String?): AppActions? {
            if (host != null) {
                for (appAction in values()) {
                    if (host.equals(appAction.host, ignoreCase = true)) {
                        return appAction
                    }
                }
            }
            return null
        }

        fun getActions(): List<String> {
            return AppActions.values().map { it.toString() }
        }
    }
}
