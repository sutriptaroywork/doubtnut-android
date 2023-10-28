package com.doubtnutapp.di.component

import android.app.Application
import android.content.SharedPreferences
import com.doubtnut.noticeboard.di.NoticeBoardModule
import com.doubtnut.olympiad.di.OlympiadModule
import com.doubtnut.referral.di.ReferralModule
import com.doubtnut.scholarship.di.module.ScholarshipModule
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.ExploreMoreWidget
import com.doubtnutapp.MultiSelectFilterWidget
import com.doubtnutapp.PendingPaymentWidget
import com.doubtnutapp.addtoplaylist.HomePageAskDoubtWidget
import com.doubtnutapp.bottomnavigation.BottomNavCustomView
import com.doubtnutapp.callingnotice.CallingNoticeWidget
import com.doubtnutapp.course.widgets.*
import com.doubtnutapp.data.remote.ApiMetaInterceptor
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.GlobalErrorHandler
import com.doubtnutapp.db.DoubtnutDatabase
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.di.module.*
import com.doubtnutapp.dnr.widgets.*
import com.doubtnutapp.doubt.bookmark.widget.BookmarkListWidget
import com.doubtnutapp.examcorner.widgets.ExamCornerAutoplayWidget
import com.doubtnutapp.examcorner.widgets.ExamCornerDefaultWidget
import com.doubtnutapp.examcorner.widgets.ExamCornerPopularWidget
import com.doubtnutapp.explore_v2.ExploreCourseV2WidgetCircle
import com.doubtnutapp.explore_v2.ExploreCourseV2WidgetSquare
import com.doubtnutapp.fallbackquiz.fallbackjob.FallbackReceiver
import com.doubtnutapp.fallbackquiz.ui.FallbackActionHandlerActivity
import com.doubtnutapp.fallbackquiz.worker.FallbackQuizWorker
import com.doubtnutapp.feed.view.*
import com.doubtnutapp.feed.view.viewholders.FeedVideoItemViewHolder
import com.doubtnutapp.freeclasses.widgets.*
import com.doubtnutapp.icons.widgets.ExploreCardWidget
import com.doubtnutapp.icons.widgets.FavouriteExploreCardWidget
import com.doubtnutapp.leaderboard.widget.*
import com.doubtnutapp.libraryhome.course.ui.ScheduleResourceViewHolder
import com.doubtnutapp.libraryhome.coursev3.ui.CouponBannerWidget
import com.doubtnutapp.librarylisting.viewholder.BookViewHolder
import com.doubtnutapp.liveclass.ui.practice_english.PracticeEnglishWidget
import com.doubtnutapp.matchquestion.ui.viewholder.AutoPlayMatchResultViewHolder
import com.doubtnutapp.matchquestion.ui.viewholder.MatchFilterTopicV2ViewHolder
import com.doubtnutapp.matchquestion.ui.viewholder.MatchQuestionListItemViewHolder
import com.doubtnutapp.mediumSwitch.MediumSwitchWidget
import com.doubtnutapp.networkstats.utils.NetworkUsageStatsWorker
import com.doubtnutapp.newlibrary.viewholder.LibrarySavedItemsViewHolder
import com.doubtnutapp.paymentplan.widgets.*
import com.doubtnutapp.profile.social.UserRelationshipAdapter
import com.doubtnutapp.quiztfs.widgets.*
import com.doubtnutapp.resultpage.di.ResultPageModule
import com.doubtnutapp.resultpage.widgets.ExcelCoursesWidget
import com.doubtnutapp.resultpage.widgets.MoreTestimonialsWidget
import com.doubtnutapp.resultpage.widgets.ToppersWidget
import com.doubtnutapp.revisioncorner.ui.RCPreviousYearPapersWidget
import com.doubtnutapp.revisioncorner.ui.RCTestPapersWidget
import com.doubtnutapp.reward.receiver.*
import com.doubtnutapp.reward.ui.viewholder.BottomDeeplinkViewHolder
import com.doubtnutapp.sales.PrePurchaseCallingCard2
import com.doubtnutapp.sales.widget.PrePurchaseCallingCard
import com.doubtnutapp.scheduledquiz.ScheduledQuizNotificationWorker
import com.doubtnutapp.scheduledquiz.ui.viewholders.*
import com.doubtnutapp.sharing.di.WhatsAppSharingBindModule
import com.doubtnutapp.similarVideo.viewholder.LandscapeSimilarVideoListItemViewHolder
import com.doubtnutapp.similarVideo.viewholder.SaleTimerViewHolder
import com.doubtnutapp.similarVideo.viewholder.ScratchCardViewHolder
import com.doubtnutapp.similarVideo.viewholder.SimilarVideoListItemViewHolder
import com.doubtnutapp.similarVideo.widgets.*
import com.doubtnutapp.studygroup.ui.viewholder.StudyGroupMemberListViewHolder
import com.doubtnutapp.topicboostergame2.ui.viewholder.LeaderboardViewHolder
import com.doubtnutapp.topicboostergame2.ui.viewholder.LevelViewHolder
import com.doubtnutapp.training.OnboardingManager
import com.doubtnutapp.ui.forum.doubtsugggester.widget.DoubtSuggesterWidget
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.onboarding.ui.viewholder.OnBoardingStepHeaderViewHolder
import com.doubtnutapp.ui.onboarding.ui.viewholder.OnBoardingStepViewHolder
import com.doubtnutapp.ui.questionAskedHistory.QuestionAskedHistoryListAdapter
import com.doubtnutapp.ui.quiz.FetchQuizJob
import com.doubtnutapp.ui.quiz.FetchQuizJobWorker
import com.doubtnutapp.ui.topperStudyPlan.ChapterVideoAdapter
import com.doubtnutapp.utils.BannerActionUtils
import com.doubtnutapp.video.VideoPlayerManager
import com.doubtnutapp.videoPage.widgets.LiveClassCarouselCard2Widget
import com.doubtnutapp.videoPage.widgets.LiveClassCarouselCardWidget
import com.doubtnutapp.videoPage.widgets.SrpNudgeCourseWidget
import com.doubtnutapp.widgetmanager.widgets.*
import com.doubtnutapp.widgetmanager.widgets.tablist.TabListWidget
import com.doubtnutapp.widgets.*
import com.doubtnutapp.widgettest.widgets.DummyWidget
import com.doubtnutapp.workmanager.workers.UpdateVideoStatsWorker
import com.doubtnutapp.workmanager.workers.ViewIdTrackerWorker
import com.doubtnutapp.workmanager.workers.ViewStatusUpdateWorker
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Component(
    modules = [
        AppModule::class,
        ViewModelProviderModule::class,
        RepositoryModule::class,
        ApiServiceModule::class,
        BindingActivityModule::class,
        BindingActivityModule2::class,
        BindingFragmentModule::class,
        BindingFragmentModule2::class,
        WhatsAppSharingBindModule::class,
        ReferralModule::class,
        OlympiadModule::class,
        ScholarshipModule::class,
        NoticeBoardModule::class,
        ServiceModule::class,
        CustomClassDiModule::class,
        CustomViewDiModule::class,
        CoreCustomViewDiModule::class,
        AndroidSupportInjectionModule::class,
        ResultPageModule::class
    ]
)
@Singleton
interface DoubtnutAppComponent {

