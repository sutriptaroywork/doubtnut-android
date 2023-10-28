package com.doubtnutapp.di.module

import com.doubtnut.core.di.scope.PerActivity
import com.doubtnut.core.di.scope.PerFragment
import com.doubtnut.core.dummy.CoreDummyModule
import com.doubtnut.noticeboard.di.NoticeBoardProfileFragmentModule
import com.doubtnut.noticeboard.ui.NoticeBoardDetailFragment
import com.doubtnut.noticeboard.ui.NoticeBoardProfileFragment
import com.doubtnut.olympiad.di.OlympiadRegisterFragmentModule
import com.doubtnut.olympiad.di.OlympiadSuccessFragmentModule
import com.doubtnut.olympiad.ui.OlympiadRegisterFragment
import com.doubtnut.olympiad.ui.OlympiadSuccessFragment
import com.doubtnutapp.*
import com.doubtnutapp.addtoplaylist.AddToPlaylistFragment
import com.doubtnutapp.addtoplaylist.di.AddToPlaylistModule
import com.doubtnutapp.appexitdialog.di.AppExitDialogModule
import com.doubtnutapp.appexitdialog.ui.AppExitDialogFragment
import com.doubtnutapp.bottomsheet.BaseWidgetPaginatedBottomSheetDialogFragment
import com.doubtnutapp.bottomsheet.BaseWidgetPaginatedBottomSheetDialogFragmentModule
import com.doubtnutapp.camera.ui.InvalidImageDialogFragment
import com.doubtnutapp.common.di.module.BookCallDialogFragmentModule
import com.doubtnutapp.common.ui.dialog.BookCallDialogFragment
import com.doubtnutapp.dnr.di.*
import com.doubtnutapp.dnr.ui.fragment.*
import com.doubtnutapp.doubtfeed.di.DoubtFeedModule
import com.doubtnutapp.doubtfeed.ui.DailyGoalTaskCompletedDialogFragment
import com.doubtnutapp.doubtfeed.ui.DoubtFeedBackPressDialogFragment
import com.doubtnutapp.doubtfeed.ui.DoubtFeedFragment
import com.doubtnutapp.doubtfeed2.di.DoubtFeedModule2
import com.doubtnutapp.doubtfeed2.leaderboard.di.LeaderboardModule
import com.doubtnutapp.doubtfeed2.reward.di.DfRewardModule
import com.doubtnutapp.doubtfeed2.reward.ui.RewardFragment
import com.doubtnutapp.doubtfeed2.ui.*
import com.doubtnutapp.doubtpecharcha.di.DoubtP2pActivityModule
import com.doubtnutapp.doubtpecharcha.di.DoubtPeCharchaEndFragmentModule
import com.doubtnutapp.doubtpecharcha.di.DoubtPeCharchaUserFeedbackModule
import com.doubtnutapp.doubtpecharcha.di.P2PDoubtCollectionFragmentModule
import com.doubtnutapp.doubtpecharcha.ui.fragment.*
import com.doubtnutapp.doubtplan.DoubtPackageDialog
import com.doubtnutapp.dummy.DummyModule
import com.doubtnutapp.examcorner.ExamCornerFragment
import com.doubtnutapp.examcorner.di.ExamCornerModule
import com.doubtnutapp.feed.dimodule.FeedFragmentModule
import com.doubtnutapp.feed.view.AddTopicDialog
import com.doubtnutapp.feed.view.FeedFragment
import com.doubtnutapp.feed.view.ImagePagerFragment
import com.doubtnutapp.freeTrialCourse.di.FreeTrialCourseModule
import com.doubtnutapp.freeTrialCourse.ui.FreeTrialCourseFragment
import com.doubtnutapp.gallery.di.GalleryFragmentModule
import com.doubtnutapp.gallery.ui.GalleryFragment
import com.doubtnutapp.gamification.badgesscreen.di.BadgeDialogFragmentBindModule
import com.doubtnutapp.gamification.badgesscreen.ui.badgesDialogs.AchievedBadgesDialog
import com.doubtnutapp.gamification.gamepoints.di.ViewLevelInformationFragmentModule
import com.doubtnutapp.gamification.gamepoints.ui.ViewLevelInformationFragment
import com.doubtnutapp.gamification.leaderboard.di.LeaderboardFragmentModule
import com.doubtnutapp.gamification.leaderboard.ui.LeaderboardFragment
import com.doubtnutapp.gamification.myachievment.di.UserAchievementFragmentModule
import com.doubtnutapp.gamification.myachievment.ui.UserAchievementFragment
import com.doubtnutapp.gamification.mybio.di.UserBioFragmentModule
import com.doubtnutapp.gamification.mybio.ui.MyBioFragment
import com.doubtnutapp.gamification.otheruserprofile.di.OtherUserAchievementBindModule
import com.doubtnutapp.gamification.otheruserprofile.ui.OtherUserAchievementFragment
import com.doubtnutapp.gamification.otheruserprofile.ui.OtherUserActivityFragment
import com.doubtnutapp.gamification.userProfileData.di.ProfileFragmentModule
import com.doubtnutapp.gamification.userProfileData.ui.ProfileFragment
import com.doubtnutapp.home.DoubtFeedBannerBottomSheetFragment
import com.doubtnutapp.home.WebViewBottomSheetFragment
import com.doubtnutapp.home.dimodule.MainActivityModule
import com.doubtnutapp.imagedirectory.ui.ImageDirectoryFragment
import com.doubtnutapp.leaderboard.ui.dialog.LeaderboardHelpBottomSheetDialogFragment
import com.doubtnutapp.libraryhome.course.CoursesFragmentModule
import com.doubtnutapp.libraryhome.course.ScheduleFragmentModule
import com.doubtnutapp.libraryhome.course.ui.ExploreFragment
import com.doubtnutapp.libraryhome.course.ui.ScheduleFragment
import com.doubtnutapp.libraryhome.course.ui.VipClassesDetailFragment
import com.doubtnutapp.libraryhome.coursev3.di.*
import com.doubtnutapp.libraryhome.coursev3.ui.*
import com.doubtnutapp.libraryhome.dailyquiz.di.DailyQuizFragmentModule
import com.doubtnutapp.libraryhome.dailyquiz.ui.DailyQuizFragment
import com.doubtnutapp.libraryhome.library.LibraryFragmentHome
import com.doubtnutapp.libraryhome.library.di.LibraryHomeFragmentModule
import com.doubtnutapp.libraryhome.mocktest.di.MockTestFragmentModule
import com.doubtnutapp.libraryhome.mocktest.di.ReviewQuestionModule
import com.doubtnutapp.libraryhome.mocktest.ui.MockTestFragment
import com.doubtnutapp.live.ui.JoinLivePaymentDialog
import com.doubtnutapp.live.ui.ScheduleLiveConfirmationDialog
import com.doubtnutapp.liveclass.di.*
import com.doubtnutapp.liveclass.ui.*
import com.doubtnutapp.liveclass.ui.dialog.*
import com.doubtnutapp.liveclass.ui.practice_english.*
import com.doubtnutapp.login.di.AnonymousLoginModule
import com.doubtnutapp.login.di.LoginActivityModule
import com.doubtnutapp.login.di.LoginActivityProviderModule
import com.doubtnutapp.login.di.LoginBackPressDialogFragmentModule
import com.doubtnutapp.login.ui.fragment.*
import com.doubtnutapp.matchquestion.di.BlurQuestionImageDialogFragmentModule
import com.doubtnutapp.matchquestion.di.MatchQuestionActivityModule
import com.doubtnutapp.matchquestion.di.MatchQuestionFragmentBindModule
import com.doubtnutapp.matchquestion.di.MatchQuestionFragmentProviderModule
import com.doubtnutapp.matchquestion.ui.fragment.MatchPageCarousalsFragment
import com.doubtnutapp.matchquestion.ui.fragment.MatchQuestionFragment
import com.doubtnutapp.matchquestion.ui.fragment.MatchQuestionWebViewFragment
import com.doubtnutapp.matchquestion.ui.fragment.bottomsheet.AdvanceSearchBottomSheetFragment
import com.doubtnutapp.matchquestion.ui.fragment.bottomsheet.MatchBottomSheetFragment
import com.doubtnutapp.matchquestion.ui.fragment.dialog.*
import com.doubtnutapp.newglobalsearch.di.IasAllFiltersFragmentModule
import com.doubtnutapp.newglobalsearch.di.InAppSearchFragmentModule
import com.doubtnutapp.newglobalsearch.di.InAppYoutubeSearchFragmentModule
import com.doubtnutapp.newglobalsearch.di.NoDataFoundFragmentModule
import com.doubtnutapp.newglobalsearch.ui.*
import com.doubtnutapp.newlibrary.di.LibraryExamBottomSheetModule
import com.doubtnutapp.newlibrary.di.LibraryFragmentModule
import com.doubtnutapp.newlibrary.di.LibrarySortByYearFragmentModule
import com.doubtnutapp.newlibrary.ui.LibraryExamsBottomSheetFragment
import com.doubtnutapp.newlibrary.ui.LibraryFragment
import com.doubtnutapp.newlibrary.ui.LibrarySortByYearFragment
import com.doubtnutapp.payment.ui.PaymentFailureFragment
import com.doubtnutapp.paymentplan.di.*
import com.doubtnutapp.paymentplan.ui.*
import com.doubtnutapp.paymentv2.ui.CouponBottomSheetDialogFragment
import com.doubtnutapp.paymentv2.ui.CouponSuccessDialogFragment
import com.doubtnutapp.profile.userprofile.UserProfileFragment
import com.doubtnutapp.profile.userprofile.UserProfileFragmentModule
import com.doubtnutapp.quiztfs.di.MyRewardsModule
import com.doubtnutapp.quiztfs.di.QuizTfsFragmentModule
import com.doubtnutapp.quiztfs.di.QuizTfsStatusModule
import com.doubtnutapp.quiztfs.ui.*
import com.doubtnutapp.resourcelisting.di.ResourceListingModule
import com.doubtnutapp.resourcelisting.ui.ResourceListingFragment
import com.doubtnutapp.revisioncorner.di.RevisionCornerModule
import com.doubtnutapp.revisioncorner.ui.*
import com.doubtnutapp.reward.ui.ScratchCardDialogFragment
import com.doubtnutapp.reward.ui.dialogs.AttendanceMarkedDialogFragment
import com.doubtnutapp.shorts.ShortsFragment
import com.doubtnutapp.shorts.di.ShortsModule
import com.doubtnutapp.similarVideo.di.SimilarVideoFragmentBindModule
import com.doubtnutapp.similarVideo.ui.LandscapeSimilarVideoBottomDialog
import com.doubtnutapp.similarVideo.ui.NcertBooksBottomSheetFragment
import com.doubtnutapp.similarVideo.ui.NcertSimilarFragment
import com.doubtnutapp.similarVideo.ui.SimilarVideoFragment
import com.doubtnutapp.similarplaylist.di.SimilarPlaylistFragmentBindModule
import com.doubtnutapp.similarplaylist.ui.SimilarPlaylistFragment
import com.doubtnutapp.socket.di.SocketManagerModule
import com.doubtnutapp.store.di.StoreItemBuyDialogFragmentBindModule
import com.doubtnutapp.store.di.StoreResultFragmentModule
import com.doubtnutapp.store.ui.StoreFragment
import com.doubtnutapp.store.ui.dialog.StoreItemBuyDialog
import com.doubtnutapp.store.ui.dialog.StoreItemDisabledDialog
import com.doubtnutapp.studygroup.di.*
import com.doubtnutapp.studygroup.ui.AudioPlayerDialogFragment
import com.doubtnutapp.studygroup.ui.fragment.*
import com.doubtnutapp.survey.di.UserSurveyModule
import com.doubtnutapp.survey.ui.fragments.*
import com.doubtnutapp.teacher.TeacherFragment
import com.doubtnutapp.teacher.di.TeacherModule
import com.doubtnutapp.teacherchannel.TeacherProfileBottomsheetFragment
import com.doubtnutapp.teacherchannel.di.TeacherChannelModule
import com.doubtnutapp.topicboostergame.di.TopicBoosterGameModule
import com.doubtnutapp.topicboostergame.ui.TopicBoosterGameOpponentSearchFragment
import com.doubtnutapp.topicboostergame.ui.TopicBoosterGameQuizExitDialogFragment
import com.doubtnutapp.topicboostergame.ui.TopicBoosterGameQuizFragment
import com.doubtnutapp.topicboostergame.ui.TopicBoosterGameResultFragment
import com.doubtnutapp.topicboostergame2.di.TopicBoosterGameModule2
import com.doubtnutapp.topicboostergame2.ui.*
import com.doubtnutapp.transactionhistory.TransactionHistoryFragment
import com.doubtnutapp.transactionhistory.di.TransactionHistoryFragmentModule
import com.doubtnutapp.ui.answer.VideoDislikeFeedbackDialog
import com.doubtnutapp.ui.ask.SelectImageDialog
import com.doubtnutapp.ui.common.address.SubmitAddressDialogFragment
import com.doubtnutapp.ui.common.address.SubmitAddressDialogFragmentModule
import com.doubtnutapp.ui.di.CameraGalleryDialogModule
import com.doubtnutapp.ui.di.FragmentCameraGalleryDialogModule
import com.doubtnutapp.ui.di.SelectImageDialogModule
import com.doubtnutapp.ui.downloadPdf.di.DownloadPdfModule
import com.doubtnutapp.ui.editProfile.CameraGalleryDialog
import com.doubtnutapp.ui.editProfile.FragmentCameraGalleryDialog
import com.doubtnutapp.ui.feedback.FeedbackFragment
import com.doubtnutapp.ui.feedback.NPSFeedbackFragment
import com.doubtnutapp.ui.feedback.di.FeedbackFragmentModule
import com.doubtnutapp.ui.forum.comments.CommentBottomSheetFragment
import com.doubtnutapp.ui.main.MatchQuestionDialog
import com.doubtnutapp.ui.main.OcrFromImageDialog
import com.doubtnutapp.ui.main.demoanimation.DemoAnimationFragmentV1
import com.doubtnutapp.ui.main.di.*
import com.doubtnutapp.ui.main.samplequestion.BackPressSampleQuestionFragment
import com.doubtnutapp.ui.main.samplequestion.BackPressSampleQuestionFragmentV2
import com.doubtnutapp.ui.mockTest.MockTestQuestionFragment
import com.doubtnutapp.ui.mockTest.MockTestSummaryReportFragment
import com.doubtnutapp.ui.mockTest.di.MockTestCommonModule
import com.doubtnutapp.ui.mockTest.dialog.ReviewQuestionDialogFragment
import com.doubtnutapp.ui.onboarding.LanguageFragment
import com.doubtnutapp.ui.onboarding.SelectClassFragment
import com.doubtnutapp.ui.onboarding.di.LanguageFragmentModule
import com.doubtnutapp.ui.onboarding.di.MobileVerifyFragmentModule
import com.doubtnutapp.ui.onboarding.di.SelectClassModule
import com.doubtnutapp.ui.pdfviewer.PdfViewerFragment
import com.doubtnutapp.ui.pdfviewer.di.PdfViewerModule
import com.doubtnutapp.ui.playlist.AddPlaylistFragment
import com.doubtnutapp.ui.test.LoginDialog
import com.doubtnutapp.ui.test.TestQuestionFragment
import com.doubtnutapp.ui.test.TestSummaryReportFragment
import com.doubtnutapp.ui.test.dimodule.TestQuestionFragmentModule
import com.doubtnutapp.ui.test.dimodule.TestSummaryReportFragmentModule
import com.doubtnutapp.ui.test.dimodule.VerifyOtpModule
import com.doubtnutapp.ui.userstatus.StatusActionBottomFragment
import com.doubtnutapp.ui.userstatus.StatusAdFragment
import com.doubtnutapp.ui.userstatus.StatusDetailFragment
import com.doubtnutapp.ui.userstatus.di.StatusActionBottomFragmenttModule
import com.doubtnutapp.utils.AdminOptionsDialog
import com.doubtnutapp.utils.AdminOptionsDialogModule
import com.doubtnutapp.utils.VideoDownloadOptionsDialog
import com.doubtnutapp.utils.VideoDownloadOptionsDialogModule
import com.doubtnutapp.video.SimpleVideoFragment
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.video.di.VideoFragmentModule
import com.doubtnutapp.video.videoquality.VideoQualityBottomSheet
import com.doubtnutapp.videoPage.di.VideoDislikeFeedbackFragmentModule
import com.doubtnutapp.videoPage.ui.DialogShareVideo
import com.doubtnutapp.videoscreen.YoutubeFragment
import com.doubtnutapp.videoscreen.YoutubeFragmentModule
import com.doubtnutapp.vipplan.di.BundleModule
import com.doubtnutapp.vipplan.di.PaymentHelpFragmentModule
import com.doubtnutapp.vipplan.di.VipPlanActivityModule
import com.doubtnutapp.vipplan.ui.BundleFragment
import com.doubtnutapp.vipplan.ui.CheckoutFragment
import com.doubtnutapp.vipplan.ui.PaymentHelpFragment
import com.doubtnutapp.wallet.WalletFragment
import com.doubtnutapp.wallet.di.WalletModule
import com.doubtnutapp.whatsappadmin.WhatsappAdminForm
import com.doubtnutapp.whatsappadmin.WhatsappAdminInfoFragment
import com.doubtnutapp.whatsappadmin.di.WhatsappAdminFormModule
import com.doubtnutapp.whatsappadmin.di.WhatsappAdminInfoModule
import com.doubtnutapp.widgetmanager.widgets.CarouselListWidgetBannerFragment
import com.doubtnutapp.widgets.base.BaseWidgetBottomSheetDialogFragment
import com.doubtnutapp.widgets.base.BaseWidgetBottomSheetDialogFragmentModule
import com.doubtnutapp.widgets.base.BaseWidgetDialogFragment
import com.doubtnutapp.widgets.base.BaseWidgetDialogFragmentModule
import com.doubtnutapp.widgets.countrycodepicker.CountryCodePickerDialogFragment
import com.doubtnutapp.widgets.countrycodepicker.di.CountryCodePickerModule
import com.doubtnutapp.widgettest.di.ApiTestModule
import com.doubtnutapp.widgettest.ui.JsonPreviewFragment
import com.doubtnutapp.widgettest.ui.WidgetPreviewFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BindingFragmentModule {

    @ContributesAndroidInjector(modules = [FeedFragmentModule::class])
    @PerFragment
    internal abstract fun contributeFeedFragmentInjector(): FeedFragment

    @ContributesAndroidInjector(modules = [TestSummaryReportFragmentModule::class])
    @PerFragment
    internal abstract fun contributeTestSummaryReportFragmentInjector(): TestSummaryReportFragment

    @ContributesAndroidInjector(modules = [InAppSearchFragmentModule::class])
    @PerFragment
    internal abstract fun contributeSearchFragmentInjector(): InAppSearchFragment

    @ContributesAndroidInjector(modules = [MatchQuestionFragmentBindModule::class, MatchQuestionFragmentProviderModule::class])
    @PerFragment
    internal abstract fun contributeMatchQuestionFragmentInjector(): MatchQuestionFragment

    @ContributesAndroidInjector(modules = [SimilarVideoFragmentBindModule::class])
    @PerFragment
    internal abstract fun contributeSimilarVideoFragmentInjector(): SimilarVideoFragment

    @ContributesAndroidInjector(modules = [SimilarPlaylistFragmentBindModule::class])
    @PerFragment
    internal abstract fun contributeSimilarPlaylistFragmentInjector(): SimilarPlaylistFragment

    @ContributesAndroidInjector(modules = [ProfileFragmentModule::class])
    @PerFragment
    internal abstract fun contributeProfileFragmentInjector(): ProfileFragment

    @ContributesAndroidInjector(modules = [UserAchievementFragmentModule::class])
    @PerFragment
    internal abstract fun contributeOtherUserAchievementFragmentInjector(): UserAchievementFragment

    @ContributesAndroidInjector(modules = [DailyQuizFragmentModule::class])
    @PerFragment
    internal abstract fun contributeUserDailyQuizFragmentInjector(): DailyQuizFragment

    @ContributesAndroidInjector(modules = [MockTestFragmentModule::class])
    @PerFragment
    internal abstract fun contributeUserMockTestFragmentInjector(): MockTestFragment

    @ContributesAndroidInjector(modules = [SelectClassModule::class])
    @PerFragment
    internal abstract fun contributeSelectClassFragmentInjector(): SelectClassFragment

    @ContributesAndroidInjector(modules = [MockTestCommonModule::class])
    @PerFragment
    internal abstract fun contributeMockTestQuestionFragmentInjector(): MockTestQuestionFragment

    @ContributesAndroidInjector(modules = [MockTestCommonModule::class])
    @PerFragment
    internal abstract fun contributeMockTestSummaryReportFragmentInjector(): MockTestSummaryReportFragment

    @ContributesAndroidInjector(modules = [MobileVerifyFragmentModule::class, VerifyOtpModule::class])
    @PerFragment
    internal abstract fun contributeLoginDialogInjector(): LoginDialog

    @ContributesAndroidInjector(modules = [LibraryFragmentModule::class])
    @PerFragment
    internal abstract fun contributeLibraryFragment(): LibraryFragment

    @ContributesAndroidInjector(modules = [LibraryExamBottomSheetModule::class])
    @PerFragment
    internal abstract fun contributeLibraryExamBottomSheetFragment(): LibraryExamsBottomSheetFragment

    @ContributesAndroidInjector(modules = [ResourceListingModule::class])
    @PerFragment
    internal abstract fun contributeResourceListingFragmentInjector(): ResourceListingFragment

    @ContributesAndroidInjector(modules = [UserBioFragmentModule::class])
    @PerFragment
    internal abstract fun contributeUserBioFragmentInjector(): MyBioFragment

    @ContributesAndroidInjector(modules = [LeaderboardFragmentModule::class])
    @PerFragment
    internal abstract fun contributeLeaderboardInjector(): LeaderboardFragment

    @ContributesAndroidInjector(modules = [StoreResultFragmentModule::class])
    @PerFragment
    internal abstract fun contributeStoreResultFragmentInjector(): StoreFragment

    @ContributesAndroidInjector(modules = [ViewLevelInformationFragmentModule::class])
    @PerFragment
    internal abstract fun contributeViewLevelInformationFragmentInjector(): ViewLevelInformationFragment

    @ContributesAndroidInjector(modules = [BadgeDialogFragmentBindModule::class])
    @PerFragment
    internal abstract fun contributeAchievedBadgesDialogInjector(): AchievedBadgesDialog

    @ContributesAndroidInjector(modules = [OtherUserAchievementBindModule::class])
    @PerFragment
    internal abstract fun contributeOtherUserFragmentInjector(): OtherUserAchievementFragment

    @ContributesAndroidInjector(modules = [OtherUserAchievementBindModule::class])
    @PerFragment
    internal abstract fun contributeOtherUserActivityFragmentInjector(): OtherUserActivityFragment

    @ContributesAndroidInjector(modules = [StoreItemBuyDialogFragmentBindModule::class])
    @PerFragment
    internal abstract fun contributeStoreItemBuyDialogInjector(): StoreItemBuyDialog

    @ContributesAndroidInjector(modules = [StoreItemBuyDialogFragmentBindModule::class])
    @PerFragment
    internal abstract fun contributeStoreItemDisabledDialogInjector(): StoreItemDisabledDialog

    @ContributesAndroidInjector(modules = [AddToPlaylistModule::class])
    @PerFragment
    internal abstract fun contributeAddToPlaylistFragmentInjector(): AddToPlaylistFragment

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule::class])
    @PerActivity
    internal abstract fun contributeTopicBoosterGameOpponentSearchFragment(): TopicBoosterGameOpponentSearchFragment

    @ContributesAndroidInjector(modules = [YoutubeFragmentModule::class])
    @PerFragment
    internal abstract fun contributeYoutubeFragmentInjector(): YoutubeFragment

    @ContributesAndroidInjector(modules = [MainActivityModule::class, UserSurveyModule::class])
    @PerActivity
    internal abstract fun contributeWebViewBottomSheetFragmentInjector(): WebViewBottomSheetFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeMatchQuestionWebViewFragmentInjector(): MatchQuestionWebViewFragment

    @ContributesAndroidInjector(modules = [VideoDislikeFeedbackFragmentModule::class])
    @PerFragment
    internal abstract fun contributeVideoDislikeFeedbackDialogInjector(): VideoDislikeFeedbackDialog

    @ContributesAndroidInjector(modules = [VideoDislikeFeedbackFragmentModule::class, DummyModule::class])
    @PerFragment
    internal abstract fun contributeMatchQuestionBookFeedbackDialogFragment(): MatchQuestionBookFeedbackDialogFragment

    @ContributesAndroidInjector(modules = [VideoFragmentModule::class])
    @PerFragment
    internal abstract fun contributeVideoFragmentInjector(): VideoFragment

    @ContributesAndroidInjector(modules = [CoursesFragmentModule::class])
    @PerFragment
    internal abstract fun contributeVmcDetailFragmentInjector(): VipClassesDetailFragment

    @ContributesAndroidInjector(modules = [SimilarVideoFragmentBindModule::class])
    @PerFragment
    internal abstract fun contributeLandscapeSimilarVideoBottomDialogInjector(): LandscapeSimilarVideoBottomDialog

    @ContributesAndroidInjector(modules = [InAppSearchFragmentModule::class])
    @PerFragment
    internal abstract fun contributeSearchTrendingDataFragmentInjector(): InAppSearchTrendingDataFragment

    @ContributesAndroidInjector(modules = [InAppYoutubeSearchFragmentModule::class])
    @PerFragment
    internal abstract fun contributeInAppYoutubeSearchFragmentInjector(): InAppYoutubeSearchFragment

    @ContributesAndroidInjector(modules = [IasAllFiltersFragmentModule::class])
    @PerFragment
    internal abstract fun contributeIasAllFilterFragmentInjector(): IasAllFilterFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeIasAdvancedSearchFragmentInjector(): IasAdvancedSearchFragment

    @ContributesAndroidInjector(modules = [UserProfileFragmentModule::class, UserSurveyModule::class])
    @PerFragment
    internal abstract fun contributeUserProfileFragmentInjector(): UserProfileFragment

    @ContributesAndroidInjector(modules = [LibraryHomeFragmentModule::class])
    @PerFragment
    internal abstract fun contributeLibraryHomeFragmentInjector(): LibraryFragmentHome

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributesSimpleVideoFragmentInjector(): SimpleVideoFragment

    @ContributesAndroidInjector(modules = [CoursesFragmentModule::class, DnrRewardModule::class])
    @PerFragment
    internal abstract fun contributeExploreFragmentInjector(): ExploreFragment

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeDialogShareVideoInjector(): DialogShareVideo

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeMatchFilterFragmentInjector(): AdvanceSearchBottomSheetFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeMatchQuestionDialogInjector(): MatchQuestionDialog

    @ContributesAndroidInjector(modules = [LiveClassQnaModule::class])
    @PerFragment
    internal abstract fun contributeLiveClassQnaFragmentInjector(): LiveClassQnaFragment

    @ContributesAndroidInjector(modules = [LoginActivityModule::class, LoginActivityProviderModule::class])
    @PerFragment
    internal abstract fun contributePhoneFragmentInjector(): StudentPhoneFragment

    @ContributesAndroidInjector(modules = [LoginActivityModule::class, LoginActivityProviderModule::class])
    @PerFragment
    internal abstract fun contributeStudentOtpFragmentInjector(): StudentOtpFragment

    @ContributesAndroidInjector(modules = [LoginActivityModule::class, LoginActivityProviderModule::class])
    @PerFragment
    internal abstract fun contributeOtpOverCallDialogFragment(): OtpOverCallDialogFragment

    @ContributesAndroidInjector(modules = [LoginActivityModule::class, LoginActivityProviderModule::class])
    @PerFragment
    internal abstract fun contributeMissCallVerificationFragment(): MissCallVerificationFragment

    @ContributesAndroidInjector(modules = [CourseFragmentModuleV3::class])
    @PerFragment
    internal abstract fun contributeCourseFragmentInjector(): CourseTabFragment

    @ContributesAndroidInjector(modules = [BlurQuestionImageDialogFragmentModule::class])
    @PerFragment
    internal abstract fun contributeBlurQuestionImageDialogFragmentInjector(): BlurQuestionImageDialogFragment

    @ContributesAndroidInjector(modules = [InvalidImageDialogFragmentModule::class])
    @PerFragment
    internal abstract fun contributeInvalidImageDialogFragment(): InvalidImageDialogFragment

    @ContributesAndroidInjector(modules = [LiveClassCommentModule::class])
    @PerFragment
    internal abstract fun contributeCommentBottomSheetFragmentInjector(): CommentBottomSheetFragment

    @ContributesAndroidInjector(modules = [VideoStatusFragmentModule::class])
    @PerFragment
    internal abstract fun contributeVideoStatusFragmentInjector(): VideoStatusFragment

    @ContributesAndroidInjector(modules = [VideoBlockedFragmentModule::class])
    @PerFragment
    internal abstract fun contributeVideoBlockedFragmentInjector(): VideoBlockedFragment

