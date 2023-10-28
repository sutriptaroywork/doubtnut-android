package com.doubtnutapp.widgetmanager

import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreWidgetVH
import com.doubtnut.referral.widgets.*
import com.doubtnut.scholarship.widget.*
import com.doubtnutapp.*
import com.doubtnutapp.addtoplaylist.HomePageAskDoubtWidget
import com.doubtnutapp.addtoplaylist.HomePageAskDoubtWidgetHolder
import com.doubtnutapp.addtoplaylist.HomePageAskDoubtWidgetModel
import com.doubtnutapp.callingnotice.CallingNoticeWidget
import com.doubtnutapp.callingnotice.CallingNoticeWidgetModel
import com.doubtnutapp.course.widgets.*
import com.doubtnutapp.course.widgets.ImageTextWidget
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.data.remote.models.feed.IncompleteChapterWidgetModel
import com.doubtnutapp.data.remote.models.feed.TopOptionsWidgetModel
import com.doubtnutapp.databinding.WidgetAllClassesBinding
import com.doubtnutapp.dnr.widgets.*
import com.doubtnutapp.doubt.bookmark.widget.BookmarkListWidget
import com.doubtnutapp.doubt.bookmark.widget.BookmarkListWidgetModel
import com.doubtnutapp.examcorner.widgets.*
import com.doubtnutapp.explore_v2.ExploreCourseV2WidgetCircle
import com.doubtnutapp.explore_v2.ExploreCourseV2WidgetCircleModel
import com.doubtnutapp.explore_v2.ExploreCourseV2WidgetSquare
import com.doubtnutapp.explore_v2.ExploreCourseV2WidgetSquareModel
import com.doubtnutapp.freeclasses.widgets.*
import com.doubtnutapp.icons.widgets.ExploreCardWidget
import com.doubtnutapp.icons.widgets.FavouriteExploreCardWidget
import com.doubtnutapp.leaderboard.widget.*
import com.doubtnutapp.libraryhome.coursev3.ui.CouponBannerWidget
import com.doubtnutapp.libraryhome.coursev3.ui.CouponBannerWidgetHolder
import com.doubtnutapp.libraryhome.coursev3.ui.CouponBannerWidgetModel
import com.doubtnutapp.liveclass.ui.practice_english.PracticeEnglishWidget
import com.doubtnutapp.mediumSwitch.MediumSwitchWidget
import com.doubtnutapp.mediumSwitch.MediumSwitchWidgetModel
import com.doubtnutapp.paymentplan.widgets.*
import com.doubtnutapp.quiztfs.widgets.*
import com.doubtnutapp.referral.ReferralVideoWidget
import com.doubtnutapp.resultpage.widgets.ExcelCoursesWidget
import com.doubtnutapp.resultpage.widgets.MoreTestimonialsWidget
import com.doubtnutapp.resultpage.widgets.ToppersWidget
import com.doubtnutapp.revisioncorner.ui.RCPreviousYearPapersWidget
import com.doubtnutapp.revisioncorner.ui.RCTestPapersWidget
import com.doubtnutapp.sales.PrePurchaseCallingCard2
import com.doubtnutapp.sales.PrePurchaseCallingCardModel2
import com.doubtnutapp.sales.widget.PrePurchaseCallingCard
import com.doubtnutapp.sales.widget.PrePurchaseCallingCardModel
import com.doubtnutapp.similarVideo.widgets.*
import com.doubtnutapp.ui.forum.doubtsugggester.widget.DoubtSuggesterWidget
import com.doubtnutapp.videoPage.widgets.LiveClassCarouselCard2Widget
import com.doubtnutapp.videoPage.widgets.LiveClassCarouselCardWidget
import com.doubtnutapp.videoPage.widgets.SrpNudgeCourseWidget
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.*
import com.doubtnutapp.widgetmanager.widgets.tablist.TabListWidget
import com.doubtnutapp.widgetmanager.widgets.tablist.TabListWidgetModel
import com.doubtnutapp.widgets.*
import com.doubtnutapp.widgettest.widgets.DummyWidget
import com.doubtnutapp.widgettest.widgets.DummyWidgetModel

object WidgetFactory {