    fun getSharedPreference(): SharedPreferences

    fun getRoomDb(): DoubtnutDatabase

    @Component.Builder
    interface Builder {

        fun build(): DoubtnutAppComponent

        @BindsInstance
        fun application(application: Application): Builder
    }

    fun inject(app: DoubtnutApp)

    fun inject(dataHandler: DataHandler)

    fun inject(interceptor: ApiMetaInterceptor)

    fun inject(mediumSwitchWidget: MediumSwitchWidget)

    fun inject(examCornerDefaultWidget: ExamCornerDefaultWidget)

    fun inject(examCornerPopularWidget: ExamCornerPopularWidget)

    fun inject(examCornerAutoplayWidget: ExamCornerAutoplayWidget)

    fun inject(callingNoticeWidget: CallingNoticeWidget)

    fun inject(horizontalListWidget: HorizontalListWidget)

    fun inject(tabListWidget: TabListWidget)

    fun inject(verticalListWidget: VerticalListWidget)

    fun inject(carouselListWidget: CarouselListWidget)

    fun inject(faqWidget: FaqWidget)

    fun inject(courseSubjectWidget: CourseSubjectWidget)

    fun inject(courseInfoWidget2: CourseInfoWidget2)

    fun inject(courseDetailsWidget: CourseDetailsWidget)

