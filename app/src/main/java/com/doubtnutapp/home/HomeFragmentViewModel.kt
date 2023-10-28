package com.doubtnutapp.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.EVENT_HOME
import com.doubtnut.analytics.EventConstants.EVENT_WHATSAPP_CARD_CLICK
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.constant.CoreConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnut.core.utils.ThreadUtils
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.common.ScreenEventMapper
import com.doubtnutapp.common.di.module.CommonRepository
import com.doubtnutapp.common.mapper.PopUpMapper
import com.doubtnutapp.common.model.PopUpDialog
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.TestDetails
import com.doubtnutapp.data.remote.models.Widgets
import com.doubtnutapp.db.DoubtnutDatabase
import com.doubtnutapp.db.entity.LocalOfflineOcr
import com.doubtnutapp.domain.common.entities.model.ApiPopUpData
import com.doubtnutapp.domain.common.interactor.CheckQuizAttempted
import com.doubtnutapp.domain.common.interactor.GetPopUpListUseCase
import com.doubtnutapp.domain.homefeed.interactor.*
import com.doubtnutapp.domain.library.entities.ClassListEntity
import com.doubtnutapp.domain.library.entities.ClassListViewEntity
import com.doubtnutapp.home.event.HomeEventManager
import com.doubtnutapp.home.mapper.ActionToScreenMapper
import com.doubtnutapp.home.model.HomeFeedViewItem
import com.doubtnutapp.home.model.PostStudentRatingFeedback
import com.doubtnutapp.home.model.QuizFeedViewItem
import com.doubtnutapp.libraryhome.library.mapper.ClassesViewItemMapper
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.plus
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.utils.AppUtils
import com.doubtnutapp.utils.BannerActionUtils
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.FileUtils
import com.doubtnutapp.videoPage.event.VideoPageEventManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.LinkProperties
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject

