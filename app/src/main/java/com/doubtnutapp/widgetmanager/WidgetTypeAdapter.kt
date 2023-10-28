package com.doubtnutapp.widgetmanager

import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.referral.widgets.*
import com.doubtnut.referral.widgets.ImageTextWidget
import com.doubtnut.scholarship.widget.*
import com.doubtnutapp.ExploreMoreWidgetModel
import com.doubtnutapp.Log.e
import com.doubtnutapp.MultiSelectFilterWidgetModel
import com.doubtnutapp.PendingPaymentWidgetModel
import com.doubtnutapp.addtoplaylist.HomePageAskDoubtWidgetModel
import com.doubtnutapp.callingnotice.CallingNoticeWidgetModel
import com.doubtnutapp.course.widgets.*
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.data.remote.models.feed.IncompleteChapterWidgetModel
import com.doubtnutapp.data.remote.models.feed.TopOptionsWidgetModel
import com.doubtnutapp.dnr.widgets.*
import com.doubtnutapp.doubt.bookmark.widget.BookmarkListWidgetModel
import com.doubtnutapp.examcorner.widgets.ExamCornerAutoplayWidgetModel
import com.doubtnutapp.examcorner.widgets.ExamCornerDefaultWidgetModel
import com.doubtnutapp.examcorner.widgets.ExamCornerPopularWidgetModel
import com.doubtnutapp.explore_v2.ExploreCourseV2WidgetCircleModel
import com.doubtnutapp.explore_v2.ExploreCourseV2WidgetSquareModel
import com.doubtnutapp.feed.view.widgets.FeedPostWidgetModel
import com.doubtnutapp.freeclasses.widgets.*
import com.doubtnutapp.icons.widgets.ExploreCardWidget
import com.doubtnutapp.leaderboard.widget.*
import com.doubtnutapp.libraryhome.course.data.ScheduleHeaderWidgetModel
import com.doubtnutapp.libraryhome.course.data.ScheduleNoDataWidgetModel
import com.doubtnutapp.libraryhome.course.data.ScheduleWidgetModel
import com.doubtnutapp.libraryhome.coursev3.ui.CouponBannerWidgetModel
import com.doubtnutapp.liveclass.ui.practice_english.PracticeEnglishWidget
import com.doubtnutapp.mediumSwitch.MediumSwitchWidgetModel
import com.doubtnutapp.paymentplan.widgets.*
import com.doubtnutapp.quiztfs.widgets.*
import com.doubtnutapp.referral.ReferralVideoWidget
import com.doubtnutapp.resultpage.widgets.ExcelCoursesWidget
import com.doubtnutapp.resultpage.widgets.MoreTestimonialsWidget
import com.doubtnutapp.resultpage.widgets.ToppersWidget
import com.doubtnutapp.revisioncorner.ui.RCPreviousYearPapersWidget
import com.doubtnutapp.revisioncorner.ui.RCTestPapersWidget
import com.doubtnutapp.sales.PrePurchaseCallingCardModel2
import com.doubtnutapp.sales.widget.PrePurchaseCallingCardModel
import com.doubtnutapp.similarVideo.widgets.*
import com.doubtnutapp.ui.forum.doubtsugggester.widget.DoubtSuggesterWidget
import com.doubtnutapp.videoPage.widgets.LiveClassCarouselCard2Widget
import com.doubtnutapp.videoPage.widgets.LiveClassCarouselCardWidget
import com.doubtnutapp.videoPage.widgets.SrpNudgeCourseWidget
import com.doubtnutapp.widgetmanager.widgets.*
import com.doubtnutapp.widgetmanager.widgets.tablist.TabListWidgetModel
import com.doubtnutapp.widgets.*
import com.doubtnutapp.widgettest.widgets.DummyWidgetModel
import com.google.gson.*
import java.lang.reflect.Type

class WidgetTypeAdapter : JsonDeserializer<WidgetEntityModel<*, *>?> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): WidgetEntityModel<*, *>? {

        val jsonObject = json as? JsonObject
        var widget: WidgetEntityModel<*, *>? = null

