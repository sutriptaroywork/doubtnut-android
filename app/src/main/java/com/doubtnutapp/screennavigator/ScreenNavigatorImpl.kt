package com.doubtnutapp.screennavigator

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.*
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.data.remote.models.TestDetails
import com.doubtnutapp.gamification.badgesscreen.ui.BadgesActivity
import com.doubtnutapp.gamification.badgesscreen.ui.badgesDialogs.AchievedBadgesDialog
import com.doubtnutapp.gamification.dailyattendance.ui.DailyAttendanceActivity
import com.doubtnutapp.gamification.earnedPointsHistory.ui.EarnedPointsHistoryActivity
import com.doubtnutapp.gamification.gamepoints.ui.GamePointsActivity
import com.doubtnutapp.gamification.leaderboard.ui.GameLeaderBoardActivity
import com.doubtnutapp.gamification.mybio.ui.MyBioActivity
import com.doubtnutapp.gamification.otheruserprofile.ui.OthersProfileActivity
import com.doubtnutapp.gamification.settings.profilesetting.ui.ProfileSettingActivity
import com.doubtnutapp.gamification.settings.settingdetail.ui.SettingDetailActivity
import com.doubtnutapp.home.WebViewBottomSheetFragment
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.newglobalsearch.ui.InAppSearchActivity
import com.doubtnutapp.pcmunlockpopup.ui.PCMUnlockPopActivity
import com.doubtnutapp.profile.uservideohistroy.ui.UserWatchedVideoActivity
import com.doubtnutapp.quicksearch.QuickSearchSettingActivity
import com.doubtnutapp.store.ui.dialog.StoreItemBuyDialog
import com.doubtnutapp.store.ui.dialog.StoreItemDisabledDialog
import com.doubtnutapp.textsolution.ui.TextSolutionActivity
import com.doubtnutapp.ui.FragmentHolderActivity
import com.doubtnutapp.ui.cameraGuide.CameraGuideActivity
import com.doubtnutapp.ui.contest.ContestListActivity
import com.doubtnutapp.ui.dailyPrize.DailyPrizeActivity
import com.doubtnutapp.ui.downloadPdf.DownloadNShareActivity
import com.doubtnutapp.ui.downloadPdf.DownloadNShareLevelOneActivity
import com.doubtnutapp.ui.downloadPdf.DownloadNShareLevelTwoActivity
import com.doubtnutapp.ui.games.DnGamesActivity
import com.doubtnutapp.ui.mockTest.MockTestActivity
import com.doubtnutapp.ui.mockTest.MockTestListActivity
import com.doubtnutapp.ui.mypdf.MyPdfActivity
import com.doubtnutapp.ui.onboarding.SelectClassFragment
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.ui.questionAskedHistory.QuestionAskedHistoryActivity
import com.doubtnutapp.ui.splash.SplashActivity
import com.doubtnutapp.ui.test.QuizActivity
import com.doubtnutapp.ui.test.TestQuestionActivity
import com.doubtnutapp.utils.BranchIOUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.TestUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.instacart.library.truetime.TrueTimeRx
import org.json.JSONObject
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Deprecated("Use Deeplink Instead")
@Singleton
class ScreenNavigatorImpl @Inject constructor() : Navigator {