class
HomeFragmentViewModel @Inject constructor(
    compositeDis: CompositeDisposable,
    private val submitStudentRatingUseCase: SubmitStudentRatingUseCase,
    private val submitStudentFeedbackUseCase: SubmitStudentFeedbackUseCase,
    private val studentRatingCrossUseCase: StudentRatingCrossUseCase,
    private val getClassListAtHome: GetClassesListHome,
    private val classesViewMapper: ClassesViewItemMapper,
    private val getPopUpListUseCase: GetPopUpListUseCase,
    private val actionToScreenMapper: ActionToScreenMapper,
    private val popUpMapper: PopUpMapper,
    private val checkQuizAttempted: CheckQuizAttempted,
    private val whatsAppSharing: WhatsAppSharing,
    private val homeEventManager: HomeEventManager,
    private val videoPageEventManager: VideoPageEventManager,
    private val screenEventMapper: ScreenEventMapper,
    private val getIncompleteChapterHome: GetIncompleteChapterHome,
    private val doubtnutDatabase: DoubtnutDatabase,
    private val commonRepository: CommonRepository,
    private val userPreference: UserPreference
) : BaseViewModel(compositeDis) {

    companion object {
        private const val BRANCH_HOME_FEED_CHANNEL = "home_feed_share"
    }

    var extraParams: HashMap<String, Any> = hashMapOf()

    private val _popularCourseLiveData: MutableLiveData<Outcome<Widgets>> = MutableLiveData()
    val popularCourseLiveData: LiveData<Outcome<Widgets>>
        get() = _popularCourseLiveData

    val completeChapterLiveData = MutableLiveData<IncompleteChapterWidgetData>()

    private val _homeFeedListLiveData: MutableLiveData<Outcome<List<HomeFeedViewItem>>> =
        MutableLiveData()

    val homeFeedListLiveData: LiveData<Outcome<List<HomeFeedViewItem>>>
        get() = _homeFeedListLiveData

    private val _popUpListLiveData: MutableLiveData<Outcome<List<PopUpDialog>>> = MutableLiveData()

    val popUpListLiveData: LiveData<Outcome<List<PopUpDialog>>>
        get() = _popUpListLiveData

    private val _removeRatingFragmentLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val removeRatingFragmentLiveData: LiveData<Boolean>
        get() = _removeRatingFragmentLiveData

    private val _studentRatingSubmitLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val studentRatingSubmitLiveData: LiveData<Boolean>
        get() = _studentRatingSubmitLiveData

    private val _studentFeedbackSubmitLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val studentFeedbackSubmitLiveData: LiveData<Boolean>
        get() = _studentFeedbackSubmitLiveData

    private val _classesListLiveData = MutableLiveData<Outcome<ClassListViewEntity>>()

    val classesListLiveData: LiveData<Outcome<ClassListViewEntity>>
        get() = _classesListLiveData

    private val _eventLiveData: MutableLiveData<String> = MutableLiveData()

    val eventLiveData: LiveData<String>
        get() = _eventLiveData

    val pdfUriLiveData = MutableLiveData<Pair<File, String>>()
    val showShareProgressBar = MutableLiveData<Boolean>()

    val showWhatsAppShareProgressBar = whatsAppSharing.showWhatsAppProgressLiveData

    val whatsAppShareableData: LiveData<Event<Triple<String?, String?, String?>>>
        get() = whatsAppSharing.whatsAppShareableData

    val showWhatsappProgressLiveData: LiveData<Boolean>
        get() = whatsAppSharing.showWhatsAppProgressLiveData

    val gridSelectedItemList = MutableLiveData<HashSet<String>>()
    val gridSelectedItemSet = HashSet<String>()

    private val _gridListSubmitMessage: MutableLiveData<String> = MutableLiveData()
    val gridListSubmitMessage: LiveData<String>
        get() = _gridListSubmitMessage

    private val _refetchHomeFeed: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val refetchHomeFeed: LiveData<Event<Boolean>>
        get() = _refetchHomeFeed

    private val _searchHintMapLiveData = MutableLiveData<Map<String, List<String>?>>()
    val searchHintMapLiveData: LiveData<Map<String, List<String>?>>
        get() = _searchHintMapLiveData

    private val _latestOcrFromDb = MutableLiveData<LocalOfflineOcr>()
    val latestOcrFromDb: LiveData<LocalOfflineOcr>
        get() = _latestOcrFromDb

    private val _bottomSheetDeeplink = MutableLiveData<String>()
    val bottomSheetDeeplink: LiveData<String>
        get() = _bottomSheetDeeplink

    var otherSelectedBoardId: String = ""

    fun getIncompleteChapter() {
        compositeDisposable.add(
            getIncompleteChapterHome.execute(Unit)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    completeChapterLiveData.postValue(it)
                })
        )
    }

    fun checkOcrFromDb() {
        compositeDisposable + doubtnutDatabase.offlineOcrDao().getLatestOcr()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _latestOcrFromDb.postValue(it)
                },
                { it.printStackTrace() }
            )
    }

    fun getPopularCourses(page: String) {
        _popularCourseLiveData.value = Outcome.loading(true)
        compositeDisposable + DataHandler.INSTANCE.courseRepository.getPopoularCourse(page)
            .applyIoToMainSchedulerOnSingle()
            .map {
                it.data.widgets.map { widget ->
                    if (widget != null) {
                        if (widget.extraParams == null) {
                            widget.extraParams = hashMapOf()
                        }
                        widget.extraParams?.putAll(extraParams)
                    }
                }
                it.data
            }.subscribeToSingle({
                _popularCourseLiveData.value = Outcome.success(it)
                _popularCourseLiveData.value = Outcome.loading(false)
            }, {
                _popularCourseLiveData.value = Outcome.loading(false)
            })
    }

    fun getPopUpList() {
        compositeDisposable + getPopUpListUseCase
            .execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onPopUpListSuccess, this::onPopUpListError)
    }

    fun getBottomSheetData() {
        viewModelScope.launch {
            commonRepository.getBottomSheetData("home")
                .catch {
                    it.printStackTrace()
                }
                .collect {
                    _bottomSheetDeeplink.value = it.deeplink
                }

        }
    }

    fun handleAction(action: Any, context: Context) {
        when (action) {

            is OpenNCERTChapter -> openNcertChapterScreen(action)

            is PlayVideo -> openVideoScreen(action)

            is OpenPDFPage -> openPDFViewerScreen(action)

            is BannerAction -> handleBannerAction(action, context)

            is OpenContestPage -> openDailyPrizeScreen(action)

            is ShareOnWhatApp -> shareOnWhatsApp(context, action)

            is OpenQuizDetail -> handleQuizDetailAction(action)

            is OpenTopicPage -> openTopicPage(action)

            is OpenLearnMoreVideo -> openScreen(action, null)

            is OpenWhatsapp -> openWhatsapp(action, action.externalUrl)

            is OpenPCPopup -> openUnlockPopup()

            is OpenLiveClassFragment -> OpenLiveClassFragmentWithLiveClassesSelected(
                action.courseId
            )

            is UpdateSelectedGridItemList -> {
                updateSelectedGridItem(action)
            }

            is SendHomeWidgetEvent -> {
                homeEventManager.eventWith(action.eventName)
            }

            is ClickAction -> performClickAction(action.event, action.hashMap)

            is OpenLibraryVideoPlayListScreen -> openPlayListScreen(action)

            is OpenLibraryPlayListActivity -> openLibraryListingActivity(action)

            is PublishEvent -> publishEvent(action.event)

            is OpenWebView -> openWebView(action)

            is LaunchGame -> launchGame(action)

        }
    }

    fun sendSelectBoardEvent(id: String) {
        publishEventWith(
            EventConstants.EVENT_NAME_BOARD_SUBMIT_CLICK,
            hashMapOf<String, Any>().apply {
                put(EventConstants.PAGE, "home")
                put(EventConstants.BOARD, id)
            }, ignoreSnowplow = true
        )
    }

    private fun performClickAction(clickEvent: String, args: HashMap<String, String>) {
        val screen = screenEventMapper.map(clickEvent)

        screen?.let {
            when (screen) {
                is LiveClassesScreen -> {
                    homeEventManager.eventWith(EventConstants.EVENT_HOMEPAGE_CRASH_COURSE)
                }

                is OpenDemoVideoScreen -> {
                    sendVideoClickEvent(args[Constants.SUBJECT].toString())
                }
            }
            _navigateLiveData.value = Event(NavigationModel(screen, args))
        }
    }

    fun updateSelectedGridItem(action: UpdateSelectedGridItemList) {
        if (action.isChecked) {
            gridSelectedItemSet.add(action.id)
            gridSelectedItemList.value = gridSelectedItemSet
        } else {
            gridSelectedItemSet.remove(action.id)
            gridSelectedItemList.value = gridSelectedItemSet
        }
    }

    private fun getCommaSeperatedItemIds(): String {
        var commaSeperatedItemIds = ""
        gridSelectedItemSet.forEach {
            commaSeperatedItemIds += "$it,"
        }

        if (commaSeperatedItemIds.isNotEmpty()) {
            commaSeperatedItemIds =
                commaSeperatedItemIds.substring(0, commaSeperatedItemIds.length - 1)
        }
        return commaSeperatedItemIds
    }

    fun sendSelectExamItemEvent() {
        publishEventWith(
            EventConstants.EVENT_NAME_EXAM_SUBMIT_CLICK,
            hashMapOf<String, Any>().apply {
                put(EventConstants.PAGE, "home")
                put(EventConstants.EXAM, gridSelectedItemSet.toArray())
            }, ignoreSnowplow = true
        )
    }

    fun getClassesList() {
        compositeDisposable + getClassListAtHome
            .execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onClassListSuccess, this::onError)
    }

    private fun onClassListSuccess(classesData: ClassListEntity) {
        _classesListLiveData.value = Outcome.success(classesViewMapper.map(classesData))
    }

    private fun onError(error: Throwable) {
        _classesListLiveData.value = Outcome.loading(false)
        _classesListLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    private fun onPopUpListSuccess(apiPopUpData: ApiPopUpData) {
        if (apiPopUpData.popUpList.isNotEmpty()) {
            homeEventManager.eventWith(EventConstants.EVENT_RATING_WIDGET, true)
            _popUpListLiveData.value = Outcome.success(popUpMapper.map(apiPopUpData.popUpList))
        }
    }

    private fun sendVideoClickEvent(subject: String) {
        homeEventManager.eventWith(
            EventConstants.EVENT_HOMEPAGE_CRASH_COURSE_INTRO_VIDEO,
            HashMap<String, Any>().apply {
                put(EventConstants.SUBJECT, subject)
            })
    }

    private fun openDailyPrizeScreen(action: OpenContestPage) {

        val args = hashMapOf(Constants.CONTEST_ID to action.contestId)

        openScreen(action, args)
    }

    private fun openUnlockPopup() {
        _navigateLiveData.value = Event(NavigationModel(PCUnlockScreen, null))
    }

    private fun OpenLiveClassFragmentWithLiveClassesSelected(courseId: Int) {
        val params = HashMap<String, Int>()
        params[Constants.LIBRARY_SCREEN_SELECTED_TAB] = 1
        params[Constants.COURSE_ID] = courseId
        _navigateLiveData.value = Event(NavigationModel(LiveClassesScreen, params))

        homeEventManager.eventWith(EventConstants.EVENT_HOMEPAGE_CRASH_COURSE)
    }

    private fun shareOnWhatsApp(context: Context, action: ShareOnWhatApp) {

        val pdfUrl = action.controlParams?.get(Constants.INTENT_EXTRA_PDF_URL)

        val shouldDownloadPdf =
            action.featureType == Constants.PDF_VIEWER && (pdfUrl?.isNotEmpty() == true)

        if (shouldDownloadPdf) {
            showShareProgressBar.value = true
            downloadAndSharePdfOnWhatsApp(
                context,
                action.featureType,
                action.controlParams,
                action.sharingMessage,
                pdfUrl
            )
        } else {
            whatsAppSharing.shareOnWhatsApp(action)
        }
        sendEvent(EventConstants.EVENT_NEW_HOME_SHARE_CLICK + action.featureType)
    }

    private fun openTopicPage(action: OpenTopicPage) {
        val args = hashMapOf(
            SCREEN_NAV_PARAM_PLAYLIST_ID to (action.playlistId ?: ""),
            SCREEN_NAV_PARAM_PLAYLIST_TITLE to (action.title ?: "")
        )
        openScreen(action, args)
        sendEvent(EventConstants.EVENT_NAME_HOME_PAGE_CLICK_ACTION + action.headerTitle)
        homeEventManager.onHomeClick(action.headerTitle, action.title ?: "")
    }

    private fun openWebView(action: OpenWebView) {
        val args = hashMapOf(
            Constants.EXTERNAL_URL to (action.webPageUrl ?: "")
        )
        openScreen(action, args)
        sendEvent(EventConstants.EVENT_NAME_HOME_PAGE_CLICK_ACTION + action.webPageUrl)
        homeEventManager.onHomeClick(action.title ?: "webViewer", action.title ?: "")
    }

    private fun launchGame(action: LaunchGame) {
        val args = hashMapOf(
            Constants.GAME_TITLE to (action.title ?: ""),
            Constants.GAME_URL to (action.url ?: "")
        )
        openScreen(action, args)
        sendEvent(EventConstants.EVENT_NAME_HOME_PAGE_CLICK_ACTION + action.url)
        homeEventManager.onHomeClick(action.title ?: "game", action.title ?: "")
    }

    private fun getSharableDeepLink(
        context: Context, type: String?,
        controlParams: HashMap<String, String>?,
        sharingMessage: String?,
        onSuccess: (String) -> Unit
    ) {

        val linkProperties = LinkProperties().apply {

            channel = BRANCH_HOME_FEED_CHANNEL
            feature = type
            campaign = CoreConstants.CAMPAIGN

        }.also {

            controlParams?.let { params ->

                params.forEach { (key, value) ->
                    it.addControlParameter(key, value)
                }

            }
        }

        ThreadUtils.runOnAnalyticsThread {
            with(BranchUniversalObject()) {
                generateShortUrl(context, linkProperties) { url, error ->
                    CoreApplication.INSTANCE.runOnMainThread {
                        if (error == null) {
                            onSuccess(url)
                        } else {
                            ToastUtils.makeText(
                                context,
                                Constants.QUESTION_SHORT_URL_ERROR_BRANCH_TOAST,
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun getFileDestinationPath(context: Context, url: String): String {

        val externalDirectoryPath = context.getExternalFilesDir(null)?.path
            ?: ""
        val isChildDirCreated =
            FileUtils.createDirectory(externalDirectoryPath, AppUtils.PDF_DIR_NAME)

        if (isChildDirCreated) {
            val fileName = FileUtils.fileNameFromUrl(url)
            return AppUtils.getPdfDirectoryPath(context) + File.separator + fileName
        }

        return FileUtils.EMPTY_PATH
    }

    private fun onHomeFeedError(error: Throwable) {
        _studentRatingSubmitLiveData.value = false
        _studentFeedbackSubmitLiveData.value = false
        _gridListSubmitMessage.value = ""
        _homeFeedListLiveData.value = Outcome.loading(false)
        _homeFeedListLiveData.value = if (error is HttpException) {
            val errorCode = error.response()?.code()
            when (errorCode) {
                HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    private fun onPopUpListError(error: Throwable) {

    }

    private fun openNcertChapterScreen(action: OpenNCERTChapter) {
        val args = hashMapOf(
            Constants.PLAYLIST_ID to action.playlistId,
            Constants.PLAYLIST_TITLE to action.playlistTitle
        )
        openScreen(action, args)
        sendEvent(EventConstants.EVENT_NEW_HOME_PAGE_CLICK_ACTION + "openNcertChapterScreen")

        val category = (action.categoryTitle).replace(" ", "_").toLowerCase()
        sendEvent("${EventConstants.EVENT_NAME_HOME_PAGE_CLICK_ACTION}$category")
        homeEventManager.onHomeClick(category, action.playlistTitle)
    }

    private fun openVideoScreen(action: PlayVideo) {
        val args = hashMapOf(
            Constants.PAGE to action.page,
            Constants.QUESTION_ID to action.videoId,
            Constants.PLAYLIST_ID to action.playlistId
        )
        openScreen(action, args)
        publishPlayVideoClickEvent(action.videoId, HomeFeedFragmentV2.TAG)
        sendEvent(EventConstants.EVENT_NEW_HOME_PAGE_CLICK_ACTION + "openVideoScreen")
        val category = (action.categoryTitle).replace(" ", "_").toLowerCase()
        sendEvent("${EventConstants.EVENT_NAME_HOME_PAGE_CLICK_ACTION}$category")
        sendEvent(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK)

        homeEventManager.onHomeClick(category, action.videoId)
    }

    private fun openWhatsapp(action: Any, externalUrl: String) {
        val screen = actionToScreenMapper.map(action)
        val args = hashMapOf(
            Constants.EXTERNAL_URL to externalUrl
        )
        _navigateLiveData.value = Event(NavigationModel(screen, args))
        sendEvent(EVENT_WHATSAPP_CARD_CLICK + "home")
    }

    private fun openPDFViewerScreen(action: OpenPDFPage) {
        val args = hashMapOf(
            Constants.PDF_ACTION_ACTIVITY to action.actionActivity!!,
            Constants.PDF_ACTION_ACTIVITY_LEVEL_ONE to (action.actionData?.levelOne ?: ""),
            Constants.PDF_ACTION_DATA_URL to (action.actionData?.pdfUrl ?: ""),
            Constants.PDF_ACTION_DATA_PACKAGE to (action.actionData?.pdfPackage ?: "")
        )
        _navigateLiveData.value = Event(NavigationModel(PDFScreen, args))

        val category = (action.categoryTitle).replace(" ", "_").toLowerCase()
        sendEvent("${EventConstants.EVENT_NAME_HOME_PAGE_CLICK_ACTION}$category")
        homeEventManager.onHomeClick(category, action.actionData?.pdfPackage ?: "")
    }

    private fun handleQuizDetailAction(action: OpenQuizDetail) {
        compositeDisposable.add(
            checkQuizAttempted.execute(CheckQuizAttempted.Param(action.quizItem.testId))
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({ attemptedQuiz ->

                    val attemptedCount = 1
                    val testSubcriptionId = attemptedQuiz.subscriptionId
                    val updatedAction = OpenQuizDetail(
                        action.quizItem.copy(
                            attemptCount = attemptedCount,
                            testSubscriptionId = testSubcriptionId.toString()
                        )
                    )

                    openQuizDetailScreen(updatedAction)
                }, {
                    openQuizDetailScreen(action)
                })
        )

    }

    private fun openQuizDetailScreen(action: OpenQuizDetail) {

        val testSubscriptionId =
            if (action.quizItem.testSubscriptionId.isNullOrEmpty()) 0 else action.quizItem.testSubscriptionId.toInt()

        val quizDetailArgs = hashMapOf(
            Constants.TEST_DETAILS_OBJECT to getTestDetail(action.quizItem),
            Constants.TEST_SUBSCRIPTION_ID to testSubscriptionId
        )

        openScreen(action, quizDetailArgs)
        sendEvent(EventConstants.EVENT_NEW_HOME_PAGE_CLICK_ACTION + "openQuizDetailScreen")
        val category = (action.categoryTitle).replace(" ", "_").toLowerCase()
        sendEvent("${EventConstants.EVENT_NAME_HOME_PAGE_CLICK_ACTION}$category")
        homeEventManager.onHomeClick(category, "")
    }

    private fun openScreen(action: Any, args: HashMap<String, out Any>?) {
        val screen = actionToScreenMapper.map(action)
        _navigateLiveData.value = Event(NavigationModel(screen, args))
        val actionType = screen.toString().split("@")[0].split(".").last()
        sendEvent(EventConstants.EVENT_NEW_HOME_PAGE_CLICK_ACTION + actionType)
    }

    private fun handleBannerAction(action: BannerAction, context: Context) {
        val eventTracker = (context.applicationContext as DoubtnutApp).getEventTracker()
        BannerActionUtils.navigateToPage(
            context,
            action.actionActivity,
            action.actionData,
            eventTracker
        )
        try {
            homeEventManager.onHomeBannerClick(
                EVENT_HOME,
                action.actionActivity.orEmpty(),
                action.actionData?.id.orEmpty()
            )
        } catch (e: Exception) {

        }

    }

    private fun downloadAndSharePdfOnWhatsApp(
        context: Context, featureType: String?,
        controlParams: HashMap<String, String>?, sharingMessage: String?, pdfUrl: String?
    ) {

        getSharableDeepLink(context, featureType, controlParams, sharingMessage) { deepLink ->

            pdfUrl?.let {

                val filepath = getFileDestinationPath(context, pdfUrl)

                when {

                    FileUtils.isFilePresent(filepath) -> pdfUriLiveData.value =
                        Pair(File(filepath), "$sharingMessage $deepLink")

                    else -> compositeDisposable.add(
                        DataHandler.INSTANCE.pdfRepository.downloadPdf(
                            pdfUrl,
                            filepath
                        )
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                pdfUriLiveData.value =
                                    Pair(File(filepath), "$sharingMessage $deepLink")
                            }, {
                                pdfUriLiveData.value = null
                            })
                    )
                }
            }
        }
    }

    private fun getTestDetail(quizItem: QuizFeedViewItem): TestDetails = with(quizItem) {
        TestDetails(
            testId,
            classCode,
            subjectCode,
            chapterCode,
            title,
            description,
            durationInMin,
            solutionPdf,
            totalQuestions,
            publishTime,
            unpublishTime,
            isActive,
            null,
            type,
            ruleId,
            attemptCount = attemptCount,
            canAttempt = canAttempt,
            bottomWidgetEntity = bottomWidgetEntity
        )
    }

    fun startRateScreen(rating: Int) {
        compositeDisposable + submitStudentRatingUseCase
            .execute(rating)
            .applyIoToMainSchedulerOnCompletable()
            .subscribe(
                this::onSubmitRatingSuccess,
                this::onHomeFeedError
            )
    }

    private fun onSubmitRatingSuccess() {
        _studentRatingSubmitLiveData.value = true
    }

    fun startRatingScreen() {
        _navigateLiveData.value = Event(NavigationModel(RateUsScreen, null))
    }

    fun submitStudentFeedback(rating: Int, optionsList: List<String>) {
        val json = Gson().toJson(
            PostStudentRatingFeedback(
                rating = rating.toString(),
                optionsList = optionsList
            )
        )

        compositeDisposable + submitStudentFeedbackUseCase
            .execute(JsonParser().parse(json).asJsonObject)
            .applyIoToMainSchedulerOnCompletable()
            .subscribe(
                this::onStudentFeedbackSubmit,
                this::onHomeFeedError
            )
    }

    fun studentRatingCross() {
        compositeDisposable + studentRatingCrossUseCase
            .execute(Unit)
            .applyIoToMainSchedulerOnCompletable()
            .subscribe()
    }

    fun removeStudentRatingFragment() {
        _removeRatingFragmentLiveData.value = true
    }

    private fun onStudentFeedbackSubmit() {
        _studentFeedbackSubmitLiveData.value = true
    }

    private fun sendEvent(eventName: String) {
        _eventLiveData.value = eventName
    }

    fun publishEventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        homeEventManager.eventWith(eventName, ignoreSnowplow)
    }

    fun publishEventWith(
        eventName: String,
        params: HashMap<String, Any>,
        ignoreSnowplow: Boolean = false
    ) {
        homeEventManager.eventWith(eventName, params, ignoreSnowplow = ignoreSnowplow)
    }

    fun publishPlayVideoClickEvent(questionId: String, source: String) {
        videoPageEventManager.playVideoClick(questionId, source)
    }

    fun publishSearchIconEvent(source: String) {
        homeEventManager.onSearchIconClick(source)
    }

    fun publishCameraButtonClickEvent(source: String) {
        homeEventManager.cameraButtonClicked(source)
    }

    fun publishEvent(event: AnalyticsEvent) {
        homeEventManager.publishEvent(event)
    }

    fun publishEvent(event: StructuredEvent) {
        homeEventManager.publishEvent(event)
    }

    private fun openPlayListScreen(action: OpenLibraryVideoPlayListScreen) {
        _navigateLiveData.value = Event(
            NavigationModel(
                LibraryVideoPlayListScreen, hashMapOf(
                    SCREEN_NAV_PARAM_PLAYLIST_ID to action.playlistId,
                    SCREEN_NAV_PARAM_PLAYLIST_TITLE to (action.playlistName)
                )
            )
        )
    }

    private fun openLibraryListingActivity(action: OpenLibraryPlayListActivity) {
        _navigateLiveData.value = Event(
            NavigationModel(
                LibraryPlayListScreen, hashMapOf(
                    LibraryListingActivity.INTENT_EXTRA_PLAYLIST_ID to action.playlistId,
                    LibraryListingActivity.INTENT_EXTRA_PACKAGE_ID to action.packageDetailsId.orEmpty(),
                    LibraryListingActivity.INTENT_EXTRA_PLAYLIST_TITLE to action.playlistName
                )
            )
        )
    }

    fun getSearchHintData() {
        compositeDisposable + DataHandler.INSTANCE.searchRepository
            .getHintAnimationStrings()
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _searchHintMapLiveData.value = it.data
            })
    }

    fun submitInAppReviewCompletion() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("is_google_in_app_review", 1)
        submitStudentFeedbackUseCase
            .execute(jsonObject)
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({})
    }
}