    fun inject(courseResourcesWidget: CourseResourcesWidget)

    fun inject(bannerImageWidget: BannerImageWidget)

    fun inject(whatsappWidget: WhatsappWidget)

    fun inject(dailyQuizWidget: DailyQuizWidget)

    fun inject(feedAdapter: FeedAdapter)

    fun inject(feedPostActionsView: FeedPostActionsView)

    fun inject(globalErrorHandler: GlobalErrorHandler)

    fun inject(feedAttachmentView: FeedAttachmentView)

    fun inject(feedPinnedVideoAutoplayChildWidget: FeedPinnedVideoAutoplayChildWidget)

    fun inject(feedLiveInfoView: FeedLiveInfoView)

    fun inject(linkPreviewView: LinkPreviewView)

    fun inject(feedPollView: FeedPollView)

    fun inject(deeplinkAction: DeeplinkAction)

    fun inject(bannerActionUtils: BannerActionUtils)

    fun inject(tabCourseWidget: TabCourseWidget)

    fun inject(courseFilterWidget: CourseFilterWidget)

    fun inject(reminderCardWidget: ReminderCardWidget)

    fun inject(allCourseWidget2: AllCourseWidget2)

    fun inject(filterExamWidget: FilterExamWidget)

    fun inject(filterSubjectWidget: FilterSubjectWidget)

    fun inject(matchQuestionListItemViewHolder: MatchQuestionListItemViewHolder)

    fun inject(matchFilterTopicV2ViewHolder: MatchFilterTopicV2ViewHolder)

    fun inject(onBoardingStepViewHolder: OnBoardingStepViewHolder)

    fun inject(onBoardingStepHeaderViewHolder: OnBoardingStepHeaderViewHolder)

    fun inject(chapterVideoAdapter: ChapterVideoAdapter)

    fun inject(courseContentWidget: CourseContentWidget)

    fun inject(courseExamTabWidget: CourseExamTabWidget)

    fun inject(widget: CourseTypeTabWidget)

    fun inject(widget: CourseClassTabWidget)

    fun inject(widget: PromoListWidget)

    fun inject(questionAskedHistoryListAdapter: QuestionAskedHistoryListAdapter)

    fun inject(planTypeTabWidget: PlanTypeTabWidget)

    fun inject(planInfoWidget: PlanInfoWidget)

    fun inject(planListWidget: PlanListWidget)

    fun inject(filterCourseTypeWidget: FilterCourseTypeWidget)

    fun inject(userRelationshipAdapter: UserRelationshipAdapter)

    fun inject(autoPlayMatchResultViewHolder: AutoPlayMatchResultViewHolder)

    fun inject(widget: LiveClassCarouselWidget)

    fun inject(widget: LiveClassCarouselCardWidget)

    fun inject(widget: LiveClassCarouselCard2Widget)

    fun inject(widget: NotesWidget)

    fun inject(widget: RelatedLecturesWidget)

    fun inject(widget: TopicsCoveredWidget)

    fun inject(widget: NotifyClassWidget)

    fun inject(widget: ResourcePageUpcomingWidget)

    fun inject(widget: UpcomingLecturesWidget)

    fun inject(widget: FacultyWidget)

    fun inject(widget: CourseInfoWidget)

    fun inject(widget: AllTopicsWidget)

    fun inject(widget: CommonCourseWidget)

    fun inject(widget: SyllabusWidgetTwo)

    fun inject(updateVideoStatsWorker: UpdateVideoStatsWorker)

    fun inject(widget: TimetableWidget)

    fun inject(widget: PackageDetailWidget)

    fun inject(widget: CourseCategoryWidget)

    fun inject(widget: CourseChildWidget)

    fun inject(widget: CourseCarouselChildWidget)

    fun inject(widget: CourseResourceChildWidget)

    fun inject(widget: ParentWidget)

    fun inject(widget: ParentTabWidget)

    fun inject(widget: IplScoreBoardWidget)

    fun inject(widget: FacultyGridWidget)

    fun inject(widget: SubjectWidget)

    fun inject(widget: PaymentListWidget)

    fun inject(widget: PaymentCardWidget)

    fun inject(widget: ScheduleResourceViewHolder)

    fun inject(widget: MyPlanWidget)

    fun inject(widget: PurchasedClassesWidget)

