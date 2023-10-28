package com.doubtnutapp.matchquestion.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.CountDownTimer
import android.util.Base64
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.attachRetryHandler
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.DoubtP2PData
import com.doubtnutapp.db.DoubtnutDatabase
import com.doubtnutapp.domain.base.SolutionResourceType
import com.doubtnutapp.domain.camerascreen.entity.CameraSettingEntity
import com.doubtnutapp.doubtpecharcha.model.P2PDoubtData
import com.doubtnutapp.doubtpecharcha.model.P2pRequestData
import com.doubtnutapp.matchquestion.event.MatchQuestionEventManager
import com.doubtnutapp.matchquestion.mapper.AdvancedSearchOptionsMapper
import com.doubtnutapp.matchquestion.mapper.MatchQuestionBannerMapper
import com.doubtnutapp.matchquestion.mapper.MatchQuestionMapper
import com.doubtnutapp.matchquestion.model.*
import com.doubtnutapp.matchquestion.service.MatchQuestionRepository
import com.doubtnutapp.plus
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.screennavigator.TextSolutionScreen
import com.doubtnutapp.screennavigator.VideoScreen
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.NetworkUtil
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgets.MatchPageExtraFeatureWidget
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import io.reactivex.functions.Function4
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MatchQuestionViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val matchQuestionMapper: MatchQuestionMapper,
    private val advancedSearchOptionsMapper: AdvancedSearchOptionsMapper,
    private val matchQuestionBannerMapper: MatchQuestionBannerMapper,
    private val matchQuestionEventManager: MatchQuestionEventManager,
    private val analyticsPublisher: AnalyticsPublisher,
    private val database: DoubtnutDatabase,
    private val networkUtil: NetworkUtil,
    private val userPreference: UserPreference,
    private val matchQuestionRepository: MatchQuestionRepository
) : BaseViewModel(compositeDisposable) {

    var backPressExperimentVariant: Int? = -1

    // Scroll animation visibility from api
    var scrollAnimation: Boolean? = false

    var questionMetaData: ImageMetaData? = null

    /**
     * RecyclerView scrolling events
     * SCROLL_DOWN - when scrolls down
     * SCROLL_UP - when scrolls up
     * SCROLL_DOWN_NONE - when reached last item
     * SCROLL_UP_NONE - when reached first item
     */
    enum class ScrollDirection {
        SCROLL_DOWN, SCROLL_UP, SCROLL_DOWN_NONE, SCROLL_UP_NONE
    }

    enum class BackPressPopup(val variant: Int) {
        FIRST_MATCH_WITH_P2P(1),
        USER_FEEDBACK(2),
        BOOK_FEEDBACK(3),
    }

    // Screen time for match page tabs
    private val tabScreenTimeMap = hashMapOf<String?, Long?>()

    // locally extracted ocr from image using google vision library and image file bytearray size
    private var googleVisionImageOcrData: Pair<String, Int>? = null

    // start Doubt P2P

    // list of thumbnail images received from ask api,
    // which will be used to show in slides before user tries to connect P2P
    private var p2pThumbnailImages = listOf<String>()

    // stores the data related to P2P bottom sheet,
    // which is being shown to user before actual P2P (intro screen) screen.
    private var p2pRequestData: P2pRequestData? = null
    // end Doubt P2P

    private var feedbackTypeSelectedForPopupDialog: String = ""
    private var feedbackTypeDisplayForPopupDialog: String = ""
    var isPopupFeedbackDialogAlreadyShown = false

    // is feedback selected by user
    var isFeedbackOptionSelectedByUser = false

    // stores the tab title (second last tab) received from api
    var liveTabData: LiveTabData? = null

    // Start - D0 User data
    // related to D0 experiment for which bottom navigation will
    // be hidden to avoid user to explore other screens in the app
    var d0UserData: D0UserData? = null

    // End - D0 User data

    // Map containing title of bottom textview on each tab
    // (key -> tab title, value -> bottom text info i.e. title and deeplink)
    var bottomTextData: HashMap<String, BottomTextData>? = null

    var source = ""
    var parentQuestionId: String = ""
    var isMatchResponseFetched: Boolean = false
    val playerState: MutableLiveData<PlayerState> = MutableLiveData()

    var signedUrlEntity: SignedUrlEntityData? = null

    // Stores image info after scaling -
    // (1. Base64 string of scaled down image 2. scaled down bitmap) before uploading to server.
    // Width and height of image is being sent in ask api.
    var scaleDownImageData: Pair<String, Bitmap>? = null

    // list of match result originally received from api
    var unmodifiedMatchResults: List<MatchQuestionViewItem> = listOf()

    // list of match result populated with nudges
    var matchResultsWithNudges: List<MatchQuestionViewItem> = listOf()

    // Variable to check whether user is already connected P2P for the same question
    var isDoubtP2PConnected: Boolean = false

    // OCR text of upload image received from api,
    // it will be passed in api when user clicks on advance search options
    // to filter match page results. Api will use this ocr to filter.
    var imageOcrText: String? = null

    val showNoInternetActivity: MutableLiveData<Pair<Boolean, Int>> = MutableLiveData()
    val matchPageScrollDirection = MutableLiveData<ScrollDirection>()
    val shouldLockBottomSheet = MutableLiveData<Boolean>()

    var doShowFeedbackPopDialog = false

    // Track whether user watched any video from the results shown on the screen.
    var isAnySolutionWatched: Boolean = false

    // We use parallel request to upload question image.
    // If any request is successful, we abort another image upload request.
    private var shouldAbortParallelImageUploadRequest = false

    // AutoPlay state
    val autoPlayState: MutableLiveData<Boolean> = MutableLiveData()

    // Live data to remove p2p and book feedback widget
    // once user sent question on P2P or submitted book feedback successfully to avoid showing popup again.
    val removeFeatureWidget: MutableLiveData<SingleEvent<MatchPageFeature>> = MutableLiveData()

    private var isAdvancedSearchShownFirstTime: Boolean = true
        get() {
            val value = field
            field = false
            return value
        }

    // To track different api responses called on match page.
    private val apiResponseTime: ApiResponseTime = ApiResponseTime(
        signedUrlApi = 0,
        imageUploadApi = 0,
        imageMatchResultApi = 0,
        textMatchResultApi = 0
    )

    @Keep
    data class ImageMetaData(
        val rotationAngle: Int,
        val sendEvent: Boolean
    )

    @Keep
    data class ApiResponseTime(
        var signedUrlApi: Long,
        var imageUploadApi: Long,
        var imageMatchResultApi: Long,
        var textMatchResultApi: Long,
    )

    @Keep
    enum class PlayerState {
        PAUSE,
        RESUME
    }

    private val doubtPeCharchaRepository = DataHandler.INSTANCE.doubtPeCharchaRepository

    private val uploadImageCompositeDisposable = CompositeDisposable()

    // Start - Upload Image counter - showing messages at different interval of time
    private var counter: Int = 1

    private val uploadImageTimer: CountDownTimer by lazy {
        object : CountDownTimer(60 * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                when (counter) {
                    // At 10 sec, make parallel request to upload image.
                    Constants.UPLOAD_IMAGE_TIMER_AT_10 -> requestParallelImageUpload()

                    // Show different messages to user if uploading image is taking time.
                    Constants.UPLOAD_IMAGE_TIMER_AT_15 -> _uploadImageError.value =
                        R.string.err_upload_image_15_sec
                    Constants.UPLOAD_IMAGE_TIMER_AT_30 -> _uploadImageError.value =
                        R.string.err_upload_image_30_sec
                    Constants.UPLOAD_IMAGE_TIMER_AT_45 -> _uploadImageError.value =
                        R.string.err_upload_image_45_sec
                }
                counter++
            }

            override fun onFinish() {}
        }
    }

    private fun startUploadImageTimer() {
        uploadImageTimer.start()
    }

    private fun cancelUploadImageTimer() {
        uploadImageTimer.cancel()
    }
    // End - Upload Image counter - showing messages at different interval of time

    // Start - Scroll Animation on MatchQuestionFragment
    private val _showScrollAnimation: MutableLiveData<SingleEvent<Boolean>> =
        MutableLiveData(SingleEvent(true))
    val showScrollAnimation: LiveData<SingleEvent<Boolean>>
        get() = _showScrollAnimation

    fun setScrollAnimationVisibility(visibility: Boolean) {
        _showScrollAnimation.postValue(SingleEvent(visibility))
    }
    // End - Scroll Animation on MatchQuestionFragment

    // Start -  P2P and Feedback widgets
    @Keep
    enum class MatchPageFeature(val feature: String) {
        P2P("p2p"),
        BOOK_FEEDBACK("book_feedback")
    }
    // End -  P2P and Feedback widgets

    private val _matchResultsLiveData: MutableLiveData<Outcome<MatchQuestion>> = MutableLiveData()
    val matchResultsLiveData: LiveData<Outcome<MatchQuestion>>
        get() = _matchResultsLiveData

    private val _firstMatchLiveData: MutableLiveData<ApiMatchedQuestionItem> = MutableLiveData()
    val firstMatchLiveData: LiveData<ApiMatchedQuestionItem>
        get() = _firstMatchLiveData

    private val _advancedSearchOptionsLiveData: MutableLiveData<MatchFilterFacetListViewItem> =
        MutableLiveData()
    val advancedSearchOptionsLiveData: LiveData<MatchFilterFacetListViewItem>
        get() = _advancedSearchOptionsLiveData

    private val _matchFailureOptionLiveData: MutableLiveData<Outcome<SingleEvent<MatchFailureOption>>> =
        MutableLiveData()
    val matchFailureOptionLiveData: LiveData<Outcome<SingleEvent<MatchFailureOption>>>
        get() = _matchFailureOptionLiveData

    private val _imageBitmapLiveData: MutableLiveData<Outcome<Bitmap?>> = MutableLiveData()
    val imageBitmapLiveData: LiveData<Outcome<Bitmap?>>
        get() = _imageBitmapLiveData

    private val _matchQuestionBannerLiveData: MutableLiveData<Outcome<MatchQuestionBanner>> =
        MutableLiveData()
    val matchQuestionBannerLiveData: LiveData<Outcome<MatchQuestionBanner>>
        get() = _matchQuestionBannerLiveData

    // sets true when feedback submitted. Also reset it, when advanced search applied
    // Because new match results will come and will be required new feedback for the results.
    var feedbackSubmitted: Boolean = false

    private val _feedbackBooks: MutableLiveData<Outcome<SingleEvent<MatchFeedbackEntity>>> =
        MutableLiveData()
    val feedbackBooks: LiveData<Outcome<SingleEvent<MatchFeedbackEntity>>>
        get() = _feedbackBooks

    private val _filterFacetLiveData: MutableLiveData<List<MatchFilterFacetViewItem>> =
        MutableLiveData()
    val filterFacetLiveData: LiveData<List<MatchFilterFacetViewItem>>
        get() = _filterFacetLiveData

    private val _isAdvancedSearchEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    val isAdvancedSearchEnabled: LiveData<Boolean>
        get() = _isAdvancedSearchEnabled

    private val _preventExitMatchPage: MutableLiveData<Boolean> = MutableLiveData()
    val preventExitMatchPage: LiveData<Boolean>
        get() = _preventExitMatchPage

    fun setPreventMatchExit() {
        _preventExitMatchPage.value = true
    }

    private val _matchPageCarousal: MutableLiveData<MatchCarousalsData> = MutableLiveData()
    val matchPageCarousal: LiveData<MatchCarousalsData>
        get() = _matchPageCarousal

    private val _doubtP2PData: MutableLiveData<SingleEvent<DoubtP2PData>> = MutableLiveData()
    val doubtP2PData: LiveData<SingleEvent<DoubtP2PData>>
        get() = _doubtP2PData

    private val _matchPageDoubtLiveData = MutableLiveData<Outcome<P2PDoubtData>>()
    val matchPageDoubtLiveData: LiveData<Outcome<P2PDoubtData>>
        get() = _matchPageDoubtLiveData

    private val _matchesFromNotification: MutableLiveData<Boolean> = MutableLiveData()
    val matchesFromNotification: LiveData<Boolean>
        get() = _matchesFromNotification

    fun setMatchesFromNotification() {
        _matchesFromNotification.postValue(true)
    }

    private val _matchesFromInApp: MutableLiveData<Boolean> = MutableLiveData()
    val matchesFromInApp: LiveData<Boolean>
        get() = _matchesFromInApp

    fun setMatchesFromInApp(isFromInApp: Boolean) {
        _matchesFromInApp.postValue(isFromInApp)
    }

    private val _message: MutableLiveData<String?> = MutableLiveData()
    val message: LiveData<String?>
        get() = _message

    private val _popupDeeplink: MutableLiveData<SingleEvent<String?>> = MutableLiveData()
    val popupDeeplink: LiveData<SingleEvent<String?>>
        get() = _popupDeeplink

    private val _cameraSettingConfig = MutableLiveData<Outcome<CameraSettingEntity>>()
    val cameraSettingConfig: LiveData<Outcome<CameraSettingEntity>>
        get() = _cameraSettingConfig

    val showFilterBottomSheet: MutableLiveData<Boolean> = MutableLiveData()

    private val _isDialogShowingLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val isDialogShowingLiveData: LiveData<Boolean>
        get() = _isDialogShowingLiveData
    var isDialogShowing: Boolean = false
        set(value) {
            field = isDialogShowing
            _isDialogShowingLiveData.value = value
        }

    private val _popupFeedbackLiveData: MutableLiveData<ApiFeedbackResponseData> =
        MutableLiveData()
    val popupFeedbackLiveData: LiveData<ApiFeedbackResponseData> = _popupFeedbackLiveData

    private val _submitFeedbackPreferenceData: MutableLiveData<Outcome<ApiSubmitFeedbackPreference?>> =
        MutableLiveData()
    val submitFeedbackPreferenceLiveData: LiveData<Outcome<ApiSubmitFeedbackPreference?>> =
        _submitFeedbackPreferenceData

    private val _uploadImageError: MutableLiveData<Int> = MutableLiveData()
    val uploadImageError: LiveData<Int>
        get() = _uploadImageError

    // Decides whether partial list or complete list should be shown,
    // depends upon whether user has already click on Show More Item or not
    var shouldShowPartialResults: Boolean = false

    fun getCameraSetting() {
        _cameraSettingConfig.value = Outcome.loading(true)
        compositeDisposable + matchQuestionRepository.getCameraSettingConfig()
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _cameraSettingConfig.value = Outcome.loading(false)
                _cameraSettingConfig.value = Outcome.success(it)
            }, {
                print("")
            })
    }

    fun clearMatchFilter() {
        val filterList = _filterFacetLiveData.value ?: return
        filterMatchResult(filterList, true)
    }

    fun updateMatchFilter(
        topicPosition: Int,
        isTopicSelected: Boolean,
        toRefresh: Boolean,
        facetPosition: Int
    ) {

        if (!networkUtil.isConnectedWithMessage()) return

        val filterList = _filterFacetLiveData.value ?: return

        if (filterList.size <= facetPosition) return

        if (topicPosition == -1 || facetPosition == -1) return

        if (filterList[facetPosition].data.size <= topicPosition) return

        if (filterList[facetPosition].isMultiSelect) {
            filterList[facetPosition].data[topicPosition].isSelected = isTopicSelected
        } else {
            if (isTopicSelected) {
                filterList[facetPosition].data.forEachIndexed { index, _ ->
                    filterList[facetPosition].data[index].isSelected = index == topicPosition
                }
            } else {
                filterList[facetPosition].data[topicPosition].isSelected = isTopicSelected
            }
        }

        filterList.forEach { facet ->
            val topic = facet.data.find { topic ->
                topic.isSelected
            }
            facet.isSelected = topic != null
        }

        if (toRefresh) {
            filterMatchResult(filterList)
        }
    }

    fun filterMatchResult(
        matchFilterFacetList: List<MatchFilterFacetViewItem>,
        reset: Boolean = false
    ) {
        val filteredList = advancedSearchOptionsMapper.unMapMatchFilterData(matchFilterFacetList)
        if (reset) {
            filteredList.forEach { apiFacetV2 ->
                apiFacetV2.isSelected = false
                apiFacetV2.data.forEach { it.isSelected = false }
            }
        }
        sendEventForFilterSelection(matchFilterFacetList)
        feedbackSubmitted = false

        makeParallelRequestsToFilterAndOtherWidgets(filteredList)
    }

    /**
     * This method is used to refresh the page when clicks on advanced filter options item.
     * Makes three parallel requests to fetch data
     * @param filteredList - list of selected item in advanced search options
     */
    private fun makeParallelRequestsToFilterAndOtherWidgets(filteredList: List<ApiAdvanceSearchData>) {

        if (!networkUtil.isConnected()) {
            sendEvent(EventConstants.NO_NETWORK_SCREEN, hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, EventConstants.CLIENT_SIDE)
                put(EventConstants.FUNCTION, "filterMatchResult")
            })
            handleNetwork(Constants.REQUEST_CODE_FILTER_RESULT)
            return
        }

        _matchResultsLiveData.value = Outcome.loading(true)

        val filterResultSingle = matchQuestionRepository
            .getFilterMatchResult(
                imageString = imageOcrText.orEmpty(),
                facets = filteredList,
                question_id = parentQuestionId,
                source = source
            )
        val srpNudgesSingle = getSrpNudgesRequestSingle(parentQuestionId)
        val matchPageCarouselRequestSingle = getMatchPageCarouselRequest(parentQuestionId)

        compositeDisposable.add(
            Single.zip(
                filterResultSingle,
                srpNudgesSingle,
                matchPageCarouselRequestSingle,
                Function3 { matchQuestionData, matchPageNudgesData, matchCarousalsData ->
                    onAdvancedSearchOptionsSuccess(
                        ApiAdvancedSearchOptions(
                            facets = matchQuestionData.facets ?: filteredList,
                            displayFilter = true
                        )
                    )
                    _isAdvancedSearchEnabled.postValue(true)
                    onImageMatchQuestionSuccess(matchQuestionData)
                    _matchPageCarousal.postValue(matchCarousalsData)

                    val matchResult = matchQuestionMapper.map(matchQuestionData)
                    unmodifiedMatchResults = matchResult.matchedQuestions.toList()

                    return@Function3 combineMatchResultsWithNudges(
                        matchResult,
                        matchPageNudgesData,
                        matchQuestionData.matchesDisplayComfig
                    )
                }).applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    { matchResultsWithNudges ->
                        _matchResultsLiveData.value = Outcome.loading(false)
                        _matchResultsLiveData.value = Outcome.success(matchResultsWithNudges)
                    },
                    {
                        onApiError(
                            error = it,
                            requestCode = Constants.REQUEST_CODE_FILTER_RESULT,
                            fromMethod = "filterMatchResult"
                        )
                    }
                )
        )
    }

    fun onSolutionWatched() {
        isAnySolutionWatched = true
    }

    fun onNavigateToScreen(navigationModel: NavigationModel) {
        _navigateLiveData.postValue(Event(navigationModel))
    }

    fun getMatchFeedbackData() {
        _matchFailureOptionLiveData.value = Outcome.loading(true)
        compositeDisposable.add(
            matchQuestionRepository
                .getMatchFailureOption()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    this::onMatchFailureOptionSuccess
                ) {
                    _matchFailureOptionLiveData.value = Outcome.loading(false)
                    onApiError(error = it, fromMethod = "getMatchFeedbackData")
                }
        )
    }

    private fun onMatchFailureOptionSuccess(matchFailureOptionEntity: MatchFailureOption) {
        _matchFailureOptionLiveData.value = Outcome.loading(false)
        _matchFailureOptionLiveData.value = Outcome.success(SingleEvent(matchFailureOptionEntity))
    }

    /**
     * This method is used to submit book feedback when
     * user has not watched any video from this page.
     *
     */
    fun postMatchFailureFeedback(source: String, feedback: String) {

        _feedbackBooks.value = Outcome.loading(true)

        // List of question ids displayed on match page
        // it will pass to api which is used to submit book names in feedback popup
        // when user does not click any solution and press back (Still in experiment driven by flagr)
        val answerDisplayed: MutableList<String> = mutableListOf()
        unmodifiedMatchResults.forEach {
            if (it is MatchedQuestionsList) {
                answerDisplayed.add(it.id)
            }
        }

        compositeDisposable.add(
            matchQuestionRepository.postMatchFailureFeedback(
                questionId = parentQuestionId,
                isPositive = false,
                source = source,
                feedback = feedback,
                answersDisplayed = answerDisplayed
            ).applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        feedbackSubmitted = true
                        removeFeatureWidget.value = SingleEvent(MatchPageFeature.BOOK_FEEDBACK)
                        _feedbackBooks.value = Outcome.loading(false)
                        _feedbackBooks.value = Outcome.success(SingleEvent(it))
                        sendEvent(
                            EventConstants.BOOK_FEEDBACK_SUBMITTED,
                            hashMapOf()
                        )
                    },
                    {
                        _feedbackBooks.value = Outcome.loading(false)
                        onApiError(error = it, fromMethod = "postMatchFailureFeedback")
                    }
                )
        )
    }

    /**
     * This method is used to get match results from text search.
     * It is called for 1. AskedQuestionHistory screen for text question,
     * 2. Edit OCR flow - User can edit ocr and research.
     * 3. Signed Url or Image upload api fail
     * 4. TYD flow - text search TYDActivity
     * @param questionText - question text
     * @param uploadedImageQuestionId - question id generate after signed url in case of image upload fail,
     * parent question id in case of edit ocr flow otherwise null
     * @param imageOcrFeedback - 'blur' or 'handwritten' only in case of edit ocr flow
     * @param isOcrFromImage - true only in case of locally extracted ocr otherwise false - signed url or image upload fail flow
     */
    fun getTextQuestionMatchResults(
        questionText: String,
        uploadedImageQuestionId: String? = null,
        imageOcrFeedback: String? = null,
        isOcrFromImage: Boolean? = false
    ) {

        if (!networkUtil.isConnected()) {
            sendEvent(EventConstants.NO_NETWORK_SCREEN, hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, EventConstants.CLIENT_SIDE)
                put(EventConstants.FUNCTION, "getTextQuestionMatchResults")
            })
            handleNetwork(Constants.REQUEST_CODE_ASK_TEXT_SEARCH)
            return
        }

        val startTime = System.currentTimeMillis()

        _matchResultsLiveData.value = Outcome.loading(true)

        compositeDisposable.add(
            matchQuestionRepository.getMatchedResults(
                MatchQuestionRepository.Param(
                    uploadedImageName = "",
                    uploadedImageQuestionId = uploadedImageQuestionId,
                    questionText = questionText,
                    source = source,
                    croppedImageUrl = null,
                    retryCounter = null,
                    imageOcrFeedback = imageOcrFeedback,
                    isOcrFromImage = isOcrFromImage,
                    googleVisionImageOcr = null
                )
            ).applyIoToMainSchedulerOnSingle()
                .doAfterSuccess { matchQuestionData ->
                    makeParallelRequestsForAdvancedSearchAndOtherWidgets(
                        apiAskQuestionResponse = matchQuestionData,
                        startTime = startTime
                    )
                }.subscribeToSingle({},
                    {
                        onApiError(
                            error = it,
                            requestCode = Constants.REQUEST_CODE_ASK_TEXT_SEARCH,
                            fromMethod = "getTextQuestionMatchResults"
                        )
                    }
                )
        )
    }

    fun deleteOcrFromDb(ocrNotificationId: Long) {
        compositeDisposable + database.offlineOcrDao().deleteOcr(ocrNotificationId)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({}, {})
    }

    fun getMatchResultsFromDb(questionId: String) {

        _matchResultsLiveData.value = Outcome.loading(true)

        compositeDisposable + database.matchQuestionDao().getMatches(questionId)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _matchResultsLiveData.value = Outcome.loading(false)
                    onImageMatchQuestionSuccess(it)
                    deleteQuestionFromDb(questionId)
                },
                {
                    _matchResultsLiveData.value = Outcome.loading(false)
                    it.printStackTrace()
                }
            )
    }

    private fun deleteQuestionFromDb(questionId: String) {
        compositeDisposable + database.matchQuestionDao()
            .deleteMatchQuestion(questionId)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({})
    }

    /**
     * This method makes parallel requests to fetch
     *  match results for image, advanced search options, srp nudges
     *  and carousels data for Live Class and VIP tab
     * @param uploadedImageName - either from signed url api or from AskedQuestionHistory
     * @param uploadedImageQuestionId - either generated after signed url or coming from AskedQuestionHistory
     * @param croppedImageUrl - only coming from AskedQuestionHistory
     * @param - retryCounter - its value is 1 for AskedQuestionHistory otherwise null
     */
    fun makeParallelRequestsToGetImageResultsAndOtherWidgets(
        uploadedImageName: String,
        uploadedImageQuestionId: String,
        croppedImageUrl: String? = null,
        retryCounter: Int? = null
    ) {

        if (!networkUtil.isConnected()) {
            sendEvent(EventConstants.NO_NETWORK_SCREEN, hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, EventConstants.CLIENT_SIDE)
                put(EventConstants.FUNCTION, "getImageQuestionMatchResults")
            })
            handleNetwork(Constants.REQUEST_CODE_ASK_IMAGE_SEARCH)
            return
        }

        val startTime = System.currentTimeMillis()

        _matchResultsLiveData.value = Outcome.loading(true)

        val advancedSearchSingle = getAdvancedSearchRequestSingle(uploadedImageQuestionId)
        val srpNudgesSingle = getSrpNudgesRequestSingle(uploadedImageQuestionId)
        val matchResultSingle = getImageMatchRequestSingle(
            uploadedImageName = uploadedImageName,
            uploadedImageQuestionId = uploadedImageQuestionId,
            croppedImageUrl = croppedImageUrl,
            retryCounter = retryCounter
        )
        val matchPageCarouselRequestSingle = getMatchPageCarouselRequest(uploadedImageQuestionId)

        compositeDisposable.add(
            Single.zip(
                matchResultSingle,
                advancedSearchSingle,
                srpNudgesSingle,
                matchPageCarouselRequestSingle,
                Function4 { matchQuestionData, advancedSearchOptions, matchPageNudgesData, matchCarousalsData ->

                    // Set response time for these parallel requests.
                    apiResponseTime.apply {
                        imageMatchResultApi = System.currentTimeMillis() - startTime
                    }

                    // send different events
                    sendApiResponseTimeEvent(matchQuestionData.questionId)
                    sendMoEngageEvent(matchQuestionData.questionId)
                    sendImageRotationAngleEvent(
                        matchQuestionData.questionId,
                        matchQuestionData.questionImage
                    )

                    onAdvancedSearchOptionsSuccess(advancedSearchOptions)
                    onImageMatchQuestionSuccess(matchQuestionData)
                    _matchPageCarousal.postValue(matchCarousalsData)

                    val matchResult = matchQuestionMapper.map(matchQuestionData)
                    unmodifiedMatchResults = matchResult.matchedQuestions.toList()

                    return@Function4 combineMatchResultsWithNudges(
                        matchResult,
                        matchPageNudgesData,
                        matchQuestionData.matchesDisplayComfig
                    )
                }
            ).applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    { matchResultsWithNudges ->
                        _matchResultsLiveData.value = Outcome.loading(false)
                        _matchResultsLiveData.value = Outcome.success(matchResultsWithNudges)
                    },
                    {
                        onApiError(
                            error = it,
                            requestCode = Constants.REQUEST_CODE_ASK_IMAGE_SEARCH,
                            fromMethod = "getImageQuestionMatchResults"
                        )
                    }
                )
        )
    }

    /**
     * @param questionId - question id generated after generating signed url/ parent question id
     * @return - Advanced search Single request
     */
    private fun getAdvancedSearchRequestSingle(questionId: String) =
        matchQuestionRepository.getAdvancedSearchOptions(questionId)

    /**
     * @param questionId - question id generated after generating signed url/ parent question id
     * @return - SRP widget Single request
     */
    private fun getSrpNudgesRequestSingle(questionId: String) =
        matchQuestionRepository.getSrpNudgesData(questionId)

    /**
     * @param questionId - question id generated after generating signed url/ parent question id
     * @return - Live Class and VIP tab widget data Single request
     */
    private fun getMatchPageCarouselRequest(questionId: String) =
        matchQuestionRepository.getMatchPageCarousals(questionId)

    /**
     * @param uploadedImageName - either from signed url api or from AskedQuestionHistory
     * @param uploadedImageQuestionId - either generated after signed url or coming from AskedQuestionHistory
     * @param croppedImageUrl - only coming from AskedQuestionHistory
     * @param - retryCounter - its value is 1 for AskedQuestionHistory otherwise null
     * @return - ask/v10 Single request Single
     */
    private fun getImageMatchRequestSingle(
        uploadedImageName: String,
        uploadedImageQuestionId: String,
        croppedImageUrl: String? = null,
        retryCounter: Int? = null
    ) =
        matchQuestionRepository.getMatchedResults(
            MatchQuestionRepository.Param(
                uploadedImageName = uploadedImageName,
                uploadedImageQuestionId = uploadedImageQuestionId,
                questionText = "",
                source = source,
                croppedImageUrl = croppedImageUrl,
                retryCounter = retryCounter,
                imageWidth = scaleDownImageData?.second?.width,
                imageHeight = scaleDownImageData?.second?.height,
                googleVisionImageOcr = googleVisionImageOcrData?.first.orEmpty()
            )
        )

    /**
     * This method inserts each SRP nudge in match results list base on the index.
     * @param matchResults - match result list
     * @param matchPageNudgesData - SRP nudges map with key -> index and value -> widget
     * @param matchesDisplayConfig - configuration related to number of items shown on the first go
     */
    private fun combineMatchResultsWithNudges(
        matchResults: MatchQuestion,
        matchPageNudgesData: MatchPageNudgesData?,
        matchesDisplayConfig: MatchesDisplayConfig?
    ): MatchQuestion {
        if (matchesDisplayConfig != null) {
            val partialMatchResult = mutableListOf<MatchQuestionViewItem>()
                .apply {
                    addAll(
                        matchResults.matchedQuestions.toList()
                            .subList(0, matchesDisplayConfig.displayLimit + 1)
                    )
                    // add show more widget
                    val displayMoreWidget =
                        MatchPageWidgetViewItem(widget = matchesDisplayConfig.displayMoreActionWidget)
                    add(displayMoreWidget)
                }
            // Set it to true to show partial match results for the first time
            shouldShowPartialResults = true
            matchResults.partialMatchedQuestions.apply {
                clear()
                addAll(
                    populateMatchResulsListtWithNudgets(
                        matchPageNudgesData = matchPageNudgesData,
                        matchedQuestionsList = partialMatchResult
                    )
                )
            }
        }

        val completeMatchResult = mutableListOf<MatchQuestionViewItem>()
            .apply {
                addAll(matchResults.matchedQuestions.toList())
            }
        matchResultsWithNudges = populateMatchResulsListtWithNudgets(
            matchPageNudgesData = matchPageNudgesData,
            matchedQuestionsList = completeMatchResult
        )
        return matchResults.apply {
            matchedQuestions.clear()
            matchedQuestions.addAll(matchResultsWithNudges)
        }
    }

    private fun populateMatchResulsListtWithNudgets(
        matchPageNudgesData: MatchPageNudgesData?,
        matchedQuestionsList: MutableList<MatchQuestionViewItem>,
    ): MutableList<MatchQuestionViewItem> {
        // insert widget at relative position, as on every insertion size of list increases.
        var countOfNudges = 0

        val nudgesMap = matchPageNudgesData?.nudges
        nudgesMap?.forEach { entry ->
            val index = entry.key.toIntOrNull() ?: return@forEach
            val widget = entry.value
            countOfNudges++
            when {
                // index is put of list size, add items at the end, otherwise at the specified position
                index + countOfNudges >= matchedQuestionsList.size -> {
                    matchedQuestionsList.add(MatchPageWidgetViewItem(widget = widget))
                }
                else -> {
                    matchedQuestionsList.add(
                        index + countOfNudges,
                        MatchPageWidgetViewItem(widget = widget)
                    )
                }
            }
        }
        return matchedQuestionsList
    }

    fun getSignedUrl(fileName: String) {
        val startTime = System.currentTimeMillis()
        if (networkUtil.isConnected()) {
            _matchResultsLiveData.value = Outcome.loading(true)
            compositeDisposable.add(
                matchQuestionRepository
                    .getSignedUrl(fileName)
                    .attachRetryHandler()
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle({

                        // Set response time for this api
                        apiResponseTime.apply {
                            signedUrlApi = System.currentTimeMillis() - startTime
                        }
                        signedUrlEntity = it
                        matchQuestionEventManager.eventWith(
                            EventConstants.ASK_SIGNED_URL_SUCCESS,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.QUESTION_ID, it.questionId)
                                put(EventConstants.FILE_NAME, it.fileName)
                                put(EventConstants.UPLOAD_URL, it.url)
                            }
                        )
                        uploadImage(it.url, it.fileName, it.questionId)
                    }, {
                        matchQuestionEventManager.eventWith(
                            EventConstants.ASK_SIGNED_URL_FAILURE,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.ERROR_MESSAGE, it.message.orEmpty())
                            }
                        )
                        onApiError(
                            error = it,
                            requestCode = Constants.REQUEST_CODE_GET_SIGNED_URL,
                            fromMethod = "getSignedUrl"
                        )

                        // on fail, use extracted ocr from image to search\
                        researchWithExtractedOcrFromImage()
                    })
            )
        } else {
            sendEvent(EventConstants.NO_NETWORK_SCREEN, hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, EventConstants.CLIENT_SIDE)
                put(EventConstants.FUNCTION, "getSignedUrl")
            })
            handleNetwork(Constants.REQUEST_CODE_GET_SIGNED_URL)
        }
    }

    /**
     * This method is called if signed url or upload image api fails.
     * It will initiate text search with locally extracted ocr from image
     * using google vision api.
     */
    private fun researchWithExtractedOcrFromImage() {

        val googleVisionOcr = googleVisionImageOcrData ?: return

        sendEvent(EventConstants.IMAGE_TO_OCR_CONVERTED, hashMapOf<String, Any>().apply {
            put(EventConstants.OCR_TEXT, googleVisionOcr.first)
            put(EventConstants.FILE_SIZE, googleVisionOcr.second)
            signedUrlEntity?.questionId?.let {
                put(EventConstants.QUESTION_ID, it)
            }
        })

        getTextQuestionMatchResults(
            questionText = googleVisionOcr.first,
            uploadedImageQuestionId = signedUrlEntity?.questionId,
            isOcrFromImage = true
        )

        // Stop timer which is showing upload messages to user
        cancelUploadImageTimer()

        // clear upload image request if any
        uploadImageCompositeDisposable.clear()
    }

    fun retryUploadImage() {
        val signedUrlEntity = signedUrlEntity ?: return
        uploadImage(
            url = signedUrlEntity.url,
            uploadedImageName = signedUrlEntity.fileName,
            questionId = signedUrlEntity.questionId
        )
    }

    private fun requestParallelImageUpload() {
        // Question image already uploaded successfully, avoid parallel request.
        if (shouldAbortParallelImageUploadRequest) return

        val signedUrlEntity = signedUrlEntity ?: return
        startImageUpload(
            url = signedUrlEntity.url,
            uploadedImageName = signedUrlEntity.fileName,
            questionId = signedUrlEntity.questionId,
            isRetry = true
        )

        sendEvent(EventConstants.REQUEST_PARALLEL_IMAGE_UPLOAD)
    }

    private fun uploadImage(url: String, uploadedImageName: String, questionId: String) {
        if (networkUtil.isConnected()) {
            startUploadImageTimer()
            startImageUpload(url, uploadedImageName, questionId)
        } else {
            sendEvent(EventConstants.NO_NETWORK_SCREEN, hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, EventConstants.CLIENT_SIDE)
                put(EventConstants.FUNCTION, "uploadImage")
            })
            handleNetwork(Constants.REQUEST_CODE_UPLOAD_ASK_IMAGE)
        }
    }

    private fun startImageUpload(
        url: String,
        uploadedImageName: String,
        questionId: String,
        isRetry: Boolean = false
    ) {
        val startTime = System.currentTimeMillis()
        val byteArray = Base64.decode(scaleDownImageData?.first, Base64.DEFAULT)
        uploadImageCompositeDisposable.add(
            matchQuestionRepository
                .uploadImage(url, byteArray)
                .applyIoToMainSchedulerOnSingle()
                .attachRetryHandler(60, TimeUnit.SECONDS)
                .subscribeToSingle({

                    // Set response time for this api
                    apiResponseTime.apply {
                        imageUploadApi = System.currentTimeMillis() - startTime
                    }
                    shouldAbortParallelImageUploadRequest = true
                    cancelImageUploadRequest()

                    // Stop timer which is showing upload messages to user
                    cancelUploadImageTimer()

                    matchQuestionEventManager.eventWith(
                        EventConstants.IMAGE_UPLOAD_SUCCESS,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.QUESTION_ID, questionId)
                            put(EventConstants.IS_RETRIED, isRetry)
                            put(EventConstants.FILE_SIZE, byteArray.size.toString())
                            put(EventConstants.UPLOAD_URL, url)
                        }
                    )
                    makeParallelRequestsToGetImageResultsAndOtherWidgets(
                        uploadedImageName = uploadedImageName,
                        uploadedImageQuestionId = questionId
                    )
                }, {
                    // Stop timer which is showing upload messages to user
                    cancelUploadImageTimer()
                    matchQuestionEventManager.imageUploadFail(
                        byteArray.size.toString(),
                        questionId,
                        url,
                        it.message.orEmpty(),
                        isRetry
                    )

                    researchWithExtractedOcrFromImage()
                })
        )
    }

    private fun cancelImageUploadRequest() {
        uploadImageCompositeDisposable.clear()
    }

    private fun onImageMatchQuestionSuccess(apiMatchQuestion: ApiAskQuestionResponse) {
        parentQuestionId = apiMatchQuestion.questionId
        p2pThumbnailImages = apiMatchQuestion.p2pThumbnailImages.orEmpty()
        imageOcrText = apiMatchQuestion.ocrText
        liveTabData = apiMatchQuestion.liveTabData
        d0UserData = apiMatchQuestion.d0UserData
        bottomTextData = apiMatchQuestion.bottomTextData
        backPressExperimentVariant = apiMatchQuestion.backpressVariant
        scrollAnimation = apiMatchQuestion.scrollAnimation
        _message.postValue(apiMatchQuestion.message)

        // Show any popup related to question e.g if user asked question other than Physics, Chemistry or Maths
        _popupDeeplink.postValue(SingleEvent(apiMatchQuestion.popupDeeplink))

        when (backPressExperimentVariant) {
            MatchQuestionViewModel.BackPressPopup.FIRST_MATCH_WITH_P2P.variant -> {
                val matchQuestionListForBackPressPopup: List<ApiMatchedQuestionItem> =
                    if (!apiMatchQuestion.backPressMatchArray.isNullOrEmpty()) {
                        apiMatchQuestion.backPressMatchArray
                    } else {
                        apiMatchQuestion.matchedQuestions
                    }
                _firstMatchLiveData.postValue(matchQuestionListForBackPressPopup.find {
                    it.resourceType == SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO ||
                            it.resourceType == SolutionResourceType.SOLUTION_RESOURCE_TYPE_TEXT
                })
            }
        }

        // If any exact solution found, open video page.
        apiMatchQuestion.matchedQuestions.find {
            it.source?.isExactMatch == true
        }?.let { exactMatchSolution ->
            val argument = hashMapOf(
                Constants.PAGE to Constants.PAGE_SRP,
                Constants.QUESTION_ID to exactMatchSolution.id,
                Constants.PARENT_ID to parentQuestionId,
                Constants.OCR_TEXT to exactMatchSolution.source?.ocrText.orEmpty(),
                Constants.PARENT_PAGE to if (_isAdvancedSearchEnabled.value == true) {
                    Constants.PAGE_ADV_SEARCH
                } else ""
            )

            val screen =
                if (exactMatchSolution.resourceType == SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO) {
                    VideoScreen
                } else {
                    TextSolutionScreen
                }

            _navigateLiveData.postValue(Event(NavigationModel(screen, argument)))
        }
    }

    private fun onApiError(
        error: Throwable,
        requestCode: Int = 0,
        fromMethod: String? = null
    ) {
        _matchResultsLiveData.value = Outcome.loading(false)
        when (error) {
            is UnknownHostException -> {
                if (requestCode != 0) {
                    sendEvent(EventConstants.NO_NETWORK_SCREEN, hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, EventConstants.SERVER_SIDE)
                        put(EventConstants.FUNCTION, fromMethod.orEmpty())
                    })
                    handleNetwork(requestCode)
                }
            }
            else -> handleError(error)
        }

        sendEvent(EventConstants.EVENT_MATCH_PAGE_ERROR, hashMapOf<String, Any>().apply {
            put(EventConstants.ERROR_MESSAGE, error.message.orEmpty())
            put(EventConstants.FUNCTION, fromMethod.orEmpty())
        })
    }

    private fun handleError(error: Throwable) {
        _matchResultsLiveData.value = Outcome.loading(false)
        _matchResultsLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    private fun handleNetwork(requestCode: Int) {
        compositeDisposable.clear()
        showNoInternetActivity.postValue(Pair(true, requestCode))
    }

    /**
     * This method is called after text search results
     * (because we need question id to make other api calls and
     * in case of text search we do not have question id before
     * like in image question search from signed url api) to get
     * advanced search options, srp nudges and
     * carousels data for Live Class and VIP tab.
     * @param apiAskQuestionResponse - results received after text search
     * @param startTime - time to calculate total api response time in case of text search.
     */
    private fun makeParallelRequestsForAdvancedSearchAndOtherWidgets(
        apiAskQuestionResponse: ApiAskQuestionResponse,
        startTime: Long
    ) {

        val advancedSearchSingle = getAdvancedSearchRequestSingle(apiAskQuestionResponse.questionId)
        val srpNudgesSingle = getSrpNudgesRequestSingle(apiAskQuestionResponse.questionId)
        val matchPageCarouselRequestSingle =
            getMatchPageCarouselRequest(apiAskQuestionResponse.questionId)

        compositeDisposable.add(
            Single.zip(
                advancedSearchSingle,
                srpNudgesSingle,
                matchPageCarouselRequestSingle,
                Function3 { advancedSearchOptions, matchPageNudgesData, matchCarousalsData ->

                    // Set api response time for text search api
                    apiResponseTime.apply {
                        textMatchResultApi = System.currentTimeMillis() - startTime
                    }
                    // Send event for api response time
                    sendApiResponseTimeEvent(apiAskQuestionResponse.questionId)
                    sendMoEngageEvent(apiAskQuestionResponse.questionId)

                    _matchPageCarousal.postValue(matchCarousalsData)
                    onAdvancedSearchOptionsSuccess(advancedSearchOptions)
                    onImageMatchQuestionSuccess(apiAskQuestionResponse)

                    val matchResult = matchQuestionMapper.map(apiAskQuestionResponse)
                    unmodifiedMatchResults = matchResult.matchedQuestions.toList()

                    return@Function3 combineMatchResultsWithNudges(
                        matchResult,
                        matchPageNudgesData,
                        apiAskQuestionResponse.matchesDisplayComfig
                    )
                }
            ).applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    { matchResultsWithNudges ->
                        _matchResultsLiveData.value = Outcome.loading(false)
                        _matchResultsLiveData.value = Outcome.success(matchResultsWithNudges)
                    },
                    {
                        onApiError(
                            error = it,
                            requestCode = Constants.REQUEST_CODE_ASK_TEXT_SEARCH,
                            fromMethod = "makeParallelRequestsForAdvancedSearchAndOtherWidgets"
                        )
                    }
                )
        )
    }

    private fun onAdvancedSearchOptionsSuccess(advancedSearchOptions: ApiAdvancedSearchOptions) {
        val advancedSearchOptionsViewItem =
            advancedSearchOptionsMapper.map(Pair(advancedSearchOptions, false))
        _filterFacetLiveData.postValue(advancedSearchOptionsViewItem.facetList)
        _advancedSearchOptionsLiveData.postValue(advancedSearchOptionsViewItem)
        if (isAdvancedSearchShownFirstTime) {
            sendEventForFilterFirstShown(advancedSearchOptions.facets)
        }
    }

    fun getImageAsByteArray(askedQuestionImageUri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val imageFile = File(askedQuestionImageUri)
            if (imageFile.isFile) {

                val byteArray = imageFile.readBytes()

                val quesBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                _imageBitmapLiveData.postValue(Outcome.success(quesBitmap))
            } else {
                _imageBitmapLiveData.postValue(Outcome.Failure(FileNotFoundException()))
            }
        }
    }

    fun runTextRecognition() {
        val bitmap = scaleDownImageData?.second ?: return
        viewModelScope.launch {
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { result ->
                        googleVisionImageOcrData = Pair(result.text, bitmap.allocationByteCount)
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getMatchQuestionBannerData() {
        if (networkUtil.isConnected()) {
            compositeDisposable.add(
                matchQuestionRepository
                    .getMatchQuestionBanner()
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle({
                        _matchQuestionBannerLiveData.value =
                            Outcome.success(matchQuestionBannerMapper.map(it))
                    }, {

                    })
            )
        }
    }

    fun getMatchPageDoubts(questionId: String, offsetCursor: Int?) {
        _matchPageDoubtLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            doubtPeCharchaRepository.getMatchPageDoubts(questionId, offsetCursor)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        _matchPageDoubtLiveData.value = Outcome.loading(false)
                        _matchPageDoubtLiveData.value = Outcome.success(it)
                    },
                    {
                        _matchPageDoubtLiveData.value = Outcome.loading(false)
                    }
                )
        }
    }

    fun getPopupOptionsData(page: String, feedback: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = matchQuestionRepository.getFeedbackPopupData(page, feedback)
                withContext(Dispatchers.Main) {
                    _popupFeedbackLiveData.value = result
                    doShowFeedbackPopDialog = result.options.isNotEmpty()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onApiError(error = e, fromMethod = "getPopupOptionsData")
                }
            }
        }
    }

    fun submitPopupSelection(
        page: String,
        feedbackOptionSelected: String,
        entityId: Long,
        isCancelClicked: Boolean,
        display: String
    ) {
        val feedbackSelected = arrayOf(feedbackOptionSelected)
        _submitFeedbackPreferenceData.postValue(Outcome.loading(true))
        feedbackTypeSelectedForPopupDialog = feedbackOptionSelected
        feedbackTypeDisplayForPopupDialog = display
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = matchQuestionRepository.submitPopupSelections(
                    page, entityId,
                    feedbackSelected, isCancelClicked
                )
                withContext(Dispatchers.Main) {
                    _submitFeedbackPreferenceData.postValue(Outcome.loading(false))
                }
                _popupFeedbackLiveData.postValue(result)
                isFeedbackOptionSelectedByUser = true
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _submitFeedbackPreferenceData.postValue(Outcome.loading(false))
                }
                onApiError(error = e, fromMethod = "submitPopupSelection")
            }
        }
    }

    fun submitPopupFeedbackSelection(page: String, preferenceFromUser: String, entityId: Long) {
        _submitFeedbackPreferenceData.value = Outcome.loading(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = matchQuestionRepository.submitFeedbackPreferences(
                    page, entityId,
                    feedbackTypeSelectedForPopupDialog, arrayOf(preferenceFromUser)
                )
                withContext(Dispatchers.Main) {
                    _submitFeedbackPreferenceData.postValue(Outcome.loading(false))
                    _submitFeedbackPreferenceData.postValue(Outcome.success(result))
                }
            } catch (e: Exception) {
                onApiError(error = e, fromMethod = "submitPopupFeedbackSelection")
                _submitFeedbackPreferenceData.postValue(Outcome.loading(false))
            }
        }
    }

    // Start P2P
    fun connectToPeer(questionImage: String?, questionText: String?, questionId: String) {
        compositeDisposable +
                DataHandler.INSTANCE.doubtPeCharchaRepository.connectToPeer(
                    questionImage = questionImage,
                    questionText = questionText,
                    questionId = questionId
                ).applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        { p2PData ->
                            _doubtP2PData.postValue(SingleEvent(p2PData))
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun setDataToRequestP2p(questionImageUrl: String?, questionText: String?, questionId: String) {
        p2pRequestData = P2pRequestData(
            questionImageUrl = questionImageUrl,
            questionText = questionText,
            questionId = questionId,
            thumbnailImages = p2pThumbnailImages
        )
    }

    fun getDataToRequestP2p() = p2pRequestData

    fun getDoubtP2pData() = userPreference.getDoubtP2pData()

    fun getMatchResultsWithNudges(
        isP2pConnected: Boolean,
        isFeedbackSubmitted: Boolean
    ): List<MatchQuestionViewItem> {
        val matchResultsWithNudges = matchResultsWithNudges.toMutableList()
        if (isP2pConnected) {
            val indexOfP2pWidget = matchResultsWithNudges.indexOfFirst {
                it is MatchPageWidgetViewItem &&
                        it.widget.type == WidgetTypes.TYPE_MATCH_PAGE_EXTRA_FEATURE &&
                        (it.widget._data as MatchPageExtraFeatureWidget.Data).feature == MatchPageFeature.P2P.feature
            }
            if (indexOfP2pWidget != -1) {
                matchResultsWithNudges.removeAt(indexOfP2pWidget)
            }
        }
        if (isFeedbackSubmitted) {
            val indexOfBookFeedbackWidget = matchResultsWithNudges.indexOfFirst {
                it is MatchPageWidgetViewItem &&
                        it.widget.type == WidgetTypes.TYPE_MATCH_PAGE_EXTRA_FEATURE &&
                        (it.widget._data as MatchPageExtraFeatureWidget.Data).feature == MatchPageFeature.BOOK_FEEDBACK.feature
            }
            if (indexOfBookFeedbackWidget != -1) {
                matchResultsWithNudges.removeAt(indexOfBookFeedbackWidget)
            }
        }
        return matchResultsWithNudges
    }
    // End P2P

    // Start Events
    fun sendEventForFilterSelection(filterList: List<MatchFilterFacetViewItem>) {
        filterList.filter { it.isSelected }.forEach { facet ->
            val items = facet.data.filter { it.isSelected }.joinToString { it.display }
            sendEvent(
                "${EventConstants.EVENT_FILTER_SELECTED}_${facet.facetType}", hashMapOf(
                    Constants.ITEMS to items
                )
            )
        }
    }

    private fun sendEventForFilterFirstShown(filterList: List<ApiAdvanceSearchData>) {
        filterList.forEach { facet ->
            val items = facet.data.joinToString { it.display }
            sendEvent(
                "${EventConstants.EVENT_FILTER_AVAILABLE}_${facet.facetType}", hashMapOf(
                    Constants.ITEMS to items
                ), true
            )
        }
    }

    fun sendTabClickEvent(source: String) {
        matchQuestionEventManager.askTabClick(source)
    }

    fun addScreenTime(screenName: String?, screenTime: Long?) {
        if (screenName != null && screenTime != null) {
            val difference = System.currentTimeMillis() - screenTime
            val lastScreenTime = tabScreenTimeMap[screenName] ?: 0L
            tabScreenTimeMap[screenName] = difference + lastScreenTime
        }
    }

    fun sendEventForTabScreenTime() {
        tabScreenTimeMap.forEach {
            val screenName = it.key
            val screenTime = it.value
            if (screenName != null && screenTime != null) {
                sendEvent(
                    EventConstants.EVENT_NAME_MATCHES_TABS.replace("?", screenName),
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.SCREEN_TIME, screenTime)
                    })
            }
        }

        // Clear map
        tabScreenTimeMap.clear()
    }

    private fun sendApiResponseTimeEvent(questionId: String) {
        sendEvent(
            event = EventConstants.MATCH_PAGE_API_RESPONSE_TIME,
            param = hashMapOf<String, Any>().apply {
                put(EventConstants.MATCH_PAGE_SIGNED_URL_API, apiResponseTime.signedUrlApi)
                put(EventConstants.MATCH_PAGE_IMAGE_UPLOAD_API, apiResponseTime.imageUploadApi)
                put(EventConstants.MATCH_PAGE_IMAGE_RESULT_API, apiResponseTime.imageMatchResultApi)
                put(EventConstants.MATCH_PAGE_TEXT_RESULT_API, apiResponseTime.textMatchResultApi)
                put(EventConstants.QUESTION_ID, questionId)
            }
        )

        val elapsedTime: Long = apiResponseTime.run {
            signedUrlApi + imageUploadApi + imageMatchResultApi + textMatchResultApi
        }
        sendEventForApiResponseBucketing(elapsedTime)
    }

    private fun sendMoEngageEvent(parentQuestionId: String) {
        analyticsPublisher.publishMoEngageEvent(
            AnalyticsEvent(
                EventConstants.QUESTION_ASK,
                hashMapOf<String, Any>().apply {
                    put(Constants.QUESTION_ID, parentQuestionId)
                }
            )
        )
    }

    private fun sendImageRotationAngleEvent(parentQuestionId: String, questionImageUrl: String?) {
        val selectedImageMetaData = questionMetaData ?: return
        if (selectedImageMetaData.sendEvent.not()) return
        sendEvent(
            EventConstants.SELECTED_ROTATED_IMAGE,
            hashMapOf<String, Any>().apply {
                put(EventConstants.QUESTION_ID, parentQuestionId)
                put(EventConstants.ROTATION_ANGLE, selectedImageMetaData.rotationAngle)
                put(Constants.IMAGE_URL, questionImageUrl.orEmpty())
            }
        )
    }

    fun sendMatchPageExitEvent(source: String, ignoreSnowplow: Boolean = true) {
        val param = hashMapOf<String, Any>().apply {
            put(EventConstants.SOURCE, source)
            put(EventConstants.MATCH_RESULT_SHOWN, isMatchResponseFetched)
        }
        matchQuestionEventManager.eventWith(EventConstants.MATCH_PAGE_EXIT, param, ignoreSnowplow)
    }

    private fun sendEventForApiResponseBucketing(elapsedTime: Long) {

        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.API_RESPONSE_TIME, elapsedTime / 1000)
            put(EventConstants.QUESTION_ID2, parentQuestionId)
        }

        sendEvent(EventConstants.ASK_API_RESPONSE, params)

        when {
            elapsedTime / 1000 in 0L..5L -> sendEvent(EventConstants.ASK_QUESTION_0_5_SEC)
            elapsedTime / 1000 in 5L..10L -> sendEvent(EventConstants.ASK_QUESTION_5_10_SEC)
            elapsedTime / 1000 in 10L..15L -> sendEvent(EventConstants.ASK_QUESTION_10_15_SEC)
            elapsedTime / 1000 in 15L..20L -> sendEvent(EventConstants.ASK_QUESTION_15_20_SEC)
            else -> sendEvent(EventConstants.ASK_QUESTION_MORE_THAN_20_SEC)
        }
    }

    fun sendEvent(
        event: String,
        param: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false
    ) = matchQuestionEventManager.eventWith(event, param, ignoreSnowplow)
    // End events
}