    fun createViewHolder(
        context: Context,
        parent: ViewGroup?,
        type: String,
        actionsPerformerListener: ActionPerformer? = null,
        source: String? = "",
        lifecycleOwner: LifecycleOwner? = null
    ): CoreWidgetVH? {
        when (type) {
            // Structured Course and course list should have been the same and action should have been
            // configured through deeplinks, not supported from backend currently
            WidgetTypes.TYPE_STRUCTURED_COURSE_LIST -> {
                return CourseListWidget(context, true).getViewHolder()
            }
            WidgetTypes.TYPE_COURSE_LIST -> {
                return CourseListWidget(context, false).getViewHolder()
            }
            WidgetTypes.TYPE_HEADER_MESSAGE -> {
                return HeaderMessageWidget(context).getViewHolder()
            }
            WidgetTypes.TYPE_FILTER_TABS -> {
                return FilterTabsWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_BANNER_IMAGE -> {
                return BannerImageWidget(context).apply {
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_PAYMENT_CARD -> {
                return PaymentCardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_FACULTY_GRID -> {
                return FacultyListV2Widget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_VERTICAL_LIST -> {
                return VerticalWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_TOPPERS_SPEAK -> {
                return ToppersSpeakWidget(context).getViewHolder()
            }
            WidgetTypes.TYPE_MEDIUM_SWITCH_WIDGET -> {
                return MediumSwitchWidget(context).getViewHolder()
            }
            WidgetTypes.TYPE_EXAM_CORNER_DEFAULT_WIDGET -> {
                return ExamCornerDefaultWidget(context).getViewHolder()
            }
            WidgetTypes.TYPE_EXAM_CORNER_POPULAR_WIDGET -> {
                return ExamCornerPopularWidget(context).getViewHolder()
            }
            WidgetTypes.TYPE_EXAM_CORNER_AUTOPLAY_WIDGET -> {
                return ExamCornerAutoplayWidget(context).getViewHolder()
            }
            WidgetTypes.TYPE_SHORTS_VIDEOS_EXHAUSTED_WIDGET -> {
                return ShortsVideosExhaustedWidget(context).getViewHolder()
            }
            WidgetTypes.TYPE_CALLING_NOTICE_WIDGET -> {
                return CallingNoticeWidget(context).apply {
                    this.source = source
                }
                    .getViewHolder()
            }
            WidgetTypes.TYPE_BUTTON -> {
                return ButtonWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_HORIZONTAL_LIST -> {
                return HorizontalListWidget(context).getViewHolder()
            }

            WidgetTypes.WIDGET_TYPE_RECENT_STATUS -> {
                return RecentStatusWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_VERTICAL_LIST_2 -> {
                return VerticalListWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_CAROUSEL_LIST -> {
                return CarouselListWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WHATSAPP -> {
                return WhatsappWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_DAILY_QUIZ -> {
                return DailyQuizWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_TAB -> {
                return TabCourseWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_LIST_FILTER -> {
                return CourseFilterWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_REMINDER_CARD -> {
                return ReminderCardWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_SIMPLE_TEXT -> {
                return SimpleTextWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_ALL_COURSE_LIST -> {
                return AllCourseWidget2(context).apply {
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_FILTER_EXAM -> {
                return FilterExamWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_FILTER_SUBJECT -> {
                return FilterSubjectWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_FAQ -> {
                return FaqWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_NOTES_LIST -> {
                return NotesWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_PAYMENT_HISTORY -> {
                return PaymentHistoryWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE -> {
                return CourseWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_V2 -> {
                return CourseWidgetV2(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_PURCHASED_CLASSES -> {
                return PurchasedClassesWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_LIVE_CLASS_CATEGORY -> {
                return LiveClassCategoryWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_PROGRESS -> {
                return CourseProgressWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_RELATED_LECTURES_WIDGET -> {
                return RelatedLecturesWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_NOTIFY_LIVE_CLASS_WIDGET -> {
                return NotifyClassWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_RESOURCE_PAGE_UPCOMING_WIDGET -> {
                return ResourcePageUpcomingWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_UPCOMING_LECTURES_WIDGET -> {
                return UpcomingLecturesWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_INFO_WIDGET -> {
                return CourseInfoWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_ALL_TOPICS_WIDGET -> {
                return AllTopicsWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_TOPICS_COVERED_WIDGET -> {
                return TopicsCoveredWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_LIVE_CLASSES_INFO -> {
                return LiveClassInfoWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_RANKERS_CLASSES -> {
                return RankersClassesWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_RANKERS -> {
                return RankersWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_CONTENT -> {
                return CourseContentWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_EXAM_TABS -> {
                return CourseExamTabWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_TYPE_TABS -> {
                return CourseTypeTabWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_CLASS_TABS -> {
                return CourseClassTabWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_PAYMENT_CARD_LIST -> {
                return PaymentListWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_PROMO_LIST -> {
                return PromoListWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_PLAN_TAB -> {
                return PlanTypeTabWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_PLAN_INFO -> {
                return PlanInfoWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_PLAN_LIST -> {
                return PlanListWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_MY_PLAN -> {
                return MyPlanWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_TYPE_FILTER -> {
                return FilterCourseTypeWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_LIVE_CLASS_CAROUSEL -> {
                return LiveClassCarouselWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_TAB_LIST -> {
                return TabListWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_COURSE_COMMON -> {
                return CommonCourseWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_SYLLABUS2 -> {
                return SyllabusWidgetTwo(context).getViewHolder()
            }
            WidgetTypes.TYPE_TIME_TABLE -> {
                return TimetableWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_ICON_HEADER -> {
                return IconHeaderWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_PLAN_DESCRIPTION -> {
                return PlanDescriptionInfoWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_PACKAGE_DETAIL -> {
                return PackageDetailWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_PACKAGE_DETAIL_V2 -> {
                return PackageDetailWidgetV2(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.PURCHASED_COURSE_LIST_WIDGET -> {
                return PurchasedCourseListWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_CATEGORY -> {
                return CourseCategoryWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_PARENT -> {
                return ParentWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_PARENT_TAB -> {
                return ParentTabWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_IPL_SCORE_BOARD -> {
                return IplScoreBoardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_CHILD -> {
                return CourseChildWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_CHILD_CAROUSEL -> {
                return CourseCarouselChildWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_CHILD_RESOURCE -> {
                return CourseResourceChildWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_FACULTY_GRID2 -> {
                return FacultyGridWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_SUBJECT -> {
                return SubjectWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_SUBJECT_POST_PURCHASE -> {
                return CourseSubjectWidget(context).apply {
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_RESOURCE_POST_PURCHASE -> {
                return CourseResourcesWidget(context).apply {
                    this.source = source
                    this.lifecycleOwner = lifecycleOwner
                }.getViewHolder()
            }

            WidgetTypes.TYPE_LIVE_CLASS_CAROUSEL_CARD -> {
                return LiveClassCarouselCardWidget(context).apply {
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_LIVE_CLASS_CAROUSEL_CARD_2 -> {
                return LiveClassCarouselCard2Widget(context).apply {
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_SALE_WIDGET -> {
                return SaleWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_ACTIVATE_TRIAL_WIDGET -> {
                return ActivateTrialWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_NOTES_FILTER -> {
                return NotesFilterWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_CATEGORY_PAGE_FILTER -> {
                return MultiSelectFilterWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_CATEGORY_PAGE_FILTER_V2 -> {
                return MultiSelectFilterWidgetV2(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_ASK_DOUBT_CARD -> {
                return AskDoubtCardWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_IMAGE_CARD -> {
                return ImageCardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_FEED_BANNER -> {
                return FeedBannerWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_HOME_WORK -> {
                return HomeWorkWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_HOME_WORK_LIST -> {
                return HomeWorkListWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_HOME_WORK_LIST_V2 -> {
                return HomeWorkListWidgetV2(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_HOME_WORK_HORIZONTAL_LIST -> {
                return HomeWorkHorizontalListWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_TOP_OPTION -> {
                return TopOptionsWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_INCOMPLETE_CHAPTER -> {
                return IncompleteChapterWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COLLAPSED -> {
                return CollapsedWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_PLAYLIST -> {
                return PlaylistWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_GRADIENT_CARD -> {
                return GradientCardWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_BOOK_PROGRESS -> {
                return BookProgressWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_AUTOPLAY -> {
                return ParentAutoplayWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_CHILD_AUTOPLAY -> {
                return AutoPlayChildWidget(context).apply {
                    this.source = source
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_UPCOMING_LIVE_CLASS -> {
                return UpcomingLiveClassWidget(context).apply {
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_PAID_COURSE -> {
                return PaidCourseWidget(context).apply {
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_YOU_WERE_WATCHING -> {
                return YouWereWatchingWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_VIDEO_BANNER_AUTOPLAY_CHILD -> {
                return VideoBannerAutoplayChildWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_VIDEO_AUTOPLAY_CHILD2 -> {
                return VideoAutoplayChildWidget2(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_FEED_PINNED_VIDEO_AUTOPLAY_CHILD -> {
                return FeedPinnedVideoAutoplayChildWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_EXPLORE_MORE -> {
                return ExploreMoreWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_IMAGE_CARD_GROUP -> {
                return ImageCardGroupWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_FOLLOW_WIDGET -> {
                return FollowWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_TESTIMONIAL -> {
                return TestimonialWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_FACULTY_LIST -> {
                return FacultyListWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_FAQS -> {
                return CourseFaqsWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_CONTENT_LIST -> {
                return CourseContentListWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_INFO -> {
                return CourseItemInfoWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_FEATURE -> {
                return CourseFeatureWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_EMI_INFO -> {
                return CourseEmiInfoWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_TOP_DOUBT_ANSWER_VIDEO -> {
                return TopDoubtAnswerVideoWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_TOP_DOUBT_ANSWER_AUDIO -> {
                return TopDoubtAnswerAudioWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_TOP_DOUBT_ANSWER_IMAGE -> {
                return TopDoubtAnswerImageWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_PENDING_PAYMENT -> {
                return PendingPaymentWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COURSE_PARENT -> {
                return CourseParentWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COURSE_CLASSES -> {
                return CourseClassesWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COURSE_TEST -> {
                return CourseTestWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_ALL_CLASSES -> {
                return AllClassesWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_INCREASE_VALIDITY -> {
                return IncreaseValidityWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_STORIES -> {
                return StoryWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_RESOURCE_V4 -> {
                return ResourceV4Widget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_IMAGE_TEXT -> {
                return ImageTextWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_RESOURCE_NOTES_V3 -> {
                return ResourceNotesV3Widget(context).getViewHolder()
            }

            WidgetTypes.TYPE_NOTES_FILTER2 -> {
                return NotesFilterByWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COURSE_AUTOPLAY -> {
                return CourseAutoPlayChildWidget(context).apply {
                    this.source = source
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_SCHEDULE_V2 -> {
                return ScheduleWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_TRIAL_TIMER -> {
                return TrialTimerWidget(context).getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_TEST -> {
                return TestWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_VIEW_ALL -> {
                return ViewAllWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_SCHEDULE_MONTH_FILTER -> {
                return ScheduleMonthFilterWidget(context).apply {
                    this.source = source
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_COLLAPSE_EXPAND_MATH_VIEW -> {
                return CollapseExpandMathViewWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_VIDEO_ACTIONS -> {
                return VideoActionWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_PARENT_GRID_SELECTION -> {
                return ParentGridSelectionWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_CHILD_GRID_SELECTION -> {
                return ChildGridSelectionWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_NCERT_SIMILAR -> {
                return NcertSimilarWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_NCERT_BOOK -> {
                return NcertBookWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_POPULAR_COURSE -> {
                return PopularCourseWidget(context).apply {
                    this.source = source
                    actionPerformer = actionsPerformerListener
                }
                    .getViewHolder()
            }

            WidgetTypes.TYPE_ATTENDANCE -> {
                return AttendanceWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_SIMILAR -> {
                return SimilarWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_ASK_DOUBT -> {
                return HomePageAskDoubtWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_STUDY_DOST -> {
                return StudyDostWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_TEXT_WIDGET -> {
                return TextWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_BANNER_IMAGE -> {
                return StudyGroupBannerImageWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COUPON_BANNER -> {
                return CouponBannerWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_PDF_NOTES -> {
                return PdfNotesWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_DOUBT_FEED_DAILY_GOAL -> {
                return DoubtFeedDailyGoalWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_TOPIC_BOOSTER -> {
                return TopicBoosterWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_FORMULA_SHEET -> {
                return FormulaSheetWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_TOPIC_VIDEO -> {
                return TopicVideoWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_ASKED_QUESTION -> {
                return AskedQuestionWidget(context).apply {
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_DOUBT_FEED_START_PRACTICE_WIDGET -> {
                return DoubtFeedStartPracticeWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_TOPIC_BOOSTER_GAME_BANNER -> {
                return TopicBoosterGameBannerWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_KHELO_JEETO_BANNER -> {
                return KheloJeetoBannerWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_MY_COURSE -> {
                return MyCourseWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_PRE_PURCHASE_CALL_CARD -> {
                return PrePurchaseCallingCard(context).getViewHolder()
            }

            WidgetTypes.TYPE_PRE_PURCHASE_CALL_CARD_V2 -> {
                return PrePurchaseCallingCard2(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_PARENT -> {
                return StudyGroupParentWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_REPORT_PARENT -> {
                return StudyGroupReportParentWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_AUDIO_PLAYER -> {
                return AudioPlayerWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_GUIDELINE -> {
                return StudyGroupGuidelineWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_INVITATION -> {
                return StudyGroupInvitationWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_JOINED_INFO -> {
                return StudyGroupJoinedInfoWidget(context).getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_FEATURE_UNAVAILABLE -> {
                return StudyGroupFeatureUnavailableWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_PDF_VIEW -> {
                return PdfViewWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_LIVE_CLASS -> {
                return StudyGroupLiveClassWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_DOUBT_FEED_BANNER -> {
                return DoubtFeedBannerWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_MY_SACHET -> {
                return MySachetWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_NUDGE -> {
                return NudgeWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_NUDGE_POPUP -> {
                return NudgePopupWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_DOUBT_P2P_HOME -> {
                return DoubtP2PHomeWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DOUBT_P2P -> {
                return DoubtP2PWidget(context).apply {
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DOUBT_P2P_ANIMATION -> {
                return DoubtP2PAnimationWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_PACKAGE_DETAIL_V3 -> {
                return PackageDetailWidgetV3(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_CLASS_BOARD_EXAM -> {
                return ClassBoardExamWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_PARENT_COURSE_RECOMMENDATION_INCOMING -> {
                return CourseRecommendationParentWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COURSE_RECOMMENDATION_MESSAGE -> {
                return CourseRecommendationMessageWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_COURSE_RECOMMENDATION_RADIO_BUTTON -> {
                return CourseRecommendationRadioButtonWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_LEADERBOARD_PERSONAL -> {
                return LeaderboardPersonalWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_LEADERBOARD_TAB -> {
                return LeaderboardTabWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_LEADERBOARD_TOP_THREE -> {
                return LeaderboardTopThreeWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_LEADERBOARD -> {
                return LeaderBoardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_VIDEO_CARD -> {
                return StudyGroupVideoCardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COURSE_RECOMMENDATION_SUBMITTED_ANSWER -> {
                return CourseRecommendationSubmittedAnswerWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_BOOKMARK_LIST -> {
                return BookmarkListWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_AUTO_SCROLL_CARD_WIDGET -> {
                return AutoScrollImageWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_SAMPLE_QUESTION -> {
                return SampleQuestionWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COURSE_EXPLORE -> {
                return CourseExploreWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_DOUBT_FEED -> {
                return DoubtFeedWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COURSE_INFO_WIDGET_2 -> {
                return CourseInfoWidget2(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COURSE_DETAILS -> {
                return CourseDetailsWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_TRIAL_BUTTON -> {
                return TrialButtonWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_TIME_TABLE -> {
                return CourseTimeTableWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_BUY_COMPLETE_COURSE -> {
                return BuyCompleteCourseWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_COURSE_PLAN -> {
                return PlanWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_VIEW_PLAN_BUTTON -> {
                return WidgetViewPlanButton(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COURSE_TEST_V2 -> {
                return CourseTestWidgetV2(context).getViewHolder()
            }

            WidgetTypes.TYPE_NOTES_LIST_V2 -> {
                return NotesWidgetV2(context).getViewHolder()
            }

            WidgetTypes.TYPE_PACKAGE_DETAIL_V4 -> {
                return PackageDetailWidgetV4(context).apply {
                    this.source = source.orEmpty()
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_BUTTON_BORDER -> {
                return ButtonBorderWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_SELECT_MEDIUM -> {
                return SelectMediumWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_SYLLABUS -> {
                return SyllabusWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_CONTENT_FILTER -> {
                return ContentFilterWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_INFO_V2 -> {
                return CourseInfoWidgetV2(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_TESTIMONIAL_V2 -> {
                return TestimonialWidgetV2(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_V4 -> {
                return CourseWidgetV4(context).getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_V3 -> {
                return CourseWidgetV3(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COUPON_LIST -> {
                return CouponListWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_RESOURCE_V2 -> {
                return CourseResourceWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_TOP_SELLING_SUBJECT -> {
                return TopSellingSubjectWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_CALLING_BIG_CARD -> {
                return CallingBigCardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COURSE_RECOMMENDATION -> {
                return CourseRecommendationWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_CATEGORY -> {
                return CategoryWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_RECOMMENDED_TEST -> {
                return RecommendedTestWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COURSE_INFO_V3 -> {
                return CourseInfoWidgetV3(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_EXPLORE_PROMO_WIDGET -> {
                return ExplorePromoWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_VIDEO_WIDGET -> {
                return VideoWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_RECOMMENDATION_WIDGET -> {
                return RecommendationWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_VPA -> {
                return VpaWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_AWARDED_STUDENTS_LIST -> {
                return AwardedStudentsWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_REFERRAL -> {
                return ReferralWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_PREVIOUS_TEST_RESULTS -> {
                return PreviousTestResultsWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_PRACTICE_TEST -> {
                return PracticeTestWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_SCHOLARSHIP_TABS -> {
                return ScholarshipTabsWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_PROGRESS -> {
                return WidgetProgress(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_SCHOLARSHIP_PROGRESS_CARD -> {
                return ScholarshipProgressCardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_REPORT_CARD -> {
                return ReportCardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_REGISTER_TEST -> {
                return RegisterTestWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_TEST_ANALYSIS -> {
                return TestAnalysisWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_TEST_RESULT -> {
                return TestResultWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_REVISION_CORNER_BANNER -> {
                return RevisionCornerBannerWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_ICON_CTA -> {
                return IconCtaWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_LIVE_QUESTIONS_DAILY_PRACTICE_WIDGET -> {
                return LiveQuestionsDailyPracticeWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_LIVE_QUESTIONS_DAILY_PRACTICE_RANK_WIDGET -> {
                return LiveQuestionsDailyPracticeRankWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_LIVE_QUESTIONS_DAILY_PRACTICE_FAQ_WIDGET -> {
                return LiveQuestionsDailyPracticeFAQWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DAILY_PRACTICE -> {
                return DailyPracticeWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.MY_REWARDS_POINTS_WIDGET -> {
                return MyRewardsPointsWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.MY_REWARDS_SCRATCH_CARD_WIDGET -> {
                return MyRewardsScratchCardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_BULLET_LIST_WIDGET -> {
                return BulletListWidget(context).apply {
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_PADHO_AUR_JEETO_WIDGET -> {
                return PadhoAurJeetoWidget(context).apply {
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_LEADER_BOARD_PROGRESS -> {
                return LeaderboardProgressWidget(context).apply {
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_TFS_ANALYSIS -> {
                return TfsAnalysisWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_PARENT_TAB_2 -> {
                return ParentTabWidget2(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_PARENT_TAB_3 -> {
                return ParentTabWidget3(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_EXPLORE_COURSE_V2_SQUARE -> {
                return ExploreCourseV2WidgetSquare(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_EXPLORE_COURSE_V2_CIRCLE -> {
                return ExploreCourseV2WidgetCircle(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_LATEST_LAUNCHES -> {
                return LatestLaunchesWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_SG_GROUP_CHAT -> {
                return SgGroupChatWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.WIDGET_JOIN_NEW_STUDYGROUP -> {
                return SgJoinNewGroupWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_SG_INDIVIDUAL_CHAT -> {
                return SgPersonalChatWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_SG_REQUEST -> {
                return SgRequestWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_SG_BLOCKED_MEMBER -> {
                return SgBlockedMemberWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_SG_HOME -> {
                return SgHomeWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            // teacher
            WidgetTypes.TYPE_TEACHER_HEADER -> {
                return TeacherHeaderWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_ANNOUNCEMENT_WIDGET -> {
                return ChannelAnnouncementWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_CHANNEL_FILTER_TAB -> {
                return ChannelTabFilterWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_CHANNEL_FILTER -> {
                return ChannelSubTabFilterWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_CHANNEL_FILTER_CONTENT -> {
                return ChannelContentFilterWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_VIDEO_TYPE -> {
                return ChannelVideoContentWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_PDF_TYPE -> {
                return ChannelPDFContentWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_SUBSCRIBED_TEACHERS -> {
                return SubscribedTeacherChannelWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_TEACHERS_LIST -> {
                return TeacherChannelWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_TEACHER_CHANNEL_2 -> {
                return TeacherChannelWidget2(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_DUMMY -> {
                return DummyWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_EXPLORE_CARD -> {
                return ExploreCardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_FAVOURITE_EXPLORE_CARD -> {
                return FavouriteExploreCardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_CHANNEL -> {
                return ChannelWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_VIDEO_OFFSET -> {
                return VideoOffsetWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DNR_EARNED_HISTORY -> {
                return DnrEarnedHistoryWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DNR_EARNED_HISTORY_ITEM -> {
                return DnrEarnedHistoryItemWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DNR_EARNED_SUMMARY -> {
                return DnrEarnedSummaryWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DNR_UNLOCKED_VOUCHER -> {
                return DnrUnlockedVoucherWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DNR_STREAK -> {
                return DnrStreakWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DNR_TEXT -> {
                return DnrTextWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DNR_NO_EARNED_HISTORY -> {
                return DnrNoEarnedHistoryWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DNR_TOTAL_REWARD -> {
                return DnrTotalRewardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DNR_EARNING_DETAILS -> {
                return DnrEarningsDetailWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DNR_REWARD_DETAIL -> {
                return DnrRewardDetailWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DNR_REDEEM_VOUCHER -> {
                return DnrRedeemVoucherWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DNR_TODAY_REWARD -> {
                return DnrTodayRewardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DNR_REWARD_HISTORY -> {
                return DnrRewardHistoryWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_DNR_HOME -> {
                return DnrHomeWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_TOP_SUBJECT_STUDYING -> {
                return TopSubjectStudyingWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_CLASSES_BY_TEACHER -> {
                return ClassesByTeacherWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_YOU_WERE_WATCHING_V2 -> {
                return YouWereWatchingV2Widget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_CHAPTER_BY_CLASSES -> {
                return ChapterByClassesWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_LIBRARY_CARD -> {
                return LibraryCardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_LIBRARY_EXAM -> {
                return LibraryExamWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_MATCH_PAGE -> {
                return MatchPageWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_CHECKOUT_V2_HEADER -> {
                return CheckoutV2HeaderWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_CHECKOUT_V2_COUPON -> {
                return CheckoutV2CouponWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_CHECKOUT_V2_PARENT -> {
                return CheckoutV2ParentWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_CHECKOUT_V2_CHILD -> {
                return CheckoutV2ChildWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_CHECKOUT_V2_WALLET -> {
                return CheckoutV2WalletWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_CHECKOUT_V2_TALK_TO_US -> {
                return CheckoutV2TalkToUsWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COURSE_TIME_TABLE_V2 -> {
                return CourseTimeTableWidgetV2(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_MOST_VIEWED_CLASSES -> {
                return MostViewedClassesWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_FILTER_BUTTON -> {
                return FilterButtonWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_FILTER_DROPDOWN -> {
                return FilterDropDownWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_NO_DATA -> {
                return NoDataWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_FILTER_SORT -> {
                return FilterSortWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_TWO_TEXTS_HORIZONTAL -> {
                return TwoTextsHorizontalWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_WATCH_AND_WIN -> {
                return WatchAndWinWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

             WidgetTypes.TYPE_WIDGETS_TWO_TEXTS_VERTICAL_TABS -> {
                return TwoTextsVerticalTabsWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

             WidgetTypes.TYPE_WIDGET_WATCH_NOW -> {
                return WatchNowWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_PREVIOUS_WINNERS_WIDGET -> {
                return PreviousWinnersWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WINNERS_CARD_WIDGET -> {
                return WinnersCardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_COPY_TEXT -> {
                return CopyTextWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_MULTISELECT_SUBJECT_FILTER -> {
                return MultiSelectSubjectFilterWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_PRACTICE_ENGLISH -> {
                return PracticeEnglishWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_SRP_NUDGE -> {
                return SrpNudgeCourseWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_SUBJECT_COURSE_CARD -> {
                return SubjectCourseCardWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_FREE_TRIAL_COURSE -> {
                return FreeTrialCourseWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_GRADIENT_CARD_WITH_BUTTON -> {
                return GradientCardWithButtonWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_COURSE_CAROUSEL -> {
                return CourseCarouselWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.NEXT_SCREEN_WIDGET -> {
                return NextScreenWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.ENGLISH_QUIZ_INFO_WIDGET -> {
                return EnglishQuizInfoWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.INVITE_FRIEND_WIDGET -> {
                return InviteFriendWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_REFERRAL_STEPS_WIDGET -> {
                return ReferralStepsWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_REFERRAL_LEVEL_WIDGET -> {
                return ReferralLevelWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_IMAGE_TEXT_WIDGET -> {
                return com.doubtnut.referral.widgets.ImageTextWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_REFERRAL_WINNER_EARN_MORE_WIDGET -> {
                return ReferralWinnerEarnWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_REFERRAL_WINNER_CONGRATULATIONS_WIDGET -> {
                return ReferralWinnerCongratulationsWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_REFERRAL_WINNER_EARN_MORE_WIDGET_V2 -> {
                return ReferralWinnerEarnWidgetV2(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_REFERRAL_VIDEO_WIDGET -> {
                return ReferralVideoWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_REFERRAL_CLAIM_WIDGET -> {
                return ReferralClaimWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_MATCH_PAGE_EXTRA_FEATURE -> {
                return MatchPageExtraFeatureWidget(context).apply {
                    this.source = source
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_SHORTS_VIDEO_DEFAULT -> {
                return ShortsVideoDefaultWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_SHORTS_VIDEO_PROGRESS -> {
                return ShortsVideoProgressWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_WIDGET_IAS -> {
                return IASWidget(context).apply {
                    this.source = source
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_REFER_AND_EARN_HEADER_WIDGET -> {
                return ReferAndEarnHeaderWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_REFER_AND_EARN_STEPS_WIDGET -> {
                return ReferAndEarnStepsWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_REFERRAL_CODE_WIDGET -> {
                return ReferralCodeWidget(context).getViewHolder()
            }

            WidgetTypes.TYPE_REFERRED_FRIENDS_WIDGET -> {
                return ReferredFriendsWidget(context).getViewHolder()
            }
            WidgetTypes.TYPE_D0_QA_WIDGET -> {
                return D0QaWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_RC_TEST_PAPER -> {
                return RCTestPapersWidget(context).apply {
                    this.source = source
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_WIDGET_RC_PREVIOUS_YEAR_PAPER -> {
                return RCPreviousYearPapersWidget(context).apply {
                    this.source = source
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_VERTICAL_PARENT_WIDGET -> {
                return VerticalParentWidget(context).apply {
                    this.source = source
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_COUPON_APPLIED_WIDGET -> {
                return CouponAppliedWidget(context).apply {
                    this.source = source
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_CLIPBOARD_WIDGET -> {
                return ClipboardWidget(context).apply {
                    this.source = source
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_TIMER_WIDGET -> {
                return TimerWidget(context).apply {
                    this.source = source
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }
            WidgetTypes.TYPE_DOUBT_SUGGESTER_WIDGET -> {
                return DoubtSuggesterWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()
            }

            WidgetTypes.TYPE_DOUBT_PE_CHARCHA_QUESTION ->
                return DoubtPeCharchaQuestionWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()

            WidgetTypes.TYPE_GRADIENT_BANNER_WITH_ACTION_BUTTON_WIDGET ->
                return GradientBannerWithActionButtonWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                }.getViewHolder()

            WidgetTypes.TYPE_BADGE_FOR_LEVEL ->
                return BadgesForLevelWidget(context).getViewHolder()

            WidgetTypes.TYPE_USER_BADGE_BANNER_WIDGET ->
                return UserBadgeBannerWidget(context).getViewHolder()

            WidgetTypes.TYPE_MORE_TESTIMONIALS_WIDGET -> {
                return MoreTestimonialsWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }
            WidgetTypes.TYPE_TOPPERS_WIDGET -> {
                return ToppersWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

            WidgetTypes.TYPE_EXCEL_COURSES_WIDGET -> {
                return ExcelCoursesWidget(context).apply {
                    actionPerformer = actionsPerformerListener
                    this.source = source
                }.getViewHolder()
            }

        }
        return null
    }

    fun bindViewHolder(
        holder: CoreWidgetVH,
        widget: WidgetEntityModel<*, *>,
        adapter: WidgetLayoutAdapter? = null
    ): CoreWidgetVH? {
        when (widget.type) {
            WidgetTypes.TYPE_STRUCTURED_COURSE_LIST, WidgetTypes.TYPE_COURSE_LIST -> {
                return (holder.itemView as CourseListWidget).bindWidget(
                    holder as CourseListWidget.WidgetHolder,
                    widget as CourseListWidget.CourseListWidgetModel
                )
            }
            WidgetTypes.TYPE_HEADER_MESSAGE -> {
                return (holder.itemView as HeaderMessageWidget).bindWidget(
                    holder as HeaderMessageWidget.HeaderMessageWidgetHolder,
                    widget as HeaderMessageWidget.HeaderMessageWidgetModel
                )
            }
            WidgetTypes.TYPE_FILTER_TABS -> {
                return (holder.itemView as FilterTabsWidget).bindWidget(
                    holder as FilterTabsWidget.FilterTabsWidgetHolder,
                    widget as FilterTabsWidget.FilterTabsWidgetModel
                )
            }
            WidgetTypes.TYPE_BANNER_IMAGE -> {
                return (holder.itemView as BannerImageWidget).bindWidget(
                    holder as BannerImageWidget.BannerImageWidgetHolder,
                    widget as BannerImageWidget.BannerImageWidgetModel
                )
            }

            WidgetTypes.TYPE_PAYMENT_CARD -> {
                return (holder.itemView as PaymentCardWidget).bindWidget(
                    holder as PaymentCardWidget.PaymentCardWidgetHolder,
                    widget as PaymentCardWidgetModel
                )
            }

            WidgetTypes.TYPE_FACULTY_GRID -> {
                return (holder.itemView as FacultyListV2Widget).bindWidget(
                    holder as FacultyListV2Widget.FacultyListWidgetHolder,
                    widget as FacultyGridWidgetModel
                )
            }

            WidgetTypes.TYPE_VERTICAL_LIST -> {
                return (holder.itemView as VerticalWidget).bindWidget(
                    holder as VerticalWidget.WidgetHolder,
                    widget as VerticalListWidgetModel
                )
            }

            WidgetTypes.TYPE_TOPPERS_SPEAK -> {
                return (holder.itemView as ToppersSpeakWidget).bindWidget(
                    holder as ToppersSpeakWidget.WidgetHolder,
                    widget as ToppersSpeakWidgetModel
                )
            }

            WidgetTypes.TYPE_BUTTON -> {
                return (holder.itemView as ButtonWidget).bindWidget(
                    holder as ButtonWidget.ButtonWidgetHolder,
                    widget as ButtonWidgetModel
                )
            }

            WidgetTypes.TYPE_HORIZONTAL_LIST -> {
                return (holder.itemView as HorizontalListWidget).bindWidget(
                    holder as HorizontalListWidget.HorizontalListWidgetHolder,
                    widget as HorizontalListWidget.HorizontalListWidgetModel
                )
            }

            WidgetTypes.WIDGET_TYPE_RECENT_STATUS -> {
                return (holder.itemView as RecentStatusWidget).bindWidget(
                    holder as RecentStatusWidget.WidgetHolder,
                    widget as RecentStatusWidget.RecentStatusWidgetModel
                )
            }

            WidgetTypes.TYPE_VERTICAL_LIST_2 -> {
                return (holder.itemView as VerticalListWidget).bindWidget(
                    holder as VerticalListWidget.WidgetHolder,
                    widget as VerticalListWidget.VerticalListWidgetModel
                )
            }

            WidgetTypes.TYPE_CAROUSEL_LIST -> {
                return (holder.itemView as CarouselListWidget).bindWidget(
                    holder as CarouselListWidget.CarouselListWidgetHolder,
                    widget as CarouselListWidget.CarouselListWidgetModel
                )
            }

            WidgetTypes.TYPE_WHATSAPP -> {
                return (holder.itemView as WhatsappWidget).bindWidget(
                    holder as WhatsappWidget.WhatsappWidgetHolder,
                    widget as WhatsappWidget.WhatsappWidgetModel
                )
            }

            WidgetTypes.TYPE_DAILY_QUIZ -> {
                return (holder.itemView as DailyQuizWidget).bindWidget(
                    holder as DailyQuizWidget.DailyQuizWidgetHolder,
                    widget as DailyQuizWidget.DailyQuizWidgetModel
                )
            }

            WidgetTypes.TYPE_COURSE_TAB -> {
                return (holder.itemView as TabCourseWidget).bindWidget(
                    holder as TabCourseWidget.WidgetHolder,
                    widget as TabCourseWidgetModel
                )
            }

            WidgetTypes.TYPE_COURSE_LIST_FILTER -> {
                return (holder.itemView as CourseFilterWidget).bindWidget(
                    holder as CourseFilterWidget.WidgetHolder,
                    widget as CourseFilterWidgetModel
                )
            }

            WidgetTypes.TYPE_REMINDER_CARD -> {
                return (holder.itemView as ReminderCardWidget).bindWidget(
                    holder as ReminderCardWidget.ReminderCardWidgetHolder,
                    widget as ReminderCardWidgetModel
                )
            }

            WidgetTypes.TYPE_SIMPLE_TEXT -> {
                return (holder.itemView as SimpleTextWidget).bindWidget(
                    holder as SimpleTextWidget.WidgetHolder,
                    widget as SimpleTextWidgetModel
                )
            }

            WidgetTypes.TYPE_ALL_COURSE_LIST -> {
                return (holder.itemView as AllCourseWidget2)
                    .bindWidget(
                        holder as AllCourseWidget2.WidgetHolder,
                        widget as AllCourseWidget2Model
                    )
            }

            WidgetTypes.TYPE_COURSE_FILTER_EXAM -> {
                return (holder.itemView as FilterExamWidget)
                    .bindWidget(
                        holder as FilterExamWidget.WidgetHolder,
                        widget as FilterExamWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_FILTER_SUBJECT -> {
                return (holder.itemView as FilterSubjectWidget)
                    .bindWidget(
                        holder as FilterSubjectWidget.FilterTabsWidgetHolder,
                        widget as FilterSubjectWidget.FilterTabsWidgetModel
                    )
            }

            WidgetTypes.TYPE_FAQ -> {
                return (holder.itemView as FaqWidget)
                    .bindWidget(
                        holder as FaqWidget.FaqWidgetHolder,
                        widget as FaqWidget.FaqWidgetModel
                    )
            }

            WidgetTypes.TYPE_NOTES_LIST -> {
                return (holder.itemView as NotesWidget)
                    .apply {
                        this.adapter = adapter
                    }
                    .bindWidget(
                        holder as NotesWidget.NotesWidgetViewHolder,
                        widget as NotesWidgetModel
                    )
            }

            WidgetTypes.TYPE_PURCHASED_CLASSES -> {
                return (holder.itemView as PurchasedClassesWidget).bindWidget(
                    holder as PurchasedClassesWidget.PurchasedClassesViewHolder,
                    widget as PurchasedClassesWidgetModel
                )
            }

            WidgetTypes.TYPE_PAYMENT_HISTORY -> {
                return (holder.itemView as PaymentHistoryWidget).bindWidget(
                    holder as PaymentHistoryWidget.WidgetHolder,
                    widget as PaymentHistoryWidgetModel
                )
            }

            WidgetTypes.TYPE_COURSE -> {
                return (holder.itemView as CourseWidget).bindWidget(
                    holder as CourseWidget.WidgetHolder,
                    widget as CourseWidget.CourseWidgetModel
                )
            }

            WidgetTypes.TYPE_COURSE_V2 -> {
                return (holder.itemView as CourseWidgetV2).bindWidget(
                    holder as CourseWidgetV2.WidgetHolder,
                    widget as CourseWidgetV2.CourseWidgetModelV2
                )
            }

            WidgetTypes.TYPE_LIVE_CLASS_CATEGORY -> {
                return (holder.itemView as LiveClassCategoryWidget).bindWidget(
                    holder as LiveClassCategoryWidget.CategoryWidgetViewHolder,
                    widget as LiveClassCategoryWidgetModel
                )
            }

            WidgetTypes.TYPE_COURSE_PROGRESS -> {
                return (holder.itemView as CourseProgressWidget).bindWidget(
                    holder as CourseProgressWidget.WidgetHolder,
                    widget as CourseProgressWidgetModel
                )
            }

            WidgetTypes.TYPE_RELATED_LECTURES_WIDGET -> {
                return (holder.itemView as RelatedLecturesWidget).bindWidget(
                    holder as RelatedLecturesWidget.WidgetHolder,
                    widget as RelatedLecturesWidgetModel
                )
            }

            WidgetTypes.TYPE_NOTIFY_LIVE_CLASS_WIDGET -> {
                return (holder.itemView as NotifyClassWidget).bindWidget(
                    holder as NotifyClassWidget.WidgetHolder,
                    widget as NotifyClassWidgetModel
                )
            }

            WidgetTypes.TYPE_RESOURCE_PAGE_UPCOMING_WIDGET -> {
                return (holder.itemView as ResourcePageUpcomingWidget).bindWidget(
                    holder as ResourcePageUpcomingWidget.WidgetHolder,
                    widget as ResourcePageUpcomingWidgetModel
                )
            }

            WidgetTypes.TYPE_COURSE_UPCOMING_LECTURES_WIDGET -> {
                return (holder.itemView as UpcomingLecturesWidget).bindWidget(
                    holder as UpcomingLecturesWidget.WidgetHolder,
                    widget as UpcomingLecturesWidget.UpcomingLectureWidgetModel
                )
            }

            WidgetTypes.TYPE_COURSE_INFO_WIDGET -> {
                return (holder.itemView as CourseInfoWidget).bindWidget(
                    holder as CourseInfoWidget.WidgetHolder,
                    widget as CourseInfoWidget.CourseInfoWidgetModel
                )
            }

            WidgetTypes.TYPE_ALL_TOPICS_WIDGET -> {
                return (holder.itemView as AllTopicsWidget).bindWidget(
                    holder as AllTopicsWidget.WidgetHolder,
                    widget as AllTopicsWidgetModel
                )
            }

            WidgetTypes.TYPE_TOPICS_COVERED_WIDGET -> {
                return (holder.itemView as TopicsCoveredWidget).bindWidget(
                    holder as TopicsCoveredWidget.WidgetHolder,
                    widget as TopicsCoveredWidgetModel
                )
            }

            WidgetTypes.TYPE_LIVE_CLASSES_INFO -> {
                return (holder.itemView as LiveClassInfoWidget).bindWidget(
                    holder as LiveClassInfoWidgetHolder,
                    widget as LiveClassInfoWidgetModel
                )
            }

            WidgetTypes.TYPE_RANKERS_CLASSES -> {
                return (holder.itemView as RankersClassesWidget).bindWidget(
                    holder as RankersClassesWidgetViewHolder,
                    widget as RankersClassesWidgetModel
                )
            }

            WidgetTypes.TYPE_RANKERS -> {
                return (holder.itemView as RankersWidget).bindWidget(
                    holder as RankersWidgetViewHolder,
                    widget as RankersWidgetModel
                )
            }

            WidgetTypes.TYPE_COURSE_CONTENT -> {
                return (holder.itemView as CourseContentWidget)
                    .bindWidget(
                        holder as CourseContentWidget.WidgetHolder,
                        widget as CourseContentWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_EXAM_TABS -> {
                return (holder.itemView as CourseExamTabWidget)
                    .bindWidget(
                        holder as CourseExamTabWidget.WidgetHolder,
                        widget as CourseExamTabWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_TYPE_TABS -> {
                return (holder.itemView as CourseTypeTabWidget)
                    .bindWidget(
                        holder as CourseTypeTabWidget.WidgetHolder,
                        widget as CourseTypeTabWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_CLASS_TABS -> {
                return (holder.itemView as CourseClassTabWidget)
                    .bindWidget(
                        holder as CourseClassTabWidget.WidgetHolder,
                        widget as CourseClassTabWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_PAYMENT_CARD_LIST -> {
                return (holder.itemView as PaymentListWidget)
                    .bindWidget(
                        holder as PaymentListWidget.WidgetHolder,
                        widget as PaymentCardListWidgetModel
                    )
            }

            WidgetTypes.TYPE_PROMO_LIST -> {
                return (holder.itemView as PromoListWidget)
                    .bindWidget(
                        holder as PromoListWidget.WidgetHolder,
                        widget as PromoListWidgetModel
                    )
            }

            WidgetTypes.TYPE_PLAN_TAB -> {
                return (holder.itemView as PlanTypeTabWidget)
                    .bindWidget(
                        holder as PlanTypeTabWidget.WidgetHolder,
                        widget as PlanTypeTabWidgetModel
                    )
            }

            WidgetTypes.TYPE_PLAN_INFO -> {
                return (holder.itemView as PlanInfoWidget)
                    .bindWidget(
                        holder as PlanInfoWidget.WidgetHolder,
                        widget as PlanInfoWidgetModel
                    )
            }

            WidgetTypes.TYPE_PLAN_LIST -> {
                return (holder.itemView as PlanListWidget)
                    .bindWidget(
                        holder as PlanListWidget.WidgetHolder,
                        widget as PlanListWidgetModel
                    )
            }

            WidgetTypes.TYPE_MY_PLAN -> {
                return (holder.itemView as MyPlanWidget)
                    .bindWidget(
                        holder as MyPlanWidget.WidgetHolder,
                        widget as MyPlanWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_TYPE_FILTER -> {
                return (holder.itemView as FilterCourseTypeWidget)
                    .bindWidget(
                        holder as FilterCourseTypeWidget.FilterTabsWidgetHolder,
                        widget as FilterCourseTypeWidget.FilterTabsWidgetModel
                    )
            }

            WidgetTypes.TYPE_LIVE_CLASS_CAROUSEL -> {
                return (holder.itemView as LiveClassCarouselWidget)
                    .bindWidget(
                        holder as LiveClassCarouselWidget.WidgetHolder,
                        widget as LiveClassCarouselWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_COMMON -> {
                return (holder.itemView as CommonCourseWidget)
                    .bindWidget(
                        holder as CommonCourseWidget.WidgetHolder,
                        widget as CommonCourseWidgetModel
                    )
            }

            WidgetTypes.TYPE_SYLLABUS2 -> {
                return (holder.itemView as SyllabusWidgetTwo)
                    .bindWidget(
                        holder as SyllabusWidgetTwo.WidgetHolder,
                        widget as SyllabusWidgetTwoModel
                    )
            }

            WidgetTypes.TYPE_TAB_LIST -> {
                return (holder.itemView as TabListWidget)
                    .bindWidget(
                        holder as TabListWidget.WidgetHolder,
                        widget as TabListWidgetModel
                    )
            }

            WidgetTypes.TYPE_TIME_TABLE -> {
                return (holder.itemView as TimetableWidget)
                    .bindWidget(
                        holder as TimetableWidget.WidgetHolder,
                        widget as TimetableWidgetModel
                    )
            }

            WidgetTypes.TYPE_ICON_HEADER -> {
                return (holder.itemView as IconHeaderWidget)
                    .bindWidget(
                        holder as IconHeaderWidget.WidgetHolder,
                        widget as IconHeaderWidgetModel
                    )
            }

            WidgetTypes.TYPE_PLAN_DESCRIPTION -> {
                return (holder.itemView as PlanDescriptionInfoWidget)
                    .bindWidget(
                        holder as PlanDescriptionInfoWidget.WidgetHolder,
                        widget as PlanDescriptionInfoWidgetModel
                    )
            }

            WidgetTypes.TYPE_PACKAGE_DETAIL -> {
                return (holder.itemView as PackageDetailWidget)
                    .bindWidget(
                        holder as PackageDetailWidget.WidgetHolder,
                        widget as PackageDetailWidgetModel
                    )
            }

            WidgetTypes.TYPE_PACKAGE_DETAIL_V2 -> {
                return (holder.itemView as PackageDetailWidgetV2)
                    .bindWidget(
                        holder as PackageDetailWidgetV2.WidgetHolder,
                        widget as PackageDetailWidgetModelV2
                    )
            }

            WidgetTypes.PURCHASED_COURSE_LIST_WIDGET -> {
                return (holder.itemView as PurchasedCourseListWidget)
                    .bindWidget(
                        holder as PurchasedCourseListWidget.WidgetHolder,
                        widget as CourseListWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_CATEGORY -> {
                return (holder.itemView as CourseCategoryWidget)
                    .bindWidget(
                        holder as CourseCategoryWidget.WidgetHolder,
                        widget as CourseCategoryWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_PARENT -> {
                return (holder.itemView as ParentWidget)
                    .bindWidget(
                        holder as ParentWidget.WidgetHolder,
                        widget as ParentWidget.WidgetChildModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_PARENT_TAB -> {
                return (holder.itemView as ParentTabWidget)
                    .bindWidget(
                        holder as ParentTabWidget.WidgetHolder,
                        widget as ParentTabWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_IPL_SCORE_BOARD -> {
                return (holder.itemView as IplScoreBoardWidget)
                    .bindWidget(
                        holder as IplScoreBoardWidget.WidgetHolder,
                        widget as IplScoreBoardWidget.Model
                    )
            }

            WidgetTypes.TYPE_COURSE_CHILD -> {
                return (holder.itemView as CourseChildWidget)
                    .bindWidget(
                        holder as CourseChildWidget.WidgetHolder,
                        widget as CourseChildWidget.CourseChildWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_CHILD_CAROUSEL -> {
                return (holder.itemView as CourseCarouselChildWidget)
                    .bindWidget(
                        holder as CourseCarouselChildWidget.WidgetHolder,
                        widget as CourseCarouselChildWidget.CourseCarouselChildWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_CHILD_RESOURCE -> {
                return (holder.itemView as CourseResourceChildWidget)
                    .bindWidget(
                        holder as CourseResourceChildWidget.WidgetHolder,
                        widget as CourseResourceChildWidget.CourseResourceChildWidgetModel
                    )
            }

            WidgetTypes.TYPE_FACULTY_GRID2 -> {
                return (holder.itemView as FacultyGridWidget)
                    .bindWidget(
                        holder as FacultyGridWidget.WidgetHolder,
                        widget as FacultyGridWidget.Model
                    )
            }

            WidgetTypes.TYPE_COURSE_SUBJECT -> {
                return (holder.itemView as SubjectWidget)
                    .bindWidget(
                        holder as SubjectWidget.WidgetHolder,
                        widget as SubjectWidget.Model
                    )
            }

            WidgetTypes.TYPE_COURSE_SUBJECT_POST_PURCHASE -> {
                return (holder.itemView as CourseSubjectWidget)
                    .bindWidget(
                        holder as CourseSubjectWidget.WidgetHolder,
                        widget as CourseSubjectWidget.Model
                    )
            }

            WidgetTypes.TYPE_COURSE_RESOURCE_POST_PURCHASE -> {
                return (holder.itemView as CourseResourcesWidget)
                    .bindWidget(
                        holder as CourseResourcesWidget.WidgetHolder,
                        widget as CourseResourcesWidget.Model
                    )
            }

            WidgetTypes.TYPE_LIVE_CLASS_CAROUSEL_CARD -> {
                return (holder.itemView as LiveClassCarouselCardWidget)
                    .bindWidget(
                        holder as LiveClassCarouselCardWidget.WidgetHolder,
                        widget as LiveClassCarouselCardWidget.Model
                    )
            }

            WidgetTypes.TYPE_LIVE_CLASS_CAROUSEL_CARD_2 -> {
                return (holder.itemView as LiveClassCarouselCard2Widget).apply {
                    this.source = source
                }
                    .bindWidget(
                        holder as LiveClassCarouselCard2Widget.WidgetHolder,
                        widget as LiveClassCarouselCard2Widget.Model
                    )
            }

            WidgetTypes.TYPE_SALE_WIDGET -> {
                return (holder.itemView as SaleWidget)
                    .bindWidget(
                        holder as SaleWidget.WidgetHolder,
                        widget as SaleWidgetModel
                    )
            }

            WidgetTypes.TYPE_ACTIVATE_TRIAL_WIDGET -> {
                return (holder.itemView as ActivateTrialWidget)
                    .bindWidget(
                        holder as ActivateTrialWidget.WidgetHolder,
                        widget as TrialWidgetModel
                    )
            }

            WidgetTypes.TYPE_NOTES_FILTER -> {
                return (holder.itemView as NotesFilterWidget)
                    .bindWidget(
                        holder as NotesFilterWidget.WidgetHolder,
                        widget as NotesFilterModel
                    )
            }

            WidgetTypes.TYPE_CATEGORY_PAGE_FILTER -> {
                return (holder.itemView as MultiSelectFilterWidget)
                    .bindWidget(
                        holder as MultiSelectFilterWidget.WidgetHolder,
                        widget as MultiSelectFilterWidgetModel
                    )
            }

            WidgetTypes.TYPE_CATEGORY_PAGE_FILTER_V2 -> {
                return (holder.itemView as MultiSelectFilterWidgetV2)
                    .bindWidget(
                        holder as MultiSelectFilterWidgetV2.WidgetHolder,
                        widget as MultiSelectFilterWidgetV2Model
                    )
            }

            WidgetTypes.TYPE_ASK_DOUBT_CARD -> {
                return (holder.itemView as AskDoubtCardWidget)
                    .bindWidget(
                        holder as AskDoubtCardWidget.WidgetHolder,
                        widget as AskDoubtCardWidget.Model
                    )
            }

            WidgetTypes.TYPE_IMAGE_CARD -> {
                return (holder.itemView as ImageCardWidget)
                    .bindWidget(
                        holder as ImageCardWidget.WidgetHolder,
                        widget as ImageCardWidget.Model
                    )
            }

            WidgetTypes.TYPE_FEED_BANNER -> {
                return (holder.itemView as FeedBannerWidget)
                    .bindWidget(
                        holder as FeedBannerWidget.WidgetHolder,
                        widget as FeedBannerWidget.Model
                    )
            }

            WidgetTypes.TYPE_HOME_WORK -> {
                return (holder.itemView as HomeWorkWidget)
                    .bindWidget(
                        holder as HomeWorkWidget.WidgetHolder,
                        widget as HomeWorkWidgetModel
                    )
            }

            WidgetTypes.TYPE_HOME_WORK_LIST -> {
                return (holder.itemView as HomeWorkListWidget)
                    .bindWidget(
                        holder as HomeWorkListWidget.WidgetHolder,
                        widget as HomeWorkListWidgetModel
                    )
            }

            WidgetTypes.TYPE_HOME_WORK_LIST_V2 -> {
                return (holder.itemView as HomeWorkListWidgetV2)
                    .bindWidget(
                        holder as HomeWorkListWidgetV2.WidgetHolder,
                        widget as HomeWorkListWidgetModelV2
                    )
            }

            WidgetTypes.TYPE_HOME_WORK_HORIZONTAL_LIST -> {
                return (holder.itemView as HomeWorkHorizontalListWidget)
                    .bindWidget(
                        holder as HomeWorkHorizontalListWidget.WidgetHolder,
                        widget as HomeWorkHorizontalListWidgetModel
                    )
            }

            WidgetTypes.TYPE_TOP_OPTION -> {
                return (holder.itemView as TopOptionsWidget)
                    .bindWidget(
                        holder as TopOptionsWidget.WidgetHolder,
                        widget as TopOptionsWidgetModel
                    )
            }

            WidgetTypes.TYPE_INCOMPLETE_CHAPTER -> {
                return (holder.itemView as IncompleteChapterWidget)
                    .bindWidget(
                        holder as IncompleteChapterWidget.WidgetHolder,
                        widget as IncompleteChapterWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_COLLAPSED -> {
                return (holder.itemView as CollapsedWidget)
                    .bindWidget(
                        holder as CollapsedWidget.WidgetHolder,
                        widget as CollapsedWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_PLAYLIST -> {
                return (holder.itemView as PlaylistWidget)
                    .bindWidget(
                        holder as PlaylistWidget.WidgetHolder,
                        widget as PlaylistWidget.PlaylistChildWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_AUTOPLAY -> {
                return (holder.itemView as ParentAutoplayWidget)
                    .bindWidget(
                        holder as ParentAutoplayWidget.WidgetHolder,
                        widget as ParentAutoplayWidget.ParentAutoplayWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_CHILD_AUTOPLAY -> {
                return (holder.itemView as AutoPlayChildWidget)
                    .bindWidget(
                        holder as AutoPlayChildWidget.WidgetHolder,
                        widget as AutoPlayChildWidget.AutoplayChildWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_UPCOMING_LIVE_CLASS -> {
                return (holder.itemView as UpcomingLiveClassWidget)
                    .bindWidget(
                        holder as UpcomingLiveClassWidget.WidgetHolder,
                        widget as UpcomingLiveClassWidget.UpcomingLiveClassWidgetModel
                    )
            }

            WidgetTypes.TYPE_GRADIENT_CARD -> {
                return (holder.itemView as GradientCardWidget)
                    .bindWidget(
                        holder as GradientCardWidget.WidgetHolder,
                        widget as GradientCardWidget.Model
                    )
            }

            WidgetTypes.TYPE_BOOK_PROGRESS -> {
                return (holder.itemView as BookProgressWidget)
                    .bindWidget(
                        holder as BookProgressWidget.WidgetHolder,
                        widget as BookProgressWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_PAID_COURSE -> {
                return (holder.itemView as PaidCourseWidget)
                    .bindWidget(
                        holder as PaidCourseWidget.WidgetHolder,
                        widget as PaidCourseWidget.PaidCourseChildWidgetModel
                    )
            }

            WidgetTypes.TYPE_YOU_WERE_WATCHING -> {
                return (holder.itemView as YouWereWatchingWidget)
                    .bindWidget(
                        holder as YouWereWatchingWidget.WidgetHolder,
                        widget as YouWereWatchingWidget.Model
                    )
            }

            WidgetTypes.TYPE_VIDEO_BANNER_AUTOPLAY_CHILD -> {
                return (holder.itemView as VideoBannerAutoplayChildWidget)
                    .bindWidget(
                        holder as VideoBannerAutoplayChildWidget.WidgetHolder,
                        widget as VideoBannerAutoplayChildWidget.Model
                    )
            }

            WidgetTypes.TYPE_VIDEO_AUTOPLAY_CHILD2 -> {
                return (holder.itemView as VideoAutoplayChildWidget2)
                    .bindWidget(
                        holder as VideoAutoplayChildWidget2.WidgetHolder,
                        widget as VideoAutoplayChildWidget2.Model
                    )
            }

            WidgetTypes.TYPE_FEED_PINNED_VIDEO_AUTOPLAY_CHILD -> {
                return (holder.itemView as FeedPinnedVideoAutoplayChildWidget)
                    .bindWidget(
                        holder as FeedPinnedVideoAutoplayChildWidget.WidgetHolder,
                        widget as FeedPinnedVideoAutoplayChildWidget.Model
                    )
            }

            WidgetTypes.TYPE_IMAGE_CARD_GROUP -> {
                return (holder.itemView as ImageCardGroupWidget)
                    .bindWidget(
                        holder as ImageCardGroupWidget.WidgetHolder,
                        widget as ImageCardGroupWidget.Model
                    )
            }

            WidgetTypes.TYPE_FOLLOW_WIDGET -> {
                return (holder.itemView as FollowWidget)
                    .bindWidget(
                        holder as FollowWidget.WidgetHolder,
                        widget as FollowWidget.Model
                    )
            }

            WidgetTypes.TYPE_COURSE_TESTIMONIAL -> {
                return (holder.itemView as TestimonialWidget)
                    .bindWidget(
                        holder as TestimonialWidget.WidgetHolder,
                        widget as TestimonialWidgetModel
                    )
            }

            WidgetTypes.TYPE_FACULTY_LIST -> {
                return (holder.itemView as FacultyListWidget)
                    .bindWidget(
                        holder as FacultyListWidget.WidgetHolder,
                        widget as FacultyListWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_FAQS -> {
                return (holder.itemView as CourseFaqsWidget)
                    .bindWidget(
                        holder as CourseFaqsWidget.WidgetHolder,
                        widget as CourseFaqsWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_CONTENT_LIST -> {
                return (holder.itemView as CourseContentListWidget)
                    .bindWidget(
                        holder as CourseContentListWidget.WidgetHolder,
                        widget as CourseContentListWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_INFO -> {
                return (holder.itemView as CourseItemInfoWidget)
                    .bindWidget(
                        holder as CourseItemInfoWidget.WidgetHolder,
                        widget as CourseItemInfoWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_FEATURE -> {
                return (holder.itemView as CourseFeatureWidget)
                    .bindWidget(
                        holder as CourseFeatureWidget.WidgetHolder,
                        widget as CourseFeatureWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_EMI_INFO -> {
                return (holder.itemView as CourseEmiInfoWidget)
                    .bindWidget(
                        holder as CourseEmiInfoWidget.WidgetHolder,
                        widget as CourseEmiInfoWidgetModel
                    )
            }

            WidgetTypes.TYPE_TOP_DOUBT_ANSWER_VIDEO -> {
                return (holder.itemView as TopDoubtAnswerVideoWidget)
                    .bindWidget(
                        holder as TopDoubtAnswerVideoWidget.WidgetHolder,
                        widget as TopDoubtAnswerVideoWidgetModel
                    )
            }

            WidgetTypes.TYPE_TOP_DOUBT_ANSWER_AUDIO -> {
                return (holder.itemView as TopDoubtAnswerAudioWidget)
                    .bindWidget(
                        holder as TopDoubtAnswerAudioWidget.WidgetHolder,
                        widget as TopDoubtAnswerAudioWidgetModel
                    )
            }

            WidgetTypes.TYPE_TOP_DOUBT_ANSWER_IMAGE -> {
                return (holder.itemView as TopDoubtAnswerImageWidget)
                    .bindWidget(
                        holder as TopDoubtAnswerImageWidget.WidgetHolder,
                        widget as TopDoubtAnswerImageWidgetModel
                    )
            }

            WidgetTypes.TYPE_PENDING_PAYMENT -> {
                return (holder.itemView as PendingPaymentWidget)
                    .bindWidget(
                        holder as PendingPaymentWidget.WidgetHolder,
                        widget as PendingPaymentWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_COURSE_PARENT -> {
                return (holder.itemView as CourseParentWidget)
                    .bindWidget(
                        holder as CourseParentWidget.WidgetHolder,
                        widget as CourseParentWidget.WidgetChildModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_COURSE_CLASSES -> {
                return (holder.itemView as CourseClassesWidget)
                    .apply {
                        this.adapter = adapter
                    }
                    .bindWidget(
                        holder as CourseClassesWidget.WidgetHolder,
                        widget as CourseClassesWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_COURSE_TEST -> {
                return (holder.itemView as CourseTestWidget)
                    .bindWidget(
                        holder as CourseTestWidget.WidgetHolder,
                        widget as CourseTestWidgetModel
                    )
            }

            WidgetTypes.TYPE_ALL_CLASSES -> {
                return (holder.itemView as AllClassesWidget)
                    .bindWidget(
                        holder as BaseBindingViewHolder<WidgetAllClassesBinding>,
                        widget as AllClassesWidgetModel
                    )
            }

            WidgetTypes.TYPE_INCREASE_VALIDITY -> {
                return (holder.itemView as IncreaseValidityWidget)
                    .bindWidget(
                        holder as IncreaseValidityWidget.WidgetHolder,
                        widget as IncreaseValidityWidgetModel
                    )
            }

            WidgetTypes.TYPE_STORIES -> {
                return (holder.itemView as StoryWidget)
                    .bindWidget(
                        holder as StoryWidget.WidgetHolder,
                        widget as StoryWidgetModel
                    )
            }

            WidgetTypes.TYPE_RESOURCE_V4 -> {
                return (holder.itemView as ResourceV4Widget)
                    .bindWidget(
                        holder as ResourceV4Widget.WidgetHolder,
                        widget as ResourceV4WidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_IMAGE_TEXT -> {
                return (holder.itemView as ImageTextWidget)
                    .bindWidget(
                        holder as ImageTextWidget.WidgetHolder,
                        widget as ImageTextWidgetModel
                    )
            }

            WidgetTypes.TYPE_RESOURCE_NOTES_V3 -> {
                return (holder.itemView as ResourceNotesV3Widget)
                    .bindWidget(
                        holder as ResourceNotesV3Widget.WidgetHolder,
                        widget as ResourceNotesV3WidgetModel
                    )
            }
            WidgetTypes.TYPE_NOTES_FILTER2 -> {
                return (holder.itemView as NotesFilterByWidget)
                    .bindWidget(
                        holder as NotesFilterByWidget.WidgetHolder,
                        widget as NotesFilterByWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_COURSE_AUTOPLAY -> {
                return (holder.itemView as CourseAutoPlayChildWidget)
                    .bindWidget(
                        holder as CourseAutoPlayChildWidget.WidgetHolder,
                        widget as CourseAutoPlayChildWidget.CourseAutoPlayChildWidgetModel
                    )
            }

            WidgetTypes.TYPE_SCHEDULE_V2 -> {
                return (holder.itemView as ScheduleWidget)
                    .bindWidget(
                        holder as ScheduleWidget.WidgetHolder,
                        widget as ScheduleWidget.WidgetChildModel
                    )
            }

            WidgetTypes.TYPE_TRIAL_TIMER -> {
                return (holder.itemView as TrialTimerWidget)
                    .bindWidget(
                        holder as TrialTimerWidget.WidgetHolder,
                        widget as TrialTimerWidget.TrialTimerModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_TEST -> {
                return (holder.itemView as TestWidget)
                    .bindWidget(
                        holder as TestWidget.WidgetHolder,
                        widget as TestWidget.TestWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_VIEW_ALL -> {
                return (holder.itemView as ViewAllWidget)
                    .bindWidget(
                        holder as ViewAllWidget.WidgetHolder,
                        widget as ViewAllWidget.ViewAllWigetModel
                    )
            }

            WidgetTypes.TYPE_SCHEDULE_MONTH_FILTER -> {
                return (holder.itemView as ScheduleMonthFilterWidget)
                    .bindWidget(
                        holder as ScheduleMonthFilterWidget.WidgetHolder,
                        widget as ScheduleMonthFilterWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_COLLAPSE_EXPAND_MATH_VIEW -> {
                return (holder.itemView as CollapseExpandMathViewWidget)
                    .bindWidget(
                        holder as CollapseExpandMathViewWidget.WidgetHolder,
                        widget as CollapseExpandMathViewWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_VIDEO_ACTIONS -> {
                return (holder.itemView as VideoActionWidget)
                    .bindWidget(
                        holder as VideoActionWidget.WidgetHolder,
                        widget as VideoActionWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_PARENT_GRID_SELECTION -> {
                return (holder.itemView as ParentGridSelectionWidget)
                    .bindWidget(
                        holder as ParentGridSelectionWidget.WidgetHolder,
                        widget as ParentGridSelectionWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_CHILD_GRID_SELECTION -> {
                return (holder.itemView as ChildGridSelectionWidget)
                    .bindWidget(
                        holder as ChildGridSelectionWidget.WidgetHolder,
                        widget as ChildGridSelectionWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_NCERT_SIMILAR -> {
                return (holder.itemView as NcertSimilarWidget)
                    .bindWidget(
                        holder as NcertSimilarWidget.WidgetHolder,
                        widget as NcertSimilarWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_NCERT_BOOK -> {
                return (holder.itemView as NcertBookWidget)
                    .bindWidget(
                        holder as NcertBookWidget.WidgetHolder,
                        widget as NcertBookWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_POPULAR_COURSE -> {
                return (holder.itemView as PopularCourseWidget)
                    .bindWidget(
                        holder as PopularCourseWidget.WidgetHolder,
                        widget as PopularCourseWidgetModel
                    )
            }

            WidgetTypes.TYPE_ATTENDANCE -> {
                return (holder.itemView as AttendanceWidget)
                    .bindWidget(
                        holder as AttendanceWidget.WidgetHolder,
                        widget as AttendanceWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_SIMILAR -> {
                return (holder.itemView as SimilarWidget)
                    .bindWidget(
                        holder as SimilarWidget.WidgetHolder,
                        widget as SimilarWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_ASK_DOUBT -> {
                return (holder.itemView as HomePageAskDoubtWidget)
                    .bindWidget(
                        holder as HomePageAskDoubtWidgetHolder,
                        widget as HomePageAskDoubtWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_STUDY_DOST -> {
                return (holder.itemView as StudyDostWidget)
                    .bindWidget(
                        holder as StudyDostWidget.WidgetHolder,
                        widget as StudyDostWidget.Model
                    )
            }

            WidgetTypes.TYPE_TEXT_WIDGET -> {
                return (holder.itemView as TextWidget)
                    .bindWidget(
                        holder as TextWidget.WidgetHolder,
                        widget as TextWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_COUPON_BANNER -> {
                return (holder.itemView as CouponBannerWidget)
                    .bindWidget(
                        holder as CouponBannerWidgetHolder,
                        widget as CouponBannerWidgetModel
                    )
            }

            WidgetTypes.TYPE_PDF_NOTES -> {
                return (holder.itemView as PdfNotesWidget)
                    .bindWidget(
                        holder as PdfNotesWidget.WidgetHolder,
                        widget as PdfNotesWidget.Model
                    )
            }

            WidgetTypes.TYPE_DOUBT_FEED_DAILY_GOAL -> {
                return (holder.itemView as DoubtFeedDailyGoalWidget)
                    .bindWidget(
                        holder as DoubtFeedDailyGoalWidget.WidgetHolder,
                        widget as DoubtFeedDailyGoalWidget.Model
                    )
            }

            WidgetTypes.TYPE_TOPIC_BOOSTER -> {
                return (holder.itemView as TopicBoosterWidget)
                    .bindWidget(
                        holder as TopicBoosterWidget.WidgetHolder,
                        widget as TopicBoosterWidget.Model
                    )
            }

            WidgetTypes.TYPE_FORMULA_SHEET -> {
                return (holder.itemView as FormulaSheetWidget)
                    .bindWidget(
                        holder as FormulaSheetWidget.WidgetHolder,
                        widget as FormulaSheetWidget.Model
                    )
            }

            WidgetTypes.TYPE_TOPIC_VIDEO -> {
                return (holder.itemView as TopicVideoWidget)
                    .bindWidget(
                        holder as TopicVideoWidget.WidgetHolder,
                        widget as TopicVideoWidget.Model
                    )
            }

            WidgetTypes.TYPE_ASKED_QUESTION -> {
                return (holder.itemView as AskedQuestionWidget)
                    .bindWidget(
                        holder as AskedQuestionWidget.WidgetHolder,
                        widget as AskedQuestionWidget.Model
                    )
            }
            WidgetTypes.TYPE_DOUBT_FEED_START_PRACTICE_WIDGET -> {
                return (holder.itemView as DoubtFeedStartPracticeWidget)
                    .bindWidget(
                        holder as DoubtFeedStartPracticeWidget.WidgetHolder,
                        widget as DoubtFeedStartPracticeWidget.Model
                    )
            }

            WidgetTypes.TYPE_TOPIC_BOOSTER_GAME_BANNER -> {
                return (holder.itemView as TopicBoosterGameBannerWidget)
                    .bindWidget(
                        holder as TopicBoosterGameBannerWidget.WidgetHolder,
                        widget as TopicBoosterGameBannerWidget.Model
                    )
            }

            WidgetTypes.TYPE_KHELO_JEETO_BANNER -> {
                return (holder.itemView as KheloJeetoBannerWidget)
                    .bindWidget(
                        holder as KheloJeetoBannerWidget.WidgetHolder,
                        widget as KheloJeetoBannerWidget.Model
                    )
            }

            WidgetTypes.TYPE_MY_COURSE -> {
                return (holder.itemView as MyCourseWidget)
                    .bindWidget(
                        holder as MyCourseWidget.WidgetHolder,
                        widget as MyCourseWidgetModel
                    )
            }

            WidgetTypes.TYPE_PRE_PURCHASE_CALL_CARD -> {
                return (holder.itemView as PrePurchaseCallingCard)
                    .bindWidget(
                        holder as PrePurchaseCallingCard.WidgetHolder,
                        widget as PrePurchaseCallingCardModel
                    )
            }

            WidgetTypes.TYPE_PRE_PURCHASE_CALL_CARD_V2 -> {
                return (holder.itemView as PrePurchaseCallingCard2)
                    .bindWidget(
                        holder as PrePurchaseCallingCard2.WidgetHolder,
                        widget as PrePurchaseCallingCardModel2
                    )
            }

            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_PARENT -> {
                return (holder.itemView as StudyGroupParentWidget)
                    .bindWidget(
                        holder as StudyGroupParentWidget.WidgetHolder,
                        widget as StudyGroupParentWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_REPORT_PARENT -> {
                return (holder.itemView as StudyGroupReportParentWidget)
                    .bindWidget(
                        holder as StudyGroupReportParentWidget.WidgetHolder,
                        widget as StudyGroupReportParentWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_LIVE_CLASS -> {
                return (holder.itemView as StudyGroupLiveClassWidget)
                    .bindWidget(
                        holder as StudyGroupLiveClassWidget.WidgetHolder,
                        widget as StudyGroupLiveClassWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_AUDIO_PLAYER -> {
                return (holder.itemView as AudioPlayerWidget)
                    .bindWidget(
                        holder as AudioPlayerWidget.WidgetHolder,
                        widget as AudioPlayerWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_GUIDELINE -> {
                return (holder.itemView as StudyGroupGuidelineWidget)
                    .bindWidget(
                        holder as StudyGroupGuidelineWidget.WidgetHolder,
                        widget as StudyGroupGuidelineWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_INVITATION -> {
                return (holder.itemView as StudyGroupInvitationWidget)
                    .bindWidget(
                        holder as StudyGroupInvitationWidget.WidgetHolder,
                        widget as StudyGroupInvitationWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_JOINED_INFO -> {
                return (holder.itemView as StudyGroupJoinedInfoWidget)
                    .bindWidget(
                        holder as StudyGroupJoinedInfoWidget.WidgetHolder,
                        widget as StudyGroupJoinedInfoWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_STUDY_GROUP_FEATURE_UNAVAILABLE -> {
                return (holder.itemView as StudyGroupFeatureUnavailableWidget)
                    .bindWidget(
                        holder as StudyGroupFeatureUnavailableWidget.WidgetHolder,
                        widget as StudyGroupFeatureUnavailableWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_PDF_VIEW -> {
                return (holder.itemView as PdfViewWidget)
                    .bindWidget(
                        holder as PdfViewWidget.WidgetHolder,
                        widget as PdfViewWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_MY_SACHET -> {
                return (holder.itemView as MySachetWidget)
                    .bindWidget(
                        holder as MySachetWidget.MySachetWidgetViewHolder,
                        widget as MySachetWidget.SachetWidgetModel
                    )
            }

            WidgetTypes.TYPE_DOUBT_FEED_BANNER -> {
                return (holder.itemView as DoubtFeedBannerWidget)
                    .bindWidget(
                        holder as DoubtFeedBannerWidget.WidgetHolder,
                        widget as DoubtFeedBannerWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_NUDGE -> {
                return (holder.itemView as NudgeWidget)
                    .bindWidget(
                        holder as NudgeWidget.NudgeWidgetHolder,
                        widget as NudgeWidget.NudgeWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_NUDGE_POPUP -> {
                return (holder.itemView as NudgePopupWidget)
                    .bindWidget(
                        holder as NudgePopupWidget.NudgePopupWidgetHolder,
                        widget as NudgePopupWidget.NudgePopupWidgetModel
                    )
            }
            WidgetTypes.TYPE_MEDIUM_SWITCH_WIDGET -> {
                return (holder.itemView as MediumSwitchWidget)
                    .bindWidget(
                        holder as MediumSwitchWidget.WidgetHolder,
                        widget as MediumSwitchWidgetModel
                    )
            }
            WidgetTypes.TYPE_EXAM_CORNER_DEFAULT_WIDGET -> {
                return (holder.itemView as ExamCornerDefaultWidget)
                    .bindWidget(
                        holder as ExamCornerDefaultWidget.WidgetHolder,
                        widget as ExamCornerDefaultWidgetModel
                    )
            }
            WidgetTypes.TYPE_EXAM_CORNER_POPULAR_WIDGET -> {
                return (holder.itemView as ExamCornerPopularWidget)
                    .bindWidget(
                        holder as ExamCornerPopularWidget.WidgetHolder,
                        widget as ExamCornerPopularWidgetModel
                    )
            }
            WidgetTypes.TYPE_EXAM_CORNER_AUTOPLAY_WIDGET -> {
                return (holder.itemView as ExamCornerAutoplayWidget)
                    .bindWidget(
                        holder as ExamCornerAutoplayWidget.WidgetHolder,
                        widget as ExamCornerAutoplayWidgetModel
                    )
            }
            WidgetTypes.TYPE_CALLING_NOTICE_WIDGET -> {
                return (holder.itemView as CallingNoticeWidget)
                    .bindWidget(
                        holder as CallingNoticeWidget.WidgetHolder,
                        widget as CallingNoticeWidgetModel
                    )
            }

            WidgetTypes.TYPE_SHORTS_VIDEOS_EXHAUSTED_WIDGET -> {
                return (holder.itemView as ShortsVideosExhaustedWidget)
                    .bindWidget(
                        holder as ShortsVideosExhaustedWidget.WidgetHolder,
                        widget as ShortsVideosExhaustedWidgetModel
                    )
            }
            WidgetTypes.TYPE_WIDGET_DOUBT_P2P_HOME -> {
                return (holder.itemView as DoubtP2PHomeWidget)
                    .bindWidget(
                        holder as DoubtP2PHomeWidget.WidgetHolder,
                        widget as DoubtP2PHomeWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_DOUBT_P2P -> {
                return (holder.itemView as DoubtP2PWidget)
                    .bindWidget(
                        holder as DoubtP2PWidget.WidgetHolder,
                        widget as DoubtP2PWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_DOUBT_P2P_ANIMATION -> {
                return (holder.itemView as DoubtP2PAnimationWidget)
                    .bindWidget(
                        holder as DoubtP2PAnimationWidget.WidgetHolder,
                        widget as DoubtP2PAnimationWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_CLASS_BOARD_EXAM -> {
                return (holder.itemView as ClassBoardExamWidget)
                    .bindWidget(
                        holder as ClassBoardExamWidget.WidgetHolder,
                        widget as ClassBoardExamWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_LEADERBOARD_PERSONAL -> {
                return (holder.itemView as LeaderboardPersonalWidget)
                    .bindWidget(
                        holder as LeaderboardPersonalWidget.WidgetHolder,
                        widget as LeaderboardPersonalModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_LEADERBOARD_TAB -> {
                return (holder.itemView as LeaderboardTabWidget)
                    .bindWidget(
                        holder as LeaderboardTabWidget.WidgetHolder,
                        widget as LeaderboardTabModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_LEADERBOARD_TOP_THREE -> {
                return (holder.itemView as LeaderboardTopThreeWidget)
                    .bindWidget(
                        holder as LeaderboardTopThreeWidget.WidgetHolder,
                        widget as LeaderboardTopThreeWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_LEADERBOARD -> {
                return (holder.itemView as LeaderBoardWidget)
                    .bindWidget(
                        holder as LeaderBoardWidget.WidgetHolder,
                        widget as LeaderBoardWidgetModel
                    )
            }

            WidgetTypes.TYPE_PACKAGE_DETAIL_V3 -> {
                return (holder.itemView as PackageDetailWidgetV3)
                    .bindWidget(
                        holder as PackageDetailWidgetV3.WidgetHolder,
                        widget as PackageDetailWidgetModelV3
                    )
            }

            WidgetTypes.TYPE_WIDGET_PARENT_COURSE_RECOMMENDATION_INCOMING -> {
                return (holder.itemView as CourseRecommendationParentWidget)
                    .bindWidget(
                        holder as CourseRecommendationParentWidget.WidgetHolder,
                        widget as CourseRecommendationParentWidget.WidgetChildModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_COURSE_RECOMMENDATION_MESSAGE -> {
                return (holder.itemView as CourseRecommendationMessageWidget)
                    .bindWidget(
                        holder as CourseRecommendationMessageWidget.WidgetHolder,
                        widget as CourseRecommendationMessageWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_COURSE_RECOMMENDATION_RADIO_BUTTON -> {
                return (holder.itemView as CourseRecommendationRadioButtonWidget)
                    .bindWidget(
                        holder as CourseRecommendationRadioButtonWidget.WidgetHolder,
                        widget as CourseRecommendationRadioButtonWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_COURSE_RECOMMENDATION_SUBMITTED_ANSWER -> {
                return (holder.itemView as CourseRecommendationSubmittedAnswerWidget)
                    .bindWidget(
                        holder as CourseRecommendationSubmittedAnswerWidget.WidgetHolder,
                        widget as CourseRecommendationSubmittedAnswerWidget.Model
                    )
            }

            WidgetTypes.TYPE_AUTO_SCROLL_CARD_WIDGET -> {
                return (holder.itemView as AutoScrollImageWidget)
                    .bindWidget(
                        holder as AutoScrollImageWidget.WidgetHolder,
                        widget as AutoScrollImageWidget.Model
                    )
            }

            WidgetTypes.TYPE_VIDEO_CARD -> {
                return (holder.itemView as StudyGroupVideoCardWidget)
                    .bindWidget(
                        holder as StudyGroupVideoCardWidget.WidgetHolder,
                        widget as StudyGroupVideoCardWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_SAMPLE_QUESTION -> {
                return (holder.itemView as SampleQuestionWidget)
                    .bindWidget(
                        holder as SampleQuestionWidget.WidgetHolder,
                        widget as SampleQuestionWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_COURSE_EXPLORE -> {
                return (holder.itemView as CourseExploreWidget)
                    .bindWidget(
                        holder as CourseExploreWidget.WidgetHolder,
                        widget as CourseExploreWidget.Model
                    )
            }

            WidgetTypes.TYPE_DOUBT_FEED -> {
                return (holder.itemView as DoubtFeedWidget)
                    .bindWidget(
                        holder as DoubtFeedWidget.WidgetHolder,
                        widget as DoubtFeedWidget.Model,
                    )
            }
            WidgetTypes.TYPE_WIDGET_COURSE_INFO_WIDGET_2 -> {
                return (holder.itemView as CourseInfoWidget2)
                    .bindWidget(
                        holder as CourseInfoWidget2.WidgetHolder,
                        widget as CourseInfoWidget2Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_COURSE_DETAILS -> {
                return (holder.itemView as CourseDetailsWidget)
                    .bindWidget(
                        holder as CourseDetailsWidget.WidgetHolder,
                        widget as CourseDetailsWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_TRIAL_BUTTON -> {
                return (holder.itemView as TrialButtonWidget)
                    .bindWidget(
                        holder as TrialButtonWidget.WidgetHolder,
                        widget as TrialButtonWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_TIME_TABLE -> {
                return (holder.itemView as CourseTimeTableWidget)
                    .bindWidget(
                        holder as CourseTimeTableWidget.WidgetHolder,
                        widget as CourseTimetableWidgetModel
                    )
            }

            WidgetTypes.TYPE_COURSE_PLAN -> {
                return (holder.itemView as PlanWidget)
                    .bindWidget(
                        holder as PlanWidget.WidgetHolder,
                        widget as PlanWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_COURSE_TEST_V2 -> {
                return (holder.itemView as CourseTestWidgetV2).bindWidget(
                    holder as CourseTestWidgetV2.WidgetHolder,
                    widget as CourseTestWidgetModelV2
                )
            }

            WidgetTypes.TYPE_NOTES_LIST_V2 -> {
                return (holder.itemView as NotesWidgetV2).bindWidget(
                    holder as NotesWidgetV2.WidgetViewHolder,
                    widget as NotesWidgetModel
                )
            }

            WidgetTypes.TYPE_PACKAGE_DETAIL_V4 -> {
                return (holder.itemView as PackageDetailWidgetV4).bindWidget(
                    holder as PackageDetailWidgetV4.WidgetHolder,
                    widget as PackageDetailWidgetModelV4
                )
            }

            WidgetTypes.TYPE_WIDGET_BUY_COMPLETE_COURSE -> {
                return (holder.itemView as BuyCompleteCourseWidget)
                    .bindWidget(
                        holder as BuyCompleteCourseWidget.WidgetHolder,
                        widget as BuyCompleteCourseWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_VIEW_PLAN_BUTTON -> {
                return (holder.itemView as WidgetViewPlanButton)
                    .bindWidget(
                        holder as WidgetViewPlanButton.WidgetHolder,
                        widget as WidgetViewPlanButtonModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_BUTTON_BORDER -> {
                return (holder.itemView as ButtonBorderWidget)
                    .bindWidget(
                        holder as ButtonBorderWidget.WidgetHolder,
                        widget as ButtonBorderWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_SELECT_MEDIUM -> {
                return (holder.itemView as SelectMediumWidget)
                    .bindWidget(
                        holder as SelectMediumWidget.WidgetHolder,
                        widget as SelectMediumWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_SYLLABUS -> {
                return (holder.itemView as SyllabusWidget)
                    .bindWidget(
                        holder as SyllabusWidget.WidgetHolder,
                        widget as SyllabusWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_CONTENT_FILTER -> {
                return (holder.itemView as ContentFilterWidget)
                    .bindWidget(
                        holder as ContentFilterWidget.WidgetHolder,
                        widget as ContentFilterWidget.Model
                    )
            }

            WidgetTypes.TYPE_COURSE_INFO_V2 -> {
                return (holder.itemView as CourseInfoWidgetV2).bindWidget(
                    holder as CourseInfoWidgetV2.WidgetHolder,
                    widget as CourseInfoWidgetV2Model
                )
            }

            WidgetTypes.TYPE_TESTIMONIAL_V2 -> {
                return ((holder.itemView as TestimonialWidgetV2).bindWidget(
                    holder as TestimonialWidgetV2.WidgetViewHolder,
                    widget as TestimonialWidgetModelV2
                ))
            }

            WidgetTypes.TYPE_COURSE_V4 -> {
                return ((holder.itemView as CourseWidgetV4).bindWidget(
                    holder as CourseWidgetV4.WidgetViewHolder,
                    widget as CourseWidgetV4.CourseWidgetModelV4
                ))
            }

            WidgetTypes.TYPE_COURSE_V3 -> {
                return ((holder.itemView as CourseWidgetV3).bindWidget(
                    holder as CourseWidgetV3.WidgetViewHolder,
                    widget as CourseWidgetV3.CourseWidgetModelV3
                ))
            }

            WidgetTypes.TYPE_COUPON_LIST -> {
                return ((holder.itemView as CouponListWidget).bindWidget(
                    holder as CouponListWidget.WidgetViewHolder,
                    widget as CouponListWidget.CouponListWidgetModel
                ))
            }

            WidgetTypes.TYPE_COURSE_RESOURCE_V2 -> {
                return ((holder.itemView as CourseResourceWidget).bindWidget(
                    holder as CourseResourceWidget.WidgetViewHolder,
                    widget as CourseResourceWidget.CourseResourceWidgetModel
                ))
            }

            WidgetTypes.TYPE_WIDGET_TOP_SELLING_SUBJECT -> {
                return (holder.itemView as TopSellingSubjectWidget).bindWidget(
                    holder as TopSellingSubjectWidget.WidgetHolder,
                    widget as TopSellingSubjectWidgetModel
                )
            }

            WidgetTypes.TYPE_WIDGET_CALLING_BIG_CARD -> {
                return (holder.itemView as CallingBigCardWidget).bindWidget(
                    holder as CallingBigCardWidget.WidgetHolder,
                    widget as CallingBigCardWidgetModel
                )
            }

            WidgetTypes.TYPE_WIDGET_COURSE_RECOMMENDATION -> {
                return (holder.itemView as CourseRecommendationWidget)
                    .bindWidget(
                        holder as CourseRecommendationWidget.WidgetHolder,
                        widget as CourseRecommendationWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_CATEGORY -> {
                return (holder.itemView as CategoryWidget)
                    .bindWidget(
                        holder as CategoryWidget.WidgetHolder,
                        widget as CategoryWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_RECOMMENDED_TEST -> {
                return (holder.itemView as RecommendedTestWidget)
                    .bindWidget(
                        holder as RecommendedTestWidget.WidgetHolder,
                        widget as RecommendedTestWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_COURSE_INFO_V3 -> {
                return (holder.itemView as CourseInfoWidgetV3)
                    .bindWidget(
                        holder as CourseInfoWidgetV3.WidgetHolder,
                        widget as CourseInfoWidgetV3.Model
                    )
            }

            WidgetTypes.TYPE_EXPLORE_PROMO_WIDGET -> {
                return (holder.itemView as ExplorePromoWidget)
                    .bindWidget(
                        holder as ExplorePromoWidget.WidgetViewHolder,
                        widget as ExplorePromoWidget.Model
                    )
            }

            WidgetTypes.TYPE_VIDEO_WIDGET -> {
                return (holder.itemView as VideoWidget)
                    .bindWidget(
                        holder as VideoWidget.WidgetHolder,
                        widget as VideoWidget.VideoWidgetModel
                    )
            }

            WidgetTypes.TYPE_RECOMMENDATION_WIDGET -> {
                return (holder.itemView as RecommendationWidget)
                    .bindWidget(
                        holder as RecommendationWidget.WidgetHolder,
                        widget as RecommendationWidget.RecommendationWidgetModel
                    )
            }
            WidgetTypes.TYPE_WIDGET_VPA -> {
                return (holder.itemView as VpaWidget)
                    .bindWidget(
                        holder as VpaWidget.WidgetHolder,
                        widget as VpaWidget.Model
                    )
            }

            WidgetTypes.TYPE_TEST_ANALYSIS -> {
                return (holder.itemView as TestAnalysisWidget)
                    .bindWidget(
                        holder as TestAnalysisWidget.WidgetViewHolder,
                        widget as TestAnalysisWidget.TestAnalysisWidgetModel
                    )
            }

            WidgetTypes.TYPE_TEST_RESULT -> {
                return (holder.itemView as TestResultWidget)
                    .bindWidget(
                        holder as TestResultWidget.WidgetViewHolder,
                        widget as TestResultWidget.TestResultWidgetModel
                    )
            }

            WidgetTypes.TYPE_REVISION_CORNER_BANNER -> {
                return (holder.itemView as RevisionCornerBannerWidget)
                    .bindWidget(
                        holder as RevisionCornerBannerWidget.WidgetHolder,
                        widget as RevisionCornerBannerWidget.Model
                    )
            }
            WidgetTypes.TYPE_ICON_CTA -> {
                return (holder.itemView as IconCtaWidget)
                    .bindWidget(
                        holder as IconCtaWidget.WidgetHolder,
                        widget as IconCtaWidget.Model
                    )
            }

            WidgetTypes.TYPE_LIVE_QUESTIONS_DAILY_PRACTICE_WIDGET -> {
                return (holder.itemView as LiveQuestionsDailyPracticeWidget).bindWidget(
                    holder as LiveQuestionsDailyPracticeWidget.WidgetHolder,
                    widget as LiveQuestionDailyPracticeWidgetModel
                )
            }

            WidgetTypes.TYPE_LIVE_QUESTIONS_DAILY_PRACTICE_RANK_WIDGET -> {
                return (holder.itemView as LiveQuestionsDailyPracticeRankWidget).bindWidget(
                    holder as LiveQuestionsDailyPracticeRankWidget.WidgetHolder,
                    widget as LiveQuestionDailyPracticeRankWidgetModel
                )
            }

            WidgetTypes.TYPE_LIVE_QUESTIONS_DAILY_PRACTICE_FAQ_WIDGET -> {
                return (holder.itemView as LiveQuestionsDailyPracticeFAQWidget).bindWidget(
                    holder as LiveQuestionsDailyPracticeFAQWidget.WidgetHolder,
                    widget as LiveQuestionDailyPracticeFAQWidgetModel
                )
            }

            WidgetTypes.TYPE_WIDGET_DAILY_PRACTICE -> {
                return (holder.itemView as DailyPracticeWidget).bindWidget(
                    holder as DailyPracticeWidget.WidgetHolder,
                    widget as DailyPracticeWidgetModel
                )
            }

            WidgetTypes.MY_REWARDS_POINTS_WIDGET -> {
                return (holder.itemView as MyRewardsPointsWidget).bindWidget(
                    holder as MyRewardsPointsWidget.WidgetHolder,
                    widget as MyRewardsPointsWidgetModel
                )
            }

            WidgetTypes.MY_REWARDS_SCRATCH_CARD_WIDGET -> {
                return (holder.itemView as MyRewardsScratchCardWidget).bindWidget(
                    holder as MyRewardsScratchCardWidget.WidgetHolder,
                    widget as MyRewardsScratchCardWidgetModel
                )
            }

            WidgetTypes.TYPE_WIDGET_SG_GROUP_CHAT -> {
                return (holder.itemView as SgGroupChatWidget)
                    .bindWidget(
                        holder as SgGroupChatWidget.WidgetHolder,
                        widget as SgGroupChatWidget.Model
                    )
            }

            WidgetTypes.WIDGET_JOIN_NEW_STUDYGROUP -> {
                return (holder.itemView as SgJoinNewGroupWidget)
                    .bindWidget(
                        holder as SgJoinNewGroupWidget.WidgetHolder,
                        widget as SgJoinNewGroupWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_SG_INDIVIDUAL_CHAT -> {
                return (holder.itemView as SgPersonalChatWidget)
                    .bindWidget(
                        holder as SgPersonalChatWidget.WidgetHolder,
                        widget as SgPersonalChatWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_SG_REQUEST -> {
                return (holder.itemView as SgRequestWidget)
                    .bindWidget(
                        holder as SgRequestWidget.WidgetHolder,
                        widget as SgRequestWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_SG_BLOCKED_MEMBER -> {
                return (holder.itemView as SgBlockedMemberWidget)
                    .bindWidget(
                        holder as SgBlockedMemberWidget.WidgetHolder,
                        widget as SgBlockedMemberWidget.Model
                    )
            }
            WidgetTypes.TYPE_BULLET_LIST_WIDGET -> {
                return (holder.itemView as BulletListWidget)
                    .bindWidget(
                        holder as BulletListWidget.WidgetHolder,
                        widget as BulletListWidgetModel
                    )
            }
            WidgetTypes.TYPE_PADHO_AUR_JEETO_WIDGET -> {
                return (holder.itemView as PadhoAurJeetoWidget)
                    .bindWidget(
                        holder as PadhoAurJeetoWidget.WidgetHolder,
                        widget as PadhoAurJeetoWidgetModel
                    )
            }
            WidgetTypes.TYPE_LEADER_BOARD_PROGRESS -> {
                return (holder.itemView as LeaderboardProgressWidget)
                    .bindWidget(
                        holder as LeaderboardProgressWidget.WidgetHolder,
                        widget as LeaderboardProgressWidgetModel
                    )
            }

            WidgetTypes.TYPE_TFS_ANALYSIS -> {
                return (holder.itemView as TfsAnalysisWidget)
                    .bindWidget(
                        holder as TfsAnalysisWidget.WidgetViewHolder,
                        widget as TfsAnalysisWidget.TfsAnalysisWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_PARENT_TAB_2 -> {
                return (holder.itemView as ParentTabWidget2)
                    .bindWidget(
                        holder as ParentTabWidget2.WidgetHolder,
                        widget as ParentTabWidget2.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_PARENT_TAB_3 -> {
                return (holder.itemView as ParentTabWidget3)
                    .bindWidget(
                        holder as ParentTabWidget3.WidgetHolder,
                        widget as ParentTabWidget3.Model
                    )
            }

            WidgetTypes.TYPE_AWARDED_STUDENTS_LIST -> {
                return (holder.itemView as AwardedStudentsWidget).bindWidget(
                    holder as AwardedStudentsWidget.WidgetHolder,
                    widget as AwardedStudentsWidgetModel
                )
            }
            WidgetTypes.TYPE_REFERRAL -> {
                return (holder.itemView as ReferralWidget).bindWidget(
                    holder as ReferralWidget.WidgetHolder,
                    widget as ReferralWidgetModel
                )
            }
            WidgetTypes.TYPE_PREVIOUS_TEST_RESULTS -> {
                return (holder.itemView as PreviousTestResultsWidget).bindWidget(
                    holder as PreviousTestResultsWidget.WidgetHolder,
                    widget as PreviousTestResultsWidgetModel
                )
            }
            WidgetTypes.TYPE_PRACTICE_TEST -> {
                return (holder.itemView as PracticeTestWidget).bindWidget(
                    holder as PracticeTestWidget.WidgetHolder,
                    widget as PracticeTestWidgetModel
                )
            }
            WidgetTypes.TYPE_SCHOLARSHIP_TABS -> {
                return (holder.itemView as ScholarshipTabsWidget).bindWidget(
                    holder as ScholarshipTabsWidget.WidgetHolder,
                    widget as ScholarshipTabsWidgetModel
                )
            }
            WidgetTypes.TYPE_REPORT_CARD -> {
                return (holder.itemView as ReportCardWidget).bindWidget(
                    holder as ReportCardWidget.WidgetHolder,
                    widget as ReportCardWidgetModel
                )
            }
            WidgetTypes.TYPE_REGISTER_TEST -> {
                return (holder.itemView as RegisterTestWidget).bindWidget(
                    holder as RegisterTestWidget.WidgetHolder,
                    widget as RegisterTestWidgetModel
                )
            }
            WidgetTypes.TYPE_PROGRESS -> {
                return (holder.itemView as WidgetProgress).bindWidget(
                    holder as WidgetProgress.WidgetHolder,
                    widget as ProgressWidgetModel
                )
            }
            WidgetTypes.TYPE_SCHOLARSHIP_PROGRESS_CARD -> {
                return (holder.itemView as ScholarshipProgressCardWidget).bindWidget(
                    holder as ScholarshipProgressCardWidget.WidgetHolder,
                    widget as ScholarshipProgressCardWidgetModel
                )
            }

            WidgetTypes.TYPE_EXPLORE_COURSE_V2_SQUARE -> {
                return (holder.itemView as ExploreCourseV2WidgetSquare).bindWidget(
                    holder as ExploreCourseV2WidgetSquare.WidgetHolder,
                    widget as ExploreCourseV2WidgetSquareModel
                )
            }
            WidgetTypes.TYPE_EXPLORE_COURSE_V2_CIRCLE -> {
                return (holder.itemView as ExploreCourseV2WidgetCircle).bindWidget(
                    holder as ExploreCourseV2WidgetCircle.WidgetHolder,
                    widget as ExploreCourseV2WidgetCircleModel
                )
            }
            WidgetTypes.TYPE_LATEST_LAUNCHES -> {
                return (holder.itemView as LatestLaunchesWidget).bindWidget(
                    holder as LatestLaunchesWidget.WidgetViewHolder,
                    widget as LatestLaunchesWidget.LatestLaunchesWidgetModel
                )
            }
            WidgetTypes.TYPE_WIDGET_SG_HOME -> {
                return (holder.itemView as SgHomeWidget)
                    .bindWidget(
                        holder as SgHomeWidget.WidgetHolder,
                        widget as SgHomeWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_DNR_EARNED_HISTORY -> {
                return (holder.itemView as DnrEarnedHistoryWidget)
                    .bindWidget(
                        holder as DnrEarnedHistoryWidget.WidgetHolder,
                        widget as DnrEarnedHistoryWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_DNR_EARNED_HISTORY_ITEM -> {
                return (holder.itemView as DnrEarnedHistoryItemWidget)
                    .bindWidget(
                        holder as DnrEarnedHistoryItemWidget.WidgetHolder,
                        widget as DnrEarnedHistoryItemWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_DNR_EARNED_SUMMARY -> {
                return (holder.itemView as DnrEarnedSummaryWidget)
                    .bindWidget(
                        holder as DnrEarnedSummaryWidget.WidgetHolder,
                        widget as DnrEarnedSummaryWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_DNR_UNLOCKED_VOUCHER -> {
                return (holder.itemView as DnrUnlockedVoucherWidget)
                    .bindWidget(
                        holder as DnrUnlockedVoucherWidget.WidgetHolder,
                        widget as DnrUnlockedVoucherWidgetModel
                    )
            }
            WidgetTypes.TYPE_WIDGET_DNR_STREAK -> {
                return (holder.itemView as DnrStreakWidget)
                    .bindWidget(
                        holder as DnrStreakWidget.WidgetHolder,
                        widget as DnrStreakWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_DNR_TEXT -> {
                return (holder.itemView as DnrTextWidget)
                    .bindWidget(
                        holder as DnrTextWidget.WidgetHolder,
                        widget as DnrTextWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_DNR_NO_EARNED_HISTORY -> {
                return (holder.itemView as DnrNoEarnedHistoryWidget)
                    .bindWidget(
                        holder as DnrNoEarnedHistoryWidget.WidgetHolder,
                        widget as DnrNoEarnedHistoryWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_DNR_TOTAL_REWARD -> {
                return (holder.itemView as DnrTotalRewardWidget)
                    .bindWidget(
                        holder as DnrTotalRewardWidget.WidgetHolder,
                        widget as DnrTotalRewardWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_DNR_EARNING_DETAILS -> {
                return (holder.itemView as DnrEarningsDetailWidget)
                    .bindWidget(
                        holder as DnrEarningsDetailWidget.WidgetHolder,
                        widget as DnrEarningsDetailWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_DNR_REWARD_DETAIL -> {
                return (holder.itemView as DnrRewardDetailWidget)
                    .bindWidget(
                        holder as DnrRewardDetailWidget.WidgetHolder,
                        widget as DnrRewardDetailWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_DNR_REDEEM_VOUCHER -> {
                return (holder.itemView as DnrRedeemVoucherWidget)
                    .bindWidget(
                        holder as DnrRedeemVoucherWidget.WidgetHolder,
                        widget as DnrRedeemVoucherWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_DNR_TODAY_REWARD -> {
                return (holder.itemView as DnrTodayRewardWidget)
                    .bindWidget(
                        holder as DnrTodayRewardWidget.WidgetHolder,
                        widget as DnrTodayRewardWidget.DnrTodayRewardWidgetModel
                    )
            }
            WidgetTypes.TYPE_WIDGET_DNR_REWARD_HISTORY -> {
                return (holder.itemView as DnrRewardHistoryWidget)
                    .bindWidget(
                        holder as DnrRewardHistoryWidget.WidgetHolder,
                        widget as DnrRewardHistoryWidget.DnrRewardHistoryWidgetModel
                    )
            }
            WidgetTypes.TYPE_WIDGET_DNR_HOME -> {
                return (holder.itemView as DnrHomeWidget)
                    .bindWidget(
                        holder as DnrHomeWidget.WidgetHolder,
                        widget as DnrHomeWidget.DnrHomeWidgetModel
                    )
            }

            // teacher
            WidgetTypes.TYPE_TEACHER_HEADER -> {
                return (holder.itemView as TeacherHeaderWidget)
                    .bindWidget(
                        holder as TeacherHeaderWidget.WidgetHolder,
                        widget as TeacherHeaderWidget.Model
                    )
            }
            WidgetTypes.TYPE_ANNOUNCEMENT_WIDGET -> {
                return (holder.itemView as ChannelAnnouncementWidget)
                    .bindWidget(
                        holder as ChannelAnnouncementWidget.WidgetHolder,
                        widget as ChannelAnnouncementWidget.Model
                    )
            }
            WidgetTypes.TYPE_CHANNEL_FILTER_TAB -> {
                return (holder.itemView as ChannelTabFilterWidget)
                    .bindWidget(
                        holder as ChannelTabFilterWidget.WidgetHolder,
                        widget as ChannelTabFilterWidget.Model
                    )
            }
            WidgetTypes.TYPE_CHANNEL_FILTER -> {
                return (holder.itemView as ChannelSubTabFilterWidget)
                    .bindWidget(
                        holder as ChannelSubTabFilterWidget.WidgetHolder,
                        widget as ChannelSubTabFilterWidget.Model
                    )
            }
            WidgetTypes.TYPE_CHANNEL_FILTER_CONTENT -> {
                return (holder.itemView as ChannelContentFilterWidget)
                    .bindWidget(
                        holder as ChannelContentFilterWidget.WidgetHolder,
                        widget as ChannelContentFilterWidget.Model
                    )
            }
            WidgetTypes.TYPE_VIDEO_TYPE -> {
                return (holder.itemView as ChannelVideoContentWidget)
                    .bindWidget(
                        holder as ChannelVideoContentWidget.WidgetHolder,
                        widget as ChannelVideoContentWidget.Model
                    )
            }
            WidgetTypes.TYPE_PDF_TYPE -> {
                return (holder.itemView as ChannelPDFContentWidget)
                    .bindWidget(
                        holder as ChannelPDFContentWidget.WidgetHolder,
                        widget as ChannelPDFContentWidget.Model
                    )
            }

            WidgetTypes.TYPE_SUBSCRIBED_TEACHERS -> {
                return (holder.itemView as SubscribedTeacherChannelWidget)
                    .bindWidget(
                        holder as SubscribedTeacherChannelWidget.WidgetHolder,
                        widget as SubscribedTeacherChannelWidget.Model
                    )
            }
            WidgetTypes.TYPE_TEACHERS_LIST -> {
                return (holder.itemView as TeacherChannelWidget)
                    .bindWidget(
                        holder as TeacherChannelWidget.WidgetHolder,
                        widget as TeacherChannelWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_TEACHER_CHANNEL_2 -> {
                return (holder.itemView as TeacherChannelWidget2)
                    .bindWidget(
                        holder as TeacherChannelWidget2.WidgetHolder,
                        widget as TeacherChannelWidget2.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_DUMMY -> {
                return (holder.itemView as DummyWidget)
                    .bindWidget(
                        holder as DummyWidget.WidgetHolder,
                        widget as DummyWidgetModel
                    )
            }
            WidgetTypes.TYPE_WIDGET_EXPLORE_CARD -> {
                return (holder.itemView as ExploreCardWidget)
                    .bindWidget(
                        holder as ExploreCardWidget.WidgetHolder,
                        widget as ExploreCardWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_FAVOURITE_EXPLORE_CARD -> {
                return (holder.itemView as FavouriteExploreCardWidget)
                    .bindWidget(
                        holder as FavouriteExploreCardWidget.WidgetHolder,
                        widget as ExploreCardWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_CHANNEL -> {
                return (holder.itemView as ChannelWidget)
                    .bindWidget(
                        holder as ChannelWidget.WidgetHolder,
                        widget as ChannelWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_VIDEO_OFFSET -> {
                return (holder.itemView as VideoOffsetWidget)
                    .bindWidget(
                        holder as VideoOffsetWidget.VideoOffsetWidgetHolder,
                        widget as VideoOffsetWidget.VideoOffsetWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_EXPLORE_MORE -> {
                return (holder.itemView as ExploreMoreWidget)
                    .bindWidget(
                        holder as ExploreMoreWidget.WidgetHolder,
                        widget as ExploreMoreWidgetModel
                    )
            }

            WidgetTypes.TYPE_BOOKMARK_LIST -> {
                return (holder.itemView as BookmarkListWidget)
                    .apply {
                        this.adapter = adapter
                    }
                    .bindWidget(
                        holder as BookmarkListWidget.WidgetHolder,
                        widget as BookmarkListWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_LIBRARY_CARD -> {
                return (holder.itemView as LibraryCardWidget)
                    .bindWidget(
                        holder as LibraryCardWidget.WidgetHolder,
                        widget as LibraryCardWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_LIBRARY_EXAM -> {
                return (holder.itemView as LibraryExamWidget)
                    .bindWidget(
                        holder as LibraryExamWidget.WidgetHolder,
                        widget as LibraryExamWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_MATCH_PAGE -> {
                return (holder.itemView as MatchPageWidget)
                    .bindWidget(
                        holder as MatchPageWidget.WidgetHolder,
                        widget as MatchPageWidget.Model
                    )
            }
            WidgetTypes.TYPE_CHECKOUT_V2_HEADER -> {
                return (holder.itemView as CheckoutV2HeaderWidget)
                    .bindWidget(
                        holder as CheckoutV2HeaderWidget.WidgetHolder,
                        widget as CheckoutV2HeaderWidgetModel
                    )
            }
            WidgetTypes.TYPE_CHECKOUT_V2_COUPON -> {
                return (holder.itemView as CheckoutV2CouponWidget)
                    .bindWidget(
                        holder as CheckoutV2CouponWidget.WidgetViewHolder,
                        widget as CheckoutV2CouponWidgetModel
                    )
            }
            WidgetTypes.TYPE_CHECKOUT_V2_PARENT -> {
                return (holder.itemView as CheckoutV2ParentWidget)
                    .bindWidget(
                        holder as CheckoutV2ParentWidget.WidgetHolder,
                        widget as CheckoutV2ParentWidgetModel
                    )
            }
            WidgetTypes.TYPE_CHECKOUT_V2_CHILD -> {
                return (holder.itemView as CheckoutV2ChildWidget)
                    .bindWidget(
                        holder as CheckoutV2ChildWidget.WidgetHolder,
                        widget as CheckoutV2ChildWidgetModel
                    )
            }
            WidgetTypes.TYPE_CHECKOUT_V2_WALLET -> {
                return (holder.itemView as CheckoutV2WalletWidget)
                    .bindWidget(
                        holder as CheckoutV2WalletWidget.WidgetHolder,
                        widget as CheckoutV2WalletWidgetModel
                    )
            }
            WidgetTypes.TYPE_CHECKOUT_V2_TALK_TO_US -> {
                return (holder.itemView as CheckoutV2TalkToUsWidget)
                    .bindWidget(
                        holder as CheckoutV2TalkToUsWidget.WidgetHolder,
                        widget as CheckoutV2TalkToUsWidgetModel
                    )
            }
            WidgetTypes.TYPE_WIDGET_TOP_SUBJECT_STUDYING -> {
                return (holder.itemView as TopSubjectStudyingWidget).bindWidget(
                    holder as TopSubjectStudyingWidget.WidgetHolder,
                    widget as TopSubjectsStudyingWidgetModel
                )
            }
            WidgetTypes.TYPE_WIDGET_CLASSES_BY_TEACHER -> {
                return (holder.itemView as ClassesByTeacherWidget).bindWidget(
                    holder as ClassesByTeacherWidget.WidgetViewHolder,
                    widget as ClassesByTeacherWidget.ClassesByTeacherWidgetModel
                )
            }
            WidgetTypes.TYPE_YOU_WERE_WATCHING_V2 -> {
                return (holder.itemView as YouWereWatchingV2Widget)
                    .bindWidget(
                        holder as YouWereWatchingV2Widget.WidgetHolder,
                        widget as YouWereWatchingV2Widget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_CHAPTER_BY_CLASSES -> {
                return (holder.itemView as ChapterByClassesWidget)
                    .bindWidget(
                        holder as ChapterByClassesWidget.WidgetHolder,
                        widget as ChapterByClassesWidget.Model
                    )
            }

            WidgetTypes.TYPE_COURSE_TIME_TABLE_V2 -> {
                return (holder.itemView as CourseTimeTableWidgetV2)
                    .bindWidget(
                        holder as CourseTimeTableWidgetV2.WidgetHolder,
                        widget as CourseTimetableWidgetV2Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_MOST_VIEWED_CLASSES -> {
                return (holder.itemView as MostViewedClassesWidget)
                    .bindWidget(
                        holder as MostViewedClassesWidget.WidgetHolder,
                        widget as MostViewedClassesWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_COURSE_CAROUSEL -> {
                return (holder.itemView as CourseCarouselWidget)
                    .bindWidget(
                        holder as CourseCarouselWidget.WidgetHolder,
                        widget as CourseCarouselWidget.Model
                    )
            }

            WidgetTypes.TYPE_PREVIOUS_WINNERS_WIDGET -> {
                return (holder.itemView as PreviousWinnersWidget)
                    .bindWidget(
                        holder as PreviousWinnersWidget.WidgetHolder,
                        widget as PreviousWinnersWidget.Model
                    )
            }
            WidgetTypes.TYPE_WINNERS_CARD_WIDGET -> {
                return (holder.itemView as WinnersCardWidget)
                    .bindWidget(
                        holder as WinnersCardWidget.WidgetHolder,
                        widget as WinnersCardWidget.Model
                    )
            }
            WidgetTypes.TYPE_WIDGET_COPY_TEXT -> {
                return (holder.itemView as CopyTextWidget)
                    .bindWidget(
                        holder as CopyTextWidget.WidgetHolder,
                        widget as CopyTextWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_MULTISELECT_SUBJECT_FILTER -> {
                return (holder.itemView as MultiSelectSubjectFilterWidget)
                    .bindWidget(
                        holder as MultiSelectSubjectFilterWidget.WidgetHolder,
                        widget as MultiSelectSubjectFilterWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_PRACTICE_ENGLISH -> {
                return (holder.itemView as PracticeEnglishWidget)
                    .bindWidget(
                        holder as PracticeEnglishWidget.PracticeEnglishWidgetHolder,
                        widget as PracticeEnglishWidget.PracticeEnglishWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_SRP_NUDGE -> {
                return (holder.itemView as SrpNudgeCourseWidget)
                    .bindWidget(
                        holder as SrpNudgeCourseWidget.WidgetHolder,
                        widget as SrpNudgeCourseWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_SUBJECT_COURSE_CARD -> {
                return (holder.itemView as SubjectCourseCardWidget)
                    .bindWidget(
                        holder as SubjectCourseCardWidget.WidgetHolder,
                        widget as SubjectCourseCardWidget.Model
                    )
            }

            WidgetTypes.TYPE_FREE_TRIAL_COURSE -> {
                return (holder.itemView as FreeTrialCourseWidget)
                    .bindWidget(
                        holder as FreeTrialCourseWidget.FreeTrialCourseWidgetHolder,
                        widget as FreeTrialCourseWidget.FreeTrialCourseItemModel
                    )
            }

            WidgetTypes.TYPE_GRADIENT_CARD_WITH_BUTTON -> {
                return (holder.itemView as GradientCardWithButtonWidget)
                    .bindWidget(
                        holder as GradientCardWithButtonWidget.WidgetHolder,
                        widget as GradientCardWithButtonWidget.GradientCardWithButtonWidgetModel
                    )
            }

            WidgetTypes.NEXT_SCREEN_WIDGET -> {
                return (holder.itemView as NextScreenWidget)
                    .bindWidget(
                        holder as NextScreenWidget.NextScreenWidgetHolder,
                        widget as NextScreenWidget.NextScreenWidgetModel
                    )
            }

            WidgetTypes.ENGLISH_QUIZ_INFO_WIDGET -> {
                return (holder.itemView as EnglishQuizInfoWidget)
                    .bindWidget(
                        holder as EnglishQuizInfoWidget.EnglishQuizInfoWidgetHolder,
                        widget as EnglishQuizInfoWidget.EnglishQuizInfoWidgetModel
                    )
            }

            WidgetTypes.INVITE_FRIEND_WIDGET -> {
                return (holder.itemView as InviteFriendWidget)
                    .bindWidget(
                        holder as InviteFriendWidget.WidgetHolder,
                        widget as InviteFriendWidget.InviteFriendWidgetModel
                    )
            }

            WidgetTypes.TYPE_REFERRAL_STEPS_WIDGET -> {
                return (holder.itemView as ReferralStepsWidget)
                    .bindWidget(
                        holder as ReferralStepsWidget.WidgetHolder,
                        widget as ReferralStepsWidget.ReferralStepsWidgetModel
                    )
            }
            WidgetTypes.TYPE_REFERRAL_LEVEL_WIDGET -> {
                return (holder.itemView as ReferralLevelWidget)
                    .bindWidget(
                        holder as ReferralLevelWidget.WidgetHolder,
                        widget as ReferralLevelWidget.WidgetModel
                    )
            }
            WidgetTypes.TYPE_IMAGE_TEXT_WIDGET -> {
                return (holder.itemView as com.doubtnut.referral.widgets.ImageTextWidget)
                    .bindWidget(
                        holder as com.doubtnut.referral.widgets.ImageTextWidget.WidgetHolder,
                        widget as com.doubtnut.referral.widgets.ImageTextWidget.WidgetModel
                    )
            }
            WidgetTypes.TYPE_REFERRAL_WINNER_EARN_MORE_WIDGET -> {
                return (holder.itemView as ReferralWinnerEarnWidget)
                    .bindWidget(
                        holder as ReferralWinnerEarnWidget.WidgetHolder,
                        widget as ReferralWinnerEarnWidget.WidgetModel
                    )
            }
            WidgetTypes.TYPE_REFERRAL_WINNER_CONGRATULATIONS_WIDGET -> {
                return (holder.itemView as ReferralWinnerCongratulationsWidget)
                    .bindWidget(
                        holder as ReferralWinnerCongratulationsWidget.WidgetHolder,
                        widget as ReferralWinnerCongratulationsWidget.WidgetModel
                    )
            }
            WidgetTypes.TYPE_REFERRAL_WINNER_EARN_MORE_WIDGET_V2 -> {
                return (holder.itemView as ReferralWinnerEarnWidgetV2)
                    .bindWidget(
                        holder as ReferralWinnerEarnWidgetV2.WidgetHolder,
                        widget as ReferralWinnerEarnWidgetV2.WidgetModel
                    )
            }
            WidgetTypes.TYPE_REFERRAL_VIDEO_WIDGET -> {
                return (holder.itemView as ReferralVideoWidget)
                    .bindWidget(
                        holder as ReferralVideoWidget.WidgetHolder,
                        widget as ReferralVideoWidget.WidgetModel
                    )
            }
            WidgetTypes.TYPE_REFERRAL_CLAIM_WIDGET -> {
                return (holder.itemView as ReferralClaimWidget)
                    .bindWidget(
                        holder as ReferralClaimWidget.WidgetHolder,
                        widget as ReferralClaimWidget.WidgetModel
                    )
            }
            WidgetTypes.TYPE_MATCH_PAGE_EXTRA_FEATURE -> {
                return (holder.itemView as MatchPageExtraFeatureWidget)
                    .bindWidget(
                        holder as MatchPageExtraFeatureWidget.WidgetHolder,
                        widget as MatchPageExtraFeatureWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_SHORTS_VIDEO_DEFAULT -> {
                return (holder.itemView as ShortsVideoDefaultWidget)
                    .bindWidget(
                        holder as ShortsVideoDefaultWidget.WidgetHolder,
                        widget as ShortsVideoDefaultWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_SHORTS_VIDEO_PROGRESS -> {
                return (holder.itemView as ShortsVideoProgressWidget)
                    .bindWidget(
                        holder as ShortsVideoProgressWidget.WidgetHolder,
                        widget as ShortsVideoProgressWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_FILTER_BUTTON -> {
                return (holder.itemView as FilterButtonWidget)
                    .bindWidget(
                        holder as FilterButtonWidget.FilterButtonWidgetViewHolder,
                        widget as FilterButtonWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_FILTER_DROPDOWN -> {
                return (holder.itemView as FilterDropDownWidget)
                    .bindWidget(
                        holder as FilterDropDownWidget.FilterDropDownWidgetViewHolder,
                        widget as FilterDropDownWidgetModel
                    )
            }

            WidgetTypes.TYPE_WIDGET_NO_DATA -> {
                return (holder.itemView as NoDataWidget)
                    .bindWidget(
                        holder as NoDataWidget.WidgetViewHolder,
                        widget as NoDataWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_FILTER_SORT -> {
                return (holder.itemView as FilterSortWidget)
                    .bindWidget(
                        holder as FilterSortWidget.WidgetViewHolder,
                        widget as FilterSortWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_TWO_TEXTS_HORIZONTAL -> {
                return (holder.itemView as TwoTextsHorizontalWidget)
                    .bindWidget(
                        holder as TwoTextsHorizontalWidget.WidgetViewHolder,
                        widget as TwoTextsHorizontalWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_WATCH_AND_WIN -> {
                return (holder.itemView as WatchAndWinWidget)
                    .bindWidget(
                        holder as WatchAndWinWidget.WidgetViewHolder,
                        widget as WatchAndWinWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGETS_TWO_TEXTS_VERTICAL_TABS -> {
                return (holder.itemView as TwoTextsVerticalTabsWidget)
                    .bindWidget(
                        holder as TwoTextsVerticalTabsWidget.WidgetViewHolder,
                        widget as TwoTextsVerticalTabsWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_WATCH_NOW -> {
                return (holder.itemView as WatchNowWidget)
                    .bindWidget(
                        holder as WatchNowWidget.WidgetViewHolder,
                        widget as WatchNowWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_IAS -> {
                return (holder.itemView as IASWidget)
                    .bindWidget(
                        holder as IASWidget.WidgetHolder,
                        widget as IASWidget.WidgetModel
                    )
            }

            WidgetTypes.TYPE_REFER_AND_EARN_HEADER_WIDGET -> {
                return (holder.itemView as ReferAndEarnHeaderWidget)
                    .bindWidget(
                        holder as ReferAndEarnHeaderWidget.ReferAndEarnHeaderWidgetHolder,
                        widget as ReferAndEarnHeaderWidget.WidgetModel
                    )
            }

            WidgetTypes.TYPE_REFER_AND_EARN_STEPS_WIDGET -> {
                return (holder.itemView as ReferAndEarnStepsWidget)
                    .bindWidget(
                        holder as ReferAndEarnStepsWidget.WidgetHolder,
                        widget as ReferAndEarnStepsWidget.WidgetModel
                    )
            }

            WidgetTypes.TYPE_REFERRAL_CODE_WIDGET -> {
                return (holder.itemView as ReferralCodeWidget)
                    .bindWidget(
                        holder as ReferralCodeWidget.WidgetHolder,
                        widget as ReferralCodeWidget.WidgetModel
                    )
            }

            WidgetTypes.TYPE_REFERRED_FRIENDS_WIDGET -> {
                return (holder.itemView as ReferredFriendsWidget)
                    .bindWidget(
                        holder as ReferredFriendsWidget.WidgetHolder,
                        widget as ReferredFriendsWidget.WidgetModel
                    )
            }
            WidgetTypes.TYPE_D0_QA_WIDGET -> {
                return (holder.itemView as D0QaWidget)
                    .bindWidget(
                        holder as D0QaWidget.WidgetHolder,
                        widget as D0QaWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_RC_TEST_PAPER -> {
                return (holder.itemView as RCTestPapersWidget)
                    .bindWidget(
                        holder as RCTestPapersWidget.WidgetHolder,
                        widget as RCTestPapersWidget.Model
                    )
            }

            WidgetTypes.TYPE_WIDGET_RC_PREVIOUS_YEAR_PAPER -> {
                return (holder.itemView as RCPreviousYearPapersWidget)
                    .bindWidget(
                        holder as RCPreviousYearPapersWidget.WidgetHolder,
                        widget as RCPreviousYearPapersWidget.Model
                    )
            }

            WidgetTypes.TYPE_VERTICAL_PARENT_WIDGET -> {
                return (holder.itemView as VerticalParentWidget)
                    .bindWidget(
                        holder as VerticalParentWidget.WidgetViewHolder,
                        widget as VerticalParentWidget.VerticalParentWidgetModel
                    )
            }

            WidgetTypes.TYPE_COUPON_APPLIED_WIDGET -> {
                return (holder.itemView as CouponAppliedWidget)
                    .bindWidget(
                        holder as CouponAppliedWidget.WidgetViewHolder,
                        widget as CouponAppliedWidget.Model
                    )
            }

            WidgetTypes.TYPE_CLIPBOARD_WIDGET -> {
                return (holder.itemView as ClipboardWidget)
                    .bindWidget(
                        holder as ClipboardWidget.WidgetViewHolder,
                        widget as ClipboardWidget.Model
                    )
            }

            WidgetTypes.TYPE_TIMER_WIDGET -> {
                return (holder.itemView as TimerWidget).apply {
                    this.adapter = adapter
                }
                    .bindWidget(
                        holder as TimerWidget.WidgetViewHolder,
                        widget as TimerWidget.Model
                    )
            }

            WidgetTypes.TYPE_DOUBT_PE_CHARCHA_QUESTION -> {
                return (holder.itemView as DoubtPeCharchaQuestionWidget)
                    .bindWidget(
                        holder as DoubtPeCharchaQuestionWidget.DoubtPeCharchaQuestionViewholder,
                        widget as DoubtPeCharchaQuestionWidget.WidgetModel
                    )
            }

            WidgetTypes.TYPE_GRADIENT_BANNER_WITH_ACTION_BUTTON_WIDGET -> {
                return (holder.itemView as GradientBannerWithActionButtonWidget)
                    .bindWidget(
                        holder as GradientBannerWithActionButtonWidget.GradientBannerWidgetViewHolder,
                        widget as GradientBannerWithActionButtonWidget.WidgetModel
                    )
            }

            WidgetTypes.TYPE_BADGE_FOR_LEVEL -> {
                (holder.itemView as BadgesForLevelWidget)
                    .bindWidget(
                        holder as BadgesForLevelWidget.BadgeForWidgetViewHolder,
                        widget as BadgesForLevelWidget.WidgetModel
                    )
            }

            WidgetTypes.TYPE_USER_BADGE_BANNER_WIDGET ->
                (holder.itemView as UserBadgeBannerWidget)
                    .bindWidget(
                        holder as UserBadgeBannerWidget.WidgetViewHolder,
                        widget as UserBadgeBannerWidget.WidgetModel
                    )

            WidgetTypes.TYPE_DOUBT_SUGGESTER_WIDGET -> {
                return (holder.itemView as DoubtSuggesterWidget)
                    .bindWidget(
                        holder as DoubtSuggesterWidget.WidgetHolder,
                        widget as DoubtSuggesterWidget.DoubtSuggesterWidgetModel
                    )
            }
            WidgetTypes.TYPE_MORE_TESTIMONIALS_WIDGET -> {
                return (holder.itemView as MoreTestimonialsWidget)
                    .bindWidget(
                        holder as MoreTestimonialsWidget.WidgetHolder,
                        widget as MoreTestimonialsWidget.MoreTestimonialsWidgetModel
                    )
            }
            WidgetTypes.TYPE_TOPPERS_WIDGET -> {
                return (holder.itemView as ToppersWidget)
                    .bindWidget(
                        holder as ToppersWidget.WidgetHolder,
                        widget as ToppersWidget.ToppersWidgetModel
                    )
            }

            WidgetTypes.TYPE_EXCEL_COURSES_WIDGET -> {
                return (holder.itemView as ExcelCoursesWidget)
                    .bindWidget(
                        holder as ExcelCoursesWidget.WidgetHolder,
                        widget as ExcelCoursesWidget.ExcelCoursesWidgetModel
                    )
            }
        }
        return null
    }
}