    fun inject(widget: LiveClassCategoryWidget)

    fun inject(widget: CourseProgressWidget)

    fun inject(widget: PaymentHistoryWidget)

    fun inject(widget: CourseWidget)

    fun inject(widget: SaleWidget)

    fun inject(widget: LiveQuestionsDailyPracticeWidget)

    fun inject(widget: LiveQuestionsDailyPracticeRankWidget)

    fun inject(widget: LiveQuestionsDailyPracticeFAQWidget)

    fun inject(widget: DailyPracticeWidget)

    fun inject(widget: MyRewardsScratchCardWidget)

    fun inject(widget: MyRewardsPointsWidget)

    fun inject(videoPlayerManager: VideoPlayerManager)

    fun inject(saleTimerViewHolder: SaleTimerViewHolder)

    fun inject(scrachCardViewHolder: ScratchCardViewHolder)

    fun inject(widget: ActivateTrialWidget)

    fun inject(widget: NotesFilterWidget)

    fun inject(viewIdTrackerWorker: ViewIdTrackerWorker)

    fun inject(viewStatusUpdateWorker: ViewStatusUpdateWorker)

    fun inject(multiSelectFilterWidget: MultiSelectFilterWidget)

    fun inject(multiSelectFilterWidgetV2: MultiSelectFilterWidgetV2)

    fun inject(myCourseWidget: MyCourseWidget)

    fun inject(prePurchaseCallingCard: PrePurchaseCallingCard)

    fun inject(widget: PrePurchaseCallingCard2)

    fun inject(leaderboardPersonalWidget: LeaderboardPersonalWidget)

    fun inject(leaderboardTabWidget: LeaderboardTabWidget)

    fun inject(leaderboardTopThreeWidget: LeaderboardTopThreeWidget)

    fun inject(leaderBoardWidget: LeaderBoardWidget)

    fun inject(askDoubtCardWidget: AskDoubtCardWidget)

    fun inject(imageCardWidget: ImageCardWidget)

    fun inject(feedBannerWidget: FeedBannerWidget)

    fun inject(homeWorkWidget: HomeWorkWidget)

    fun inject(homeWorkWidget: HomeWorkListWidget)

    fun inject(topOptionsWidget: TopOptionsWidget)

    fun inject(topOptionViewHolder: TopOptionViewHolder)

    fun inject(incompleteChapterWidget: IncompleteChapterWidget)

    fun inject(collapsedWidget: CollapsedWidget)

    fun inject(playlistWidget: PlaylistWidget)

    fun inject(popularStatusWidget: RecentStatusWidget)

    fun inject(homeWorkWidget: HomeWorkHorizontalListWidget)

    fun inject(gradientCardWidget: GradientCardWidget)

    fun inject(bookProgressWidget: BookProgressWidget)

    fun inject(parentAutoplayWidget: ParentAutoplayWidget)

    fun inject(autoPlayChildWidget: AutoPlayChildWidget)

    fun inject(upcomingLiveClassWidget: UpcomingLiveClassWidget)

    fun inject(manager: OnboardingManager)

    fun inject(paidCourseWidget: PaidCourseWidget)

    fun inject(youWereWatchingWidget: YouWereWatchingWidget)

    fun inject(videoBannerAutoplayChildWidget: VideoBannerAutoplayChildWidget)

    fun inject(scheduleNotificationViewHolder: ScheduleNotificationViewHolder)

    fun inject(scheduledQuizNotificationWorker: ScheduledQuizNotificationWorker)

    fun inject(missedClassViewHolder: MissedClassViewHolder)

    fun inject(quizSolutionViewHolder: QuizSolutionViewHolder)

    fun inject(quizSubjectViewHolder: QuizSubjectViewHolder)

    fun inject(quizExploreViewHolder: QuizExploreViewHolder)

    fun inject(quizWordViewHolder: QuizWordViewHolder)

    fun inject(quizPostViewHolder: QuizPostViewHolder)

    fun inject(quizNcertViewHolder: QuizNcertViewHolder)

    fun inject(quizAskViewHolder: QuizAskViewHolder)

    fun inject(quizPlaylistViewHolder: QuizPlaylistViewHolder)

    fun inject(quizMotivationViewHolder: QuizMotivationViewHolder)

    fun inject(fetchQuizJobWorker: FetchQuizJobWorker)