    override fun openDialogFromFragment(
        activity: Activity,
        screen: Screen,
        params: Bundle?,
        supportFragmentManager: FragmentManager
    ) {
        activity.apply {

            when (screen) {
                is BuyStoreItemDialogScreen -> {
                    params?.apply {
                        val dialog = StoreItemBuyDialog.newInstance(
                            params.getInt(StoreItemBuyDialog.RESOURCE_ID, -999),
                            params.getString(StoreItemBuyDialog.RESOURCE_TYPE)!!,
                            params.getString(StoreItemBuyDialog.TITLE)!!,
                            params.getString(StoreItemBuyDialog.IMG_URL)!!,
                            params.getInt(StoreItemBuyDialog.REDEEM_STATUS, -999),
                            params.getInt(StoreItemBuyDialog.ITEM_ID, -999),
                            params.getInt(StoreItemBuyDialog.PRICE, -999),
                            params.getInt(StoreItemBuyDialog.IS_LAST, -999)

                        )
                        dialog.show(supportFragmentManager, "StoreItemBuyDialog")
                    }

                }
                is DisabledStoreItemDialogScreen -> {

                    params?.apply {
                        val dialog = StoreItemDisabledDialog.newInstance(
                            params.getInt(StoreItemDisabledDialog.AVAILABLE_DN_CASH, -999),
                            params.getInt(StoreItemDisabledDialog.ITEM_PRICE, -999)

                        )
                        dialog.show(supportFragmentManager, "StoreItemDisabledDialog")
                    }
                }

                is BadgeProgressDialogScreen -> {
                    params?.apply {
                        val dialog = AchievedBadgesDialog.newInstance(
                            params.getString(
                                Constants.BADGE_ID,
                                params.getString(Constants.BADGE_ID, "")
                            ),
                            params.getString(
                                Constants.NUDE_DESCRIPTION,
                                params.getString(Constants.NUDE_DESCRIPTION, "")
                            ),
                            params.getString(
                                Constants.IMAGE_URL,
                                params.getString(Constants.IMAGE_URL, "")
                            ),
                            params.getString(
                                Constants.FEATURE_TYPE,
                                params.getString(Constants.FEATURE_TYPE, "")
                            ),
                            params.getString(
                                Constants.SHARING_MESSAGE,
                                params.getString(Constants.SHARING_MESSAGE, "")
                            ),
                            params.getString(
                                Constants.ACTION_PAGE,
                                params.getString(Constants.ACTION_PAGE, "")
                            )

                        )
                        dialog.show(supportFragmentManager, "AchievedBadgesDialog")
                    }
                }

                is WebViewBottomSheet -> {
                    params?.apply {
                        val dialog = WebViewBottomSheetFragment.newInstance(
                            getString(WebViewBottomSheetFragment.WEB_VIEW_URL, "")
                        )
                        dialog.show(supportFragmentManager, "WebViewBottomSheetFragment")
                    }
                }

            }
        }
    }

    override fun openDialogFromFragment(
        fragment: Fragment,
        screen: Screen,
        params: Bundle?,
        supportFragmentManager: FragmentManager
    ) {
        fragment.apply {

            when (screen) {

            }
        }
    }