//    @ContributesAndroidInjector()
//    @PerFragment
//    internal abstract fun contributeLiveClassQuizFragmentInjector(): LiveClassQuizFragment

    @ContributesAndroidInjector(modules = [LiveClassQnaModule::class])
    @PerFragment
    internal abstract fun contributeLiveClassPollsFragmentInjector(): LiveClassPollsFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeLiveClassAnnouncementFragmentInjector(): LiveClassCommFragment

    @ContributesAndroidInjector(modules = [LiveClassFeedbackModule::class])
    @PerFragment
    internal abstract fun contributeLiveClassFeedbackFragmentInjector(): LiveClassFeedbackFragment

    @ContributesAndroidInjector(modules = [EmiReminderModule::class])
    @PerFragment
    internal abstract fun contributeEMIReminderInjector(): EMIReminderDialog

    @ContributesAndroidInjector(modules = [LiveClassCommentModule::class])
    @PerFragment
    internal abstract fun contributeLiveClassCommentsDialogInjector(): LiveClassCommentFragment

    @ContributesAndroidInjector(modules = [LoginBackPressDialogFragmentModule::class])
    @PerFragment
    internal abstract fun contributeLoginBackPressDialogFragment(): LoginBackPressDialogFragment

    @ContributesAndroidInjector(modules = [ScheduleFragmentModule::class])
    @PerFragment
    internal abstract fun contributeScheduleFragmentInjector(): ScheduleFragment

    @ContributesAndroidInjector(modules = [BundleModule::class])
    @PerFragment
    internal abstract fun contributeBundleFragmentInjector(): BundleFragment

    @ContributesAndroidInjector(modules = [BundleModule::class])
    @PerFragment
    internal abstract fun contributeBundleFragment2Injector(): BundleFragmentV2

    @ContributesAndroidInjector(modules = [BundleModule::class])
    @PerFragment
    internal abstract fun contributeCheckoutFragmentInjector(): CheckoutFragment

    @ContributesAndroidInjector(modules = [SaleModule::class])
    @PerFragment
    internal abstract fun contributeSaleFragmentInjector(): SaleFragment

    @ContributesAndroidInjector(modules = [LoginActivityModule::class, LoginActivityProviderModule::class])
    @PerFragment
    internal abstract fun contributeChangeLoginPinDialogFragment(): ChangeLoginPinDialogFragment

    @ContributesAndroidInjector(modules = [GalleryFragmentModule::class])
    @PerFragment
    internal abstract fun contributeGalleryFragmentInjector(): GalleryFragment

    @ContributesAndroidInjector(modules = [BundleModule::class])
    @PerFragment
    internal abstract fun contributePaymentFailureFragmentInjector(): PaymentFailureFragment

    // Using @PerActivity because all the modules used in this fragment have @PerActivity scope
    @ContributesAndroidInjector(modules = [MatchQuestionActivityModule::class])
    @PerActivity
    internal abstract fun contributeMatchBottomSheetFragment(): MatchBottomSheetFragment

    @ContributesAndroidInjector(modules = [WalletModule::class])
    @PerFragment
    internal abstract fun contributeWalletFragmentInjector(): WalletFragment

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeStatusDetailFragmentInjector(): StatusDetailFragment

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeStatusAdFragmentInjector(): StatusAdFragment

    @ContributesAndroidInjector(modules = [AppExitDialogModule::class])
    @PerFragment
    internal abstract fun contributeAppExitDialogFragment(): AppExitDialogFragment

    @ContributesAndroidInjector(modules = [PaymentSuccessfulBottomSheetFragmentModule::class, DnrRewardModule::class])
    @PerFragment
    internal abstract fun contributesPaymentSuccessfulBottomSheetInjector(): PaymentSuccessfulBottomSheet

    @ContributesAndroidInjector(modules = [TopDoubtsFragmentModule::class])
    @PerFragment
    internal abstract fun contributeTopDoubtsFragmentInjector(): TopDoubtsFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeMatchPageLiveClassFragment(): MatchPageCarousalsFragment

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeScratchCardDialogFragment(): ScratchCardDialogFragment

    @ContributesAndroidInjector(modules = [MyRewardsModule::class])
    @PerFragment
    internal abstract fun contributeMyRewardsScratchCardDialogFragment(): MyRewardsScratchCardDialogFragment

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeStoryDetailFragmentInjector(): StoryDetailFragment

    @ContributesAndroidInjector(modules = [CourseScheduleFragmentModule::class])
    @PerFragment
    internal abstract fun contributeCourseScheduleFragmentInjector(): CourseScheduleFragment

    @ContributesAndroidInjector(modules = [SimilarVideoFragmentBindModule::class])
    @PerFragment
    internal abstract fun contributeNcertSimilarFragment(): NcertSimilarFragment

    @ContributesAndroidInjector(modules = [TransactionHistoryFragmentModule::class])
    @PerFragment
    internal abstract fun contributeTransactionHistoryFragment(): TransactionHistoryFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeOcrFromImageDialogInjector(): OcrFromImageDialog

    @ContributesAndroidInjector(modules = [CourseFragmentModule::class, CreateSgFragmentModule::class])
    @PerFragment
    internal abstract fun postPurchaseFragmentModule(): CourseFragment

    @ContributesAndroidInjector(modules = [CourseSelectionFragmentModule::class])
    @PerFragment
    internal abstract fun contributeCourseSelectionFragmentModule(): CourseSelectionFragment

    @ContributesAndroidInjector(modules = [CourseSelectionFragmentModule::class])
    @PerFragment
    internal abstract fun contributeCourseSelectionDialogFragmentModule(): CourseSelectionDialogFragment

    @ContributesAndroidInjector(modules = [DoubtFeedModule::class])
    @PerFragment
    internal abstract fun contributeDoubtFeedFragment(): DoubtFeedFragment

    @ContributesAndroidInjector(modules = [FilterSelectionDialogFragmentModule::class])
    @PerFragment
    internal abstract fun contributeFilterSelectionDialogFragment(): FilterSelectionDialogFragment

    @ContributesAndroidInjector(modules = [ChooseExamBottomSheetDialogFragmentModule::class])
    @PerFragment
    internal abstract fun contributeFChooseExamBottomSheetDialogFragment(): ChooseExamBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [CoreDummyModule::class])
    @PerActivity
    internal abstract fun contributeNoticeBoardDetailFragment(): NoticeBoardDetailFragment

    @ContributesAndroidInjector(modules = [NoticeBoardProfileFragmentModule::class])
    @PerFragment
    internal abstract fun contributeNoticeBoardProfileFragment(): NoticeBoardProfileFragment

    @ContributesAndroidInjector(modules = [NudgeModule::class])
    @PerFragment
    internal abstract fun contributeNudgeFragment(): NudgeFragment

    @ContributesAndroidInjector(modules = [ChangeCourseActivityModule::class])
    @PerFragment
    internal abstract fun contributeCourseChangeDialogFragment(): CourseSwitchDialogFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeCourseHelpDialogFragment(): CourseHelpDialogFragment

    @ContributesAndroidInjector(modules = [SimilarVideoFragmentBindModule::class])
    @PerFragment
    internal abstract fun contributeNcertBooksBottomSheetFragment(): NcertBooksBottomSheetFragment

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule2::class])
    @PerFragment
    internal abstract fun contributeTbgHomeFragment(): TbgHomeFragment

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule2::class])
    @PerFragment
    internal abstract fun contributeLevelsBottomSheetDialogFragment(): LevelsBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule2::class])
    @PerFragment
    internal abstract fun contributeTbgInviteFragment(): TbgInviteFragment

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule2::class])
    @PerFragment
    internal abstract fun contributeTbgChapterSelectionFragment(): TbgChapterSelectionFragment

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule2::class])
    @PerFragment
    internal abstract fun contributeTbgInviteFriendsListFragment(): TbgInviteFriendsListFragment

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule2::class])
    @PerFragment
    internal abstract fun contributeTbgOpponentWaitFragment(): TbgOpponentWaitFragment

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule2::class])
    @PerFragment
    internal abstract fun contributeTbgQuizFragment(): TbgQuizFragment

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule2::class])
    @PerFragment
    internal abstract fun contributeTbgLeaderboardFragment(): TbgLeaderboardFragment

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule2::class])
    @PerFragment
    internal abstract fun contributeTbgLeaderboardListFragment(): TbgLeaderboardListFragment

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule2::class])
    @PerFragment
    internal abstract fun contributeTbgResultFragment(): TbgResultFragment

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeTbgUnavailableFragment(): TbgUnavailableFragment

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeLevelUnlockDialogFragment(): LevelUnlockDialogFragment

    @ContributesAndroidInjector(modules = [AnonymousLoginModule::class])
    @PerFragment
    internal abstract fun contributeAnonymousLoginBlockerFragmentInjector(): AnonymousLoginBlockerFragment

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeLeaderboardHelpBottomSheetDialogFragment(): LeaderboardHelpBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [CourseRecommendationModule::class])
    @PerFragment
    internal abstract fun contributeCourseRecommendationFragment(): CourseRecommendationFragment

    @ContributesAndroidInjector(modules = [AdminOptionsDialogModule::class])
    @PerFragment
    internal abstract fun contributeAdminOptionsDialog(): AdminOptionsDialog

    @ContributesAndroidInjector(modules = [BackPressSampleQuestionFragmentV2BindModule::class, BackPressSampleQuestionFragmentV2ProvidesModule::class])
    @PerFragment
    internal abstract fun contributeBackPressSampleQuestionFragment(): BackPressSampleQuestionFragment

    @ContributesAndroidInjector(modules = [BackPressSampleQuestionFragmentV2BindModule::class, BackPressSampleQuestionFragmentV2ProvidesModule::class])
    @PerFragment
    internal abstract fun contributeBackPressSampleQuestionFragmentV2(): BackPressSampleQuestionFragmentV2

    @ContributesAndroidInjector(modules = [DoubtFeedModule2::class])
    @PerFragment
    internal abstract fun contributeDoubtFeedFragment2(): DoubtFeedFragment2

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeDfUnavailableFragment(): DfUnavailableFragment

    @ContributesAndroidInjector(modules = [LeaderboardModule::class])
    @PerFragment
    internal abstract fun contributeDfLeaderboardFragment(): com.doubtnutapp.doubtfeed2.leaderboard.ui.LeaderboardFragment

    @ContributesAndroidInjector(modules = [LeaderboardModule::class])
    @PerFragment
    internal abstract fun contributeDfLeaderboardListFragment(): com.doubtnutapp.doubtfeed2.leaderboard.ui.LeaderboardListFragment

    @ContributesAndroidInjector(modules = [DfRewardModule::class])
    @PerFragment
    internal abstract fun contributeDfRewardFragment(): RewardFragment

    @ContributesAndroidInjector(modules = [DfRewardModule::class])
    @PerFragment
    internal abstract fun contributeDfScratchCardDialogFragment(): com.doubtnutapp.doubtfeed2.reward.ui.ScratchCardDialogFragment

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeDfGoalCompletedDialogFragment(): DfGoalCompletedDialogFragment

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeDfBackPressDialogFragment(): DfBackPressDialogFragment

    @ContributesAndroidInjector(modules = [DoubtFeedModule2::class])
    @PerFragment
    internal abstract fun contributeDfPreviousDoubtsFragment(): DfPreviousDoubtsFragment

    @ContributesAndroidInjector(modules = [WhatsappAdminInfoModule::class])
    @PerFragment
    internal abstract fun contributeWhatsappAdminInfoFragmentInjector(): WhatsappAdminInfoFragment

    @ContributesAndroidInjector(modules = [WhatsappAdminFormModule::class])
    @PerFragment
    internal abstract fun contributeWhatsappAdminFormFragmentInjector(): WhatsappAdminForm

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeCarouselListWidgetBannerFragment(): CarouselListWidgetBannerFragment

    @ContributesAndroidInjector(modules = [BaseWidgetBottomSheetDialogFragmentModule::class])
    @PerFragment
    internal abstract fun contributeBaseWidgetBottomSheetDialogFragment(): BaseWidgetBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [BaseWidgetPaginatedBottomSheetDialogFragmentModule::class])
    @PerFragment
    internal abstract fun contributeBaseWidgetPaginatedBottomSheetDialogFragment(): BaseWidgetPaginatedBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [BaseWidgetDialogFragmentModule::class])
    @PerFragment
    internal abstract fun contributeBaseWidgetDialogFragment(): BaseWidgetDialogFragment

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeCourseBottomSheetDialogFragment(): CourseBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [CourseFragmentModule::class])
    @PerFragment
    internal abstract fun contributeCourseBottomSheetFragment(): CourseBottomSheetFragment

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeDoubtFeedBannerBottomSheetFragment(): DoubtFeedBannerBottomSheetFragment

    @ContributesAndroidInjector(modules = [ExamCornerModule::class])
    @PerFragment
    internal abstract fun contributeExamCornerFragment(): ExamCornerFragment

    @ContributesAndroidInjector(modules = [SgHomeFragmentModule::class])
    @PerFragment
    internal abstract fun contributeSgHomeFragment(): SgHomeFragment

    @ContributesAndroidInjector(modules = [SgHomeFragmentModule::class, SocketManagerModule::class])
    @PerFragment
    internal abstract fun contributeSgMyGroupsFragment(): SgMyGroupsFragment

    @ContributesAndroidInjector(modules = [SgHomeFragmentModule::class, SocketManagerModule::class])
    @PerFragment
    internal abstract fun contributeSgMyChatsFragment(): SgMyChatsFragment

    @ContributesAndroidInjector(modules = [StudyGroupChatModule::class, DnrRewardModule::class])
    @PerFragment
    internal abstract fun contributeSgChatFragment(): SgChatFragment

    @ContributesAndroidInjector(modules = [StudyGroupChatModule::class])
    @PerFragment
    internal abstract fun contributeSgProfanityBottomSheetFragment(): SgUserBannedStatusBottomSheetFragment

    @ContributesAndroidInjector(modules = [SgPersonalChatModule::class])
    @PerFragment
    internal abstract fun contributeSgIndividualChatFragment(): SgPersonalChatFragment

    @ContributesAndroidInjector(modules = [SgHomeFragmentModule::class])
    @PerFragment
    internal abstract fun contributeSgSettingFragment(): SgSettingFragment

    @ContributesAndroidInjector(modules = [StudyGroupChatModule::class])
    @PerFragment
    internal abstract fun contributeSgInfoFragment(): SgInfoFragment

    @ContributesAndroidInjector(modules = [SgDashboardFragmentModule::class])
    @PerFragment
    internal abstract fun contributeSgAdminDashboardFragment(): SgAdminDashboardFragment

    @ContributesAndroidInjector(modules = [SgDashboardFragmentModule::class])
    @PerFragment
    internal abstract fun contributeSgUserReportedMessageFragment(): SgUserReportedMessageFragment

    @ContributesAndroidInjector(modules = [SgSelectFriendFragmentModule::class])
    @PerFragment
    internal abstract fun contributeSgSelectFriendFragment(): SgSelectFriendFragment

    @ContributesAndroidInjector(modules = [SgExtraInfoModule::class])
    @PerFragment
    internal abstract fun contributeSgExtraInfoFragment(): SgExtraInfoFragment

    @ContributesAndroidInjector(modules = [SgChatRequestModule::class])
    @PerFragment
    internal abstract fun contributeSgChatRequestBottomSheetFragment(): SgChatRequestBottomSheetFragment

    @ContributesAndroidInjector(modules = [BookCallDialogFragmentModule::class])
    @PerFragment
    internal abstract fun contributeBookCallDialogFragment(): BookCallDialogFragment

    @ContributesAndroidInjector(modules = [CouponListDialogFragmentModule::class])
    @PerFragment
    internal abstract fun contributeCouponListDialogFragment(): CouponListDialogFragment

    @ContributesAndroidInjector(modules = [StudyGroupListModule::class])
    @PerFragment
    internal abstract fun contributeSgListBottomSheetFragment(): SgListBottomSheetFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeFilterOptionsBottomSheetFragment(): FilterOptionsBottomSheetFragment

    @ContributesAndroidInjector(modules = [StudyGroupChatModule::class])
    @PerFragment
    internal abstract fun contributeInvitationEntryDialogFragment(): SgJoinGroupDialogFragment

    @ContributesAndroidInjector(modules = [ReviewQuestionModule::class])
    @PerFragment
    internal abstract fun contributeReviewQuestionDialogFragment(): ReviewQuestionDialogFragment

    @ContributesAndroidInjector(modules = [CreateSgFragmentModule::class])
    @PerFragment
    internal abstract fun contributeCreateSgBottomSheetFragment(): SgCreateBottomSheetFragment

    @ContributesAndroidInjector(modules = [RevisionCornerModule::class])
    @PerFragment
    internal abstract fun contributeRevisionCornerHomeFragment(): RcHomeFragment

    @ContributesAndroidInjector(modules = [RevisionCornerModule::class])
    @PerFragment
    internal abstract fun contributeRcStatsFragment(): RcStatsFragment

    @ContributesAndroidInjector(modules = [RevisionCornerModule::class])
    @PerFragment
    internal abstract fun contributeRcResultFragment(): RcResultHistoryFragment

    @ContributesAndroidInjector(modules = [RevisionCornerModule::class])
    @PerFragment
    internal abstract fun contributeRcRuleFragment(): RcRulesFragment

    @ContributesAndroidInjector(modules = [RevisionCornerModule::class])
    @PerFragment
    internal abstract fun contributeRcResultListFragment(): RcResultHistoryListFragment

    @ContributesAndroidInjector(modules = [RevisionCornerModule::class])
    @PerFragment
    internal abstract fun contributeRcChapterSelectionFragment(): RcChapterSelectionFragment

    @ContributesAndroidInjector(modules = [RevisionCornerModule::class])
    @PerFragment
    internal abstract fun contributeRcShortTestFragment(): RcShortTestFragment

    @ContributesAndroidInjector(modules = [RevisionCornerModule::class])
    @PerFragment
    internal abstract fun contributeRcShortTestSolutionFragment(): RcShortTestSolutionFragment

    @ContributesAndroidInjector(modules = [RevisionCornerModule::class])
    @PerFragment
    internal abstract fun contributeRcTestListFragment(): RcTestListFragment

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeRcUnavailableStatsFragment(): RcUnavailableStatsFragment

    @ContributesAndroidInjector(modules = [TestQuestionFragmentModule::class])
    @PerFragment
    internal abstract fun contributeTestQuestionFragment(): TestQuestionFragment

    @ContributesAndroidInjector(modules = [AudioPlayerDialogModule::class])
    @PerFragment
    internal abstract fun contributeAudioPlayerDialogFragment(): AudioPlayerDialogFragment

    @ContributesAndroidInjector(modules = [DoubtP2pActivityModule::class])
    @PerFragment
    internal abstract fun contributeDoubtPeCharchaRatingFragment(): DoubtPeCharchaRatingFragment

    @ContributesAndroidInjector(modules = [CountryCodePickerModule::class])
    @PerFragment
    internal abstract fun contributeCountryCodePickerDialogFragment(): CountryCodePickerDialogFragment

    @ContributesAndroidInjector(modules = [CreateGroupFragmentModule::class])
    @PerFragment
    internal abstract fun contributeCreateGroupSlideFragment(): CreateGroupSlideFragment

    @ContributesAndroidInjector(modules = [PaymentHelpFragmentModule::class])
    @PerFragment
    internal abstract fun contributePaymentHelpFragment(): PaymentHelpFragment

    @ContributesAndroidInjector(modules = [QuizTfsFragmentModule::class])
    @PerFragment
    internal abstract fun contributeQuizTfsFragmentInjector(): QuizTfsFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeQuizTfsSolutionFragmentInjector(): QuizTfsSolutionFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeQuizTfsWaitFragmentInjector(): QuizTfsWaitFragment

    @ContributesAndroidInjector(modules = [QuizTfsStatusModule::class])
    @PerFragment
    internal abstract fun contributeQuizTfsStatusDialogFragmentInjector(): QuizTfsStatusDialogFragment

    @ContributesAndroidInjector
    @PerFragment
    internal abstract fun contributeFaqBottomSheetDialogFragment(): FaqBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeAddTopicFragmentInjector(): AddTopicDialog

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeBottomDialogFragment(): BottomDialog

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeDoubtPackageDialogFragment(): DoubtPackageDialog

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeJoinLivePaymentDialog(): JoinLivePaymentDialog

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeScheduleLiveConfirmationDialog(): ScheduleLiveConfirmationDialog

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeVideoDialog(): VideoDialog

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeInAppSearchCloseDialog(): InAppSearchCloseDialog

    @ContributesAndroidInjector(modules = [CameraGalleryDialogModule::class])
    @PerFragment
    internal abstract fun contributeCameraGalleryDialogFragment(): CameraGalleryDialog

    @ContributesAndroidInjector(modules = [LoginActivityModule::class, LoginActivityProviderModule::class])
    @PerFragment
    internal abstract fun contributeLoginPinDialogFragment(): LoginPinDialogFragment

    @ContributesAndroidInjector(modules = [LanguageFragmentModule::class])
    @PerFragment
    internal abstract fun contributeLanguageFragment(): LanguageFragment

    @ContributesAndroidInjector(modules = [MatchQuestionActivityModule::class])
    @PerActivity
    internal abstract fun contributeHandWrittenQuestionFragment(): HandWrittenQuestionDialogFragment

    @ContributesAndroidInjector(modules = [CameraActivityBindModule::class, CameraActivityProvideModule::class])
    @PerActivity
    internal abstract fun contributeImageDirectoryFragment(): ImageDirectoryFragment

    @ContributesAndroidInjector(modules = [StatusActionBottomFragmenttModule::class])
    @PerFragment
    internal abstract fun contributeStatusActionBottomFragment(): StatusActionBottomFragment

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule::class])
    @PerActivity
    internal abstract fun contributeTopicBoosterGameResultFragment(): TopicBoosterGameResultFragment

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule::class])
    @PerActivity
    internal abstract fun contributeTopicBoosterGameQuizExitDialogFragment(): TopicBoosterGameQuizExitDialogFragment

    @ContributesAndroidInjector(modules = [TopicBoosterGameModule::class])
    @PerActivity
    internal abstract fun contributeTopicBoosterGameQuizFragment(): TopicBoosterGameQuizFragment

    @ContributesAndroidInjector(modules = [DoubtPeCharchaEndFragmentModule::class])
    @PerFragment
    internal abstract fun contributeDoubtPeCharchaEndFragment(): DoubtPeCharchaEndFragment

    @ContributesAndroidInjector(modules = [FragmentCameraGalleryDialogModule::class])
    @PerFragment
    internal abstract fun contributeFragmentCameraGalleryDialog(): FragmentCameraGalleryDialog

    @ContributesAndroidInjector(modules = [FeedbackFragmentModule::class])
    @PerFragment
    internal abstract fun contributeFeedbackFragment(): FeedbackFragment

    @ContributesAndroidInjector(modules = [FeedbackFragmentModule::class])
    @PerFragment
    internal abstract fun contributeNPSFeedbackFragment(): NPSFeedbackFragment

    @ContributesAndroidInjector(modules = [DoubtFeedModule::class])
    @PerFragment
    internal abstract fun contributeDoubtFeedBackPressDialogFragment(): DoubtFeedBackPressDialogFragment

    @ContributesAndroidInjector(modules = [DoubtFeedModule::class])
    @PerFragment
    internal abstract fun contributeDoubtPeCharchaReasonsFragment(): DoubtPeCharchaReasonsFragment

    @ContributesAndroidInjector(modules = [DoubtP2pActivityModule::class])
    @PerFragment
    internal abstract fun contributeDoubtPeCharchaFeedbackFragment(): DoubtPeCharchaFeedbackFragment

    @ContributesAndroidInjector(modules = [MatchQuestionActivityModule::class])
    @PerActivity
    internal abstract fun contributeP2pHostIntroductionFragment(): P2pHostIntroductionFragment

    @ContributesAndroidInjector(modules = [CameraActivityBindModule::class, CameraActivityProvideModule::class])
    @PerActivity
    internal abstract fun contributeDemoAnimationFragmentV1(): DemoAnimationFragmentV1

    @ContributesAndroidInjector(modules = [DoubtFeedModule::class])
    @PerFragment
    internal abstract fun contributeDailyGoalTaskCompletedDialogFragment(): DailyGoalTaskCompletedDialogFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeImagePagerFragment(): ImagePagerFragment

    @ContributesAndroidInjector(modules = [VideoDownloadOptionsDialogModule::class])
    @PerFragment
    internal abstract fun contributeVideoDownloadOptionsDialog(): VideoDownloadOptionsDialog

    @ContributesAndroidInjector(modules = [P2PDoubtCollectionFragmentModule::class])
    @PerFragment
    internal abstract fun contributeP2PDoubtCollectionFragmentInjector(): P2PDoubtCollectionFragment

    @ContributesAndroidInjector(modules = [NoDataFoundFragmentModule::class])
    @PerFragment
    internal abstract fun contributeNoDataFoundFragment(): NoDataFoundFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerActivity
    internal abstract fun contributeMatchQuestionP2pDialogFragment(): MatchQuestionP2pDialogFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeAddPlaylistFragment(): AddPlaylistFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeAttendanceMarkedDialogFragment(): AttendanceMarkedDialogFragment

    @ContributesAndroidInjector(modules = [TeacherChannelModule::class])
    @PerFragment
    internal abstract fun contributeTeacherChannelActivity(): TeacherProfileBottomsheetFragment

    @ContributesAndroidInjector(modules = [SelectImageDialogModule::class])
    @PerFragment
    internal abstract fun contributeSelectImageDialogModule(): SelectImageDialog

    @ContributesAndroidInjector(modules = [ApiTestModule::class])
    @PerActivity
    internal abstract fun contributeJsonPreviewFragment(): JsonPreviewFragment

    @ContributesAndroidInjector(modules = [ApiTestModule::class])
    @PerActivity
    internal abstract fun contributeWidgetPreviewFragment(): WidgetPreviewFragment

    @ContributesAndroidInjector(modules = [NextVideoModule::class])
    @PerFragment
    internal abstract fun contributeNextVideoFragment(): NextVideoDialogFragment

    @ContributesAndroidInjector(modules = [HomeWorkModule::class])
    @PerFragment
    internal abstract fun contributeHomeWorkFragment(): HomeWorkFragment

    @ContributesAndroidInjector(modules = [DownloadPdfModule::class, PdfViewerModule::class])
    @PerFragment
    internal abstract fun contributePdfViewerFragment(): PdfViewerFragment

    @ContributesAndroidInjector(modules = [CheckoutV2DialogModule::class])
    @PerFragment
    internal abstract fun contributeCheckoutV2Dialog(): CheckoutV2Dialog

    @ContributesAndroidInjector(modules = [NetBankingBottomSheetModule::class])
    @PerFragment
    internal abstract fun contributeNetBankingBottomSheet(): NetBankingBottomSheet

    @ContributesAndroidInjector(modules = [BankSelectorBottomSheetModule::class])
    @PerFragment
    internal abstract fun contributeBankSelectorBottomSheet(): BankSelectorBottomSheet

    @ContributesAndroidInjector(modules = [WalletSelectorBottomSheetModule::class])
    @PerFragment
    internal abstract fun contributeWidgetSelectorBottomSheet(): WalletSelectorBottomSheet

    @ContributesAndroidInjector(modules = [CardBottomSheetModule::class])
    @PerFragment
    internal abstract fun contributeCardBottomSheet(): CardBottomSheet

    @ContributesAndroidInjector(modules = [UpiBottomSheetModule::class])
    @PerFragment
    internal abstract fun contributeUpiBottomSheet(): UPIBottomSheet

    @ContributesAndroidInjector(modules = [DnrHomeFragmentModule::class])
    @PerFragment
    internal abstract fun contributeDnrHomeFragment(): DnrHomeFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeDnrRewardBottomSheetFragment(): DnrRewardBottomSheetFragment

    @ContributesAndroidInjector(modules = [DnrVoucherExploreFragmentModule::class])
    @PerFragment
    internal abstract fun contributeDnrMysteryBoxFragment(): DnrMysteryBoxFragment

    @ContributesAndroidInjector(modules = [DnrVoucherExploreFragmentModule::class])
    @PerFragment
    internal abstract fun contributeDnrSpinTheWheelFragment(): DnrSpinTheWheelFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeDnrRewardDialogFragment(): DnrRewardDialogFragment

    @ContributesAndroidInjector(modules = [DnrPopupDialogFragmentModule::class])
    @PerFragment
    internal abstract fun contributeDnrPopupDialogFragment(): DnrPopupDialogFragment

    @ContributesAndroidInjector(modules = [DnrWidgetListFragmentModule::class])
    @PerFragment
    internal abstract fun contributeDnrWidgetListFragment(): DnrWidgetListFragment

    @ContributesAndroidInjector(modules = [DnrVoucherListFragmentModule::class])
    @PerFragment
    internal abstract fun contributeDnrVoucherListFragment(): DnrVoucherListFragment

    @ContributesAndroidInjector(modules = [DnrVoucherExploreFragmentModule::class])
    @PerFragment
    internal abstract fun contributeDnrVoucherExploreFragment(): DnrVoucherExploreFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeDnrTncBottomSheet(): DnrTncBottomSheet

    @ContributesAndroidInjector(modules = [DnrVoucherListFragmentModule::class])
    @PerFragment
    internal abstract fun contributeDnrVoucherRedeemBottomSheetFragment(): DnrVoucherRedeemBottomSheetFragment

    @ContributesAndroidInjector(modules = [HomeWorkModule::class])
    @PerFragment
    internal abstract fun contributeHomeWorkSolutionFragment(): HomeWorkSolutionFragment

    @ContributesAndroidInjector(modules = [VideoTabFragmentModule::class])
    @PerFragment
    internal abstract fun contributeVideoTabFragmentModule(): VideoTabFragment

    @ContributesAndroidInjector(modules = [LibrarySortByYearFragmentModule::class])
    @PerFragment
    internal abstract fun contributeLibrarySortByYearFragment(): LibrarySortByYearFragment

    @ContributesAndroidInjector(modules = [MatchQuestionActivityModule::class])
    @PerFragment
    internal abstract fun contributeMatchQuestionPopupDialogFragment(): MatchQuestionPopupDialogFragment

    @ContributesAndroidInjector(modules = [OlympiadRegisterFragmentModule::class])
    @PerFragment
    internal abstract fun contributeOlympiadRegisterFragment(): OlympiadRegisterFragment

    @ContributesAndroidInjector(modules = [OlympiadSuccessFragmentModule::class])
    @PerFragment
    internal abstract fun contributeOlympiadSuccessFragment(): OlympiadSuccessFragment

    @ContributesAndroidInjector(modules = [PracticeEnglishActivityModule::class])
    @PerFragment
    internal abstract fun contributesAudioQuestionFragment(): AudioQuestionFragment

    @ContributesAndroidInjector(modules = [PracticeEnglishActivityModule::class])
    @PerFragment
    internal abstract fun contributesTextQuestionFragment(): TextQuestionFragment

    @ContributesAndroidInjector(modules = [PracticeEnglishActivityModule::class])
    @PerFragment
    internal abstract fun contributesSingleBlankQuestionFragment(): SingleBlankQuestionFragment

    @ContributesAndroidInjector(modules = [PracticeEnglishActivityModule::class])
    @PerFragment
    internal abstract fun contributesMultiBlankQuestionFragment(): MultiBlankQuestionFragment

    @ContributesAndroidInjector(modules = [PracticeEnglishActivityModule::class])
    @PerFragment
    internal abstract fun contributesSingleChoiceQuestionFragment(): SingleChoiceQuestionFragment

    @ContributesAndroidInjector(modules = [PracticeEnglishActivityModule::class])
    @PerFragment
    internal abstract fun contributesImageQuestionFragment(): ImageQuestionFragment

    @ContributesAndroidInjector(modules = [PracticeEnglishActivityModule::class])
    @PerFragment
    internal abstract fun contributesEndSessionFragment(): EndSessionFragment

    @ContributesAndroidInjector(modules = [UserSurveyModule::class])
    @PerFragment
    internal abstract fun contributeUserSurveyFragment(): UserSurveyBottomSheetFragment

    @ContributesAndroidInjector(modules = [UserSurveyModule::class])
    @PerActivity
    internal abstract fun contributeStartSurveyFragment(): StartSurveyFragment

    @ContributesAndroidInjector(modules = [UserSurveyModule::class])
    @PerFragment
    internal abstract fun contributeSingleChoiceFragment(): SingleChoiceFragment

    @ContributesAndroidInjector(modules = [UserSurveyModule::class])
    @PerFragment
    internal abstract fun contributeCalendarFragment(): CalendarFragment

    @ContributesAndroidInjector(modules = [UserSurveyModule::class])
    @PerFragment
    internal abstract fun contributeRatingFragment(): RatingFragment

    @ContributesAndroidInjector(modules = [UserSurveyModule::class])
    @PerFragment
    internal abstract fun contributeEditTextFragment(): EditTextFragment

    @ContributesAndroidInjector(modules = [UserSurveyModule::class])
    @PerFragment
    internal abstract fun contributeMultipleChoiceFragment(): MultipleChoiceFragment

    @ContributesAndroidInjector(modules = [UserSurveyModule::class])
    @PerFragment
    internal abstract fun contributeEndSurveyFragment(): EndSurveyFragment

    @ContributesAndroidInjector(modules = [FilterFragmentModule::class])
    @PerFragment
    internal abstract fun contributeFilterFragmentInjector(): FilterDialogFragment

    @ContributesAndroidInjector(modules = [TeacherModule::class])
    @PerFragment
    internal abstract fun contributeTeacherFragment(): TeacherFragment

    @ContributesAndroidInjector(modules = [CardBottomSheetModule::class])
    @PerFragment
    internal abstract fun contributeCardTooltipBottomSheet(): CardTooltipBottomSheet

    @ContributesAndroidInjector(modules = [VipPlanActivityModule::class])
    @PerFragment
    internal abstract fun contributeCouponBottomSheet(): CouponBottomSheetDialogFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeCouponSuccessDialogFragment(): CouponSuccessDialogFragment

    @ContributesAndroidInjector(modules = [SubmitAddressDialogFragmentModule::class])
    @PerFragment
    internal abstract fun contributeSubmitAddressDialogFragment(): SubmitAddressDialogFragment

    @ContributesAndroidInjector(modules = [FreeTrialCourseModule::class])
    @PerFragment
    internal abstract fun contributesFreeCoursesFragment(): FreeTrialCourseFragment

    @ContributesAndroidInjector(modules = [ShortsModule::class])
    @PerFragment
    internal abstract fun contributeShortsFragment(): ShortsFragment

    @ContributesAndroidInjector(modules = [DummyModule::class])
    @PerFragment
    internal abstract fun contributeVideoQualityBottomSheetFragment(): VideoQualityBottomSheet

    @ContributesAndroidInjector(modules = [DoubtPeCharchaUserFeedbackModule::class])
    @PerFragment
    internal abstract fun contributeSelectUserForFeedbackFragment(): UserSelectForFeedbackFragment

    @ContributesAndroidInjector(modules = [DoubtPeCharchaUserFeedbackModule::class])
    @PerFragment
    internal abstract fun contributesDoubtPeCharchaFeedbackRatingFragment(): DoubtPeCharchaUserFeedbackRatingFragment

}