    fun inject(fetchQuizJob: FetchQuizJob)

    fun inject(quizLeaderboardViewHolder: QuizLeaderboardViewHolder)

    fun inject(leaderboardViewHolder: LeaderboardViewHolder)

    fun inject(quizMcqViewHolder: QuizMcqViewHolder)

    fun inject(imageCardGroupWidget: ImageCardGroupWidget)

    fun inject(followWidget: FollowWidget)

    fun inject(testimonialWidget: TestimonialWidget)

    fun inject(facultyListWidget: FacultyListWidget)

    fun inject(courseFaqsWidget: CourseFaqsWidget)

    fun inject(courseContentListWidget: CourseContentListWidget)

    fun inject(courseItemInfoWidget: CourseItemInfoWidget)

    fun inject(courseFeatureWidget: CourseFeatureWidget)

    fun inject(courseEmiInfoWidget: CourseEmiInfoWidget)

    fun inject(course: CourseWidgetV2)

    fun inject(similarVideoListItemViewHolder: SimilarVideoListItemViewHolder)

    fun inject(similarVideoListItemViewHolder: LandscapeSimilarVideoListItemViewHolder)

    fun inject(packageDetailWidget: PackageDetailWidgetV2)

    fun inject(topDoubtAnswerVideoWidget: TopDoubtAnswerVideoWidget)

    fun inject(topDoubtAnswerAudioWidget: TopDoubtAnswerAudioWidget)

    fun inject(topDoubtAnswerImageWidget: TopDoubtAnswerImageWidget)

    fun inject(pendingPaymentWidget: PendingPaymentWidget)

    fun inject(fallbackQuizWorker: FallbackQuizWorker)

    fun inject(fallbackReceiver: FallbackReceiver)

    fun inject(fallbackActionHandlerActivity: FallbackActionHandlerActivity)

    fun inject(bottomDeeplinkViewHolder: BottomDeeplinkViewHolder)

    fun inject(rewardReceiver: LoginRewardReceiver)

    fun inject(rewardRepeatedNotificationReceiver: RewardRepeatedNotificationReceiver)

    fun inject(rewardNotificationReceiver: RewardNotificationReceiver)

    fun inject(attendanceMarkedReceiver: AttendanceMarkedReceiver)

    fun inject(rewardNotificationHandlerActivity: RewardNotificationHandlerActivity)

    fun inject(courseParentWidget: CourseParentWidget)

    fun inject(courseClassesWidget: CourseClassesWidget)

    fun inject(courseTestWidget: CourseTestWidget)

    fun inject(allClassesWidget: AllClassesWidget)

    fun inject(increaseValidityWidget: IncreaseValidityWidget)

    fun inject(storyWidget: StoryWidget)

    fun inject(notesFilterByWidget: NotesFilterByWidget)

    fun inject(homeWorkWidget: HomeWorkListWidgetV2)

    fun inject(courseAutoPlayWidget: CourseAutoPlayChildWidget)

    fun inject(scheduleWidget: ScheduleWidget)

    fun inject(trialTimerWidget: TrialTimerWidget)

    fun inject(bookmarkListWidget: BookmarkListWidget)

    fun inject(testWidget: TestWidget)

    fun inject(viewAllWidget: ViewAllWidget)

    fun inject(scheduleMonthFilterWidget: ScheduleMonthFilterWidget)

    fun inject(trialButtonWidget: TrialButtonWidget)

    fun inject(buyCompleteCourseWidget: BuyCompleteCourseWidget)

    fun inject(buttonBorderWidget: ButtonBorderWidget)

    fun inject(widgetViewPlanButton: WidgetViewPlanButton)

    fun inject(collapseExpandMathViewWidget: CollapseExpandMathViewWidget)

    fun inject(videoActionWidget: VideoActionWidget)

    fun inject(parentGridSelectionWidget: ParentGridSelectionWidget)

    fun inject(childGridSelectionWidget: ChildGridSelectionWidget)

    fun inject(ncertSimilarWidget: NcertSimilarWidget)

    fun inject(ncertBookWidget: NcertBookWidget)

    fun inject(bookViewHolder: BookViewHolder)

    fun inject(librarySavedItemsViewHolder: LibrarySavedItemsViewHolder)

    fun inject(popularCourseWidget: PopularCourseWidget)