    override fun startActivityFromOutside(context: Context, screen: Screen, params: Bundle?) {
        getIntent(context, screen, params)
            ?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }?.also {
                context.startActivity(it)
            }
    }

    override fun startActivityFromActivity(context: Context, screen: Screen, params: Bundle?) {

        getIntent(context, screen, params)?.also {
            context.startActivity(it)
        }
    }

    override fun startActivityForResultFromActivity(
        activity: Activity,
        screen: Screen,
        params: Bundle?,
        requestCode: Int
    ) {
        getIntent(activity, screen, params)?.also {
            try {
                activity.startActivityForResult(it, requestCode)
            } catch (e: Exception) {
                if (screen == ExternalUrlScreen) {
                    ToastUtils.makeText(
                        activity,
                        activity.getString(R.string.no_default_browser_found),
                        Toast.LENGTH_LONG
                    ).show()
                } else ToastUtils.makeText(
                    activity,
                    activity.getString(R.string.somethingWentWrong),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun startActivityForResultFromFragment(
        fragment: Fragment,
        screen: Screen,
        params: Bundle?,
        requestCode: Int
    ) {
        getIntent(fragment.requireContext(), screen, params)?.also {
            try {
                fragment.startActivityForResult(it, requestCode)
            } catch (e: Exception) {
                if (screen == ExternalUrlScreen) {
                    ToastUtils.makeText(
                        fragment.requireContext(),
                        fragment.getString(R.string.no_default_browser_found),
                        Toast.LENGTH_LONG
                    ).show()
                } else ToastUtils.makeText(
                    fragment.requireContext(),
                    fragment.getString(R.string.somethingWentWrong),
                    Toast.LENGTH_LONG
                ).show()

            }
        }
    }

    private fun getIntent(context: Context, screen: Screen, params: Bundle?): Intent? =
        when (screen) {

            is PDFScreen -> {
                resolvePdfIntent(context, params)
            }

            is PDFViewerScreen -> Intent(context, PdfViewerActivity::class.java).also {
                it.putExtra(
                    Constants.INTENT_EXTRA_PDF_URL,
                    params?.getString(SCREEN_NAV_PARAM_PDF_URL)
                )
            }

            is ContestDetailScreen -> {
                Intent(context, DailyPrizeActivity::class.java).also {
                    it.putExtra(Constants.CONTEST_ID, params?.getString(Constants.CONTEST_ID))
                }
            }

            is QuizScreen -> Intent(context, QuizActivity::class.java)

            is QuizDetailScreen -> Intent(context, TestQuestionActivity::class.java).also {
                val testDetail: TestDetails = params?.getParcelable(Constants.TEST_DETAILS_OBJECT)!!
                it.putExtra(Constants.TEST_DETAILS_OBJECT, testDetail)
                it.putExtra(Constants.TEST_TRUE_TIME_FLAG, getTimeFlag(testDetail))
                it.putExtra(
                    Constants.TEST_SUBSCRIPTION_ID,
                    params.getInt(Constants.TEST_SUBSCRIPTION_ID)
                )

            }

            is NcertChapterScreen -> Intent(context, LibraryListingActivity::class.java).also {
                it.action = Constants.NAVIGATE_LIBRARY
                it.putExtra(Constants.PAGE, 1)
                it.putExtra(Constants.PLAYLIST_ID, params?.getString(Constants.PLAYLIST_ID))
                it.putExtra(Constants.PLAYLIST_TITLE, params?.getString(Constants.PLAYLIST_TITLE))
            }

            is DnGamesScreen -> Intent(context, DnGamesActivity::class.java)

            is LibraryScreen -> Intent(context, MainActivity::class.java).also {
                it.action = Constants.NAVIGATE_LIBRARY
                if (params != null && !params.isEmpty && !params.getString(Constants.PLAYLIST_ID)
                        .isNullOrBlank()
                ) {
                    it.putExtra(Constants.PLAYLIST_ID, params.getString(Constants.PLAYLIST_ID))
                    it.putExtra(
                        Constants.PLAYLIST_TITLE,
                        params.getString(Constants.PLAYLIST_TITLE)
                    )
                }
            }

            is ForumScreen -> Intent(context, MainActivity::class.java).also {
                it.action = Constants.NAVIGATE_FORUM_FEED
            }

            is VideoScreen -> {
                sendCleverTapEventByQid(
                    context,
                    EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
                    params?.getString(Constants.QUESTION_ID),
                    params?.getString(Constants.PAGE)
                )
//                BranchIOUtils.userCompletedAction(
//                    EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
//                    JSONObject().apply {
//                        put(Constants.QUESTION_ID, params?.getString(Constants.QUESTION_ID))
//                        put(Constants.PAGE, params?.getString(Constants.PAGE))
//
//                    })

                VideoPageActivity.startActivity(
                    context,
                    params!!.getString(Constants.QUESTION_ID).orDefaultValue(),
                    params.getString(Constants.PLAYLIST_ID),
                    "",
                    params.getString(Constants.PAGE).orDefaultValue(),
                    params.getString(Constants.MC_CLASS),
                    params.getBoolean("isMicroconcept"),
                    "",
                    parentId = params.getString(Constants.PARENT_ID) ?: "0",
                    fromNotificationVideo = false,
                    preLoadVideoData = params.getParcelable(Constants.VIDEO_DATA),
                    ocr = params.getString(Constants.OCR_TEXT),
                    source = params.getString(Constants.PAGE).orDefaultValue(),
                    parentPage = params.getString(Constants.PARENT_PAGE)
                )
            }

            is TextSolutionScreen -> {
                TextSolutionActivity.startActivity(
                    context = context,
                    questionId = params!!.getString(Constants.QUESTION_ID).orDefaultValue(),
                    playlistId = params.getString(Constants.PLAYLIST_ID),
                    mcId = "",
                    page = params.getString(Constants.PAGE).orDefaultValue(),
                    mcClass = params.getString(Constants.MC_CLASS),
                    isMicroConcept = params.getBoolean("isMicroconcept"),
                    referredStudentId = "",
                    parentId = params.getString(Constants.PARENT_ID) ?: "0",
                    fromNotificationVideo = false,
                    resourceType = params.getString(Constants.RESOURCE_TYPE),
                    resourceData = params.getString(Constants.RESOURCE_DATA),
                    ocrText = params.getString(Constants.OCR_TEXT)
                )
            }

            is TopicDetailScreen -> Intent(context, FragmentHolderActivity::class.java).also {
                it.action = Constants.NAVIGATE_COURSE_DETAIL_NOTIFICATION
                it.putExtra(Constants.CLASS, params?.getString(Constants.CLASS))
                it.putExtra(Constants.COURSE, params?.getString(Constants.COURSE))
                it.putExtra(Constants.CHAPTER, params?.getString(Constants.CHAPTER))

            }

            is LibraryVideoPlayListScreen -> Intent(
                context,
                FragmentHolderActivity::class.java
            ).also {
                it.action = Constants.NAVIGATE_VIEW_PLAYLIST
                it.putExtra(Constants.PLAYLIST_ID, params?.getString(SCREEN_NAV_PARAM_PLAYLIST_ID))
                it.putExtra(
                    Constants.PLAYLIST_TITLE,
                    params?.getString(SCREEN_NAV_PARAM_PLAYLIST_TITLE)
                )
                it.putExtra(
                    Constants.PACKAGE_DETAIL_ID,
                    params?.getString(Constants.PACKAGE_DETAIL_ID)
                )
                it.putExtra(
                    Constants.IS_FROM_VIDEO_TAG,
                    params?.getBoolean(Constants.IS_FROM_VIDEO_TAG)
                )
                it.putExtra(Constants.QUESTION_ID, params?.getString(Constants.QUESTION_ID))
                it.putExtra(Constants.VIDEO_TAG_NAME, params?.getString(Constants.VIDEO_TAG_NAME))
                it.putExtra(Constants.PAGE, params?.getString(Constants.PAGE))
                it.putExtra(
                    Constants.IS_AUTO_PLAY,
                    params?.getBoolean(Constants.IS_AUTO_PLAY) ?: false
                )
            }

            is MockTestScreen -> Intent(context, MockTestActivity::class.java)

            is LearnMoreScreen -> Intent(context, CameraGuideActivity::class.java).apply {
                if (params != null && params.containsKey(ProfileSettingActivity.INTENT_EXTRA_SOURCE)) {
                    putExtra(
                        ProfileSettingActivity.INTENT_EXTRA_SOURCE,
                        params.getString(ProfileSettingActivity.INTENT_EXTRA_SOURCE)
                    )
                }
            }

            is LoginScreen -> Intent(context, SplashActivity::class.java)

            is CameraScreen -> {
                val source =
                    if (params != null && params.containsKey(ProfileSettingActivity.INTENT_EXTRA_SOURCE)) params.getString(
                        ProfileSettingActivity.INTENT_EXTRA_SOURCE
                    ) else "ScreenNavigator"
                val isUserOpened =
                    params?.getBoolean(CameraActivity.INTENT_EXTRA_IS_USER_OPENED, true)
                        ?: true
                CameraActivity.getStartIntent(
                    context,
                    source.orDefaultValue(),
                    isUserOpened = isUserOpened
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }

            is ClassSelectionScreen -> Intent(context, FragmentHolderActivity::class.java).apply {
                putExtra(
                    SelectClassFragment.INTENT_SOURCE_LIBRARY,
                    params?.getBoolean(SelectClassFragment.INTENT_SOURCE_LIBRARY)
                )
                action = Constants.NAVIGATE_CLASS_FRAGMENT_FROM_NAV
            }

            is WinPaytmScreen -> Intent(context, ContestListActivity::class.java)

            is ChangeLanguageScreen -> Intent(context, FragmentHolderActivity::class.java).apply {
                action = Constants.NAVIGATE_LANGUAGE_FRAGMENT_FROM_NAV
            }

            is UserVideoHistoryScreen -> Intent(context, UserWatchedVideoActivity::class.java)

            is MyPdfScreen -> Intent(context, MyPdfActivity::class.java)

            is TermsAndConditionsScreen, PrivacyPolicyScreen, AboutUsScreen, ContactUsScreen -> Intent(
                context,
                SettingDetailActivity::class.java
            ).also {
                it.putExtra(Constants.PAGE_NAME, screen.toString())
            }

            is RateUsScreen -> {
                val uri = Uri.parse("market://details?id=" + Constants.PACKAGE_NAME)
                try {
                    Intent(Intent.ACTION_VIEW, uri).addFlags(
                        Intent.FLAG_ACTIVITY_NO_HISTORY or
                                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                                Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                    )
                } catch (e: ActivityNotFoundException) {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + Constants.PACKAGE_NAME)
                    )
                }
            }

            is TestQuestionScreen ->
                Intent(context, TestQuestionActivity::class.java).also {
                    it.putExtra(
                        Constants.TEST_DETAILS_OBJECT,
                        params?.getParcelable<TestDetails>(Constants.TEST_DETAILS_OBJECT)
                    )
                    it.putExtra(
                        Constants.TEST_TRUE_TIME_FLAG,
                        params?.getString(Constants.TEST_TRUE_TIME_FLAG)
                    )
                    it.putExtra(
                        Constants.TEST_SUBSCRIPTION_ID,
                        params?.getInt(Constants.TEST_SUBSCRIPTION_ID)
                    )
                    it.putExtra(Constants.FROM_LIBRARY, params?.getBoolean(Constants.FROM_LIBRARY))
                }

            is MockTestListScreen ->
                Intent(context, MockTestListActivity::class.java).also {
                    it.putExtra(
                        Constants.MOCK_TEST_LIST,
                        params?.getParcelableArrayList<TestDetails>(Constants.TEST_DETAILS_OBJECT)
                    )
                    it.putExtra(
                        Constants.MOCK_TEST_TOOLBAR_TEXT,
                        params?.getString(Constants.MOCK_TEST_TOOLBAR_TEXT)
                    )
                }

            is ExternalUrlScreen -> Intent(
                Intent.ACTION_VIEW,
                Uri.parse(params?.getString(Constants.EXTERNAL_URL))
            )  // put the whastapp link here

            is NoScreen -> null

            is BadgesScreen -> BadgesActivity.startActivity(
                context,
                params!!.getString(SCREEN_NAV_PARAM_USER_ID).orDefaultValue()
            )

            is ProfileScreen -> Intent(context, FragmentHolderActivity::class.java).apply {
                action = FragmentHolderActivity.ACTION_NAVIGATE_PROFILE_SCREEN
            }

            is OthersProfileScreen -> OthersProfileActivity.startActivity(
                context,
                params!!.getString(SCREEN_NAV_PARAM_USER_ID).orDefaultValue()
            )

            is DailyStreakScreen -> DailyAttendanceActivity.startActivity(
                context,
                params!!.getString(SCREEN_NAV_PARAM_USER_ID).orDefaultValue()
            )

            is PCUnlockScreen ->
                Intent(context, PCMUnlockPopActivity::class.java)
            is GamePointsScreen ->
                Intent(context, GamePointsActivity::class.java)

            is EarnedPointsHistoryScreen ->
                Intent(context, EarnedPointsHistoryActivity::class.java)

            is LibraryPlayListScreen -> LibraryListingActivity.getStartIntent(
                context,
                params?.getString(LibraryListingActivity.INTENT_EXTRA_PLAYLIST_ID),
                params?.getString(LibraryListingActivity.INTENT_EXTRA_PLAYLIST_TITLE),
                packageDetailsId = params?.getString(LibraryListingActivity.INTENT_EXTRA_PACKAGE_ID)
                    .orEmpty(),
                page = params?.getString(LibraryListingActivity.INTENT_EXTRA_PLAYLIST_PAGE)
                    .orEmpty()
            )

            is CourseVideoScreen ->
                VideoPageActivity.startActivity(
                    context,
                    params?.getString(Constants.QUESTION_ID).orEmpty(),
                    "", "",
                    params?.getString(Constants.PAGE).orEmpty(),
                    "", false, "", "", false
                )

            is LiveClassesScreen ->
                Intent(context, MainActivity::class.java).also {
                    it.action = Constants.NAVIGATE_LIBRARY
                    it.putExtra(Constants.COURSE_ID, params?.getString(Constants.COURSE_ID))
                    it.putExtra(Constants.SUBJECT, params?.getString(Constants.SUBJECT))
                    it.putExtra(
                        Constants.LIBRARY_SCREEN_SELECTED_TAB,
                        params?.getString(Constants.LIBRARY_SCREEN_SELECTED_TAB)
                    )
                }

            is UpdateProfileScreen ->
                Intent(context, MyBioActivity::class.java)

            is InAppSearchScreen ->
                InAppSearchActivity.getStartIntent(
                    context,
                    params?.getString(Constants.SOURCE)
                        ?: "",
                    params?.getBoolean(Constants.VOICE_SEARCH)
                        ?: false,
                    params?.getString(Constants.SEARCH_QUERY),
                    eventType = params?.getString(Constants.EVENT_NAME)
                )

            OpenPointsHistoryScreen ->
                Intent(context, GamePointsActivity::class.java)
            OpenLeaderBoardScreen ->
                Intent(context, GameLeaderBoardActivity::class.java)

            QuickSearchSetting ->
                QuickSearchSettingActivity.getStartIntent(context)

            QuestionAskedHistoryScreen -> QuestionAskedHistoryActivity.getStartIntent(context)

            PipModeSettingsScreen -> {
                Intent(
                    "android.settings.PICTURE_IN_PICTURE_SETTINGS",
                    Uri.parse("package:" + DoubtnutApp.INSTANCE.packageName)
                )
            }

            else -> null
        }

    private fun getTimeFlag(testDetail: TestDetails?) = when {
        testDetail?.attemptCount != 0 -> Constants.USER_CANNOT_ATTEMPT_TEST
        else -> TestUtils.getTrueTimeDecision(
            testDetail.publishTime,
            testDetail.unpublishTime,
            now = when {
                TrueTimeRx.isInitialized() -> TrueTimeRx.now()
                else -> Calendar.getInstance().time
            }
        )
    }

    private fun resolvePdfIntent(context: Context, params: Bundle?): Intent? {

        return params?.getString(Constants.PDF_ACTION_ACTIVITY)?.let { activity ->
            when (activity) {
                Constants.PDF_ACTION_ACTIVITY_VIEW -> {
                    Intent(context, PdfViewerActivity::class.java).also {
                        it.putExtra(
                            Constants.INTENT_EXTRA_PDF_URL,
                            params.getString(Constants.PDF_ACTION_DATA_URL)
                        )
                    }
                }

                Constants.PDF_ACTION_ACTIVITY_LEVEL_ONE -> {
                    Intent(context, DownloadNShareLevelOneActivity::class.java).also {
                        it.putExtra(
                            Constants.FILTER_PACKAGE,
                            params.getString(Constants.PDF_ACTION_DATA_PACKAGE)
                        )
                    }
                }

                Constants.PDF_ACTION_ACTIVITY_LEVEL_TWO -> {
                    Intent(context, DownloadNShareLevelTwoActivity::class.java).also {
                        it.putExtra(
                            Constants.FILTER_PACKAGE,
                            params.getString(Constants.PDF_ACTION_DATA_PACKAGE)
                        )
                        it.putExtra(
                            Constants.FILTER_LEVEL_ONE,
                            params.getString(Constants.PDF_ACTION_ACTIVITY_LEVEL_ONE)
                        )
                    }

                }

                Constants.DOWNLOAD_PDF -> {
                    Intent(context, DownloadNShareActivity::class.java)
                }

                else -> null
            }
        }
    }

    private fun sendCleverTapEventByQid(
        context: Context,
        @Suppress("SameParameterValue") eventName: String,
        qid: String?,
        page: String?
    ) {
        context.apply {
            (context.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(context).toString())
                .addStudentId(getStudentId())
                .addEventParameter(Constants.QUESTION_ID, qid ?: "")
                .addEventParameter(Constants.PAGE, page ?: "")
                .addEventParameter(
                    Constants.STUDENT_CLASS, defaultPrefs(context)
                        .getString(Constants.STUDENT_CLASS, "").orDefaultValue()
                )
                .track()
        }
    }

}