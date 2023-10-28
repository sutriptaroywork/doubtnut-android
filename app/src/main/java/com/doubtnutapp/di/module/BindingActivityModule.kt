package com.doubtnutapp.di.module

import com.doubtnut.core.di.scope.PerActivity
import com.doubtnut.core.di.scope.PerFragment
import com.doubtnut.noticeboard.di.NoticeBoardDetailActivityModule
import com.doubtnut.noticeboard.ui.NoticeBoardDetailActivity
import com.doubtnut.olympiad.di.OlympiadActivityModule
import com.doubtnut.olympiad.ui.OlympiadActivity
import com.doubtnut.referral.di.ReferAndEarnModule
import com.doubtnut.referral.ui.ReferAndEarnActivity
import com.doubtnut.scholarship.di.module.ScholarshipActivityModule
import com.doubtnut.scholarship.ui.ScholarshipActivity
import com.doubtnutapp.*
import com.doubtnutapp.auth.di.AuthModule
import com.doubtnutapp.auth.ui.GoogleAuthActivity
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.camera.ui.CropQuestionActivity
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.course.SchedulerListingActivity
import com.doubtnutapp.course.di.SchedulerListingActivityModule
import com.doubtnutapp.dictionary.DictionaryActivity
import com.doubtnutapp.dictionary.di.DictionaryActivityModule
import com.doubtnutapp.dnr.di.DnrActivityModule
import com.doubtnutapp.dnr.di.DnrRewardModule
import com.doubtnutapp.dnr.ui.activity.DnrActivity
import com.doubtnutapp.doubtfeed2.reward.receiver.RewardNotificationHandlerActivity
import com.doubtnutapp.doubtpecharcha.di.*
import com.doubtnutapp.doubtpecharcha.ui.activity.*
import com.doubtnutapp.doubtplan.DoubtPackageActivity
import com.doubtnutapp.downloadedVideos.ApbCashPaymentModule
import com.doubtnutapp.downloadedVideos.CategorySearchModule
import com.doubtnutapp.downloadedVideos.DownloadVideoActivityModule
import com.doubtnutapp.downloadedVideos.DownloadedVideosActivity
import com.doubtnutapp.dummy.DummyModule
import com.doubtnutapp.examcorner.ExamCornerActivity
import com.doubtnutapp.examcorner.ExamCornerBookmarkActivity
import com.doubtnutapp.examcorner.di.ExamCornerBookmarkModule
import com.doubtnutapp.fallbackquiz.ui.QuizFallbackActivity
import com.doubtnutapp.faq.ui.FaqActivity
import com.doubtnutapp.feed.dimodule.CreatePostActivityModule
import com.doubtnutapp.feed.dimodule.OneTapPostsListModule
import com.doubtnutapp.feed.dimodule.TopIconsModule
import com.doubtnutapp.feed.dimodule.UnbanActivityModule
import com.doubtnutapp.feed.view.*
import com.doubtnutapp.gamification.badgesscreen.di.BadgesActivityModule
import com.doubtnutapp.gamification.badgesscreen.ui.BadgesActivity
import com.doubtnutapp.gamification.dailyattendance.di.DailyAttendanceActivityModule
import com.doubtnutapp.gamification.dailyattendance.ui.DailyAttendanceActivity
import com.doubtnutapp.gamification.earnedPointsHistory.di.EarnedPointsHistoryActivityModule
import com.doubtnutapp.gamification.earnedPointsHistory.ui.EarnedPointsHistoryActivity
import com.doubtnutapp.gamification.friendbadgesscreen.di.FriendBadgesActivityModule
import com.doubtnutapp.gamification.friendbadgesscreen.ui.FriendBadgesActivity
import com.doubtnutapp.gamification.gamepoints.di.GamePointsActivityModule
import com.doubtnutapp.gamification.gamepoints.ui.GamePointsActivity
import com.doubtnutapp.gamification.leaderboard.di.GameLeaderActivityModule
import com.doubtnutapp.gamification.leaderboard.ui.GameLeaderBoardActivity
import com.doubtnutapp.gamification.mybio.di.UserBioActivityModule
import com.doubtnutapp.gamification.mybio.ui.MyBioActivity
import com.doubtnutapp.gamification.otheruserprofile.di.OthersProfileActivityModule
import com.doubtnutapp.gamification.otheruserprofile.ui.OthersProfileActivity
import com.doubtnutapp.gamification.settings.profilesetting.di.ProfileSettingsBindModule
import com.doubtnutapp.gamification.settings.profilesetting.ui.ProfileSettingActivity
import com.doubtnutapp.gamification.settings.settingdetail.di.SettingDetailBindModule
import com.doubtnutapp.gamification.settings.settingdetail.ui.SettingDetailActivity
import com.doubtnutapp.home.dimodule.HomeFragmentProvider
import com.doubtnutapp.home.dimodule.MainActivityModule
import com.doubtnutapp.inappupdate.ui.ImmediateUpdateActivity
import com.doubtnutapp.leaderboard.di.LeaderboardActivityModule
import com.doubtnutapp.leaderboard.ui.activity.LeaderboardActivity
import com.doubtnutapp.libraryhome.course.ui.VipDetailActivity
import com.doubtnutapp.libraryhome.coursev3.di.CourseActivityModuleV3
import com.doubtnutapp.libraryhome.coursev3.di.CourseDetailActivityModuleV3
import com.doubtnutapp.libraryhome.coursev3.di.SubjectDetailActivityModule
import com.doubtnutapp.libraryhome.coursev3.di.TimetableActivityModule
import com.doubtnutapp.libraryhome.coursev3.ui.*
import com.doubtnutapp.librarylisting.di.LibraryListingActivityModule
import com.doubtnutapp.librarylisting.di.LibraryListingFragmentModuleProvider
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.live.di.LiveActivityModule
import com.doubtnutapp.live.ui.LiveActivity
import com.doubtnutapp.live.ui.VerifyProfileActivity
import com.doubtnutapp.liveclass.di.*
import com.doubtnutapp.liveclass.ui.*
import com.doubtnutapp.liveclass.ui.practice_english.PracticeEnglishActivity
import com.doubtnutapp.liveclassreminder.LiveClassReminderActivity
import com.doubtnutapp.login.di.LoginActivityModule
import com.doubtnutapp.login.di.LoginActivityProviderModule
import com.doubtnutapp.login.ui.activity.AnonymousLoginActivity
import com.doubtnutapp.login.ui.activity.LanguageActivity
import com.doubtnutapp.login.ui.activity.StudentLoginActivity
import com.doubtnutapp.matchquestion.di.CropQuestionActivityModule
import com.doubtnutapp.matchquestion.di.MatchQuestionActivityModule
import com.doubtnutapp.matchquestion.di.TYDActivityModule
import com.doubtnutapp.matchquestion.ui.activity.MatchQuestionActivity
import com.doubtnutapp.matchquestion.ui.activity.NoInternetRetryActivity
import com.doubtnutapp.matchquestion.ui.activity.OcrEditActivity
import com.doubtnutapp.matchquestion.ui.activity.TYDActivity
import com.doubtnutapp.mathview.MathViewActivity
import com.doubtnutapp.networkstats.di.NetworkStatsActivityModule
import com.doubtnutapp.networkstats.ui.NetworkStatsActivity
import com.doubtnutapp.newglobalsearch.di.InAppSearchActivityModule
import com.doubtnutapp.newglobalsearch.di.TypeYourDoubtActivityModule
import com.doubtnutapp.newglobalsearch.ui.InAppSearchActivity
import com.doubtnutapp.newglobalsearch.ui.TypeYourDoubtActivity
import com.doubtnutapp.newlibrary.di.LibraryPreviousYearPapersModule
import com.doubtnutapp.newlibrary.ui.LibraryPreviousYearPapersActivity
import com.doubtnutapp.notification.NotificationCenterActivity
import com.doubtnutapp.notification.di.NotificationCenterActivityModule
import com.doubtnutapp.payment.ApbCashPaymentActivity
import com.doubtnutapp.payment.ApbLocationActivity
import com.doubtnutapp.payment.di.DoubtPackageActivityModule
import com.doubtnutapp.payment.ui.PaymentStatusActivity
import com.doubtnutapp.paymentplan.di.PaymentPlanActivityModule
import com.doubtnutapp.paymentplan.ui.PaymentPlanActivity
import com.doubtnutapp.pcmunlockpopup.di.PCMUnlockPopActivityModule
import com.doubtnutapp.pcmunlockpopup.ui.PCMUnlockPopActivity
import com.doubtnutapp.profile.social.CommunityGuidelinesActivity
import com.doubtnutapp.profile.social.ReportUserActivity
import com.doubtnutapp.profile.social.UserRelationshipsActivity
import com.doubtnutapp.profile.uservideohistroy.di.UserWatchedVideoBindModule
import com.doubtnutapp.profile.uservideohistroy.ui.UserWatchedVideoActivity
import com.doubtnutapp.qrpayment.QrPaymentActivity
import com.doubtnutapp.qrpayment.di.QrPaymentActivityModule
import com.doubtnutapp.quicksearch.QuickSearchSettingActivity
import com.doubtnutapp.quicksearch.di.NotificationSettingActivityModule
import com.doubtnutapp.quiztfs.di.*
import com.doubtnutapp.quiztfs.ui.*
import com.doubtnutapp.resultpage.di.ResultPageActivityModule
import com.doubtnutapp.resultpage.ui.ResultPageActivity
import com.doubtnutapp.reward.di.RewardModule
import com.doubtnutapp.reward.ui.RewardActivity
import com.doubtnutapp.scheduledquiz.ui.ScheduledQuizNotificationActivity
import com.doubtnutapp.scheduledquiz.viewmodel.FallbackQuizModule
import com.doubtnutapp.scheduledquiz.viewmodel.ScheduledQuizNotificationModule
import com.doubtnutapp.socket.di.SocketManagerModule
import com.doubtnutapp.store.di.ConvertCoinsActivityModule
import com.doubtnutapp.store.di.MyOrderActivityModule
import com.doubtnutapp.store.di.StoreActivityModule
import com.doubtnutapp.store.ui.ConvertCoinsActivity
import com.doubtnutapp.store.ui.MyOrderActivity
import com.doubtnutapp.store.ui.StoreActivity
import com.doubtnutapp.studygroup.di.CreateSgFragmentModule
import com.doubtnutapp.studygroup.di.StudyGroupChatModule
import com.doubtnutapp.studygroup.di.StudyGroupListModule
import com.doubtnutapp.studygroup.di.UpdateSgInfoActivityModule
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity
import com.doubtnutapp.studygroup.ui.activity.UpdateSgImageActivity
import com.doubtnutapp.studygroup.ui.activity.UpdateSgNameActivity
import com.doubtnutapp.studygroup.ui.fragment.SgShareActivity
import com.doubtnutapp.survey.di.UserSurveyModule
import com.doubtnutapp.teacherchannel.TeacherChannelActivity
import com.doubtnutapp.teacherchannel.di.TeacherChannelModule
import com.doubtnutapp.textsolution.di.TextSolutionActivityBindingModule
import com.doubtnutapp.textsolution.di.TextSolutionActivityProviderModule
import com.doubtnutapp.textsolution.ui.TextSolutionActivity
import com.doubtnutapp.topicboostergame.di.TopicBoosterGameModule
import com.doubtnutapp.topicboostergame.ui.TopicBoosterGameActivity
import com.doubtnutapp.topicboostergame2.di.TopicBoosterGameModule2
import com.doubtnutapp.topicboostergame2.ui.TopicBoosterGameActivity2
import com.doubtnutapp.transactionhistory.TransactionHistoryActivityV2
import com.doubtnutapp.transactionhistory.di.TransactionHistoryActivityModule
import com.doubtnutapp.ui.FragmentHolderActivity
import com.doubtnutapp.ui.TransparentActivity
import com.doubtnutapp.ui.browser.HandleActionWebViewActivity
import com.doubtnutapp.ui.cameraGuide.CameraGuideActivity
import com.doubtnutapp.ui.cameraGuide.di.CameraGuideModule
import com.doubtnutapp.ui.course.microconcept.MicroconceptsActivity
import com.doubtnutapp.ui.course.microconcept.di.MicroConceptActivityModule
import com.doubtnutapp.ui.di.FragmentHolderActivityModule
import com.doubtnutapp.ui.downloadPdf.DownloadNShareActivity
import com.doubtnutapp.ui.downloadPdf.DownloadNShareLevelOneActivity
import com.doubtnutapp.ui.downloadPdf.DownloadNShareLevelTwoActivity
import com.doubtnutapp.ui.downloadPdf.di.DownloadPdfModule
import com.doubtnutapp.ui.formulaSheet.*
import com.doubtnutapp.ui.formulaSheet.di.FormulaSheetModule
import com.doubtnutapp.ui.forum.comments.CommentsActivity
import com.doubtnutapp.ui.forum.comments.CommentsActivityModule
import com.doubtnutapp.ui.games.DnGamesActivity
import com.doubtnutapp.ui.games.GamePlayerActivity
import com.doubtnutapp.ui.games.GamesModule
import com.doubtnutapp.ui.groupChat.GroupChatActivity
import com.doubtnutapp.ui.groupChat.GroupChatModule
import com.doubtnutapp.ui.groupChat.LiveChatActivity
import com.doubtnutapp.ui.likeuserlist.LikedUserListActivity
import com.doubtnutapp.ui.likeuserlist.LikedUserListActivityModule
import com.doubtnutapp.ui.main.demoanimation.DemoAnimationActivity
import com.doubtnutapp.ui.main.di.CameraActivityBindModule
import com.doubtnutapp.ui.main.di.CameraActivityProvideModule
import com.doubtnutapp.ui.mockTest.*
import com.doubtnutapp.ui.mockTest.di.MockTestAnalysisModule
import com.doubtnutapp.ui.mockTest.di.MockTestCommonModule
import com.doubtnutapp.ui.mockTest.di.MockTestSubscriptionModule
import com.doubtnutapp.ui.mypdf.MyPdfActivity
import com.doubtnutapp.ui.mypdf.MyPdfActivityModule
import com.doubtnutapp.ui.onboarding.OnBoardingStepsActivity
import com.doubtnutapp.ui.onboarding.di.OnBoardingStepsActivityModule
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.ui.pdfviewer.di.PdfViewerModule
import com.doubtnutapp.ui.questionAskedHistory.QuestionAskedHistoryActivity
import com.doubtnutapp.ui.questionAskedHistory.QuestionAskedHistoryActivityModule
import com.doubtnutapp.ui.quiz.QuizNotificationActivity
import com.doubtnutapp.ui.splash.SplashActivity
import com.doubtnutapp.ui.splash.dimodule.SplashActivityModule
import com.doubtnutapp.ui.test.QuizActivity
import com.doubtnutapp.ui.test.TestQuestionActivity
import com.doubtnutapp.ui.test.dimodule.QuizActivityModule
import com.doubtnutapp.ui.test.dimodule.TestQuestionActivityModule
import com.doubtnutapp.ui.topperStudyPlan.ChapterDetailActivity
import com.doubtnutapp.ui.topperStudyPlan.TopperStudyPlanActivity
import com.doubtnutapp.ui.topperStudyPlan.TopperStudyPlanModule
import com.doubtnutapp.ui.userstatus.CreateStatusActivity
import com.doubtnutapp.ui.userstatus.StatusDetailActivity
import com.doubtnutapp.video.VideoImageSummaryActivityDialog
import com.doubtnutapp.videoPage.di.VideoPageActivityBindModule
import com.doubtnutapp.videoPage.di.VideoPageActivityProvideModule
import com.doubtnutapp.videoPage.ui.FullScreenVideoPageActivity
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.vipplan.di.MyPlanActivityModule
import com.doubtnutapp.vipplan.di.PaymentHelpActivityModule
import com.doubtnutapp.vipplan.di.VipPlanActivityModule
import com.doubtnutapp.vipplan.ui.MyPlanActivity
import com.doubtnutapp.vipplan.ui.PaymentHelpActivity
import com.doubtnutapp.vipplan.ui.VipPlanActivity
import com.doubtnutapp.wallet.WalletActivity
import com.doubtnutapp.whatsappadmin.WhatsappAdminActivity
import com.doubtnutapp.whatsappadmin.WhatsappAdminActivityModule
import com.doubtnutapp.widgettest.di.ApiTestModule
import com.doubtnutapp.widgettest.ui.ApiTestActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BindingActivityModule {

    @ContributesAndroidInjector(modules = [HomeFragmentProvider::class, MainActivityModule::class, UserSurveyModule::class])
    @PerActivity
    internal abstract fun contributeMainActivityInjector(): MainActivity

    @ContributesAndroidInjector(modules = [SplashActivityModule::class])
    @PerActivity
    internal abstract fun contributeSplashActivity(): SplashActivity

    @ContributesAndroidInjector(modules = [TestQuestionActivityModule::class])
    internal abstract fun contributeTestQuestionActivity(): TestQuestionActivity

    @ContributesAndroidInjector(modules = [DictionaryActivityModule::class])
    internal abstract fun contributeDictionaryActivity(): DictionaryActivity

    @ContributesAndroidInjector(modules = [MicroConceptActivityModule::class])
    @PerActivity
    internal abstract fun contributeMicroConceptActivity(): MicroconceptsActivity

    @ContributesAndroidInjector(modules = [MatchQuestionActivityModule::class])
    @PerActivity
    internal abstract fun contributeMatchQuestionActivity(): MatchQuestionActivity

    @ContributesAndroidInjector(modules = [FragmentHolderActivityModule::class])
    @PerActivity
    internal abstract fun contributeFragmentHolderActivityInjector(): FragmentHolderActivity

    @ContributesAndroidInjector(modules = [UserWatchedVideoBindModule::class])
    @PerActivity
    internal abstract fun contributeUserWatchedVideosActivity(): UserWatchedVideoActivity

    @ContributesAndroidInjector(modules = [BadgesActivityModule::class])
    @PerActivity
    internal abstract fun contributeBadgesActivity(): BadgesActivity

    @ContributesAndroidInjector(modules = [FriendBadgesActivityModule::class])
    @PerActivity
    internal abstract fun contributeFriendBadgesActivity(): FriendBadgesActivity

    @ContributesAndroidInjector(modules = [GamePointsActivityModule::class])
    @PerActivity
    internal abstract fun contributeGamePointsActivity(): GamePointsActivity

    @ContributesAndroidInjector(modules = [GameLeaderActivityModule::class])
    @PerActivity
    internal abstract fun contributeGameLeaderBoardActivity(): GameLeaderBoardActivity

    @ContributesAndroidInjector(modules = [SettingDetailBindModule::class])
    @PerActivity
    internal abstract fun contributeSettingDetailActivity(): SettingDetailActivity

    @ContributesAndroidInjector(
        modules = [
            VideoPageActivityBindModule::class,
            VideoPageActivityProvideModule::class,
            DnrRewardModule::class
        ]
    )
    @PerActivity
    internal abstract fun contributeVideoPageActivity(): VideoPageActivity

    @ContributesAndroidInjector(modules = [TextSolutionActivityBindingModule::class, TextSolutionActivityProviderModule::class])
    @PerActivity
    internal abstract fun contributeTextSolutionActivity(): TextSolutionActivity

    @ContributesAndroidInjector(modules = [OthersProfileActivityModule::class])
    @PerActivity
    internal abstract fun contributeOthersProfileActivity(): OthersProfileActivity

    @ContributesAndroidInjector(modules = [ProfileSettingsBindModule::class, CreateSgFragmentModule::class])
    @PerActivity
    internal abstract fun contributeProfileSettingActivity(): ProfileSettingActivity

    @ContributesAndroidInjector(modules = [PCMUnlockPopActivityModule::class])
    @PerActivity
    internal abstract fun contributePCMUnlockPopActivity(): PCMUnlockPopActivity

    @ContributesAndroidInjector(modules = [FormulaSheetModule::class])
    @PerActivity
    internal abstract fun contributeCheatSheetActivity(): CheatSheetActivity

    @ContributesAndroidInjector(modules = [FormulaSheetModule::class])
    @PerActivity
    internal abstract fun contributeCheatSheetFormulaListActivity(): CheatSheetFormulaListActivity

    @ContributesAndroidInjector(modules = [FormulaSheetModule::class])
    @PerActivity
    internal abstract fun contributeFormulaSheetChapterActivity(): FormulaSheetChapterActivity

    @ContributesAndroidInjector(modules = [FormulaSheetModule::class])
    @PerActivity
    internal abstract fun contributeFormulaSheetFormulasActivity(): FormulaSheetFormulasActivity

    @ContributesAndroidInjector(modules = [FormulaSheetModule::class])
    @PerActivity
    internal abstract fun contributeFormulaSheetGlobalSearchActivity(): FormulaSheetGlobalSearchActivity

    @ContributesAndroidInjector(modules = [FormulaSheetModule::class])
    @PerActivity
    internal abstract fun contributeFormulaSheetTopicActivity(): FormulaSheetTopicActivity

    @ContributesAndroidInjector(modules = [FormulaSheetModule::class])
    @PerActivity
    internal abstract fun contributeGlobalSearchFormulasActivity(): GlobalSearchFormulasActivity

    @ContributesAndroidInjector(modules = [MockTestCommonModule::class])
    @PerActivity
    internal abstract fun contributeMockTestListActivity(): MockTestListActivity

    @ContributesAndroidInjector(modules = [MockTestCommonModule::class])
    @PerActivity
    internal abstract fun contributeMockTestSyllabusActivity(): MockTestSyllabusActivity

    @ContributesAndroidInjector(modules = [MockTestCommonModule::class])
    @PerActivity
    internal abstract fun contributeMockTestActivity(): MockTestActivity

    @ContributesAndroidInjector(modules = [MockTestCommonModule::class])
    @PerActivity
    internal abstract fun contributeMockTestSectionActivity(): MockTestSectionActivity

    @ContributesAndroidInjector(modules = [DownloadPdfModule::class])
    @PerActivity
    internal abstract fun contributeDownloadNShareActivity(): DownloadNShareActivity

    @ContributesAndroidInjector(modules = [DownloadPdfModule::class, PdfViewerModule::class])
    @PerActivity
    internal abstract fun contributeDownloadNShareLevelOneActivity(): DownloadNShareLevelOneActivity

    @ContributesAndroidInjector(modules = [DownloadPdfModule::class, PdfViewerModule::class])
    @PerActivity
    internal abstract fun contributeDownloadNShareLevelTwoActivity(): DownloadNShareLevelTwoActivity

    @ContributesAndroidInjector(modules = [DownloadPdfModule::class, PdfViewerModule::class])
    @PerActivity
    internal abstract fun contributePdfViewerActivity(): PdfViewerActivity

    @ContributesAndroidInjector(modules = [CommentsActivityModule::class])
    @PerActivity
    internal abstract fun contributeCommentsActivity(): CommentsActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeActionHandlerActivity(): ActionHandlerActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeTransparentActivity(): TransparentActivity

    @ContributesAndroidInjector(modules = [CameraGuideModule::class])
    @PerActivity
    internal abstract fun contributeCameraGuideActivity(): CameraGuideActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeQuizNotificationActivity(): QuizNotificationActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeVideoHolderActivity(): VideoHolderActivity

    @ContributesAndroidInjector(modules = [LibraryListingFragmentModuleProvider::class, LibraryListingActivityModule::class])
    @PerActivity
    internal abstract fun contributeLibraryListActivity(): LibraryListingActivity

    @ContributesAndroidInjector(modules = [InAppSearchActivityModule::class])
    @PerActivity
    internal abstract fun contributeInAppSearchActivity(): InAppSearchActivity

    @ContributesAndroidInjector(modules = [WhatsappAdminActivityModule::class])
    @PerActivity
    internal abstract fun contributeWhatsappAdminActivity(): WhatsappAdminActivity

    @ContributesAndroidInjector(modules = [TypeYourDoubtActivityModule::class])
    @PerActivity
    internal abstract fun contributeTypeYourDoubtActivity(): TypeYourDoubtActivity

    @ContributesAndroidInjector(modules = [UserBioActivityModule::class])
    @PerActivity
    internal abstract fun contributeUserBioActivity(): MyBioActivity

    @ContributesAndroidInjector(modules = [EarnedPointsHistoryActivityModule::class])
    @PerActivity
    internal abstract fun contributeEarnedPointsHistoryActivity(): EarnedPointsHistoryActivity

    @ContributesAndroidInjector(modules = [StoreActivityModule::class])
    @PerActivity
    internal abstract fun contributeStoreActivity(): StoreActivity

    @ContributesAndroidInjector(modules = [MyOrderActivityModule::class])
    @PerActivity
    internal abstract fun contributeMyOrderActivity(): MyOrderActivity

    @ContributesAndroidInjector(modules = [ConvertCoinsActivityModule::class])
    @PerActivity
    internal abstract fun contributeConvertCoinsActivity(): ConvertCoinsActivity

    @ContributesAndroidInjector(modules = [DailyAttendanceActivityModule::class])
    @PerActivity
    internal abstract fun contributeDailyAttendanceActivity(): DailyAttendanceActivity

    @ContributesAndroidInjector(modules = [com.doubtnutapp.paymentv2.PaymentActivityModule::class])
    @PerActivity
    internal abstract fun contributePaymentActivityv2(): com.doubtnutapp.paymentv2.ui.PaymentActivity

    @ContributesAndroidInjector(modules = [CameraActivityBindModule::class, CameraActivityProvideModule::class])
    @PerActivity
    internal abstract fun contributeImageIntentHandlerInjector(): ImageIntentHandlerActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeFullScreenVideoPageActivity(): FullScreenVideoPageActivity

    @ContributesAndroidInjector(modules = [CourseActivityModuleV3::class])
    @PerActivity
    internal abstract fun contributeCourseActivityV2(): CourseActivityV3

    @ContributesAndroidInjector(modules = [CourseActivityModuleV3::class])
    @PerActivity
    internal abstract fun contributeCourseActivityBottomSheet(): CourseActivityBottomSheet

    @ContributesAndroidInjector(modules = [VipPlanActivityModule::class, CreateSgFragmentModule::class])
    @PerActivity
    internal abstract fun contributeVipPlanActivity(): VipPlanActivity

    @ContributesAndroidInjector(modules = [CreatePostActivityModule::class])
    @PerActivity
    internal abstract fun contributeCreatePostActivity(): CreatePostActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeFragmentWrapperActivity(): FragmentWrapperActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributePostDetailActivity(): PostDetailActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeTopicFeedActivity(): TopicFeedActivity

    @ContributesAndroidInjector(modules = [CourseDetailActivityModuleV3::class])
    @PerActivity
    internal abstract fun contributeCourseDetailActivityV2(): CourseDetailActivityV3

    @ContributesAndroidInjector(modules = [GroupChatModule::class])
    @PerActivity
    internal abstract fun contributeGroupChatActivity(): GroupChatActivity

    @ContributesAndroidInjector(modules = [GroupChatModule::class])
    @PerActivity
    internal abstract fun contributeLiveChatActivity(): LiveChatActivity

    @ContributesAndroidInjector(modules = [OnBoardingStepsActivityModule::class])
    @PerActivity
    internal abstract fun contributeOnBoardingActivityV3(): OnBoardingStepsActivity

    @ContributesAndroidInjector(modules = [GamesModule::class])
    @PerActivity
    internal abstract fun contributeDnGamesActivity(): DnGamesActivity

    @ContributesAndroidInjector(modules = [GamesModule::class])
    @PerActivity
    internal abstract fun contributeGamesPlayerActivity(): GamePlayerActivity

    @ContributesAndroidInjector(
        modules = [
            LiveClassActivityModule::class,
            DnrRewardModule::class
        ]
    )
    @PerActivity
    internal abstract fun contributeLiveClassActivity(): LiveClassActivity

    @ContributesAndroidInjector(modules = [MockTestSubscriptionModule::class])
    @PerActivity
    internal abstract fun contributeMockTestSubscriptionActivity(): MockTestSubscriptionActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeLanguageActivity(): LanguageActivity

    @ContributesAndroidInjector(modules = [LoginActivityModule::class, LoginActivityProviderModule::class])
    @PerActivity
    internal abstract fun contributeStudentLoginActivity(): StudentLoginActivity

    @ContributesAndroidInjector(modules = [TopperStudyPlanModule::class])
    @PerActivity
    internal abstract fun contributeTopperStudyPlanActivity(): TopperStudyPlanActivity

    @ContributesAndroidInjector(modules = [TopperStudyPlanModule::class])
    @PerActivity
    internal abstract fun contributeChapterDetailActivity(): ChapterDetailActivity

    @ContributesAndroidInjector(modules = [CameraActivityBindModule::class, CameraActivityProvideModule::class])
    @PerActivity
    internal abstract fun contributeDemoAnimationActivityInjector(): DemoAnimationActivity

    @ContributesAndroidInjector(modules = [LiveActivityModule::class])
    @PerActivity
    internal abstract fun contributeLivePostActivity(): LiveActivity

    @ContributesAndroidInjector(modules = [NotificationCenterActivityModule::class])
    @PerActivity
    internal abstract fun contributeNotificationActivity(): NotificationCenterActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeLiveFeedActivity(): LiveFeedActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeVerifyProfileActivity(): VerifyProfileActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeVipDetailActivity(): VipDetailActivity

    @ContributesAndroidInjector(modules = [CameraActivityBindModule::class, CameraActivityProvideModule::class, MatchQuestionActivityModule::class])
    @PerActivity
    internal abstract fun contributeCameraActivityInjector(): CameraActivity

    @ContributesAndroidInjector(modules = [QuestionAskedHistoryActivityModule::class])
    @PerActivity
    internal abstract fun contributeQuestionAskedHistoryActivity(): QuestionAskedHistoryActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeLReportUserActivity(): ReportUserActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeLiveClassReminderActivity(): LiveClassReminderActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeNoInternetRetryActivity(): NoInternetRetryActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeImagePagerActivity(): ImagesPagerActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeCommunityGuidelinesActivity(): CommunityGuidelinesActivity

    @ContributesAndroidInjector(modules = [TimetableActivityModule::class])
    @PerActivity
    internal abstract fun contributeTimeTableActivity(): TimetableActivity

    @ContributesAndroidInjector(modules = [LiveClassChatActivityModule::class])
    @PerActivity
    internal abstract fun contributeLiveClassChatActivity(): LiveClassChatActivity

    @ContributesAndroidInjector(modules = [LiveClassChatActivityModule::class])
    @PerActivity
    internal abstract fun contributeImageSelectionActivity(): ImageCaptionActivity

    @ContributesAndroidInjector(modules = [MyPlanActivityModule::class])
    @PerActivity
    internal abstract fun contributeMyPlanActivity(): MyPlanActivity

    @ContributesAndroidInjector(modules = [CropQuestionActivityModule::class, MatchQuestionActivityModule::class])
    @PerActivity
    internal abstract fun contributeCropQuestionActivity(): CropQuestionActivity

    @ContributesAndroidInjector(modules = [DoubtPackageActivityModule::class])
    @PerActivity
    internal abstract fun contributeDoubtPackageActivity(): DoubtPackageActivity

    @ContributesAndroidInjector(modules = [ResourceListActivityModule::class])
    @PerActivity
    internal abstract fun contributeExploreActivity(): ResourceListActivity

    @ContributesAndroidInjector(modules = [VipPlanActivityModule::class])
    @PerActivity
    internal abstract fun contributePaymentStatusActivity(): PaymentStatusActivity

    @ContributesAndroidInjector(modules = [TYDActivityModule::class])
    @PerFragment
    internal abstract fun contributeTYDActivity(): TYDActivity

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeWalletActivity(): WalletActivity

    @ContributesAndroidInjector(modules = [DownloadVideoActivityModule::class])
    @PerActivity
    internal abstract fun contributeDownloadedVideosActivity(): DownloadedVideosActivity

    @ContributesAndroidInjector(modules = [TYDActivityModule::class])
    @PerActivity
    internal abstract fun contributeOcrEditActivity(): OcrEditActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeCreateStatusActivity(): CreateStatusActivity

    @ContributesAndroidInjector(modules = [UnbanActivityModule::class])
    @PerActivity
    internal abstract fun contributeUnbannedRequestActivity(): UnbannedRequestActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeStatusDetailActivity(): StatusDetailActivity

    @ContributesAndroidInjector(modules = [ApbCashPaymentModule::class])
    @PerActivity
    internal abstract fun contributeApbCashPaymentActivity(): ApbCashPaymentActivity

    @ContributesAndroidInjector(modules = [ApbLocationModule::class])
    @PerActivity
    internal abstract fun contributeApbLocationActivity(): ApbLocationActivity

    @ContributesAndroidInjector(modules = [HomeWorkModule::class])
    @PerActivity
    internal abstract fun contributeHomeWorkActivity(): HomeWorkActivity

    @ContributesAndroidInjector(modules = [HomeWorkModule::class])
    @PerActivity
    internal abstract fun contributeHomeWorkSolutionActivity(): HomeWorkSolutionActivity

    @ContributesAndroidInjector(modules = [HomeWorkModule::class])
    @PerActivity
    internal abstract fun contributeMyHomeWorkActivity(): MyHomeWorkActivity

    @ContributesAndroidInjector(modules = [TopIconsModule::class])
    @PerActivity
    internal abstract fun contributeTopIconsActivity(): TopIconsActivity

    @ContributesAndroidInjector(modules = [CategorySearchModule::class])
    @PerActivity
    internal abstract fun contributeCategorySearchActivity(): CategorySearchActivity

    @ContributesAndroidInjector(modules = [ScheduledQuizNotificationModule::class])
    @PerActivity
    internal abstract fun contributeScheduleNotificationActivity(): ScheduledQuizNotificationActivity

    @ContributesAndroidInjector(modules = [FallbackQuizModule::class])
    @PerActivity
    internal abstract fun contributeQuizFallbackActivity(): QuizFallbackActivity

    @ContributesAndroidInjector(modules = [QrPaymentActivityModule::class])
    @PerActivity
    internal abstract fun contributeQrPaymentActivity(): QrPaymentActivity

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule::class])
    @PerActivity
    internal abstract fun contributeTopicBoosterGameActivity(): TopicBoosterGameActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeAudioPlayerActivity(): AudioPlayerActivity

    @ContributesAndroidInjector(modules = [FaqActivityModule::class])
    @PerActivity
    internal abstract fun contributeFaqActivity(): FaqActivity

    @ContributesAndroidInjector(modules = [RewardModule::class])
    @PerActivity
    internal abstract fun contributeRewardActivity(): RewardActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeStoryDetailActivity(): StoryDetailActivity

    @ContributesAndroidInjector(modules = [SubjectDetailActivityModule::class])
    @PerActivity
    internal abstract fun contributeSubjectDetailActivity(): SubjectDetailActivity

    @ContributesAndroidInjector(modules = [VideoPageActivityBindModule::class, VideoPageActivityProvideModule::class])
    @PerActivity
    internal abstract fun contributeVideoDialogHolderActivity(): VideoDialogActivity

    @ContributesAndroidInjector(modules = [TransactionHistoryActivityModule::class])
    @PerActivity
    internal abstract fun contributeTransactionHistoryV2Activity(): TransactionHistoryActivityV2

    @ContributesAndroidInjector(modules = [DoubtP2pActivityModule::class, SocketManagerModule::class])
    @PerActivity
    internal abstract fun contributeDoubtP2pActivity(): DoubtP2pActivity

    @ContributesAndroidInjector(modules = [P2PDoubtCollectionActivityModule::class])
    @PerActivity
    internal abstract fun contributeP2PDoubtCollectionActivity(): P2PDoubtCollectionActivity

    @ContributesAndroidInjector(modules = [PaymentHelpActivityModule::class])
    @PerActivity
    internal abstract fun contributePaymentHelpActivity(): PaymentHelpActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributePaymentHelpActivity2(): com.doubtnutapp.payment.ui.PaymentHelpActivity

    @ContributesAndroidInjector(modules = [StudyGroupChatModule::class, SocketManagerModule::class])
    @PerActivity
    internal abstract fun contributeStudyGroupActivity(): StudyGroupActivity

    @ContributesAndroidInjector(modules = [StudyGroupChatModule::class, StudyGroupListModule::class])
    @PerActivity
    internal abstract fun contributeSgShareBottomSheetFragment(): SgShareActivity

    @ContributesAndroidInjector(modules = [NoticeBoardDetailActivityModule::class])
    @PerActivity
    internal abstract fun contributeNoticeBoardDetailActivity(): NoticeBoardDetailActivity

    @ContributesAndroidInjector(modules = [NotificationSettingActivityModule::class])
    @PerActivity
    internal abstract fun contributeQuickSearchSettingActivity(): QuickSearchSettingActivity

    @ContributesAndroidInjector(modules = [DoubtP2pHelperEntryActivityModule::class])
    @PerActivity
    internal abstract fun contributeDoubtP2pHelperEntryActivity(): DoubtP2pHelperEntryActivity

    @ContributesAndroidInjector(modules = [ChangeCourseActivityModule::class])
    @PerActivity
    internal abstract fun contributeChangeCourseActivity(): CourseSelectActivity

    @ContributesAndroidInjector(modules = [ChangeCourseActivityModule::class])
    @PerActivity
    internal abstract fun contributeCourseListActivity(): RecommendedCourseActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeHandleActionWebViewActivity(): HandleActionWebViewActivity

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule2::class])
    @PerActivity
    internal abstract fun contributeTopicBoosterGameActivity2(): TopicBoosterGameActivity2

    @ContributesAndroidInjector(modules = [LoginActivityModule::class, LoginActivityProviderModule::class])
    @PerActivity
    internal abstract fun contributeAnonymousLoginActivity(): AnonymousLoginActivity

    @ContributesAndroidInjector(modules = [LeaderboardActivityModule::class])
    @PerActivity
    internal abstract fun contributeLeaderboardActivity(): LeaderboardActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeCourseRecommendationActivity(): CourseRecommendationActivity

    @ContributesAndroidInjector
    @PerActivity
    internal abstract fun contributeRewardNotificationHandlerActivity(): RewardNotificationHandlerActivity

    @ContributesAndroidInjector(modules = [UpdateSgInfoActivityModule::class])
    @PerActivity
    internal abstract fun contributeUpdateSgNameActivity(): UpdateSgNameActivity

    @ContributesAndroidInjector(modules = [UpdateSgInfoActivityModule::class])
    @PerActivity
    internal abstract fun contributeUpdateSgImageActivity(): UpdateSgImageActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeExamCornerActivity(): ExamCornerActivity

    @ContributesAndroidInjector(modules = [ExamCornerBookmarkModule::class])
    @PerActivity
    internal abstract fun contributeExamCornerBookmarkActivity(): ExamCornerBookmarkActivity

    @ContributesAndroidInjector(modules = [MockTestAnalysisModule::class])
    @PerActivity
    internal abstract fun contributeMockTestAnalysisActivity(): MockTestAnalysisActivity

    @ContributesAndroidInjector(modules = [ApiTestModule::class])
    @PerActivity
    internal abstract fun contributeApiTestActivity(): ApiTestActivity

    @ContributesAndroidInjector(modules = [QuizTfsModule::class])
    @PerActivity
    internal abstract fun contributeQuizTfsActivity(): QuizTfsActivity

    @ContributesAndroidInjector(modules = [QuizTfsSolutionModule::class])
    @PerActivity
    internal abstract fun contributeQuizTfsSolutionActivity(): QuizTfsSolutionActivity

    @ContributesAndroidInjector(modules = [QuizTfsAnalysisModule::class])
    @PerActivity
    internal abstract fun contributeQuizTfsAnalysisActivity(): QuizTfsAnalysisActivity

    @ContributesAndroidInjector(modules = [LiveQuestionsModule::class])
    @PerActivity
    internal abstract fun contributeLiveQuestionsActivity(): QuizTfsSelectionActivity

    @ContributesAndroidInjector(modules = [DailyPracticeModule::class])
    @PerActivity
    internal abstract fun contributeDailyPracticeActivity(): DailyPracticeActivity

    @ContributesAndroidInjector(modules = [HistoryModule::class])
    @PerActivity
    internal abstract fun contributeHistoryActivity(): HistoryActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeUserRelationshipsActivity(): UserRelationshipsActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeMathViewActivity(): MathViewActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeImageViewerActivity(): ImageViewerActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeImmediateUpdateActivity(): ImmediateUpdateActivity

    @ContributesAndroidInjector(modules = [MyPdfActivityModule::class])
    @PerActivity
    internal abstract fun contributeMyPdfActivity(): MyPdfActivity

    @ContributesAndroidInjector(modules = [LikedUserListActivityModule::class])
    @PerActivity
    internal abstract fun contributeLikedUserListActivity(): LikedUserListActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeNudgeActivity(): NudgeActivity

    @ContributesAndroidInjector(modules = [QuizActivityModule::class])
    @PerActivity
    internal abstract fun contributeQuizActivity(): QuizActivity

    @ContributesAndroidInjector(modules = [MyRewardsModule::class])
    @PerActivity
    internal abstract fun contributeMyRewardsActivity(): MyRewardsActivity

    @ContributesAndroidInjector(modules = [ScholarshipActivityModule::class])
    @PerActivity
    internal abstract fun contributeScholarshipActivity(): ScholarshipActivity

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeVideoImageSummaryActivityDialog(): VideoImageSummaryActivityDialog

    @ContributesAndroidInjector(modules = [TeacherChannelModule::class])
    @PerActivity
    internal abstract fun contributeTeacherChannelActivity(): TeacherChannelActivity

    @ContributesAndroidInjector(modules = [PaymentPlanActivityModule::class, CreateSgFragmentModule::class])
    @PerActivity
    internal abstract fun contributePaymentPlanActivity(): PaymentPlanActivity

    @ContributesAndroidInjector(modules = [AuthModule::class])
    @PerActivity
    internal abstract fun contributeGoogleAuthActivity(): GoogleAuthActivity

    @ContributesAndroidInjector(modules = [DnrActivityModule::class])
    @PerActivity
    internal abstract fun contributeDnrActivity(): DnrActivity

    @ContributesAndroidInjector(modules = [LibraryPreviousYearPapersModule::class])
    @PerActivity
    internal abstract fun contributeLibraryPreviousYearPapersActivity(): LibraryPreviousYearPapersActivity

    @ContributesAndroidInjector(modules = [SchedulerListingActivityModule::class])
    @PerActivity
    internal abstract fun contributeSchedulerListingActivity(): SchedulerListingActivity

    @ContributesAndroidInjector(modules = [OlympiadActivityModule::class])
    @PerActivity
    internal abstract fun contributeOlympiadActivity(): OlympiadActivity

    @ContributesAndroidInjector(modules = [PracticeEnglishActivityModule::class])
    @PerActivity
    internal abstract fun contributesPracticeEnglishActivity(): PracticeEnglishActivity

    @ContributesAndroidInjector(modules = [NetworkStatsActivityModule::class])
    @PerActivity
    internal abstract fun contributeNetworkStatsActivity(): NetworkStatsActivity

    @ContributesAndroidInjector(modules = [OneTapPostsListModule::class])
    @PerActivity
    internal abstract fun contributesOneTapPostsListActivity(): OneTapPostsListActivity

    @ContributesAndroidInjector(modules = [ReferAndEarnModule::class])
    @PerActivity
    internal abstract fun contributesReferAndEarnActivity(): ReferAndEarnActivity

    @ContributesAndroidInjector(modules = [DoubtPeCharchaRewardsModule::class])
    @PerActivity
    internal abstract fun contributesDoubtPeCharchaRewardsActivity(): DoubtPeCharchaRewardsActivity

    @ContributesAndroidInjector(modules = [DoubtPeCharchaUserFeedbackModule::class])
    @PerActivity
    internal abstract fun contributesUserFeedbackActivity(): UserFeedbackActivity

    @ContributesAndroidInjector(modules = [ResultPageActivityModule::class])
    @PerActivity
    internal abstract fun contributeResultPageActivity(): ResultPageActivity

}