    fun inject(attendanceWidget: AttendanceWidget)

    fun inject(similarWidget: SimilarWidget)

    fun inject(widgetHomePageAskDoubt: HomePageAskDoubtWidget)

    fun inject(studyDostWidget: StudyDostWidget)

    fun inject(couponBannerWidget: CouponBannerWidget)

    fun inject(courseListWidget: PurchasedCourseListWidget)

    fun inject(pdfNotesWidget: PdfNotesWidget)

    fun inject(doubtFeedDailyGoalWidget: DoubtFeedDailyGoalWidget)

    fun inject(topicBoosterWidget: TopicBoosterWidget)

    fun inject(formulaSheetWidget: FormulaSheetWidget)

    fun inject(topicVideoWidget: TopicVideoWidget)

    fun inject(askedQuestionWidget: AskedQuestionWidget)

    fun inject(doubtFeedStartPracticeWidget: DoubtFeedStartPracticeWidget)

    fun inject(buttonWidget: ButtonWidget)

    fun inject(topicBoosterGameBannerWidget: TopicBoosterGameBannerWidget)

    fun inject(studyGroupBannerImageWidget: StudyGroupBannerImageWidget)

    fun inject(studyGroupMemberListViewHolder: StudyGroupMemberListViewHolder)

    fun inject(studyGroupParentWidget: StudyGroupParentWidget)

    fun inject(studyGroupLiveClassWidget: StudyGroupLiveClassWidget)

    fun inject(audioPlayerWidget: AudioPlayerWidget)

    fun inject(studyGroupGuidelineWidget: StudyGroupGuidelineWidget)

    fun inject(studyGroupInvitationWidget: StudyGroupInvitationWidget)

    fun inject(studyGroupJoinedInfoWidget: StudyGroupJoinedInfoWidget)

    fun inject(studyGroupFeatureUnavailableWidget: StudyGroupFeatureUnavailableWidget)

    fun inject(pdfViewWidget: PdfViewWidget)

    fun inject(mySachetWidget: MySachetWidget)

    fun inject(doubtFeedBannerWidget: DoubtFeedBannerWidget)

    fun inject(nudgeWidget: NudgeWidget)

    fun inject(nudgePopupWidget: NudgePopupWidget)

    fun inject(widget: FeedVideoItemViewHolder)

    fun inject(doubtP2PHomeWidget: DoubtP2PHomeWidget)

    fun inject(doubtP2PWidget: DoubtP2PWidget)

    fun inject(doubtP2PAnimationWidget: DoubtP2PAnimationWidget)

    fun inject(packageDetailWidget: PackageDetailWidgetV3)

    fun inject(classBoardExamWidget: ClassBoardExamWidget)

    fun inject(courseRecommendationParentWidget: CourseRecommendationParentWidget)

    fun inject(courseRecommendationMessageWidget: CourseRecommendationMessageWidget)

    fun inject(courseRecommendationRadioButtonWidget: CourseRecommendationRadioButtonWidget)

    fun inject(courseRecommendationSubmittedAnswerWidget: CourseRecommendationSubmittedAnswerWidget)

    fun inject(studyGroupVideoCardWidget: StudyGroupVideoCardWidget)

    fun inject(kheloJeetoBannerWidget: KheloJeetoBannerWidget)

    fun inject(autoScrollImageWidget: AutoScrollImageWidget)

    fun inject(notesWidgetV2: NotesWidgetV2)

    fun inject(courseTestWidgetV2: CourseTestWidgetV2)

    fun inject(packageDetailWidgetV4: PackageDetailWidgetV4)

    fun inject(planWidget: PlanWidget)

    fun inject(sampleQuestionWidget: SampleQuestionWidget)

    fun inject(courseExploreWidget: CourseExploreWidget)

    fun inject(doubtFeedWidget: DoubtFeedWidget)

    fun inject(rewardNotificationReceiver: com.doubtnutapp.doubtfeed2.reward.receiver.RewardNotificationReceiver)

    fun inject(courseTimeTableWidget: CourseTimeTableWidget)

    fun inject(selectMediumWidget: SelectMediumWidget)

    fun inject(syllabusWidget: SyllabusWidget)

    fun inject(contentFilterWidget: ContentFilterWidget)

    fun inject(courseInfoWidgetV2: CourseInfoWidgetV2)

