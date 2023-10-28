package com.doubtnutapp.deeplink

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.TaskStackBuilder
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.DefaultDataStoreImpl
import com.doubtnut.core.data.remote.PopupDetails
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.sharing.event.Share
import com.doubtnut.core.utils.EmailUtils
import com.doubtnut.core.utils.IntentUtils
import com.doubtnut.core.utils.copy
import com.doubtnut.core.utils.toast
import com.doubtnut.olympiad.ui.OlympiadActivity
import com.doubtnut.referral.ui.ReferAndEarnActivity
import com.doubtnut.referral.ui.ReferralActivityV2
import com.doubtnut.scholarship.ui.ScholarshipActivity
import com.doubtnutapp.*
import com.doubtnutapp.Constants.STUDENT_CLASS
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.auth.ui.GoogleAuthActivity
import com.doubtnutapp.base.PerformDeeplinkAction
import com.doubtnutapp.base.SeeDoubtsAction
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.bottomsheet.BaseWidgetPaginatedBottomSheetDialogFragment
import com.doubtnutapp.bottomsheetholder.BottomSheetHolderActivity
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.common.ui.dialog.BookCallDialogFragment
import com.doubtnutapp.course.SchedulerListingActivity
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.dialogHolder.DialogHolderActivity
import com.doubtnutapp.dictionary.DictionaryActivity
import com.doubtnutapp.dnr.ui.activity.DnrActivity
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_TEXT
import com.doubtnutapp.domain.library.entities.ClassListViewItem
import com.doubtnutapp.domain.payment.entities.PaymentActivityBody
import com.doubtnutapp.domain.payment.entities.PaymentStartBody
import com.doubtnutapp.domain.payment.entities.PaymentStartInfo
import com.doubtnutapp.doubtfeed2.ui.DoubtFeedActivity
import com.doubtnutapp.doubtpecharcha.ui.activity.DoubtP2pActivity
import com.doubtnutapp.doubtpecharcha.ui.activity.DoubtP2pHelperEntryActivity
import com.doubtnutapp.doubtpecharcha.ui.activity.DoubtPeCharchaRewardsActivity
import com.doubtnutapp.doubtpecharcha.ui.activity.P2PDoubtCollectionActivity
import com.doubtnutapp.doubtplan.DoubtPackageActivity
import com.doubtnutapp.downloadedVideos.DownloadedVideosActivity
import com.doubtnutapp.examcorner.ExamCornerActivity
import com.doubtnutapp.examcorner.ExamCornerBookmarkActivity
import com.doubtnutapp.faq.ui.FaqActivity
import com.doubtnutapp.feed.view.LiveFeedActivity
import com.doubtnutapp.feed.view.OneTapPostsListActivity
import com.doubtnutapp.feed.view.PostDetailActivity
import com.doubtnutapp.feed.view.TopIconsActivity
import com.doubtnutapp.gamification.badgesscreen.ui.BadgesActivity
import com.doubtnutapp.gamification.mybio.ui.MyBioActivity
import com.doubtnutapp.icons.ui.IconsActivity
import com.doubtnutapp.leaderboard.ui.activity.LeaderboardActivity
import com.doubtnutapp.libraryhome.course.ui.VipDetailActivity
import com.doubtnutapp.libraryhome.coursev3.ui.*
import com.doubtnutapp.libraryhome.library.ui.adapter.LibraryTabAdapter
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.live.ui.LiveActivity
import com.doubtnutapp.liveclass.ui.*
import com.doubtnutapp.liveclass.ui.dialog.NextVideoDialogFragment
import com.doubtnutapp.liveclass.ui.practice_english.PracticeEnglishActivity
import com.doubtnutapp.login.ui.activity.FailedGuestLoginActivity
import com.doubtnutapp.matchquestion.ui.activity.FullImageViewActivity
import com.doubtnutapp.matchquestion.ui.activity.MatchQuestionActivity
import com.doubtnutapp.matchquestion.ui.fragment.dialog.MatchQuestionBookFeedbackDialogFragment
import com.doubtnutapp.newglobalsearch.ui.InAppSearchActivity
import com.doubtnutapp.newlibrary.ui.LibraryPreviousYearPapersActivity
import com.doubtnutapp.payment.ApbCashPaymentActivity
import com.doubtnutapp.paymentplan.ui.PaymentPlanActivity
import com.doubtnutapp.paymentv2.ui.PaymentActivity
import com.doubtnutapp.profile.social.CommunityGuidelinesActivity
import com.doubtnutapp.profile.uservideohistroy.ui.UserWatchedVideoActivity
import com.doubtnutapp.quicksearch.QuickSearchSettingActivity
import com.doubtnutapp.quiztfs.ui.*
import com.doubtnutapp.resultpage.ui.ResultPageActivity
import com.doubtnutapp.revisioncorner.ui.RevisionCornerActivity
import com.doubtnutapp.reward.ui.RewardActivity
import com.doubtnutapp.sticker.EntryActivity
import com.doubtnutapp.store.ui.StoreActivity
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity
import com.doubtnutapp.teacherchannel.TeacherChannelActivity
import com.doubtnutapp.textsolution.ui.TextSolutionActivity
import com.doubtnutapp.topicboostergame.ui.TopicBoosterGameActivity
import com.doubtnutapp.topicboostergame2.ui.TopicBoosterGameActivity2
import com.doubtnutapp.transactionhistory.TransactionHistoryActivityV2
import com.doubtnutapp.ui.FragmentHolderActivity
import com.doubtnutapp.ui.browser.CustomTabActivityHelper
import com.doubtnutapp.ui.browser.HandleActionWebViewActivity
import com.doubtnutapp.ui.browser.WebViewActivity
import com.doubtnutapp.ui.browser.WebViewFallback
import com.doubtnutapp.ui.cameraGuide.CameraGuideActivity
import com.doubtnutapp.ui.contest.ContestListActivity
import com.doubtnutapp.ui.dailyPrize.DailyPrizeActivity
import com.doubtnutapp.ui.downloadPdf.DownloadNShareActivity
import com.doubtnutapp.ui.downloadPdf.DownloadNShareLevelOneActivity
import com.doubtnutapp.ui.downloadPdf.DownloadNShareLevelTwoActivity
import com.doubtnutapp.ui.formulaSheet.FormulaSheetTopicActivity
import com.doubtnutapp.ui.games.DnGamesActivity
import com.doubtnutapp.ui.games.GamePlayerActivity
import com.doubtnutapp.ui.groupChat.GroupChatActivity
import com.doubtnutapp.ui.mockTest.MockTestActivity
import com.doubtnutapp.ui.mockTest.MockTestAnalysisActivity
import com.doubtnutapp.ui.mockTest.MockTestListActivity
import com.doubtnutapp.ui.mockTest.MockTestSubscriptionActivity
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.ui.test.QuizActivity
import com.doubtnutapp.ui.topperStudyPlan.ChapterDetailActivity
import com.doubtnutapp.ui.topperStudyPlan.TopperStudyPlanActivity
import com.doubtnutapp.utils.*
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.vipplan.ui.MyPlanActivity
import com.doubtnutapp.vipplan.ui.VipPlanActivity
import com.doubtnutapp.wallet.WalletActivity
import com.doubtnutapp.whatsappadmin.WhatsappAdminActivity
import com.doubtnutapp.widgets.base.BaseWidgetBottomSheetDialogFragment
import com.doubtnutapp.widgets.base.BaseWidgetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeeplinkAction @Inject constructor() : IDeeplinkAction {

    @Inject
    lateinit var actionHandlerEventManager: ActionHandlerEventManager

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var networkUtil: NetworkUtil

    @Inject
    lateinit var defaultDataStore: DefaultDataStore

    @Inject
    lateinit var deeplinkActionHelper: DeeplinkActionHelper

    private val eventTracker: Tracker

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
        eventTracker = DoubtnutApp.INSTANCE.getEventTracker()
    }

    override fun performAction(context: Context, deeplink: String?): Boolean {
        if (deeplink == null) return false
        if (shouldAllowedForGuestUser(context)) return false
        return performAction(context, deeplink, "NA")
    }

    override fun performAction(context: Context, deeplink: String?, source: String): Boolean {
        DoubtnutApp.INSTANCE.bus()?.send(PerformDeeplinkAction())
        if (deeplink == null) return false
        if (shouldAllowedForGuestUser(context)) return false
        return performAction(
            context,
            deeplink,
            Bundle().apply { putString(Constants.SOURCE, source) }
        )
    }

    override fun performAction(context: Context, deeplink: String?, bundle: Bundle?): Boolean {
        if (deeplink == null) return false
        if (shouldAllowedForGuestUser(context)) return false
        val deeplinkIntentSuccessPair = getIntentFromUri(context, deeplink, bundle)
        val intent = deeplinkIntentSuccessPair.first
        try {
            if (intent != null) {
                if (bundle != null && bundle.getBoolean(Constants.CLEAR_TASK)) {
                    val builder = TaskStackBuilder.create(context)
                    // if we are already going to MainActivity then dont add this intent
                    if (!intent.component?.shortClassName.orEmpty().contains(".MainActivity")) {
                        builder.addNextIntent(MainActivity.getStartIntent(context, false))
                    }
                    builder.addNextIntent(intent)
                    builder.startActivities()
                } else {
                    context.startActivity(intent)
                }
                return true
            } else if (deeplinkIntentSuccessPair.second.not()) {
                Log.e(
                    DeeplinkException("Deeplink params not as per required for deeplink: $deeplink"),
                    LOG_TAG
                )
            }
        } catch (e: Exception) {
            Log.e(e, LOG_TAG)
        }
        return false
    }

    fun getIntentFromUri(context: Context?, uri: String?, bundle: Bundle?): Pair<Intent?, Boolean> {
        if (context == null) return Pair(null, false)
        try {
            val parsedUri = if (TextUtils.isEmpty(uri)) {
                throw NullPointerException("Couldn't find URI")
            } else {
                Uri.parse(uri)
            }
            return getIntentFromUri(context, parsedUri, bundle)
        } catch (e: Exception) {
            Log.e(e, LOG_TAG)
        }
        return Pair(null, false)
    }

    private fun getIntentFromUri(
        context: Context,
        parsedUri: Uri?,
        bundle: Bundle?
    ): Pair<Intent?, Boolean> {
        try {
            if (!parseAndSave(context, parsedUri, bundle)) {
                return Pair(null, false)
            }
            val deeplinkIntentSuccessPair = getActionIntent(context, bundle)
            deeplinkIntentSuccessPair.first?.putExtra(Constants.DEEPLINK, parsedUri.toString())
            return deeplinkIntentSuccessPair
        } catch (exception: java.lang.Exception) {
            exception.printStackTrace()
        }
        return Pair(null, false)
    }

    private fun getActionIntent(context: Context, extras: Bundle?): Pair<Intent?, Boolean> {
        var intent: Intent? = null
        var success: Boolean? = null

        val deeplink: String = defaultPrefs(context).getString(Constants.DEEPLINK_URI, "")!!

        val source: String = extras?.getString(Constants.SOURCE) ?: "NA"
        val isNotificationSource = source == "notification"

        try {
            val deeplinkUri = Uri.parse(deeplink)

            val host = deeplinkUri.host ?: return Pair(null, false)

            when (AppActions.fromString(host)) {
                AppActions.VIDEO, AppActions.VOD_COMMENT -> {
                    val qid = deeplinkUri.getQueryParameter("qid")
                    val sid = deeplinkUri.getQueryParameter("sid") ?: ""
                    val playlist_id = deeplinkUri.getQueryParameter("playlist_id") ?: ""
                    val mcid = deeplinkUri.getQueryParameter("mc_id") ?: ""
                    val page = deeplinkUri.getQueryParameter("page") ?: Constants.DEEPLINK
                    val createNewInstance =
                        deeplinkUri.getBooleanQueryParameter("create_new_instance", false)
                    val startPosition =
                        deeplinkUri.getQueryParameter(Constants.VIDEO_START_POSITION)?.toLong()
                            ?: 0
                    val imageUrl = deeplinkUri.getQueryParameter(Constants.IMAGE_URL) ?: ""
                    val resourceType: String? = deeplinkUri.getQueryParameter("resource_type")
                    val doubtCommentId: String? =
                        deeplinkUri.getQueryParameter("doubt_comment_id").ifEmptyThenNull()

                    if (qid != null) {
                        if (isNotificationSource) {
                            actionHandlerEventManager.onNotificationOpen("video", "firebase")
                        } else if (source == AppActions.VIDEO_STICKY_NOTIFICATION.name) {
                            actionHandlerEventManager.eventWith(
                                EventConstants.EVENT_VIDEO_STICKY_CLICKED,
                                hashMapOf(
                                    Constants.QUESTION_ID to qid
                                ),
                                ignoreSnowplow = true
                            )
                        }
                        intent =
                            if (resourceType != null && resourceType == SOLUTION_RESOURCE_TYPE_TEXT) {
                                TextSolutionActivity.startActivity(
                                    context, qid, playlist_id, mcid,
                                    page, "", false, sid, "",
                                    isNotificationSource
                                )
                            } else {
                                VideoPageActivity.startActivity(
                                    context,
                                    qid,
                                    playlist_id,
                                    mcid,
                                    page,
                                    "",
                                    false,
                                    sid,
                                    "",
                                    isNotificationSource,
                                    startPositionInSeconds = startPosition,
                                    thumbnailUrl = imageUrl,
                                    source = source,
                                    openAnsweredDoubtWithCommentId = doubtCommentId,
                                    createNewInstance = createNewInstance
                                )
                            }

                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
                                params = hashMapOf<String, Any>().apply {
                                    put(EventConstants.QUESTION_ID, qid)
                                    put(EventConstants.SOURCE, source)
                                }
                            )
                        )
                        sendEventByQid(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, qid, source)
                        sendClevertapEventByQid(
                            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
                            qid,
                            page
                        )

//                        BranchIOUtils.userCompletedAction(
//                            EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
//                            JSONObject().apply {
//                                put(Constants.QUESTION_ID, qid)
//                            }
//                        )
                    }
                }
                AppActions.LIVE_CLASS -> {
                    var qid = deeplinkUri.getQueryParameter("id").orEmpty()
                    if (qid.isBlank()) {
                        qid = deeplinkUri.getQueryParameter("qid").orEmpty()
                    }
                    intent = VideoPageActivity.startActivity(
                        context = context,
                        questionId = qid,
                        page = deeplinkUri.getQueryParameter("page").orEmpty()
                    )
                }
                AppActions.EXTERNAL_URL -> {
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen("external_url", "firebase")
                    val url = deeplinkUri.getQueryParameter("url")
                    intent = if (!url!!.startsWith("http://") && !url.startsWith("https://")) {
                        Intent(Intent.ACTION_VIEW, Uri.parse("http://" + url))
                    } else {
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    }
                    sendEventForPage(EventConstants.PAGE_EXTERNAL_URL, source)
                }
                AppActions.USER_JOURNEY -> {
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen("user_journey", "firebase")
                    intent = Intent(context, MainActivity::class.java)
                    intent.action = Constants.NAVIGATE_LIBRARY
                    sendEventForPage(EventConstants.PAGE_MAIN_LIBRARY_PAGE, source)
                }
                AppActions.PLAYLIST -> {
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen("playlist", "firebase")
                    val playlistId = deeplinkUri.getQueryParameter("playlist_id")
                    val playlistTitle = deeplinkUri.getQueryParameter("playlist_title")
                    var page = deeplinkUri.getQueryParameter("page")
                    val packageDetailsId = deeplinkUri.getQueryParameter("package_details_id")
                    val questionIds = deeplinkUri.getQueryParameter(Constants.QUESTION_IDS)

                    if (Constants.PAGE_SEARCH_SRP == source && page.isNullOrEmpty()) {
                        page = source
                    }
                    if (playlistId == Constants.NCERT_PLAYLIST_ID ||
                        playlistId == Constants.JEE_MAIN ||
                        playlistId == Constants.JEE_ADVANCE ||
                        playlistId == Constants.BOARDS_10 ||
                        playlistId == Constants.BOARDS_12 ||
                        deeplinkUri.getQueryParameter("is_last") == "0"
                    ) {
                        val packageDetailsId = deeplinkUri.getQueryParameter("package_details_id")
                            ?: ""
                        intent = Intent(context, LibraryListingActivity::class.java)
                        intent.action = Constants.NAVIGATE_LIBRARY
                        intent.putExtra(Constants.PLAYLIST_ID, playlistId)
                        intent.putExtra(Constants.PLAYLIST_TITLE, playlistTitle)
                        intent.putExtra(Constants.PAGE, page)
                        intent.putExtra(
                            LibraryListingActivity.INTENT_EXTRA_PACKAGE_ID,
                            packageDetailsId
                        )
                    } else {
                        intent = Intent(context, FragmentHolderActivity::class.java)
                        intent.action = Constants.NAVIGATE_VIEW_PLAYLIST
                        intent.putExtra(Constants.PLAYLIST_ID, playlistId)
                        intent.putExtra(Constants.PLAYLIST_TITLE, playlistTitle)
                        intent.putExtra(Constants.PACKAGE_DETAIL_ID, packageDetailsId)
                        intent.putExtra(Constants.PAGE, page)
                        intent.putExtra(Constants.QUESTION_IDS, questionIds)
                    }
                    intent.putExtra(Constants.NAVIGATE_FROM_DEEPLINK, true)
                    sendEventForPage(EventConstants.EVENT_NAME_VIEW_PLAY_LIST_PAGE, source)
                }
                AppActions.PROFILE -> {
                    if (isNotificationSource) {
                        actionHandlerEventManager.onNotificationOpen("profile", "firebase")
                    }
                    val studentId = deeplinkUri.getQueryParameter("student_id") ?: ""
                    if (studentId.isEmpty()) {
                        intent = Intent(context, MainActivity::class.java)
                        intent.action = Constants.NAVIGATE_PROFILE
                    } else {
                        FragmentWrapperActivity.userProfile(context, studentId, source)
                        success = true
                    }
                    intent?.putExtra(
                        MainActivity.KEY_RECREATE, deeplinkUri.getBooleanQueryParameter(
                            MainActivity.KEY_RECREATE,
                            false
                        )
                    )
                }
                AppActions.EDIT_PROFILE -> {
                    val refreshHomeFeed = deeplinkUri.getBooleanQueryParameter(
                        MyBioActivity.PARAM_KEY_REFRESH_HOME_FEED,
                        false
                    )
                    intent = MyBioActivity.getStartIntent(context, refreshHomeFeed)
                }
                AppActions.COUUMUNITY_QUESTION -> {
                }
                AppActions.COURSE -> {
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen("course", "firebase")
                    intent = Intent(context, MainActivity::class.java)
                    intent.action = Constants.NAVIGATE_COURSE
                    sendEventForPage(EventConstants.PAGE_MAIN_TOPICS_PAGE, source)
                }
                AppActions.LEARN_CHAPTER -> {
                    var page = deeplinkUri.getQueryParameter("page")
                    if (Constants.PAGE_SEARCH_SRP == source && page.isNullOrEmpty()) {
                        page = source
                    }
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen("learn_chapter", "firebase")
                    intent = Intent(context, FragmentHolderActivity::class.java)
                    intent.action = Constants.NAVIGATE_COURSE_DETAIL_NOTIFICATION
                    intent.putExtra(Constants.CLASS, deeplinkUri.getQueryParameter(Constants.CLASS))
                    intent.putExtra(
                        Constants.COURSE,
                        deeplinkUri.getQueryParameter(Constants.COURSE)
                    )
                    intent.putExtra(
                        Constants.CHAPTER,
                        deeplinkUri.getQueryParameter(Constants.CHAPTER)
                    )
                    intent.putExtra(Constants.PAGE, page)
                    sendEventForPage(EventConstants.EVENT_NAME_CHAPTER_DETAILS_FRAGMENT, source)
                }

                AppActions.DAILY_CONTEST -> {
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen("daily_contest", "firebase")
                    intent = Intent(context, ContestListActivity::class.java)
                    intent.putExtra(Constants.NAVIGATE_FROM_DEEPLINK, true)
                    sendEventForPage(EventConstants.PAGE_CONTEST_LIST, source)
                }
                AppActions.DAILY_CONTEST_CONTEST_ID -> {
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen(
                            "daily_contest_with_contest_id",
                            "firebase"
                        )
                    intent = Intent(context, DailyPrizeActivity::class.java)
                    intent.putExtra(Constants.NAVIGATE_FROM_DEEPLINK, true)
                    intent.putExtra(
                        Constants.CONTEST_ID,
                        deeplinkUri.getQueryParameter(Constants.CONTEST_ID)
                    )
                    sendEventForPage(EventConstants.PAGE_DAILY_PRIZE_ACTIVITY, source)
                }
                AppActions.DOWNLOADPDF,
                AppActions.DOWNLOAD_PDF,
                -> {
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen("downloadpdf", "firebase")
                    intent = Intent(context, DownloadNShareActivity::class.java)
                    intent.putExtra(Constants.NAVIGATE_FROM_DEEPLINK, true)
                    sendEventForPage(EventConstants.PAGE_DOWNLOAD_N_SHARE_ACTIVITY, source)
                }
                AppActions.DOWNLOADPDF_LEVEL_ONE,
                AppActions.DOWNLOAD_PDF_LEVEL_ONE,
                -> {
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen(
                            "downloadpdf_level_one",
                            "firebase"
                        )
                    intent = Intent(context, DownloadNShareLevelOneActivity::class.java)
                    intent.putExtra(Constants.NAVIGATE_FROM_DEEPLINK, true)
                    intent.putExtra(
                        Constants.FILTER_PACKAGE,
                        deeplinkUri.getQueryParameter("pdf_package")
                    )
                    sendEventForPage(EventConstants.PAGE_DOWNLOAD_N_SHARE_LEVEL_ONE, source)
                }
                AppActions.DOWNLOADPDF_LEVEL_TWO,
                AppActions.DOWNLOAD_PDF_LEVEL_TWO,
                -> {
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen(
                            "downloadpdf_level_two",
                            "firebase"
                        )
                    intent = Intent(context, DownloadNShareLevelTwoActivity::class.java)
                    intent.putExtra(Constants.NAVIGATE_FROM_DEEPLINK, true)
                    intent.putExtra(
                        Constants.FILTER_PACKAGE,
                        deeplinkUri.getQueryParameter("pdf_package")
                    )
                    intent.putExtra(
                        Constants.FILTER_LEVEL_ONE,
                        deeplinkUri.getQueryParameter("level_one")
                    )
                    sendEventForPage(EventConstants.PAGE_DOWNLOAD_N_SHARE_LEVEL_TWO, source)
                }
                AppActions.QUIZ -> {
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen("quiz", "firebase")
                    intent = Intent(context, QuizActivity::class.java)
                    sendEventForPage(EventConstants.EVENT_NAME_QUIZ, source)
                }
                AppActions.CAMERA_GUIDE -> {
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen("camera_guide", "firebase")
                    intent = Intent(context, CameraGuideActivity::class.java)
                    sendEventForPage(EventConstants.EVENT_NAME_CAMERA_GUIDE_CLICKED, source)
                }
                AppActions.FORMULA_SHEET -> {
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen("formula_sheet", "firebase")
                    intent = Intent(context, FormulaSheetTopicActivity::class.java)
                    sendEventForPage(EventConstants.EVENT_NAME_FORMULA_SHEET_CLICK, source)
                }
                AppActions.PDF_VIEWER -> {
                    if (isNotificationSource) {
                        actionHandlerEventManager.onNotificationOpen("pdf_viewer", "firebase")
                        sendEventForPage(
                            EventConstants.EVENT_NAME_VIEW_PDF_FROM_NOTIFICATION,
                            source
                        )
                    }
                    intent = Intent(context, PdfViewerActivity::class.java).apply {
                        putExtra(
                            Constants.INTENT_EXTRA_PDF_URL,
                            deeplinkUri.getQueryParameter(("pdf_url"))
                        )
                    }
                }
                AppActions.GROUP_CHAT -> {
                    if (isNotificationSource) {
                        actionHandlerEventManager.onNotificationOpen("group_chat", "firebase")
                        sendEventForPage(
                            EventConstants.EVENT_NAME_GROUP_CHAT_FROM_NOTIFICATION,
                            source
                        )
                    }
                    intent = Intent(context, GroupChatActivity::class.java)
                }
                AppActions.DOUBTNUT_STICKERS -> {
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen(
                            "doubtnut_stickers",
                            "firebase"
                        )
                    intent = Intent(context, EntryActivity::class.java)
                    sendEventForPage(EventConstants.EVENT_NAME_DOUBTNUT_STICKERS, source)
                }
                AppActions.TEST_SERIES -> {
                    if (isNotificationSource)
                        actionHandlerEventManager.onNotificationOpen("test_series", "firebase")
                    intent = Intent(context, MockTestActivity::class.java)
                    sendEventForPage(EventConstants.EVENT_NAME_MOCK_TEST, source)
                }
                AppActions.CAMERA -> {
                    if (isNotificationSource) {
                        actionHandlerEventManager.onNotificationOpen("camera", "firebase")
                    }
                    val cropImageUrl = deeplinkUri.getQueryParameter("camera_crop_url")
                    intent = CameraActivity.getStartIntent(context, source, cropImageUrl)
                    // camera deeplink with crop url launches another deeplink for camera activity
                    // on top of existing one. so clear_top_flag used to clear old cameraActivity instance
                    if (cropImageUrl.isNotNullAndNotEmpty()) {
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    if (deeplinkUri.getQueryParameter("intent_flag")
                            .orEmpty() == "FLAG_ACTIVITY_CLEAR_TOP"
                    ) {
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    sendEventForPage(EventConstants.EVENT_NEW_HOME_ASK_QUESTION_CLICK, source)

                }
                AppActions.LIVE_CLASSES -> {
                    intent = Intent(context, MainActivity::class.java)
                    intent.action = Constants.NAVIGATE_LIBRARY
                    intent.putExtra(
                        Constants.LIBRARY_SCREEN_SELECTED_TAB,
                        deeplinkUri.getQueryParameter(Constants.LIBRARY_SCREEN_SELECTED_TAB)
                    )
                    intent.putExtra(
                        Constants.COURSE_ID,
                        deeplinkUri.getQueryParameter(Constants.COURSE_ID)
                    )
                    intent.putExtra(
                        Constants.SUBJECT,
                        deeplinkUri.getQueryParameter(Constants.SUBJECT)
                    )
                }
                AppActions.CHANGE_LANGUAGE -> {
                    intent = Intent(context, MainActivity::class.java).apply {
                        action = Constants.CHANGE_LANGUAGE
                    }
                }
                AppActions.CHANGE_CLASS -> {
                    intent = Intent(context, MainActivity::class.java).apply {
                        action = Constants.CHANGE_CLASS
                    }
                }
                AppActions.REDEEM_STORE -> {
                    intent = StoreActivity.getStartIntent(context)
                }
                AppActions.COURSE_DETAILS -> {
                    val id = deeplinkUri.getQueryParameter("id").orEmpty()
                    when {
                        id.contains("scholarship_test_") && !id.contains("TALENTHUNT") -> {
                            intent = ScholarshipActivity.getStartIntent(
                                context = context,
                                testId = id,
                                changeTest = deeplinkUri.getBooleanQueryParameter(
                                    ScholarshipActivity.CHANGE_TEST,
                                    false
                                )
                            )
                        }
                        deeplinkUri.getBooleanQueryParameter("bottom_sheet", false) -> {
                            intent = CourseActivityBottomSheet.startActivity(
                                context,
                                false,
                                deeplinkUri.getQueryParameter(CourseActivityV3.ASSORTMENT_ID)
                                    .orEmpty(),
                                deeplinkUri.getQueryParameter(Constants.SOURCE) ?: source.orEmpty(),
                                deeplinkUri.getQueryParameter(STUDENT_CLASS).orEmpty()
                            )
                        }
                        else -> {
                            intent = CourseActivityV3.startActivity(
                                context,
                                false,
                                deeplinkUri.getQueryParameter(CourseActivityV3.ASSORTMENT_ID)
                                    .orEmpty(),
                                deeplinkUri.getQueryParameter(Constants.SOURCE) ?: source.orEmpty(),
                                deeplinkUri.getQueryParameter(STUDENT_CLASS).orEmpty()
                            )
                        }
                    }
                }

                AppActions.LIBRARY_COURSE -> {
                    intent = Intent(context, MainActivity::class.java).also {
                        it.action = Constants.NAVIGATE_LIBRARY
                        it.putExtra(Constants.TAG, LibraryTabAdapter.TAG_MY_COURSES)
                        it.putExtra(
                            MainActivity.KEY_RECREATE, deeplinkUri.getBooleanQueryParameter(
                                MainActivity.KEY_RECREATE,
                                false
                            )
                        )
                    }
                }

                AppActions.COURSE_VIDEO -> {
                    VideoPageActivity.startActivity(
                        context,
                        deeplinkUri.getQueryParameter(Constants.NOTIFICATION_QID).orEmpty(),
                        "", "",
                        deeplinkUri.getQueryParameter(Constants.PAGE).orEmpty(),
                        "", false, "", "",
                        isNotificationSource
                    )
                }

                AppActions.VIP -> {
                    val vipSource = deeplinkUri.getQueryParameter("source")
                        ?: if (source != "NA") source else "notification"
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_NAME_DN_PLAN_VIEW,
                            params = hashMapOf(Pair(EventConstants.SOURCE, vipSource)),
                            ignoreSnowplow = true
                        )
                    )
                    val assortmentId = deeplinkUri.getQueryParameter("assortment_id") ?: ""
                    val switchAssortmentId =
                        deeplinkUri.getQueryParameter("switch_assortment") ?: ""
                    val variantId = deeplinkUri.getQueryParameter("variant_id") ?: ""
                    val couponCode = deeplinkUri.getQueryParameter("coupon_code") ?: ""

                    intent = if (!assortmentId.isNullOrBlank()) {
                        VipPlanActivity.getStartIntent(
                            context = context,
                            source = source,
                            vipSource = vipSource,
                            assortmentId = assortmentId,
                            variantId = variantId,
                            couponCode = couponCode,
                            switchAssortment = switchAssortmentId
                        )
                    } else {
                        PaymentPlanActivity.getStartIntent(
                            context, source, variantId.orEmpty(), couponCode,
                            switchAssortmentId = switchAssortmentId
                        )
                    }
                }
                AppActions.MY_PLAN -> {
                    intent = MyPlanActivity.getStartIntent(context = context)
                }
                AppActions.PAYMENT_UPI_SELECT -> {
                    val paymentBody = PaymentActivityBody(
                        paymentStartBody = PaymentStartBody(
                            paymentFor = "course_package",
                            method = "upi_select",
                            paymentStartInfo = PaymentStartInfo(
                                amount = null,
                                couponCode = null,
                                variantId = deeplinkUri.getQueryParameter("variant_id"),
                                paymentForId = null,
                                useWalletCash = false,
                                selectedWallet = null,
                                useWalletReward = false,
                                switchAssortmentId = ""
                            )
                        ),
                        cardDetails = null,
                        method = "upi_select",
                        type = "",
                        deeplink = null,
                        upi = null,
                        upiPackage = deeplinkUri.getQueryParameter("upi_package")
                    )
                    intent = PaymentActivity.getPaymentIntent(context, paymentBody)
                }
                AppActions.MATCH_NOTIFICATION, AppActions.MATCH_OCR_NOTIFICATION -> {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_NAME_MATCH_PAGE_NOTIFICATION_CLICK,
                            params = hashMapOf<String, Any>().apply {
                                put(
                                    EventConstants.IS_OCR_NOTIFICATION,
                                    deeplinkUri.getQueryParameter(Constants.ASK_IMAGE_OCR)
                                        .isNullOrBlank()
                                )
                                put(EventConstants.SOURCE, "notification")
                            },
                            ignoreSnowplow = true
                        )
                    )
                    intent = Intent(context, MatchQuestionActivity::class.java).also {
                        it.putExtra(
                            Constants.QUESTION_ID,
                            deeplinkUri.getQueryParameter(Constants.QUESTION_ID)
                        )
                        it.putExtra(
                            Constants.ASK_QUE_URI,
                            deeplinkUri.getQueryParameter(Constants.ASK_QUE_URI)
                        )
                        it.putExtra(
                            Constants.ASK_IMAGE_OCR,
                            deeplinkUri.getQueryParameter(Constants.ASK_IMAGE_OCR)
                        )
                        it.putExtra(
                            Constants.OCR_NOTIFICATION_ID,
                            deeplinkUri.getQueryParameter(Constants.NOTIFICATION_ID)
                        )
                    }
                }
                AppActions.MATCH_PAGE -> {
                    intent = Intent(context, MatchQuestionActivity::class.java).also {
                        it.putExtra(
                            Constants.ASK_QUE_URL,
                            deeplinkUri.getQueryParameter(Constants.ASK_QUE_URL)
                        )
                        it.putExtra(
                            Constants.UPLOADED_IMAGE_QUESTION_ID,
                            deeplinkUri.getQueryParameter(Constants.UPLOADED_IMAGE_QUESTION_ID)
                        )
                    }
                }
                AppActions.STICKY_NOTIFICATION -> {
                    val type = if (deeplinkUri.getQueryParameter(Constants.TYPE) == null) {
                        ""
                    } else {
                        deeplinkUri.getQueryParameter(Constants.TYPE)
                    }
                    when (type) {
                        Constants.IN_APP_SEARCH -> {
                            if (isNotificationSource)
                                actionHandlerEventManager.onNotificationOpen(
                                    Constants.QUICK_SEARCH + "_" + Constants.IN_APP_SEARCH,
                                    "sticky"
                                )
                            intent = InAppSearchActivity.getStartIntent(
                                context,
                                Constants.QUICK_SEARCH,
                                false
                            )
                        }
                        Constants.IN_APP_SEARCH_MIC -> {
                            if (isNotificationSource)
                                actionHandlerEventManager.onNotificationOpen(
                                    Constants.QUICK_SEARCH + "_" + Constants.IN_APP_SEARCH_MIC,
                                    "sticky"
                                )
                            intent = InAppSearchActivity.getStartIntent(
                                context,
                                Constants.QUICK_SEARCH,
                                true
                            )
                        }
                        Constants.CAMERA -> {
                            if (isNotificationSource)
                                actionHandlerEventManager.onNotificationOpen(
                                    Constants.QUICK_SEARCH + "_" + Constants.CAMERA,
                                    "sticky"
                                )
                            intent = CameraActivity.getStartIntent(context, source)
                        }
                        Constants.QUICK_SEARCH_SETTING -> {
                            if (isNotificationSource)
                                actionHandlerEventManager.onNotificationOpen(
                                    Constants.QUICK_SEARCH + "_" + Constants.QUICK_SEARCH_SETTING,
                                    "sticky"
                                )
                            intent = QuickSearchSettingActivity.getStartIntent(context)
                        }
                        else -> {
                            if (isNotificationSource)
                                actionHandlerEventManager.onNotificationOpen(
                                    Constants.QUICK_SEARCH + "_default",
                                    "sticky"
                                )
                            intent = InAppSearchActivity.getStartIntent(
                                context,
                                Constants.QUICK_SEARCH,
                                false
                            )
                        }
                    }
                }
                AppActions.VIDEO_STICKY_NOTIFICATION -> {
                    val qid = deeplinkUri.getQueryParameter("qid")
                    actionHandlerEventManager.eventWith(
                        EventConstants.EVENT_VIDEO_STICKY_SETTINGS_CLICKED,
                        hashMapOf(
                            Constants.QUESTION_ID to qid.orDefaultValue("")
                        ),
                        ignoreSnowplow = true
                    )
                    intent = QuickSearchSettingActivity.getStartIntent(context)
                }
                AppActions.FEEDS -> {
                    intent = Intent(context, MainActivity::class.java).apply {
                        action = Constants.NAVIGATE_FEED
                        putExtra(
                            MainActivity.KEY_RECREATE, deeplinkUri.getBooleanQueryParameter(
                                MainActivity.KEY_RECREATE,
                                false
                            )
                        )
                    }
                }
                AppActions.DICTIONARY -> {
                    intent = DictionaryActivity.getStartIntent(context, source)
                }
                AppActions.FEED_POST,
                AppActions.POST_DETAIL,
                -> {
                    intent = PostDetailActivity.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter(Constants.POST_ID)!!
                    )
                }
                AppActions.BOOKS,
                AppActions.NCERT,
                AppActions.TOPIC,
                AppActions.TOPIC_PARENT,
                -> {
                    var page = deeplinkUri.getQueryParameter("page")
                    if (Constants.PAGE_SEARCH_SRP == source && page.isNullOrEmpty()) {
                        page = source
                    }
                    if ((deeplinkUri.getQueryParameter(Constants.IS_LAST) ?: "0").equals("1")) {
                        intent = Intent(context, FragmentHolderActivity::class.java).also {
                            it.action = Constants.NAVIGATE_VIEW_PLAYLIST
                            it.putExtra(
                                Constants.PLAYLIST_ID,
                                deeplinkUri.getQueryParameter(Constants.PLAYLIST_ID)
                            )
                            it.putExtra(
                                Constants.PLAYLIST_TITLE,
                                deeplinkUri.getQueryParameter(Constants.PLAYLIST_TITLE)
                            )
                            it.putExtra(Constants.PAGE, page)
                        }
                    } else {
                        val id =
                            deeplinkUri.getQueryParameter(LibraryListingActivity.INTENT_EXTRA_PLAYLIST_ID)
                                ?: ""
                        val title =
                            deeplinkUri.getQueryParameter(LibraryListingActivity.INTENT_EXTRA_PLAYLIST_TITLE)
                                ?: ""
                        val packageDetailsId = deeplinkUri.getQueryParameter("package_details_id")
                            ?: ""

                        intent = LibraryListingActivity.getStartIntent(
                            context,
                            id,
                            title,
                            packageDetailsId,
                            page
                        )
                    }
                }
                AppActions.DAILY_STREAK_BADGE -> {
                    intent = Intent(context, MainActivity::class.java).apply {
                        action = Constants.NAVIGATE_PROFILE
                    }
                }
                AppActions.GAMIFICATION_BADGE -> {
                    val userId = deeplinkUri.getQueryParameter(Constants.SID)!!
                    intent = BadgesActivity.startActivity(context, userId)
                }
                AppActions.LIVE_VOICE_CALL -> {
                    showToast(context, "This chat room no longer exists!")
                }
                AppActions.OLD_FEED_DETAILS -> {
                }
                AppActions.LIBRARY_TAB -> {
                    intent = Intent(context, MainActivity::class.java).also {
                        it.action = Constants.NAVIGATE_LIBRARY
                        var selectedTab = "0"
                        var mSource = ""
                        deeplinkUri.getQueryParameter("id")?.let { selectedTab = it }
                        deeplinkUri.getQueryParameter(Constants.LIBRARY_SCREEN_SELECTED_TAB)
                            ?.let { selectedTab = it }
                        deeplinkUri.getQueryParameter(Constants.SOURCE)
                            ?.let { mSource = it }
                        it.putExtra(Constants.LIBRARY_SCREEN_SELECTED_TAB, selectedTab)
                        it.putExtra(Constants.SOURCE, mSource)
                        it.putExtra(Constants.TAG, deeplinkUri.getQueryParameter(Constants.TAG))
                        it.putExtra(
                            MainActivity.KEY_RECREATE, deeplinkUri.getBooleanQueryParameter(
                                MainActivity.KEY_RECREATE,
                                false
                            )
                        )
                    }
                }
                AppActions.DN_GAMES -> {
                    intent = Intent(context, DnGamesActivity::class.java)
                    sendEventForPage(EventConstants.EVENT_GAME_SECTION_VIEW, source)
                }
                AppActions.LIVE_CLASS_HOME -> {
                    intent = Intent(context, MainActivity::class.java).also {
                        it.action = Constants.NAVIGATE_LIBRARY
                        it.putExtra(Constants.TAG, LibraryTabAdapter.TAG_MY_COURSES)
                    }
                }
                AppActions.COURSE_IIT -> {
                    intent = Intent(context, MainActivity::class.java).also {
                        it.action = Constants.NAVIGATE_LIBRARY
                        it.putExtra(Constants.TAG, LibraryTabAdapter.TAG_MY_COURSES)
                    }
                }
                AppActions.SCHEDULE -> {
                    intent = Intent(context, MainActivity::class.java).also {
                        it.action = Constants.NAVIGATE_LIBRARY
                        it.putExtra(Constants.TAG, LibraryTabAdapter.TAG_TIMETABLE)
                    }
                }
                AppActions.COURSE_NEET -> {
                    intent = Intent(context, MainActivity::class.java).also {
                        it.action = Constants.NAVIGATE_LIBRARY
                        it.putExtra(Constants.TAG, LibraryTabAdapter.TAG_MY_COURSES)
                    }
                }
                AppActions.TIME_TABLE -> {
                    intent = TimetableActivity.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter(TimetableActivity.COURSE_ID).orEmpty()
                    )
                }
                AppActions.MOCK_TEST_SUBSCRIBE -> {
                    intent = MockTestSubscriptionActivity.getStartIntent(
                        context = context,
                        testId = deeplinkUri.getQueryParameter(Constants.ID)?.toIntOrNull() ?: 0,
                        isRetryEnabled = true,
                        source = deeplinkUri.getQueryParameter(Constants.SOURCE),
                        examType = deeplinkUri.getQueryParameter(Constants.EXAM_TYPE),
                        ruleId = deeplinkUri.getQueryParameter(Constants.RULE_ID)?.toIntOrNull(),
                    )
                }
                AppActions.GAME -> {
                    if (deeplinkUri.getQueryParameter(Constants.GAME_URL).isNullOrEmpty()) {
                        intent = Intent(context, DnGamesActivity::class.java)
                    } else {
                        intent = GamePlayerActivity.getIntent(
                            context,
                            deeplinkUri.getQueryParameter(Constants.GAME_TITLE),
                            deeplinkUri.getQueryParameter(Constants.GAME_URL)!!,
                            deeplinkUri.getQueryParameter(Constants.GAME_ID) ?: ""
                        )
                    }
                }
                AppActions.WEB_VIEW -> {
                    val chromeCustomTab =
                        deeplinkUri.getBooleanQueryParameter(Constants.CHROME_CUSTOM_TAB, true)
                    val title = deeplinkUri.getQueryParameter(Constants.TITLE)
                    deeplinkUri.getQueryParameter(Constants.URL)?.let {
                        if (chromeCustomTab) {
                            CustomTabActivityHelper.openCustomTab(
                                context,
                                CustomTabsIntent.Builder().build(),
                                it.toUri(),
                                WebViewFallback()
                            )
                        } else {
                            intent = WebViewActivity.getIntent(context, it, title)
                        }
                    }
                }
                AppActions.ACTION_WEB_VIEW -> {
                    deeplinkUri.getQueryParameter(Constants.URL)?.let {
                        intent = HandleActionWebViewActivity.getStartIntent(context, it)
                    }
                }
                AppActions.IN_APP_SEARCH -> {
                    intent = InAppSearchActivity.getStartIntent(
                        context,
                        source,
                        false,
                        deeplinkUri.getQueryParameter(Constants.SEARCH_QUERY)
                    )
                }
                AppActions.IN_APP_SEARCH_LANDING -> {
                    val selectedClass =
                        if (deeplinkUri.getQueryParameter(Constants.SELECTED_CLASS_NAME)
                                .isNullOrEmpty() || deeplinkUri.getQueryParameter(Constants.SELECTED_CLASS_NAME)
                                .isNullOrEmpty()
                        )
                            null
                        else
                            ClassListViewItem(
                                deeplinkUri?.getQueryParameter(Constants.SELECTED_CLASS_NO)?.toInt()
                                    ?: 0,
                                deeplinkUri?.getQueryParameter(Constants.SELECTED_CLASS_NAME)
                                    ?: ""
                            )
                    intent = InAppSearchActivity.getStartIntent(
                        context,
                        source,
                        false,
                        null,
                        selectedClass
                    )
                }
                AppActions.PERSONALIZE -> {
                    intent = TopperStudyPlanActivity.getStartIntent(context)
                }

                AppActions.PERSONALIZE_CHAPTER -> {
                    intent = ChapterDetailActivity.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter(Constants.CHAPTER_ID)?.toLongOrNull()
                            ?: 0,
                        deeplinkUri.getQueryParameter(Constants.CHAPTER_TITLE) ?: " "
                    )
                }
                AppActions.FEED_LIVE_POST -> {
                    intent = LiveFeedActivity.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter(Constants.TYPE)
                    )
                }
                AppActions.GO_LIVE -> {
                    intent = LiveActivity.getStartIntent(context, LiveActivity.TYPE_SCHEDULE_LIVE)
                }

                AppActions.VIP_DETAIL -> {
                    intent = VipDetailActivity.getStartIntent(context)
                }

                AppActions.LIVE_CLASS_CHAT -> {
                    intent = LiveClassChatActivity.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter(Constants.ASSORTMENT_ID).orEmpty(),
                        deeplinkUri.getQueryParameter(LiveClassChatActivity.CHAT_ROOM_TYPE)
                    )
                }

                AppActions.DOUBT_PACKAGE -> {
                    intent = DoubtPackageActivity.getStartIntent(context)
                }

                AppActions.RESOURCE_LIST -> {
                    intent = ResourceListActivity.startActivity(
                        context,
                        deeplinkUri.getQueryParameter(Constants.ID).orEmpty(),
                        deeplinkUri.getQueryParameter(ResourceListActivity.SUBJECT).orEmpty()
                    )
                }

                AppActions.COURSE_CATEGORY -> {
                    intent = CourseCategoryActivity.startActivity(
                        context,
                        start = false,
                        categoryId = deeplinkUri.getQueryParameter(CourseCategoryActivity.CATEGORY_ID)
                            .orEmpty(),
                        title = deeplinkUri.getQueryParameter(CourseCategoryActivity.TITLE)
                            .orEmpty(),
                        filters = deeplinkUri.getQueryParameter(CourseCategoryActivity.FILTERS),
                        source = source
                    )
                }

                AppActions.WALLET -> {
                    intent = WalletActivity.getStartIntent(context)
                }

                AppActions.APB_CASH_PAYMENTS -> {
                    intent = ApbCashPaymentActivity.getStartIntent(context)
                }

                AppActions.HOME_WORK -> {
                    intent = HomeWorkActivity.startActivity(
                        context,
                        false,
                        deeplinkUri.getQueryParameter(Constants.Q_ID).orEmpty()
                    )
                }

                AppActions.HOME_WORK_SOLUTION -> {
                    intent = HomeWorkSolutionActivity.startActivity(
                        context,
                        false,
                        deeplinkUri.getQueryParameter(Constants.Q_ID).orEmpty()
                    )
                }

                AppActions.HOME_WORK_LIST -> {
                    intent = MyHomeWorkActivity.startActivity(context, false)
                }

                AppActions.DAILY_TOPPER -> {
                    intent = TopperStudyPlanActivity.getStartIntent(context)
                }

                AppActions.MY_DOWNLOADS -> {
                    intent = DownloadedVideosActivity.getStartIntent(context)
                }

                AppActions.WHATSAPP -> {
                    if (AppUtils.appInstalledOrNot(Constants.WHATSAPP_PACKAGE_NAME, context)) {
                        if (deeplink.contains(Constants.EXTERNAL_URL)) { // Check whether external_url exist or not
                            intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(deeplink.substringAfter(Constants.EXTERNAL_URL + "="))
                            ) // put the whastapp link here
                        }
                    } else {
                        showToast(context, "Whatsapp not installed", Toast.LENGTH_SHORT)
                    }
                }
                AppActions.HOME -> {
                    intent = MainActivity.getStartIntent(
                        context = context,
                        recreate = deeplinkUri.getBooleanQueryParameter(
                            MainActivity.KEY_RECREATE,
                            false
                        ),
                        scrollToId = deeplinkUri.getQueryParameter(
                            MainActivity.KEY_SCROLL_TO_ID
                        )
                    ).also {
                        val navSource = deeplinkUri.getQueryParameter("nav_source").orEmpty()
                        if (navSource.isNotBlank()) {
                            it.putExtra("nav_source", navSource)
                        }
                        it.action = Constants.NAVIGATE_HOME
                    }
                }

                AppActions.REFERRAL_PAGE, AppActions.REFERRAL -> {
                    intent = ReferralActivityV2.getDeeplinkStartIntent(context, deeplinkUri)
                }
                AppActions.SHARE_REFERRAL -> {
                    intent = BottomSheetHolderActivity.getShareReferralStartIntent(
                        context = context,
                        shareMessage = deeplinkUri.getQueryParameter("share_message").orEmpty(),
                        shareContactBatchSize = deeplinkUri.getQueryParameter("share_contact_batch_size")
                    )
                }

                AppActions.TRANSACTION_HISTORY -> {
                    intent = Intent(context, TransactionHistoryActivityV2::class.java)
                }

                AppActions.FAQ -> {
                    intent = FaqActivity.startActivity(
                        context, deeplinkUri.getQueryParameter(FaqActivity.BUCKET).orEmpty(),
                        deeplinkUri.getQueryParameter(FaqActivity.PRIORITY).orEmpty()
                    )
                }

                AppActions.COURSE_DETAIL_INFO -> {
                    intent = CourseDetailActivityV3.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter(Constants.ASSORTMENT_ID).orEmpty(),
                        deeplinkUri.getQueryParameter(Constants.TAB).orEmpty(),
                        deeplinkUri.getQueryParameter(Constants.SUBJECT).orEmpty(),
                        deeplinkUri.getBooleanQueryParameter(
                            CourseDetailActivityV3.PARAM_FILTER_V2,
                            false
                        ),
                        source.orEmpty(),
                        extras?.getString(Constants.SEARCH_ID).orEmpty()
                    )
                }

                AppActions.COURSE_SELECTION_DIALOG -> {
                    intent = CourseSelectionActivity.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter(Constants.PAGE).orEmpty()
                    )
                }

                AppActions.BUNDLE_DIALOG -> {
                    intent = BundleActivity.getStartIntent(
                        context, deeplinkUri.getQueryParameter(Constants.ID).orEmpty(),
                        deeplinkUri.getQueryParameter(Constants.SOURCE).orEmpty()
                    )
                }

                AppActions.SUBJECT_DETAL -> {
                    intent = SubjectDetailActivity.startActivity(
                        context, deeplinkUri.getQueryParameter(Constants.SUBJECT).orEmpty(),
                        deeplinkUri.getQueryParameter(Constants.ASSORTMENT_ID).orEmpty()
                    )
                }

                AppActions.REWARDS -> {
                    intent = RewardActivity.getStartIntent(context)
                }

                AppActions.VIDEO_DIALOG -> {
                    val orientation =
                        deeplinkUri.getQueryParameter(VideoDialogActivity.ORIENTATION).orEmpty()
                    intent = VideoDialogActivity.getStartIntent(
                        context,
                        orientation,
                        deeplinkUri.getQueryParameter(VideoDialogActivity.QUESTION_ID).orEmpty(),
                        deeplinkUri.getQueryParameter(VideoDialogActivity.PAGE).orEmpty()
                    )
                }

                AppActions.TOPIC_BOOSTER_GAME -> {
                    intent = TopicBoosterGameActivity.getStartIntent(
                        context, deeplinkUri.getQueryParameter("qid").orEmpty()
                    )
                }

                AppActions.DOUBT_PE_CHARCHA -> {
                    val roomId = deeplinkUri.getQueryParameter(DoubtP2pActivity.PARAM_KEY_ROOM_ID)
                    val isHost = deeplinkUri.getBooleanQueryParameter(
                        DoubtP2pActivity.PARAM_KEY_IS_HOST,
                        false
                    )
                    val isReply = deeplinkUri.getBooleanQueryParameter(
                        DoubtP2pActivity.PARAM_KEY_IS_REPLY,
                        false
                    )
                    val isMessage = deeplinkUri.getBooleanQueryParameter(
                        DoubtP2pActivity.PARAM_KEY_IS_MESSAGE,
                        false
                    )

                    val showHelpPage = deeplinkUri.getBooleanQueryParameter(
                        DoubtP2pActivity.PARAM_SHOW_HELP_PAGE, false
                    )

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.P2P_NOTIFICATION_CLICKED,
                            params = hashMapOf<String, Any>().apply {
                                put(EventConstants.P2P_IS_HOST, isHost)
                                put(EventConstants.SOURCE, source)
                            }
                        )
                    )
                    intent = if (isHost) {
                        DoubtP2pActivity.getStartIntent(
                            context = context,
                            roomId = roomId,
                            isHost = isHost,
                            isMessage = isMessage,
                            source = source
                        )
                    } else if (showHelpPage) {
                        DoubtP2pHelperEntryActivity.getStartIntent(
                            context = context,
                            roomId = roomId,
                            isMessage = isMessage,
                            source = source
                        )
                    } else {
                        if (isReply) {
                            DoubtP2pActivity.getStartIntent(
                                context = context,
                                roomId = roomId,
                                isMessage = isMessage,
                                source = source
                            )
                        } else {
                            DoubtP2pActivity.getStartIntent(
                                context = context,
                                roomId = roomId,
                                isMessage = isMessage,
                                source = source
                            )
                        }
                    }
                }

                AppActions.APP_SURVEY -> {
                    intent = BottomSheetHolderActivity.getSurveyStartIntent(
                        context = context,
                        id = deeplinkUri.getQueryParameter("survey_id")
                            ?.toLongOrNull()
                            ?: 0,
                        page = deeplinkUri.getQueryParameter(Constants.PAGE),
                        type = deeplinkUri.getQueryParameter(Constants.TYPE)
                    )
                }

                AppActions.COURSE_DETAILS_BOTTOM_SHEET -> {
                    val ids = ArrayList<String>()
                    deeplinkUri.getQueryParameter("ids")?.split(",")?.forEach {
                        ids.add(it)
                    }
                    intent = BottomSheetHolderActivity.getCourseDetailsStartIntent(
                        context = context,
                        ids = ids,
                        position = deeplinkUri.getQueryParameter("position")
                            ?.toIntOrNull()
                            ?: 0,
                        flagrId = extras?.getString(EventConstants.FLAG_ID),
                        variantId = extras?.getString(EventConstants.VARIANT_ID),
                        source = extras?.getString(EventConstants.SOURCE),
                        deeplinkSource = deeplinkUri.getQueryParameter("source")
                    )
                }

                AppActions.VIDEO_URL -> {
                    intent = DialogHolderActivity.getVideoWithUrlIntent(
                        context,
                        deeplinkUri.getQueryParameter("url").orEmpty()
                    )
                }

                AppActions.COMMUNITY_GUIDELINES -> {
                    intent = CommunityGuidelinesActivity.getIntent(
                        context,
                        deeplinkUri.getQueryParameter(CommunityGuidelinesActivity.SOURCE)
                    )
                }

                AppActions.FULL_SCREEN_IMAGE -> {
                    intent = FullImageViewActivity.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter(FullImageViewActivity.INTENT_EXTRA_ASKED_QUESTION_URI)
                            .orDefaultValue(),
                        deeplinkUri.getQueryParameter(FullImageViewActivity.INTENT_EXTRA_TITLE)
                    )
                }

                AppActions.DOUBT_FEED -> {
                    intent = Intent(context, MainActivity::class.java).apply {
                        action = Constants.NAVIGATE_DOUBT_FEED
                        putExtra(Constants.DOUBT_FEED_REFRESH, true)
                    }
                }

                AppActions.NUDGE_POPUP -> {
                    intent = NudgeActivity.getStartIntent(
                        context = context,
                        id = deeplinkUri.getQueryParameter(Constants.NUDGE_ID).orEmpty(),
                        nudgeType = deeplinkUri.getQueryParameter(Constants.NUDGE_TYPE).orEmpty(),
                        page = deeplinkUri.getQueryParameter(Constants.PAGE),
                        type = deeplinkUri.getQueryParameter(Constants.TYPE),
                        isTransparent = deeplinkUri.getBooleanQueryParameter(
                            NudgeFragment.IS_TRANSPARENT,
                            true
                        )
                    )
                }

                AppActions.COURSE_CHANGE -> {
                    intent = CourseSwitchActivity.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter(Constants.POPUP_TYPE).orEmpty(),
                        deeplinkUri.getQueryParameter(Constants.SELECTED_ASSORTMENT).orEmpty(),
                        deeplinkUri.getQueryParameter(Constants.ASSORTMENT_ID).orEmpty()
                    )
                }

                AppActions.COURSE_CHANGE_OPTION -> {
                    intent = CourseSelectActivity.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter(Constants.ASSORTMENT_ID).orEmpty()
                    )
                }

                AppActions.LEADERBOARD -> {
                    intent = LeaderboardActivity.getStartIntent(
                        context = context,
                        source = deeplinkUri.getQueryParameter(Constants.SOURCE).orEmpty(),
                        assortmentId = deeplinkUri.getQueryParameter(Constants.ASSORTMENT_ID),
                        testId = deeplinkUri.getQueryParameter(Constants.TEST_ID),
                        type = deeplinkUri.getQueryParameter(Constants.TYPE),
                    ).apply {
                        if (deeplinkUri.getBooleanQueryParameter(Constants.CLEAR_TOP, true)) {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        }
                    }
                }

                AppActions.WHATSAPP_ADMIN_FORM -> {
                    intent = WhatsappAdminActivity.getStartIntent(
                        context,
                        source.orEmpty()
                    )
                }

                AppActions.STUDY_GROUP, AppActions.STUDY_GROUP_CHAT, AppActions.STUDY_GROUP_V2 -> {
                    intent = StudyGroupActivity.getDeeplinkStartIntent(context, deeplinkUri)
                }

                AppActions.DOUBTNUT_RUPYA -> {
                    intent = DnrActivity.getDeeplinkStartIntent(context, deeplinkUri)
                }

                AppActions.OLYMPIAD, AppActions.OLYMPIAD_REGISTER, AppActions.OLYMPIAD_SUCCESS -> {
                    intent = OlympiadActivity.getDeeplinkStartIntent(context, deeplinkUri)
                }

                AppActions.ICONS -> {
                    intent = IconsActivity.getDeeplinkStartIntent(context, deeplinkUri)
                }

                AppActions.DOUBTS -> {
                    DoubtnutApp.INSTANCE.bus()?.send(SeeDoubtsAction())
                    success = true
                }

                AppActions.SHARE -> {
                    DoubtnutApp.INSTANCE.bus()?.send(
                        SingleEvent(
                            Share(
                                message = deeplinkUri.getQueryParameter("message").orEmpty(),
                                imageUrl = deeplinkUri.getQueryParameter("image_url").orEmpty(),
                                appName = deeplinkUri.getQueryParameter("app_name").orEmpty(),
                                packageName = deeplinkUri.getQueryParameter("package_name")
                                    .orEmpty(),
                                skipBranch = deeplinkUri.getBooleanQueryParameter(
                                    "skip_branch",
                                    true
                                )
                            )
                        )
                    )
                    success = true
                }

                AppActions.CHAT_SUPPORT -> {
                    if (FeaturesManager.isFeatureEnabled(
                            DoubtnutApp.INSTANCE,
                            Features.STUDY_GROUP_AS_FRESH_CHAT
                        )
                    ) {
                        val map = HashMap<String, Any>()
                        map["is_support"] = true
                        DataHandler.INSTANCE.microService.get().createGroup(
                            map.toRequestBody()
                        ).applyIoToMainSchedulerOnSingle()
                            .subscribeToSingle(
                                success = { response ->
                                    response.data.groupChatDeeplink?.let {
                                        performAction(context, it)
                                    }
                                }
                            )
                    } else {
                        val assortmentId = deeplinkUri.getQueryParameter("assortment_id").orEmpty()
                        val variantId = deeplinkUri.getQueryParameter("variant_id").orEmpty()
                        val courseName = deeplinkUri.getQueryParameter("course_name").orEmpty()
                        val duration = deeplinkUri.getQueryParameter("duration").orEmpty()
                        val linkSource =
                            deeplinkUri.getQueryParameter("source").orDefaultValue("deeplink")
                        ChatUtil.setUser(
                            context,
                            assortmentId,
                            variantId,
                            courseName,
                            duration, linkSource
                        )
                        ChatUtil.startConversation(context)
                    }
                }

                AppActions.DOUBT_P2P_COLLECTION -> {
                    val subjects = ArrayList<String>()
                    val classes = ArrayList<String>()
                    val languages = ArrayList<String>()
                    deeplinkUri.getQueryParameter("subjects")?.split(",")?.forEach {
                        subjects.add(it)
                    }
                    deeplinkUri.getQueryParameter("classes")?.split(",")?.forEach {
                        classes.add(it)
                    }
                    deeplinkUri.getQueryParameter("languages")?.split(",")?.forEach {
                        languages.add(it)
                    }
                    val primaryTabId = deeplinkUri.getQueryParameter("primary_tab_id").orEmpty()
                    val secondaryTabId = deeplinkUri.getQueryParameter("secondary_tab_id").orEmpty()
                    intent = P2PDoubtCollectionActivity.getStartIntent(
                        context, primaryTabId, secondaryTabId, subjects,
                        classes, languages
                    )
                }

                AppActions.COURSE_RECOMMENDATION -> {
                    intent = CourseRecommendationActivity.getStartIntent(
                        context,
                        deeplinkUri.getBooleanQueryParameter("is_back", false),
                        deeplinkUri.getQueryParameter("page") ?: ""
                    )
                }

                AppActions.COURSE_EXPLORE -> {
                    if (defaultPrefs().getBoolean(Constants.SHOULD_SHOW_MY_COURSE, false)) {
                        intent = Intent(context, MainActivity::class.java).also {
                            it.action = Constants.NAVIGATE_LIBRARY
                            it.putExtra(Constants.TAG, LibraryTabAdapter.TAG_MY_COURSES)
                        }
                    } else {
                        intent = Intent(context, MainActivity::class.java).also {
                            it.action = Constants.NAVIGATE_LIBRARY
                            it.putExtra(Constants.TAG, LibraryTabAdapter.TAG_CHECK_ALL_COURSES)
                        }
                    }
                }

                AppActions.KHELO_JEETO -> {
                    intent = TopicBoosterGameActivity2.getDeeplinkStartIntent(context, deeplinkUri)
                }

                AppActions.DIALER -> {
                    try {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:" + deeplinkUri.getQueryParameter("mobile"))
                            )
                        )
                    } catch (e: Exception) {
                        // No Activity found to handle Intent { act=android.intent.action.DIAL dat=tel:xxxxxxxxxxx }
                        IntentUtils.showCallActionNotPerformToast(
                            context,
                            deeplinkUri.getQueryParameter("mobile").orEmpty()
                        )
                    }
                }

                AppActions.DOUBT_FEED_2 -> {
                    intent = DoubtFeedActivity.getDeeplinkStartIntent(context, deeplinkUri)
                }
                AppActions.BOTTOM_SHEET_WIDGET -> {
                    (context as? AppCompatActivity)?.run {
                        BaseWidgetBottomSheetDialogFragment.newInstance(
                            id = deeplinkUri.getQueryParameter("id").orEmpty(),
                            widgetType = deeplinkUri.getQueryParameter("widget_type").orEmpty(),
                            title = deeplinkUri.getQueryParameter("title").orEmpty(),
                            showCloseBtn = deeplinkUri.getBooleanQueryParameter(
                                "show_close_btn",
                                true
                            ),
                            openCount = deeplinkUri.getQueryParameter("openCount"),
                            questionCount = deeplinkUri.getQueryParameter("questionAskCount"),
                            userCategory = deeplinkUri.getQueryParameter("user_category").orEmpty()
                        ).show(
                            supportFragmentManager,
                            BaseWidgetBottomSheetDialogFragment.TAG
                        )
                        success = true
                    }
                }
                AppActions.PAGINATED_BOTTOM_SHEET_WIDGET -> {
                    (context as? AppCompatActivity)?.run {
                        BaseWidgetPaginatedBottomSheetDialogFragment.newInstance(
                            id = deeplinkUri.getQueryParameter(
                                BaseWidgetPaginatedBottomSheetDialogFragment.KEY_ID
                            ).orEmpty(),
                            type = deeplinkUri.getQueryParameter(
                                BaseWidgetPaginatedBottomSheetDialogFragment.KEY_TYPE
                            ).orEmpty(),
                            tabId = deeplinkUri.getQueryParameter(
                                BaseWidgetPaginatedBottomSheetDialogFragment.KEY_TAB_ID
                            ).orEmpty(),
                            showCloseBtn = deeplinkUri.getBooleanQueryParameter(
                                BaseWidgetPaginatedBottomSheetDialogFragment.KEY_SHOW_CLOSE_BTN,
                                true
                            )
                        ).show(
                            supportFragmentManager,
                            BaseWidgetPaginatedBottomSheetDialogFragment.TAG
                        )
                        success = true
                    }
                }

                AppActions.DIALOG_WIDGET -> {
                    (context as? AppCompatActivity)?.run {
                        BaseWidgetDialogFragment.newInstance(
                            widgetType = deeplinkUri.getQueryParameter("widget_type").orEmpty(),
                            studentId = deeplinkUri.getQueryParameter("student_id").orEmpty(),
                            assortmentId = deeplinkUri.getQueryParameter("assortment_id").orEmpty(),
                            testId = deeplinkUri.getQueryParameter("test_id").orEmpty(),
                            tabNumber = deeplinkUri.getQueryParameter("tab_number").orEmpty(),
                            showCloseBtn = deeplinkUri.getBooleanQueryParameter(
                                "show_close_btn",
                                true
                            )
                        ).show(
                            supportFragmentManager,
                            BaseWidgetDialogFragment.TAG
                        )
                        success = true
                    }
                }

                AppActions.DIALOG_DISMISS -> {
                    (context as? AppCompatActivity)?.run {
                        val tag = deeplinkUri.getQueryParameter("tag").orEmpty()
                        supportFragmentManager.findFragmentByTag(tag)?.let {
                            (it as? DialogFragment)?.dismiss()
                        }
                    }
                }

                AppActions.SUBMIT_ADDRESS_DIALOG -> {
                    intent = DialogHolderActivity.getSubmitAddressDialog(
                        context = context,
                        type = deeplinkUri.getQueryParameter("type").orEmpty(),
                        id = deeplinkUri.getQueryParameter("id").orEmpty(),
                    )
                }
                AppActions.EXAM_CORNER -> {
                    intent = ExamCornerActivity.getStartIntent(context)
                }
                AppActions.EXAM_CORNER_BOOKMARK -> {
                    intent = ExamCornerBookmarkActivity.getStartIntent(context)
                }

                AppActions.BOOK_CALL -> {
                    (context as? AppCompatActivity)?.run {
                        if (!networkUtil.isConnected()) {
                            toast(R.string.string_noInternetConnection)
                            return@run
                        }
                        BookCallDialogFragment.newInstance().show(
                            supportFragmentManager,
                            BookCallDialogFragment.TAG
                        )
                        success = true
                    }
                }

                AppActions.COUPON_LIST -> {
                    intent = DialogHolderActivity.getCouponListDialog(
                        context,
                        deeplinkUri.getQueryParameter("page").orEmpty()
                    )
                }

                AppActions.MOCK_TEST_LIST -> {
                    intent = MockTestListActivity.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter("course").orEmpty()
                    )
                }

                AppActions.MOCK_TEST_ANALYSIS -> {
                    intent = MockTestAnalysisActivity.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter("testId").orEmpty(),
                        deeplinkUri.getQueryParameter("subject").orEmpty(),
                        deeplinkUri.getQueryParameter("source").orEmpty()
                    )
                }

                AppActions.REVISION_CORNER -> {
                    intent = RevisionCornerActivity.getDeeplinkStartIntent(context, deeplinkUri)
                }

                AppActions.QUIZ_TFS -> {
                    intent = QuizTfsActivity.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter("class").orEmpty(),
                        deeplinkUri.getQueryParameter("subject").orEmpty(),
                        deeplinkUri.getQueryParameter("language").orEmpty()
                    )
                }

                AppActions.QUIZ_TFS_SOLUTION -> {
                    intent = QuizTfsSolutionActivity.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter("id").orEmpty(),
                        deeplinkUri.getQueryParameter("date").orEmpty()
                    )
                }

                AppActions.QUIZ_TFS_SELECTION -> {
                    intent = QuizTfsSelectionActivity.getStartIntent(
                        context,
                        source.orEmpty()
                    )
                }

                AppActions.QUIZ_TFS_ANALYSIS -> {
                    intent = QuizTfsAnalysisActivity.getStartIntent(
                        context,
                        source.orEmpty(),
                        deeplinkUri.getQueryParameter("date").orEmpty()
                    )
                }
                AppActions.DAILY_PRACTICE -> {
                    intent = DailyPracticeActivity.getStartIntent(
                        context,
                        source,
                        deeplinkUri.getQueryParameter("type").orEmpty()
                    )
                }
                AppActions.LIVE_QUESTIONS_HISTORY -> {
                    intent = HistoryActivity.getStartIntent(
                        context,
                        source
                    )
                }
                AppActions.MY_REWARDS -> {
                    intent = MyRewardsActivity.getStartIntent(
                        context,
                        source,
                        deeplinkUri.getQueryParameter("type").orEmpty()
                    )
                }
                AppActions.TEACHER_CHANNEL -> {
                    val teacherId =
                        deeplinkUri.getQueryParameter(Constants.TEACHER_ID)?.toInt() ?: -1
                    val teacherType =
                        deeplinkUri.getQueryParameter(Constants.TYPE) ?: ""
                    intent = TeacherChannelActivity.getStartIntent(
                        context,
                        teacherId,
                        teacherType,
                        deeplinkUri.getQueryParameter(Constants.TAB_FILTER),
                        deeplinkUri.getQueryParameter(Constants.SUB_FILTER),
                        deeplinkUri.getQueryParameter(Constants.CONTENT_FILTER),
                        source
                    )
                }
                AppActions.GOOGLE_AUTH -> {
                    intent = GoogleAuthActivity.getStartIntent(context)
                }
                AppActions.LIBRARY_PREVIOUS_YEAR_PAPERS -> {
                    intent = LibraryPreviousYearPapersActivity.getStartIntent(
                        context = context,
                        examId = deeplinkUri.getQueryParameter(LibraryPreviousYearPapersActivity.PARAM_EXAM_ID)
                            .orDefaultValue(),
                        source = source
                    )
                }
                AppActions.HISTORY -> {
                    intent = UserWatchedVideoActivity.getStartIntent(
                        context,
                        source
                    )
                }
                AppActions.SCHEDULER_LISTING -> {
                    intent = SchedulerListingActivity.getStartIntent(
                        context = context,
                        commaSeparatedFilters = deeplinkUri.getQueryParameter(
                            SchedulerListingActivity.COMMA_SEPARATED_SUBJECTS
                        ),
                        slot = deeplinkUri.getQueryParameter(SchedulerListingActivity.SLOT),
                    )
                }
                AppActions.PRACTICE_ENGLISH -> {
                    intent = PracticeEnglishActivity.getStartIntent(
                        context,
                        deeplinkUri.getQueryParameter(Constants.SESSION_ID).orEmpty()
                    )
                }
                AppActions.COURSE_BOTTOM_SHEET_V2 -> {
                    (context as? AppCompatActivity)?.run {
                        NextVideoDialogFragment.newInstance(
                            qid = deeplinkUri.getQueryParameter("qid").orEmpty(),
                            title = deeplinkUri.getQueryParameter("title").orEmpty(),
                            type = deeplinkUri.getQueryParameter("type")
                        ).show(
                            supportFragmentManager,
                            NextVideoDialogFragment.TAG
                        )
                        success = true
                    }
                }

                AppActions.FREE_TRIAL_COURSE -> {
                    intent = Intent(context, MainActivity::class.java)
                    intent?.action = Constants.NAVIGATE_TO_FREE_TRIAL_COURSE
                }
                AppActions.DN_APP_SETTINGS -> {
                    intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent?.data = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                }
                AppActions.TOP_ICONS -> {
                    intent = TopIconsActivity.getStartIntent(
                        context = context,
                        screen = deeplinkUri.getQueryParameter(TopIconsActivity.EXTRA_PARAM_SCREEN)
                            .orEmpty()
                    )
                }
                AppActions.ONE_TAP_POST_LIST -> {
                    val queryCarouselType = deeplinkUri.getQueryParameter("carousel_type")
                    intent = OneTapPostsListActivity.getStartIntent(context, queryCarouselType)
                }
                AppActions.EMAIL -> {
                    EmailUtils.sendEmail(
                        context = context,
                        email = arrayOf(deeplinkUri.getQueryParameter("email").orEmpty()),
                        subject = deeplinkUri.getQueryParameter("subject"),
                        message = deeplinkUri.getQueryParameter("message")
                    )
                }

                AppActions.MATCH_PAGE_BOOK_FEEDBACK -> {
                    (context as? AppCompatActivity)?.run {
                        MatchQuestionBookFeedbackDialogFragment.newInstance(
                            source = deeplinkUri.getQueryParameter(
                                MatchQuestionBookFeedbackDialogFragment.PARAM_KEY_SOURCE
                            ).orEmpty(),
                        ).show(
                            supportFragmentManager,
                            MatchQuestionBookFeedbackDialogFragment.TAG
                        )
                        success = true
                    }
                }
                AppActions.SHORTS -> {
                    val qid = deeplinkUri.getQueryParameter("qid")
                    val type = deeplinkUri.getQueryParameter("type")
                    val navSource = deeplinkUri.getQueryParameter("nav_source")
                    intent =
                        FragmentWrapperActivity.getShortsIntent(
                            context,
                            qid,
                            type ?: "DEFAULT",
                            navSource ?: "DEFAULT"
                        )
                    intent?.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }

                AppActions.REFER_AND_EARN -> {
                    intent = ReferAndEarnActivity.getStartIntent(
                        context,
                        ReferAndEarnActivity.NAVIGATE_HOME
                    )
                }

                AppActions.REFER_AND_EARN_FAQ -> {
                    intent = ReferAndEarnActivity.getStartIntent(
                        context,
                        ReferAndEarnActivity.NAVIGATE_FAQ
                    )
                    intent?.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }

                AppActions.REFERRAL_CODE_SHARE -> {
                    val query = deeplinkUri.getQueryParameter("referrer").orEmpty()
                    saveReferredIdInDb(query)
                }

                AppActions.COMMON_POP_UP -> {
                    DataHandler.INSTANCE.networkService.get().getInAppPopup(
                        page = deeplinkUri.getQueryParameter(Constants.PAGE),
                        sessionId = defaultPrefs().getInt(Constants.SESSION_COUNT, 1)
                    ).applyIoToMainSchedulerOnSingle()
                        .subscribeToSingle(
                            success = { response ->
                                deeplinkActionHelper.inAppPopupResponsePair = Pair(
                                    deeplinkUri.getQueryParameter(Constants.PAGE),
                                    response.data
                                )
                                deeplinkActionHelper.handleData(this)
                            }
                        )
                }
                AppActions.COPY -> {
                    context.copy(
                        text = deeplinkUri.getQueryParameter("text"),
                        label = deeplinkUri.getQueryParameter("label").orEmpty(),
                        toastMessage = deeplinkUri.getQueryParameter("toast_message")
                    )
                    success = true
                }

                AppActions.DOUBT_PE_CHARCHA_REWARDS -> {
                    intent = Intent(context, DoubtPeCharchaRewardsActivity::class.java)
                }

                AppActions.RESULT_PAGE -> {
                    intent = ResultPageActivity.getStartIntent(
                        context = context,
                        deeplinkUri.getQueryParameter(ResultPageActivity.PAGE)
                            .orEmpty(),
                        deeplinkUri.getQueryParameter(ResultPageActivity.TYPE)
                            .orEmpty(),
                        deeplinkUri.getQueryParameter(ResultPageActivity.SOURCE)
                            .orEmpty()
                    )
                }

                else -> {
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(
                DeeplinkException("Deeplink params not as per required for deeplink:$deeplink"),
                LOG_TAG
            )
        }
        defaultPrefs(context).edit { remove(Constants.DEEPLINK_URI) }
        return Pair(intent, success ?: intent != null)
    }

    private fun saveReferredIdInDb(referralID: String) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            defaultDataStore.set(DefaultDataStoreImpl.PREF_KEY_REFERRAL_ID, referralID)
        }
    }

    private fun parseAndSave(context: Context, parsedUri: Uri?, extras: Bundle?): Boolean {
        val host: String?
        if (parsedUri == null || parsedUri.scheme == null) return false
        try {
            host = parsedUri.host
            when (AppActions.fromString(host)) {
                // save any extra params here to be used later on
                AppActions.VIDEO -> {
                }
                else -> {
                }
            }
        } catch (e: Exception) {
        } finally {
            defaultPrefs(context).edit {
                putString(Constants.DEEPLINK_URI, parsedUri.toString())
            }
        }
        return true
    }

    private fun sendEvent(eventName: String, payloadData: String, source: String) {
        eventTracker.addEventNames(eventName)
            .addStudentId(getStudentId())
            .addScreenName(source)
            .addEventParameter(EventConstants.PAYLOAD_DATA, payloadData)
            .track()
    }

    private fun sendEventByQid(eventName: String, qid: String, source: String) {
        eventTracker.addEventNames(eventName)
            .addStudentId(getStudentId())
            .addScreenName(source)
            .addEventParameter(Constants.QUESTION_ID, qid)
            .track()
    }

    private fun sendClevertapEventByQid(eventName: String, qid: String, page: String) {
        eventTracker.addEventNames(eventName)
            .addStudentId(getStudentId())
            .addEventParameter(Constants.PAGE, page)
            .addEventParameter(Constants.QUESTION_ID, qid)
            .addEventParameter(Constants.STUDENT_CLASS, UserUtil.getStudentClass())
            .cleverTapTrack()
    }

    private fun sendEventForPage(eventName: String, source: String) {
        eventTracker.addEventNames(eventName)
            .addStudentId(getStudentId())
            .addScreenName(source)
            .track()
    }

    private fun getStudentId() =
        defaultPrefs(DoubtnutApp.INSTANCE).getString(Constants.STUDENT_ID, "").orDefaultValue()

    private fun shouldAllowedForGuestUser(context: Context): Boolean {
        if (UserUtil.getIsGuestLogin() && !defaultPrefs().getBoolean(
                Constants.ENABLE_DEEPLINK_GUEST_LOGIN,
                false
            )
        ) {
            FailedGuestLoginActivity.getStartIntent(
                context, popupDetails = PopupDetails(
                    imageUrl = null,
                    title = context.getString(R.string.guest_login_popup_title),
                    subtitle = null,
                    ctaText = context.getString(R.string.login)
                ),
                source = CameraActivity.TAG
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(this)
            }
            return true
        } else {
            return false
        }
    }

    companion object {
        private const val LOG_TAG = "DeeplinkAction"
    }
}