        if (jsonObject != null && (jsonObject["type"] != null || jsonObject["widget_type"] != null)) {
            try {
                var clazz: Class<out WidgetEntityModel<*, *>?>? = null

                @Suppress("MoveVariableDeclarationIntoWhen")
                val widgetType = jsonObject["type"]?.asString ?: jsonObject["widget_type"]?.asString
                when (widgetType) {
                    WidgetTypes.TYPE_COURSE_LIST -> clazz =
                        CourseListWidget.CourseListWidgetModel::class.java
                    WidgetTypes.TYPE_STRUCTURED_COURSE_LIST -> clazz =
                        CourseListWidget.CourseListWidgetModel::class.java
                    WidgetTypes.TYPE_HEADER_MESSAGE -> clazz =
                        HeaderMessageWidget.HeaderMessageWidgetModel::class.java
                    WidgetTypes.TYPE_BANNER_IMAGE -> clazz =
                        BannerImageWidget.BannerImageWidgetModel::class.java
                    WidgetTypes.TYPE_FILTER_TABS -> clazz =
                        FilterTabsWidget.FilterTabsWidgetModel::class.java
                    WidgetTypes.TYPE_PAYMENT_CARD -> clazz = PaymentCardWidgetModel::class.java
                    WidgetTypes.TYPE_FACULTY_GRID -> clazz = FacultyGridWidgetModel::class.java
                    WidgetTypes.TYPE_VERTICAL_LIST -> clazz = VerticalListWidgetModel::class.java
                    WidgetTypes.TYPE_TOPPERS_SPEAK -> clazz = ToppersSpeakWidgetModel::class.java
                    WidgetTypes.TYPE_BUTTON -> clazz = ButtonWidgetModel::class.java

                    WidgetTypes.TYPE_HORIZONTAL_LIST -> clazz =
                        HorizontalListWidget.HorizontalListWidgetModel::class.java
                    WidgetTypes.TYPE_VERTICAL_LIST_2 -> clazz =
                        VerticalListWidget.VerticalListWidgetModel::class.java
                    WidgetTypes.TYPE_CAROUSEL_LIST -> clazz =
                        CarouselListWidget.CarouselListWidgetModel::class.java
                    WidgetTypes.TYPE_TAB_LIST -> clazz = TabListWidgetModel::class.java
                    WidgetTypes.TYPE_WHATSAPP -> clazz =
                        WhatsappWidget.WhatsappWidgetModel::class.java

                    WidgetTypes.TYPE_DAILY_QUIZ -> clazz =
                        DailyQuizWidget.DailyQuizWidgetModel::class.java

                    WidgetTypes.WIDGET_TYPE_FEED_POST -> clazz = FeedPostWidgetModel::class.java
                    WidgetTypes.WIDGET_TYPE_RECENT_STATUS -> clazz =
                        RecentStatusWidget.RecentStatusWidgetModel::class.java

                    WidgetTypes.TYPE_COURSE_TAB -> clazz = TabCourseWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_LIST_FILTER -> clazz =
                        CourseFilterWidgetModel::class.java
                    WidgetTypes.TYPE_REMINDER_CARD -> clazz = ReminderCardWidgetModel::class.java
                    WidgetTypes.TYPE_SIMPLE_TEXT -> clazz = SimpleTextWidgetModel::class.java
                    WidgetTypes.TYPE_ALL_COURSE_LIST -> clazz = AllCourseWidget2Model::class.java
                    WidgetTypes.TYPE_COURSE_FILTER_EXAM -> clazz = FilterExamWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_FILTER_SUBJECT -> clazz =
                        FilterSubjectWidget.FilterTabsWidgetModel::class.java
                    WidgetTypes.TYPE_NOTES_LIST -> clazz = NotesWidgetModel::class.java
                    WidgetTypes.TYPE_PURCHASED_CLASSES -> clazz =
                        PurchasedClassesWidgetModel::class.java
                    WidgetTypes.TYPE_LIVE_CLASS_CATEGORY -> clazz =
                        LiveClassCategoryWidgetModel::class.java
                    WidgetTypes.TYPE_PAYMENT_HISTORY -> clazz =
                        PaymentHistoryWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE -> clazz = CourseWidget.CourseWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_V2 -> clazz =
                        CourseWidgetV2.CourseWidgetModelV2::class.java
                    WidgetTypes.TYPE_COURSE_PROGRESS -> clazz =
                        CourseProgressWidgetModel::class.java
                    WidgetTypes.TYPE_RELATED_LECTURES_WIDGET -> clazz =
                        RelatedLecturesWidgetModel::class.java
                    WidgetTypes.TYPE_NOTIFY_LIVE_CLASS_WIDGET -> clazz =
                        NotifyClassWidgetModel::class.java
                    WidgetTypes.TYPE_TOPICS_COVERED_WIDGET -> clazz =
                        TopicsCoveredWidgetModel::class.java
                    WidgetTypes.TYPE_LIVE_CLASSES_INFO -> clazz =
                        LiveClassInfoWidgetModel::class.java
                    WidgetTypes.TYPE_RANKERS_CLASSES -> clazz =
                        RankersClassesWidgetModel::class.java
                    WidgetTypes.TYPE_RANKERS -> clazz = RankersWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_CONTENT -> clazz = CourseContentWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_EXAM_TABS -> clazz =
                        CourseExamTabWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_TYPE_TABS -> clazz =
                        CourseTypeTabWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_CLASS_TABS -> clazz =
                        CourseClassTabWidgetModel::class.java
                    WidgetTypes.TYPE_SHORTS_VIDEOS_EXHAUSTED_WIDGET -> clazz =
                        ShortsVideosExhaustedWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_PAYMENT_CARD_LIST -> clazz =
                        PaymentCardListWidgetModel::class.java
                    WidgetTypes.TYPE_PROMO_LIST -> clazz = PromoListWidgetModel::class.java
                    WidgetTypes.TYPE_PLAN_TAB -> clazz = PlanTypeTabWidgetModel::class.java
                    WidgetTypes.TYPE_PLAN_INFO -> clazz = PlanInfoWidgetModel::class.java
                    WidgetTypes.TYPE_PLAN_LIST -> clazz = PlanListWidgetModel::class.java
                    WidgetTypes.TYPE_MY_PLAN -> clazz = MyPlanWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_TYPE_FILTER -> clazz =
                        FilterCourseTypeWidget.FilterTabsWidgetModel::class.java
                    WidgetTypes.TYPE_LIVE_CLASS_CAROUSEL -> clazz =
                        LiveClassCarouselWidgetModel::class.java
                    WidgetTypes.TYPE_RESOURCE_PAGE_UPCOMING_WIDGET -> clazz =
                        ResourcePageUpcomingWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_UPCOMING_LECTURES_WIDGET -> clazz =
                        UpcomingLecturesWidget.UpcomingLectureWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_INFO_WIDGET -> clazz =
                        CourseInfoWidget.CourseInfoWidgetModel::class.java
                    WidgetTypes.TYPE_ALL_TOPICS_WIDGET -> clazz = AllTopicsWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_COMMON -> clazz = CommonCourseWidgetModel::class.java
                    WidgetTypes.TYPE_SYLLABUS2 -> clazz = SyllabusWidgetTwoModel::class.java
                    WidgetTypes.TYPE_TIME_TABLE -> clazz = TimetableWidgetModel::class.java
                    WidgetTypes.TYPE_SCHEDULE -> clazz = ScheduleWidgetModel::class.java
                    WidgetTypes.TYPE_SCHEDULE_HEADER -> clazz =
                        ScheduleHeaderWidgetModel::class.java
                    WidgetTypes.TYPE_SCHEDULE_NO_DATA -> clazz =
                        ScheduleNoDataWidgetModel::class.java
                    WidgetTypes.TYPE_ICON_HEADER -> clazz = IconHeaderWidgetModel::class.java
                    WidgetTypes.TYPE_PLAN_DESCRIPTION -> clazz =
                        PlanDescriptionInfoWidgetModel::class.java
                    WidgetTypes.TYPE_PACKAGE_DETAIL -> clazz = PackageDetailWidgetModel::class.java
                    WidgetTypes.TYPE_PACKAGE_DETAIL_V2 -> clazz =
                        PackageDetailWidgetModelV2::class.java
                    WidgetTypes.TYPE_COURSE_CATEGORY -> clazz =
                        CourseCategoryWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_PARENT -> clazz =
                        ParentWidget.WidgetChildModel::class.java
                    WidgetTypes.TYPE_WIDGET_PARENT_TAB -> clazz = ParentTabWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_IPL_SCORE_BOARD -> clazz =
                        IplScoreBoardWidget.Model::class.java
                    WidgetTypes.TYPE_COURSE_CHILD -> clazz =
                        CourseChildWidget.CourseChildWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_CHILD_CAROUSEL -> clazz =
                        CourseCarouselChildWidget.CourseCarouselChildWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_CHILD_RESOURCE -> clazz =
                        CourseResourceChildWidget.CourseResourceChildWidgetModel::class.java
                    WidgetTypes.TYPE_FACULTY_GRID2 -> clazz = FacultyGridWidget.Model::class.java
                    WidgetTypes.TYPE_COURSE_SUBJECT -> clazz = SubjectWidget.Model::class.java
                    WidgetTypes.TYPE_COURSE_SUBJECT_POST_PURCHASE -> clazz =
                        CourseSubjectWidget.Model::class.java
                    WidgetTypes.TYPE_COURSE_RESOURCE_POST_PURCHASE -> clazz =
                        CourseResourcesWidget.Model::class.java
                    WidgetTypes.TYPE_LIVE_CLASS_CAROUSEL_CARD -> clazz =
                        LiveClassCarouselCardWidget.Model::class.java
                    WidgetTypes.TYPE_LIVE_CLASS_CAROUSEL_CARD_2 -> clazz =
                        LiveClassCarouselCard2Widget.Model::class.java
                    WidgetTypes.TYPE_SALE_WIDGET -> clazz = SaleWidgetModel::class.java
                    WidgetTypes.TYPE_ACTIVATE_TRIAL_WIDGET -> clazz = TrialWidgetModel::class.java
                    WidgetTypes.TYPE_NOTES_FILTER -> clazz = NotesFilterModel::class.java
                    WidgetTypes.TYPE_CATEGORY_PAGE_FILTER -> clazz =
                        MultiSelectFilterWidgetModel::class.java
                    WidgetTypes.TYPE_CATEGORY_PAGE_FILTER_V2 -> clazz =
                        MultiSelectFilterWidgetV2Model::class.java
                    WidgetTypes.TYPE_ASK_DOUBT_CARD -> clazz = AskDoubtCardWidget.Model::class.java
                    WidgetTypes.TYPE_IMAGE_CARD -> clazz = ImageCardWidget.Model::class.java
                    WidgetTypes.TYPE_FEED_BANNER -> clazz = FeedBannerWidget.Model::class.java
                    WidgetTypes.TYPE_HOME_WORK -> clazz = HomeWorkWidgetModel::class.java
                    WidgetTypes.TYPE_HOME_WORK_LIST -> clazz = HomeWorkListWidgetModel::class.java
                    WidgetTypes.TYPE_HOME_WORK_LIST_V2 -> clazz =
                        HomeWorkListWidgetModelV2::class.java
                    WidgetTypes.TYPE_TOP_OPTION -> clazz = TopOptionsWidgetModel::class.java
                    WidgetTypes.TYPE_INCOMPLETE_CHAPTER -> clazz =
                        IncompleteChapterWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_COLLAPSED -> clazz = CollapsedWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_PLAYLIST -> clazz =
                        PlaylistWidget.PlaylistChildWidgetModel::class.java
                    WidgetTypes.TYPE_HOME_WORK_HORIZONTAL_LIST -> clazz =
                        HomeWorkHorizontalListWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_AUTOPLAY -> clazz =
                        ParentAutoplayWidget.ParentAutoplayWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_CHILD_AUTOPLAY -> clazz =
                        AutoPlayChildWidget.AutoplayChildWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_UPCOMING_LIVE_CLASS -> clazz =
                        UpcomingLiveClassWidget.UpcomingLiveClassWidgetModel::class.java
                    WidgetTypes.TYPE_GRADIENT_CARD -> clazz = GradientCardWidget.Model::class.java
                    WidgetTypes.TYPE_BOOK_PROGRESS -> clazz = BookProgressWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_PAID_COURSE -> clazz =
                        PaidCourseWidget.PaidCourseChildWidgetModel::class.java
                    WidgetTypes.TYPE_YOU_WERE_WATCHING -> clazz =
                        YouWereWatchingWidget.Model::class.java
                    WidgetTypes.TYPE_VIDEO_BANNER_AUTOPLAY_CHILD -> clazz =
                        VideoBannerAutoplayChildWidget.Model::class.java
                    WidgetTypes.TYPE_VIDEO_AUTOPLAY_CHILD2 -> clazz =
                        VideoAutoplayChildWidget2.Model::class.java
                    WidgetTypes.TYPE_FEED_PINNED_VIDEO_AUTOPLAY_CHILD -> clazz =
                        FeedPinnedVideoAutoplayChildWidget.Model::class.java
                    WidgetTypes.TYPE_IMAGE_CARD_GROUP -> clazz =
                        ImageCardGroupWidget.Model::class.java
                    WidgetTypes.TYPE_FOLLOW_WIDGET -> clazz = FollowWidget.Model::class.java
                    WidgetTypes.TYPE_COURSE_TESTIMONIAL -> clazz =
                        TestimonialWidgetModel::class.java
                    WidgetTypes.TYPE_FACULTY_LIST -> clazz = FacultyListWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_FAQS -> clazz = CourseFaqsWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_CONTENT_LIST -> clazz =
                        CourseContentListWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_INFO -> clazz = CourseItemInfoWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_FEATURE -> clazz = CourseFeatureWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_EMI_INFO -> clazz = CourseEmiInfoWidgetModel::class.java
                    WidgetTypes.TYPE_TOP_DOUBT_ANSWER_VIDEO -> clazz =
                        TopDoubtAnswerVideoWidgetModel::class.java
                    WidgetTypes.TYPE_TOP_DOUBT_ANSWER_AUDIO -> clazz =
                        TopDoubtAnswerAudioWidgetModel::class.java
                    WidgetTypes.TYPE_TOP_DOUBT_ANSWER_IMAGE -> clazz =
                        TopDoubtAnswerImageWidgetModel::class.java
                    WidgetTypes.TYPE_FAQ -> clazz = FaqWidget.FaqWidgetModel::class.java
                    WidgetTypes.TYPE_ALL_CLASSES -> clazz = AllClassesWidgetModel::class.java
                    WidgetTypes.TYPE_INCREASE_VALIDITY -> clazz =
                        IncreaseValidityWidgetModel::class.java
                    WidgetTypes.TYPE_STORIES -> clazz = StoryWidgetModel::class.java
                    WidgetTypes.TYPE_PENDING_PAYMENT -> clazz =
                        PendingPaymentWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_COURSE_PARENT -> clazz =
                        CourseParentWidget.WidgetChildModel::class.java
                    WidgetTypes.TYPE_WIDGET_COURSE_CLASSES -> clazz =
                        CourseClassesWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_COURSE_TEST -> clazz = CourseTestWidgetModel::class.java
                    WidgetTypes.TYPE_NOTES_FILTER2 -> clazz = NotesFilterByWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_COURSE_AUTOPLAY -> clazz =
                        CourseAutoPlayChildWidget.CourseAutoPlayChildWidgetModel::class.java
                    WidgetTypes.TYPE_SCHEDULE_V2 -> clazz =
                        ScheduleWidget.WidgetChildModel::class.java
                    WidgetTypes.TYPE_TRIAL_TIMER -> clazz =
                        TrialTimerWidget.TrialTimerModel::class.java
                    WidgetTypes.TYPE_WIDGET_TEST -> clazz = TestWidget.TestWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_VIEW_ALL -> clazz =
                        ViewAllWidget.ViewAllWigetModel::class.java
                    WidgetTypes.TYPE_SCHEDULE_MONTH_FILTER -> clazz =
                        ScheduleMonthFilterWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_PARENT_GRID_SELECTION -> clazz =
                        ParentGridSelectionWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_CHILD_GRID_SELECTION -> clazz =
                        ChildGridSelectionWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_NCERT_SIMILAR -> clazz =
                        NcertSimilarWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_NCERT_BOOK -> clazz = NcertBookWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_VIDEO_ACTIONS -> clazz =
                        VideoActionWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_POPULAR_COURSE -> clazz =
                        PopularCourseWidgetModel::class.java
                    WidgetTypes.TYPE_ATTENDANCE -> clazz = AttendanceWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_SIMILAR -> clazz = SimilarWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_ASK_DOUBT -> clazz =
                        HomePageAskDoubtWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_STUDY_DOST -> clazz = StudyDostWidget.Model::class.java
                    WidgetTypes.TYPE_TEXT_WIDGET -> clazz = TextWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_COUPON_BANNER -> clazz =
                        CouponBannerWidgetModel::class.java
                    WidgetTypes.PURCHASED_COURSE_LIST_WIDGET -> clazz =
                        CourseListWidgetModel::class.java
                    WidgetTypes.TYPE_PDF_NOTES -> clazz = PdfNotesWidget.Model::class.java
                    WidgetTypes.TYPE_DOUBT_FEED_DAILY_GOAL -> clazz =
                        DoubtFeedDailyGoalWidget.Model::class.java
                    WidgetTypes.TYPE_TOPIC_BOOSTER -> clazz = TopicBoosterWidget.Model::class.java
                    WidgetTypes.TYPE_FORMULA_SHEET -> clazz = FormulaSheetWidget.Model::class.java
                    WidgetTypes.TYPE_TOPIC_VIDEO -> clazz = TopicVideoWidget.Model::class.java
                    WidgetTypes.TYPE_ASKED_QUESTION -> clazz = AskedQuestionWidget.Model::class.java
                    WidgetTypes.TYPE_DOUBT_FEED_START_PRACTICE_WIDGET -> clazz =
                        DoubtFeedStartPracticeWidget.Model::class.java
                    WidgetTypes.TYPE_TOPIC_BOOSTER_GAME_BANNER -> clazz =
                        TopicBoosterGameBannerWidget.Model::class.java
                    WidgetTypes.TYPE_KHELO_JEETO_BANNER -> clazz =
                        KheloJeetoBannerWidget.Model::class.java
                    WidgetTypes.TYPE_MY_COURSE -> clazz = MyCourseWidgetModel::class.java
                    WidgetTypes.TYPE_PRE_PURCHASE_CALL_CARD -> clazz =
                        PrePurchaseCallingCardModel::class.java
                    WidgetTypes.TYPE_PRE_PURCHASE_CALL_CARD_V2 -> clazz =
                        PrePurchaseCallingCardModel2::class.java
                    WidgetTypes.TYPE_WIDGET_STUDY_GROUP_BANNER_IMAGE -> clazz =
                        StudyGroupBannerImageWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_STUDY_GROUP_PARENT -> clazz =
                        StudyGroupParentWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_STUDY_GROUP_REPORT_PARENT -> clazz =
                        StudyGroupReportParentWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_STUDY_GROUP_LIVE_CLASS -> clazz =
                        StudyGroupLiveClassWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_AUDIO_PLAYER -> clazz =
                        AudioPlayerWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_STUDY_GROUP_GUIDELINE -> clazz =
                        StudyGroupGuidelineWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_STUDY_GROUP_INVITATION -> clazz =
                        StudyGroupInvitationWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_STUDY_GROUP_JOINED_INFO -> clazz =
                        StudyGroupJoinedInfoWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_PDF_VIEW -> clazz = PdfViewWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_STUDY_GROUP_FEATURE_UNAVAILABLE -> clazz =
                        StudyGroupFeatureUnavailableWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_MY_SACHET -> clazz =
                        MySachetWidget.SachetWidgetModel::class.java
                    WidgetTypes.TYPE_DOUBT_FEED_BANNER -> clazz =
                        DoubtFeedBannerWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_NUDGE -> clazz =
                        NudgeWidget.NudgeWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_NUDGE_POPUP -> clazz =
                        NudgePopupWidget.NudgePopupWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_DOUBT_P2P_HOME -> clazz =
                        DoubtP2PHomeWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_DOUBT_P2P -> clazz = DoubtP2PWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_DOUBT_P2P_ANIMATION -> clazz =
                        DoubtP2PAnimationWidget.Model::class.java
                    WidgetTypes.TYPE_PACKAGE_DETAIL_V3 -> clazz =
                        PackageDetailWidgetModelV3::class.java
                    WidgetTypes.TYPE_WIDGET_CLASS_BOARD_EXAM -> clazz =
                        ClassBoardExamWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_LEADERBOARD_PERSONAL -> clazz =
                        LeaderboardPersonalModel::class.java
                    WidgetTypes.TYPE_WIDGET_LEADERBOARD_TAB -> clazz =
                        LeaderboardTabModel::class.java
                    WidgetTypes.TYPE_WIDGET_LEADERBOARD -> clazz =
                        LeaderBoardWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_LEADERBOARD_TOP_THREE -> clazz =
                        LeaderboardTopThreeWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_PARENT_COURSE_RECOMMENDATION_INCOMING -> clazz =
                        CourseRecommendationParentWidget.WidgetChildModel::class.java
                    WidgetTypes.TYPE_WIDGET_COURSE_RECOMMENDATION_MESSAGE -> clazz =
                        CourseRecommendationMessageWidget.Model::class.java
                    WidgetTypes.TYPE_CALLING_NOTICE_WIDGET -> clazz =
                        CallingNoticeWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_COURSE_RECOMMENDATION_RADIO_BUTTON -> clazz =
                        CourseRecommendationRadioButtonWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_COURSE_RECOMMENDATION_SUBMITTED_ANSWER -> clazz =
                        CourseRecommendationSubmittedAnswerWidget.Model::class.java
                    WidgetTypes.TYPE_AUTO_SCROLL_CARD_WIDGET -> clazz =
                        AutoScrollImageWidget.Model::class.java
                    WidgetTypes.TYPE_VIDEO_CARD -> clazz =
                        StudyGroupVideoCardWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_SAMPLE_QUESTION -> clazz =
                        SampleQuestionWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_COURSE_EXPLORE -> clazz =
                        CourseExploreWidget.Model::class.java
                    WidgetTypes.TYPE_DOUBT_FEED -> DoubtFeedWidget::class.java
                    WidgetTypes.TYPE_WIDGET_COURSE_INFO_WIDGET_2 -> clazz =
                        CourseInfoWidget2Model::class.java
                    WidgetTypes.TYPE_WIDGET_COURSE_DETAILS -> clazz =
                        CourseDetailsWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_TRIAL_BUTTON -> clazz =
                        TrialButtonWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_TIME_TABLE -> clazz =
                        CourseTimetableWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_PLAN -> clazz =
                        PlanWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_COURSE_TEST_V2 -> clazz =
                        CourseTestWidgetModelV2::class.java
                    WidgetTypes.TYPE_NOTES_LIST_V2 -> clazz =
                        NotesWidgetModel::class.java
                    WidgetTypes.TYPE_PACKAGE_DETAIL_V4 -> clazz =
                        PackageDetailWidgetModelV4::class.java
                    WidgetTypes.TYPE_WIDGET_BUY_COMPLETE_COURSE -> clazz =
                        BuyCompleteCourseWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_VIEW_PLAN_BUTTON -> clazz =
                        WidgetViewPlanButtonModel::class.java
                    WidgetTypes.TYPE_WIDGET_BUTTON_BORDER -> clazz =
                        ButtonBorderWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_SELECT_MEDIUM -> clazz =
                        SelectMediumWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_SYLLABUS -> clazz = SyllabusWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_CONTENT_FILTER -> clazz =
                        ContentFilterWidget.Model::class.java
                    WidgetTypes.TYPE_COURSE_INFO_V2 -> clazz =
                        CourseInfoWidgetV2Model::class.java
                    WidgetTypes.TYPE_MEDIUM_SWITCH_WIDGET -> clazz =
                        MediumSwitchWidgetModel::class.java
                    WidgetTypes.TYPE_EXAM_CORNER_DEFAULT_WIDGET -> clazz =
                        ExamCornerDefaultWidgetModel::class.java
                    WidgetTypes.TYPE_EXAM_CORNER_POPULAR_WIDGET -> clazz =
                        ExamCornerPopularWidgetModel::class.java
                    WidgetTypes.TYPE_EXAM_CORNER_AUTOPLAY_WIDGET -> clazz =
                        ExamCornerAutoplayWidgetModel::class.java
                    WidgetTypes.TYPE_TESTIMONIAL_V2 -> clazz =
                        TestimonialWidgetModelV2::class.java
                    WidgetTypes.TYPE_COURSE_V4 -> clazz =
                        CourseWidgetV4.CourseWidgetModelV4::class.java
                    WidgetTypes.TYPE_COURSE_V3 -> clazz =
                        CourseWidgetV3.CourseWidgetModelV3::class.java
                    WidgetTypes.TYPE_COUPON_LIST -> clazz =
                        CouponListWidget.CouponListWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_RESOURCE_V2 -> clazz =
                        CourseResourceWidget.CourseResourceWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_TOP_SELLING_SUBJECT -> clazz =
                        TopSellingSubjectWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_CALLING_BIG_CARD -> clazz =
                        CallingBigCardWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_COURSE_RECOMMENDATION -> clazz =
                        CourseRecommendationWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_CATEGORY -> clazz =
                        CategoryWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_RECOMMENDED_TEST -> clazz =
                        RecommendedTestWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_COURSE_INFO_V3 -> clazz =
                        CourseInfoWidgetV3.Model::class.java
                    WidgetTypes.TYPE_EXPLORE_PROMO_WIDGET -> clazz =
                        ExplorePromoWidget.Model::class.java
                    WidgetTypes.TYPE_VIDEO_WIDGET -> clazz =
                        VideoWidget.VideoWidgetModel::class.java
                    WidgetTypes.TYPE_RECOMMENDATION_WIDGET -> clazz =
                        RecommendationWidget.RecommendationWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_VPA -> clazz =
                        VpaWidget.Model::class.java
                    WidgetTypes.TYPE_TEST_ANALYSIS -> clazz =
                        TestAnalysisWidget.TestAnalysisWidgetModel::class.java
                    WidgetTypes.TYPE_TEST_RESULT -> clazz =
                        TestResultWidget.TestResultWidgetModel::class.java
                    WidgetTypes.TYPE_REVISION_CORNER_BANNER ->
                        clazz = RevisionCornerBannerWidget.Model::class.java
                    WidgetTypes.TYPE_ICON_CTA ->
                        clazz = IconCtaWidget.Model::class.java
                    WidgetTypes.TYPE_LIVE_QUESTIONS_DAILY_PRACTICE_WIDGET -> clazz =
                        LiveQuestionDailyPracticeWidgetModel::class.java
                    WidgetTypes.TYPE_LIVE_QUESTIONS_DAILY_PRACTICE_RANK_WIDGET -> clazz =
                        LiveQuestionDailyPracticeRankWidgetModel::class.java
                    WidgetTypes.TYPE_LIVE_QUESTIONS_DAILY_PRACTICE_FAQ_WIDGET -> clazz =
                        LiveQuestionDailyPracticeFAQWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_DAILY_PRACTICE -> clazz =
                        DailyPracticeWidgetModel::class.java
                    WidgetTypes.MY_REWARDS_POINTS_WIDGET -> clazz =
                        MyRewardsPointsWidgetModel::class.java
                    WidgetTypes.MY_REWARDS_SCRATCH_CARD_WIDGET -> clazz =
                        MyRewardsScratchCardWidgetModel::class.java
                    WidgetTypes.TYPE_BULLET_LIST_WIDGET -> clazz =
                        BulletListWidgetModel::class.java
                    WidgetTypes.TYPE_PADHO_AUR_JEETO_WIDGET -> clazz =
                        PadhoAurJeetoWidgetModel::class.java
                    WidgetTypes.TYPE_LEADER_BOARD_PROGRESS -> clazz =
                        LeaderboardProgressWidgetModel::class.java
                    WidgetTypes.TYPE_TFS_ANALYSIS -> clazz =
                        TfsAnalysisWidget.TfsAnalysisWidgetModel::class.java
                    WidgetTypes.TYPE_AWARDED_STUDENTS_LIST -> clazz =
                        AwardedStudentsWidgetModel::class.java
                    WidgetTypes.TYPE_REFERRAL -> clazz =
                        ReferralWidgetModel::class.java
                    WidgetTypes.TYPE_PRACTICE_TEST -> clazz =
                        PracticeTestWidgetModel::class.java
                    WidgetTypes.TYPE_PREVIOUS_TEST_RESULTS -> clazz =
                        PreviousTestResultsWidgetModel::class.java
                    WidgetTypes.TYPE_SCHOLARSHIP_TABS -> clazz =
                        ScholarshipTabsWidgetModel::class.java
                    WidgetTypes.TYPE_REPORT_CARD -> clazz =
                        ReportCardWidgetModel::class.java
                    WidgetTypes.TYPE_REGISTER_TEST -> clazz =
                        RegisterTestWidgetModel::class.java

                    WidgetTypes.TYPE_PROGRESS -> clazz =
                        ProgressWidgetModel::class.java
                    WidgetTypes.TYPE_SCHOLARSHIP_PROGRESS_CARD -> clazz =
                        ScholarshipProgressCardWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_PARENT_TAB_2 -> clazz =
                        ParentTabWidget2.Model::class.java
                    WidgetTypes.TYPE_EXPLORE_COURSE_V2_SQUARE -> clazz =
                        ExploreCourseV2WidgetSquareModel::class.java
                    WidgetTypes.TYPE_EXPLORE_COURSE_V2_CIRCLE -> clazz =
                        ExploreCourseV2WidgetCircleModel::class.java
                    WidgetTypes.TYPE_LATEST_LAUNCHES -> clazz =
                        LatestLaunchesWidget.LatestLaunchesWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_SG_GROUP_CHAT ->
                        clazz = SgGroupChatWidget.Model::class.java
                    WidgetTypes.WIDGET_JOIN_NEW_STUDYGROUP ->
                        clazz = SgJoinNewGroupWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_SG_INDIVIDUAL_CHAT ->
                        clazz = SgPersonalChatWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_SG_REQUEST ->
                        clazz = SgRequestWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_SG_BLOCKED_MEMBER ->
                        clazz = SgBlockedMemberWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_SG_HOME ->
                        clazz = SgHomeWidget.Model::class.java
                    WidgetTypes.TYPE_TEACHER_HEADER -> {
                        clazz = TeacherHeaderWidget.Model::class.java
                    }
                    WidgetTypes.TYPE_ANNOUNCEMENT_WIDGET -> {
                        clazz = ChannelAnnouncementWidget.Model::class.java
                    }
                    WidgetTypes.TYPE_CHANNEL_FILTER_TAB -> {
                        clazz = ChannelTabFilterWidget.Model::class.java
                    }
                    WidgetTypes.TYPE_CHANNEL_FILTER -> {
                        clazz = ChannelSubTabFilterWidget.Model::class.java
                    }
                    WidgetTypes.TYPE_CHANNEL_FILTER_CONTENT -> {
                        clazz = ChannelContentFilterWidget.Model::class.java
                    }
                    WidgetTypes.TYPE_VIDEO_TYPE -> {
                        clazz = ChannelVideoContentWidget.Model::class.java
                    }
                    WidgetTypes.TYPE_PDF_TYPE -> {
                        clazz = ChannelPDFContentWidget.Model::class.java
                    }
                    WidgetTypes.TYPE_SUBSCRIBED_TEACHERS -> {
                        clazz = SubscribedTeacherChannelWidget.Model::class.java
                    }
                    WidgetTypes.TYPE_TEACHERS_LIST -> {
                        clazz = TeacherChannelWidget.Model::class.java
                    }
                    WidgetTypes.TYPE_WIDGET_TEACHER_CHANNEL_2 ->
                        clazz = TeacherChannelWidget2.Model::class.java
                    WidgetTypes.TYPE_WIDGET_DUMMY -> {
                        clazz = DummyWidgetModel::class.java
                    }
                    WidgetTypes.TYPE_WIDGET_EXPLORE_CARD,
                    WidgetTypes.TYPE_WIDGET_FAVOURITE_EXPLORE_CARD ->
                        clazz = ExploreCardWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_CHANNEL ->
                        clazz = ChannelWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_VIDEO_OFFSET ->
                        clazz = VideoOffsetWidget.VideoOffsetWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_DNR_EARNED_HISTORY ->
                        clazz = DnrEarnedHistoryWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_DNR_EARNED_HISTORY_ITEM ->
                        clazz = DnrEarnedHistoryItemWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_DNR_EARNED_SUMMARY ->
                        clazz = DnrEarnedSummaryWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_DNR_UNLOCKED_VOUCHER ->
                        clazz = DnrUnlockedVoucherWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_DNR_STREAK ->
                        clazz = DnrStreakWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_DNR_TEXT ->
                        clazz = DnrTextWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_DNR_NO_EARNED_HISTORY ->
                        clazz = DnrNoEarnedHistoryWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_DNR_TOTAL_REWARD ->
                        clazz = DnrTotalRewardWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_DNR_EARNING_DETAILS ->
                        clazz = DnrEarningsDetailWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_DNR_REWARD_DETAIL ->
                        clazz = DnrRewardDetailWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_DNR_REDEEM_VOUCHER ->
                        clazz = DnrRedeemVoucherWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_DNR_TODAY_REWARD ->
                        clazz = DnrTodayRewardWidget.DnrTodayRewardWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_DNR_REWARD_HISTORY ->
                        clazz = DnrRewardHistoryWidget.DnrRewardHistoryWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_DNR_HOME ->
                        clazz = DnrHomeWidget.DnrHomeWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_EXPLORE_MORE ->
                        clazz = ExploreMoreWidgetModel::class.java
                    WidgetTypes.TYPE_BOOKMARK_LIST -> clazz =
                        BookmarkListWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_LIBRARY_CARD -> {
                        clazz = LibraryCardWidget.Model::class.java
                    }
                    WidgetTypes.TYPE_WIDGET_LIBRARY_EXAM -> {
                        clazz = LibraryExamWidget.Model::class.java
                    }
                    WidgetTypes.TYPE_WIDGET_MATCH_PAGE ->
                        clazz = MatchPageWidget.Model::class.java
                    WidgetTypes.TYPE_CHECKOUT_V2_HEADER ->
                        clazz = CheckoutV2HeaderWidgetModel::class.java
                    WidgetTypes.TYPE_CHECKOUT_V2_COUPON ->
                        clazz = CheckoutV2CouponWidgetModel::class.java
                    WidgetTypes.TYPE_CHECKOUT_V2_PARENT ->
                        clazz = CheckoutV2ParentWidgetModel::class.java
                    WidgetTypes.TYPE_CHECKOUT_V2_CHILD ->
                        clazz = CheckoutV2ChildWidgetModel::class.java
                    WidgetTypes.TYPE_CHECKOUT_V2_WALLET ->
                        clazz = CheckoutV2WalletWidgetModel::class.java
                    WidgetTypes.TYPE_CHECKOUT_V2_TALK_TO_US ->
                        clazz = CheckoutV2TalkToUsWidgetModel::class.java
                    WidgetTypes.TYPE_COURSE_TIME_TABLE_V2 -> clazz =
                        CourseTimetableWidgetV2Model::class.java
                    WidgetTypes.TYPE_WIDGET_TOP_SUBJECT_STUDYING ->
                        clazz = TopSubjectsStudyingWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_CLASSES_BY_TEACHER ->
                        clazz = ClassesByTeacherWidget.ClassesByTeacherWidgetModel::class.java
                    WidgetTypes.TYPE_YOU_WERE_WATCHING_V2 -> clazz =
                        YouWereWatchingV2Widget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_CHAPTER_BY_CLASSES -> clazz =
                        ChapterByClassesWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_MOST_VIEWED_CLASSES -> clazz =
                        MostViewedClassesWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_FILTER_BUTTON -> clazz =
                        FilterButtonWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_FILTER_DROPDOWN -> clazz =
                        FilterDropDownWidgetModel::class.java
                    WidgetTypes.TYPE_PREVIOUS_WINNERS_WIDGET ->
                        clazz = PreviousWinnersWidget.Model::class.java
                    WidgetTypes.TYPE_WINNERS_CARD_WIDGET ->
                        clazz = WinnersCardWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_COPY_TEXT ->
                        clazz = CopyTextWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_MULTISELECT_SUBJECT_FILTER ->
                        clazz = MultiSelectSubjectFilterWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_PRACTICE_ENGLISH ->
                        clazz = PracticeEnglishWidget.PracticeEnglishWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_SRP_NUDGE -> clazz =
                        SrpNudgeCourseWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_SUBJECT_COURSE_CARD -> clazz =
                        SubjectCourseCardWidget.Model::class.java
                    WidgetTypes.TYPE_FREE_TRIAL_COURSE -> clazz =
                        FreeTrialCourseWidget.FreeTrialCourseItemModel::class.java
                    WidgetTypes.TYPE_GRADIENT_CARD_WITH_BUTTON -> clazz =
                        GradientCardWithButtonWidget.GradientCardWithButtonWidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_COURSE_CAROUSEL -> clazz =
                        CourseCarouselWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_IMAGE_TEXT -> clazz = ImageTextWidgetModel::class.java
                    WidgetTypes.TYPE_RESOURCE_V4 -> clazz = ResourceV4WidgetModel::class.java
                    WidgetTypes.TYPE_RESOURCE_NOTES_V3 -> clazz =
                        ResourceNotesV3WidgetModel::class.java
                    WidgetTypes.TYPE_WIDGET_PARENT_TAB_3 -> clazz =
                        ParentTabWidget3.Model::class.java
                    WidgetTypes.NEXT_SCREEN_WIDGET -> clazz =
                        NextScreenWidget.NextScreenWidgetModel::class.java
                    WidgetTypes.ENGLISH_QUIZ_INFO_WIDGET -> clazz =
                        EnglishQuizInfoWidget.EnglishQuizInfoWidgetModel::class.java
                    WidgetTypes.INVITE_FRIEND_WIDGET -> clazz =
                        InviteFriendWidget.InviteFriendWidgetModel::class.java
                    WidgetTypes.TYPE_REFERRAL_STEPS_WIDGET -> clazz =
                        ReferralStepsWidget.ReferralStepsWidgetModel::class.java
                    WidgetTypes.TYPE_REFERRAL_LEVEL_WIDGET -> clazz =
                        ReferralLevelWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_IMAGE_TEXT_WIDGET -> clazz =
                        ImageTextWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_REFERRAL_WINNER_EARN_MORE_WIDGET -> clazz =
                        ReferralWinnerEarnWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_REFERRAL_WINNER_CONGRATULATIONS_WIDGET -> clazz =
                        ReferralWinnerCongratulationsWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_REFERRAL_WINNER_EARN_MORE_WIDGET_V2 -> clazz =
                        ReferralWinnerEarnWidgetV2.WidgetModel::class.java
                    WidgetTypes.TYPE_REFERRAL_VIDEO_WIDGET -> clazz =
                        ReferralVideoWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_REFERRAL_CLAIM_WIDGET -> clazz =
                        ReferralClaimWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_MATCH_PAGE_EXTRA_FEATURE -> clazz =
                        MatchPageExtraFeatureWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_SHORTS_VIDEO_DEFAULT ->
                        clazz = ShortsVideoDefaultWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_SHORTS_VIDEO_PROGRESS ->
                        clazz = ShortsVideoProgressWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_NO_DATA -> clazz =
                        NoDataWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_FILTER_SORT -> clazz =
                        FilterSortWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_TWO_TEXTS_HORIZONTAL -> clazz =
                        TwoTextsHorizontalWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_WATCH_AND_WIN -> clazz =
                        WatchAndWinWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGETS_TWO_TEXTS_VERTICAL_TABS -> clazz =
                        TwoTextsVerticalTabsWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_WATCH_NOW -> clazz =
                        WatchNowWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_IAS -> clazz =
                        IASWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_REFER_AND_EARN_HEADER_WIDGET ->
                        clazz = ReferAndEarnHeaderWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_REFER_AND_EARN_STEPS_WIDGET ->
                        clazz = ReferAndEarnStepsWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_REFERRAL_CODE_WIDGET ->
                        clazz = ReferralCodeWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_REFERRED_FRIENDS_WIDGET ->
                        clazz = ReferredFriendsWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_D0_QA_WIDGET ->
                        clazz = D0QaWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_RC_TEST_PAPER -> clazz =
                        RCTestPapersWidget.Model::class.java
                    WidgetTypes.TYPE_WIDGET_RC_PREVIOUS_YEAR_PAPER -> clazz =
                        RCPreviousYearPapersWidget.Model::class.java
                    WidgetTypes.TYPE_VERTICAL_PARENT_WIDGET -> clazz =
                        VerticalParentWidget.VerticalParentWidgetModel::class.java

                    WidgetTypes.TYPE_COUPON_APPLIED_WIDGET -> clazz =
                        CouponAppliedWidget.Model::class.java
                    WidgetTypes.TYPE_CLIPBOARD_WIDGET -> clazz =
                        ClipboardWidget.Model::class.java

                    WidgetTypes.TYPE_TIMER_WIDGET -> clazz =
                        TimerWidget.Model::class.java
                    WidgetTypes.TYPE_DOUBT_PE_CHARCHA_QUESTION->
                        clazz = DoubtPeCharchaQuestionWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_GRADIENT_BANNER_WITH_ACTION_BUTTON_WIDGET->
                        clazz = GradientBannerWithActionButtonWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_BADGE_FOR_LEVEL->
                        clazz = BadgesForLevelWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_USER_BADGE_BANNER_WIDGET->
                        clazz = UserBadgeBannerWidget.WidgetModel::class.java
                    WidgetTypes.TYPE_DOUBT_SUGGESTER_WIDGET -> clazz =
                        DoubtSuggesterWidget.DoubtSuggesterWidgetModel::class.java
                    WidgetTypes.TYPE_MORE_TESTIMONIALS_WIDGET -> clazz =
                        MoreTestimonialsWidget.MoreTestimonialsWidgetModel::class.java
                    WidgetTypes.TYPE_TOPPERS_WIDGET -> clazz =
                        ToppersWidget.ToppersWidgetModel::class.java
                    WidgetTypes.TYPE_EXCEL_COURSES_WIDGET -> clazz =
                        ExcelCoursesWidget.ExcelCoursesWidgetModel::class.java

                    else -> {
                    }
                }
                if (clazz != null) {
                    widget = context.deserialize<WidgetEntityModel<*, *>>(json, clazz)
                }
            } catch (e: Exception) {
                ToastUtils.makeTextInDev(message = e.message ?: "")
                e(e, TAG)
            }
        }
        return widget
    }

    companion object {
        const val TAG = "WidgetTypeAdapter"
    }
}