    fun inject(levelViewHolder: LevelViewHolder)

    fun inject(courseWidgetV3: CourseWidgetV3)

    fun inject(testimonialWidget: TestimonialWidgetV2)

    fun inject(courseWidgetV4: CourseWidgetV4)

    fun inject(couponListWidget: CouponListWidget)

    fun inject(leaderboardViewHolder: com.doubtnutapp.doubtfeed2.leaderboard.ui.viewholder.LeaderboardViewHolder)

    fun inject(topSellingSubjectWidget: TopSellingSubjectWidget)

    fun inject(callingBigCardWidget: CallingBigCardWidget)

    fun inject(courseResources: CourseResourceWidget)

    fun inject(courseRecommendationWidget: CourseRecommendationWidget)

    fun inject(categoryWidget: CategoryWidget)

    fun inject(recommendedTestWidget: RecommendedTestWidget)

    fun inject(courseInfoWidgetV3: CourseInfoWidgetV3)

    fun inject(studyGroupReportParentWidget: StudyGroupReportParentWidget)

    fun inject(explorePromoWidget: ExplorePromoWidget)

    fun inject(videoWidget: VideoWidget)

    fun inject(recommendationWidget: RecommendationWidget)

    fun inject(vpaWidget: VpaWidget)

    fun inject(testAnalysisWidget: TestAnalysisWidget)

    fun inject(testResultWidget: TestResultWidget)

    fun inject(revisionCornerBannerWidget: RevisionCornerBannerWidget)

    fun inject(iconCtaWidget: IconCtaWidget)

    fun inject(widget: BulletListWidget)

    fun inject(widget: PadhoAurJeetoWidget)

    fun inject(widget: LeaderboardProgressWidget)

    fun inject(widget: TfsAnalysisWidget)

    fun inject(widget: ParentTabWidget2)

    fun inject(widget: ExploreCourseV2WidgetSquare)

    fun inject(widget: ExploreCourseV2WidgetCircle)

    fun inject(widget: LatestLaunchesWidget)

    fun inject(sgGroupChatWidget: SgGroupChatWidget)

    fun inject(sgPersonalChatWidget: SgPersonalChatWidget)

    fun inject(sgRequestWidget: SgRequestWidget)

    fun inject(sgBlockedMemberWidget: SgBlockedMemberWidget)

    fun inject(sgHomeWidget: SgHomeWidget)

    fun inject(sgJoinNewGroupWidget: SgJoinNewGroupWidget)

    fun inject(videoAutoplayChildWidget2: VideoAutoplayChildWidget2)

    fun inject(teacherHeaderWidget: TeacherHeaderWidget)

    fun inject(channelAnnoucementWidget: ChannelAnnouncementWidget)

    fun inject(filterSubTabWidget: ChannelTabFilterWidget)

    fun inject(channelFilterWidget: ChannelSubTabFilterWidget)

    fun inject(channelFilterContentWidget: ChannelContentFilterWidget)

    fun inject(channelVideoContentWidget: ChannelVideoContentWidget)

    fun inject(channelPDFContentWidget: ChannelPDFContentWidget)

    fun inject(teacherChannelWidget: TeacherChannelWidget)

    fun inject(subscribedTeacherChannelWidget: SubscribedTeacherChannelWidget)

    fun inject(dummyWidget: DummyWidget)

    fun inject(widget: ExploreCardWidget)

    fun inject(widget: FavouriteExploreCardWidget)

    fun inject(widget: ChannelWidget)

    fun inject(widget: VideoOffsetWidget)

    fun inject(dnrEarnedHistoryWidget: DnrEarnedHistoryWidget)

    fun inject(dnrEarnedHistoryItemWidget: DnrEarnedHistoryItemWidget)

    fun inject(dnrEarnedSummaryWidget: DnrEarnedSummaryWidget)

    fun inject(dnrRewardDetailWidget: DnrRewardDetailWidget)

    fun inject(dnrRedeemVoucherWidget: DnrRedeemVoucherWidget)

    fun inject(dnrTodayRewardWidget: DnrTodayRewardWidget)

    fun inject(dnrTotalRewardWidget: DnrTotalRewardWidget)

    fun inject(dnrHomeWidget: DnrHomeWidget)

    fun inject(dnrEarningsDetailWidget: DnrEarningsDetailWidget)

    fun inject(dnrRewardHistoryWidget: DnrRewardHistoryWidget)

    fun inject(dnrUnlockedVoucherWidget: DnrUnlockedVoucherWidget)

    fun inject(dnrStreakWidget: DnrStreakWidget)

    fun inject(exploreMoreWidget: ExploreMoreWidget)

    fun inject(libraryCardWidget: LibraryCardWidget)

    fun inject(libraryExamWidget: LibraryExamWidget)

    fun inject(textWidget: TextWidget)

    fun inject(matchPageWidget: MatchPageWidget)

    fun inject(widget: CheckoutV2HeaderWidget)

    fun inject(widget: CheckoutV2CouponWidget)

    fun inject(widget: CheckoutV2ChildWidget)

    fun inject(widget: CheckoutV2ParentWidget)

    fun inject(widget: CheckoutV2WalletWidget)

    fun inject(checkoutV2TalkToUsWidget: CheckoutV2TalkToUsWidget)

    fun inject(topSubjectStudyingWidget: TopSubjectStudyingWidget)

    fun inject(classesByTeacherWidget: ClassesByTeacherWidget)

    fun inject(widget: YouWereWatchingV2Widget)

    fun inject(widget: ChapterByClassesWidget)

    fun inject(resourceV4Widget: ResourceV4Widget)

    fun inject(resourceNotesV3Widget: ResourceNotesV3Widget)

    fun inject(courseTimeTableWidgetV2: CourseTimeTableWidgetV2)

    fun inject(widget: MostViewedClassesWidget)

    fun inject(widget: FilterButtonWidget)

    fun inject(widget: FilterDropDownWidget)

    fun inject(widget: TeacherChannelWidget2)

    fun inject(widget: CourseCarouselWidget)

    fun inject(widget: ParentTabWidget3)

    fun inject(widget: ImageTextWidget)

    fun inject(widget: PreviousWinnersWidget)

    fun inject(widget: WinnersCardWidget)

    fun inject(widget: CopyTextWidget)

    fun inject(multiSelectSubjectFilterWidget: MultiSelectSubjectFilterWidget)

    fun inject(practiceEnglishWidget: PracticeEnglishWidget)

    fun inject(networkUsageStatsWorker: NetworkUsageStatsWorker)

    fun inject(srpNudgeCourseWidget: SrpNudgeCourseWidget)

    fun inject(subjectCourseCardWidget: SubjectCourseCardWidget)

    fun inject(widget: FreeTrialCourseWidget)

    fun inject(widget: GradientCardWithButtonWidget)

    fun inject(widget: NextScreenWidget)

    fun inject(widget: EnglishQuizInfoWidget)

    fun inject(widget: InviteFriendWidget)

    fun inject(widget: MatchPageExtraFeatureWidget)

    fun inject(bottomNav: BottomNavCustomView)

    fun inject(widget: ShortsVideosExhaustedWidget)

    fun inject(widget: ShortsVideoDefaultWidget)

    fun inject(widget: NoDataWidget)

    fun inject(widget: FilterSortWidget)

    fun inject(widget: TwoTextsHorizontalWidget)

    fun inject(widget: WatchAndWinWidget)

    fun inject(widget: TwoTextsVerticalTabsWidget)

    fun inject(widget: WatchNowWidget)

    fun inject(iasWidget: IASWidget)

    fun inject(d0QaWidget: D0QaWidget)

    fun inject(rcTestPapersWidget: RCTestPapersWidget)

    fun inject(rcPreviousYearPapersWidget: RCPreviousYearPapersWidget)

    fun inject(verticalParentWidget: VerticalParentWidget)

    fun inject(widget: ShortsVideoProgressWidget)

    fun inject(doubtPeCharchaQuestionWidget: DoubtPeCharchaQuestionWidget)

    fun inject(gradientBannerWithActionButtonWidget: GradientBannerWithActionButtonWidget)

    fun inject(badgesForLevelWidget: BadgesForLevelWidget)

    fun inject(userBadgeBannerWidget: UserBadgeBannerWidget)

    fun inject(widget: DoubtSuggesterWidget)

    fun inject(widget: MoreTestimonialsWidget)

    fun inject(widget: ToppersWidget)

    fun inject(widget: ExcelCoursesWidget)

    fun inject(exoPlayerHelper: ExoPlayerHelper)
}
