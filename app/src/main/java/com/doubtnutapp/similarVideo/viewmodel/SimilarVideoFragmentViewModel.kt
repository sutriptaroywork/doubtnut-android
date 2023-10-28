package com.doubtnutapp.similarVideo.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnutapp.Constants
import com.doubtnutapp.Log
import com.doubtnutapp.R
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.domain.similarVideo.entities.SimilarTopicBoosterEntity
import com.doubtnutapp.domain.similarVideo.entities.SimilarVideoEntity
import com.doubtnutapp.domain.similarVideo.interactor.*
import com.doubtnutapp.domain.topicbooster.interactor.GetTopicBoosterUseCase
import com.doubtnutapp.home.event.HomeEventManager
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.likeDislike.LikeDislikeVideo
import com.doubtnutapp.matchquestion.model.SubjectTabViewItem
import com.doubtnutapp.pCBanner.SimilarPCBannerVideoItem
import com.doubtnutapp.plus
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.similarVideo.mapper.SimilarVideoMapper
import com.doubtnutapp.similarVideo.model.*
import com.doubtnutapp.similarVideo.ui.SimilarVideoFragment
import com.doubtnutapp.utils.Event
import com.google.gson.JsonSyntaxException
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.util.*
import javax.inject.Inject

class SimilarVideoFragmentViewModel @Inject constructor(
    private val getSimilarVideoUseCase: GetSimilarVideoUseCase,
    private val getTopicBoosterUseCase: GetTopicBoosterUseCase,
    private val similarVideoMapper: SimilarVideoMapper,
    private val likeDislikeVideo: LikeDislikeVideo,
    private val postQuestionToCommunity: PostQuestionToCommunity,
    private val submitQuestionMatchFeedback: SubmitSimilarQuestionFeedback,
    private val getPreviousSimilarVideoInteractor: GetPreviousSimilarVideoInteractor,
    private val saveSimilarVideoInteractor: SaveSimilarVideoInteractor,
    compositeDisposable: CompositeDisposable,
    private val homeEventManager: HomeEventManager,
    private val submitSimilarTopicBoosterQuestion: SubmitSimilarTopicBoosterQuestion,
    private val popularCourseWidgetClickUseCase: PopularCourseWidgetClickUseCase
) : BaseViewModel(compositeDisposable) {

    private val FEEDBACK_YES = 1
    private val FEEDBACK_NO = 0
    private val screenName: String = "similar"

    private var textSolutionStackCount = 0

    private val _navigateScreenLiveData: MutableLiveData<SingleEvent<Pair<Screen, Map<String, Any?>?>>> =
        MutableLiveData()

    val navigateScreenLiveData: LiveData<SingleEvent<Pair<Screen, Map<String, Any?>?>>>
        get() = _navigateScreenLiveData

    private val _navigateToSameScreenLiveData: MutableLiveData<SingleEvent<Pair<Screen, Map<String, Any?>>>> =
        MutableLiveData()

    val navigateToSameScreenLiveData: LiveData<SingleEvent<Pair<Screen, Map<String, Any?>>>>
        get() = _navigateToSameScreenLiveData

    private val _navigateToNewScreenLiveData: MutableLiveData<SingleEvent<Pair<Screen, Map<String, Any?>>>> =
        MutableLiveData()

    val navigateToNewScreenLiveData: LiveData<SingleEvent<Pair<Screen, Map<String, Any?>>>>
        get() = _navigateToNewScreenLiveData

    private val _showProgressLiveData: MutableLiveData<Boolean> = MutableLiveData()

    val showProgressLiveData: LiveData<Boolean>
        get() = _showProgressLiveData

    private val _similarVideoLiveData: MutableLiveData<Outcome<Pair<List<RecyclerViewItem>, List<SubjectTabViewItem>?>>> =
        MutableLiveData()

    val similarVideoLiveData: LiveData<Outcome<Pair<List<RecyclerViewItem>, List<SubjectTabViewItem>?>>>
        get() = _similarVideoLiveData

    private val _feedbackViewReplacement: MutableLiveData<RecyclerViewItem> = MutableLiveData()

    val feedbackViewReplacement: LiveData<RecyclerViewItem>
        get() = _feedbackViewReplacement

    private val _postQuestionResponse: MutableLiveData<Outcome<Unit>> = MutableLiveData()

    val postQuestionResponse: LiveData<Outcome<Unit>>
        get() = _postQuestionResponse

    private val _addToPlayListLiveData: MutableLiveData<Event<String>> = MutableLiveData()

    val addToPlayListLiveData: LiveData<Event<String>>
        get() = _addToPlayListLiveData

    private val _eventLiveData: MutableLiveData<String> = MutableLiveData()

    val eventLiveData: LiveData<String>
        get() = _eventLiveData

    private val _emptyStackLiveData: MutableLiveData<Boolean> = MutableLiveData()

    private val _topicBoosterPosition: MutableLiveData<Int> = MutableLiveData()

    val topicBoosterPosition: LiveData<Int>
        get() = _topicBoosterPosition

    var askedQuestionId: String = ""

    var supportedResourceType: String = ""

    var page: String = "" //Will have the same value as the page in fragment

    private var similarVideoEntity: SimilarVideoEntity? = null

    var hashMapSimilar = linkedMapOf<String, List<RecyclerViewItem>>()

    var closeConvivaSession: MutableLiveData<Boolean> = MutableLiveData(false)

    fun handleAction(action: Any, playlistId: String = "") {
        when (action) {
            is LikeVideo -> {
                likeDislikeVideo.likeDislikeVideo(action.videoId, screenName, action.isLiked)
            }
            is PlayVideo -> {
                closeConvivaSession.postValue(true)
                openVideoScreen(action, false)
                sendApxorEvent(
                    EventConstants.SIMILAR_VIDEO_CARD_PLAYED, hashMapOf(
                        Constants.ASKED_QUESTION_ID to askedQuestionId,
                        Constants.VIDEO_ID to action.videoId
                    ), ignoreSnowplow = true
                )
            }
            is SubmitFeedBack -> submitFeedback(action)
            is OpenCameraFragment -> _navigateScreenLiveData.value = SingleEvent(Pair(CameraScreen, null))
            is PostQuestion -> postQuestionToCommunity()
            is AddToPlayList -> addToPlayList(action.videoId)
            is OpenWhatsapp -> openWhatsapp(action.externalUrl)
            is OpenPCPopup -> openUnlockPopup()
            is VideoTagClick -> {
                openViewPlayListScreen(action.tagName, action.questionId, playlistId)
            }
            is UpdateSimilarTopicBoosterQuestion -> {
                if (action.wrongOptionPosition == null) {
                    sendEvent(EventConstants.TOPIC_BOOSTER_ANSWER_CORRECT)
                    sendApxorEvent(EventConstants.TOPIC_BOOSTER_ANSWER_CORRECT, hashMapOf())
                }
                updateSimilarVideoEntity(
                    action.topicBoosterPosition,
                    action.correctOptionPosition,
                    action.wrongOptionPosition
                )
                submitChallengeQuestionAnswer(action.similarTopicBoosterViewItem)
                sendEvent(EventConstants.TOPIC_BOOSTER_ANSWER_SELECTED)
                sendApxorEvent(EventConstants.TOPIC_BOOSTER_ANSWER_SELECTED, hashMapOf())
            }
            is SendViewSolutionTapEvents -> {
                sendEvent(action.eventName)
                sendApxorEvent(action.eventName, hashMapOf())
            }
            is PlayTopicBoosterSolutionVideo -> {
                openVideoScreen(
                    PlayVideo(
                        action.videoId,
                        action.page,
                        action.playlistId,
                        action.categoryTitle,
                        action.resourceType
                    ), true
                )
            }

            is OpenLibraryVideoPlayListScreen -> openPlayListScreen(action)

            is OpenLibraryPlayListActivity -> openLibraryListingActivity(action)

            is PublishEvent -> sendApxorEvent(action.event.name, action.event.params)
            is OpenInAppSearch -> openInAppSearchScreen(action)
        }
    }

    private fun openInAppSearchScreen(action: OpenInAppSearch) {
        sendApxorEvent(
            EventConstants.IAS_SIMILAR_WIDGET_SEARCH, hashMapOf(
                EventConstants.SEARCHED_TEXT to (action.searchQuery ?: ""),
                Constants.SOURCE to "similar"
            )
        )
        val argument = hashMapOf(
            Constants.SEARCH_QUERY to action.searchQuery,
            Constants.VOICE_SEARCH to action.isVoiceSearch,
            Constants.SOURCE to "similar",
            Constants.EVENT_NAME to EventConstants.IAS_SIMILAR_WIDGET_SEARCH
        )
        _navigateScreenLiveData.value = SingleEvent(Pair(InAppSearchScreen, argument))
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

    private fun updateSimilarVideoEntity(
        topicBoosterPosition: Int,
        correctOptionPosition: Int?,
        wrongOptionPosition: Int?
    ) {
        similarVideoEntity?.let { similar ->

            if (topicBoosterPosition != -1 && topicBoosterPosition < similar.matchedQuestions.size) {

                try {
                    correctOptionPosition?.let {
                        (similar.matchedQuestions[topicBoosterPosition] as SimilarTopicBoosterEntity).options[it].optionStatus =
                            1
                    }
                    wrongOptionPosition?.let {
                        (similar.matchedQuestions[topicBoosterPosition] as SimilarTopicBoosterEntity).options[it].optionStatus =
                            2
                    }

                    (similar.matchedQuestions[topicBoosterPosition] as SimilarTopicBoosterEntity).isSubmitted =
                        1
                    (similar.matchedQuestions[topicBoosterPosition] as SimilarTopicBoosterEntity).submittedOption =
                        (if (wrongOptionPosition == null) {
                            (similar.matchedQuestions[topicBoosterPosition] as SimilarTopicBoosterEntity).options[correctOptionPosition!!].optionCode
                        } else (similar.matchedQuestions[topicBoosterPosition] as SimilarTopicBoosterEntity).options[wrongOptionPosition].optionCode)
                } catch (e: ArrayIndexOutOfBoundsException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun submitChallengeQuestionAnswer(similarTopicBoosterViewItem: SimilarTopicBoosterViewItem) {
        compositeDisposable.add(
            submitSimilarTopicBoosterQuestion.execute(
                SubmitSimilarTopicBoosterQuestion.Param(
                    similarTopicBoosterViewItem.submittedOption.toString(),
                    similarTopicBoosterViewItem.questionId,
                    similarTopicBoosterViewItem.submitUrlEndpoint,
                    similarTopicBoosterViewItem.widgetType
                )
            ).applyIoToMainSchedulerOnCompletable()
                .subscribe(
                    {},
                    this::onMatchQuestionError
                )
        )
    }

    private fun openViewPlayListScreen(tagName: String, questionId: String, playlistId: String) {
        _navigateLiveData.value =
            Event(NavigationModel(LibraryVideoPlayListScreen, hashMapOf<String, Any>().apply {
                put(Constants.PLAYLIST_ID, playlistId)
                put(Constants.PLAYLIST_TITLE, tagName)
                put(Constants.QUESTION_ID, questionId)
                put(Constants.VIDEO_TAG_NAME, tagName)
                put(Constants.IS_FROM_VIDEO_TAG, true)
            }))
    }

    private fun openWhatsapp(externalUrl: String) {
        val args = hashMapOf(
            Constants.EXTERNAL_URL to externalUrl
        )
        _navigateLiveData.value = Event(NavigationModel(ExternalUrlScreen, args))
        sendEvent(EventConstants.EVENT_WHATSAPP_CARD_CLICK + "SimilarVideo")

    }

    private fun addToPlayList(videoId: String) {
        _addToPlayListLiveData.value = Event(videoId)
    }

    private fun openUnlockPopup() {
        _navigateLiveData.value = Event(NavigationModel(PCUnlockScreen, null))
    }

    private fun openVideoScreen(action: PlayVideo, isFromTopicBooster: Boolean) {
        val argument = hashMapOf(
            Constants.PAGE to if (page == Constants.PAGE_SRP) Constants.PAGE_MPVP else Constants.PAGE_SIMILAR,
            Constants.QUESTION_ID to action.videoId,
            Constants.IS_FROM_TOPIC_BOOSTER_SOLUTION to isFromTopicBooster
        )
        val screen =
            if (action.resourceType == SOLUTION_RESOURCE_TYPE_VIDEO) VideoScreen else TextSolutionScreen

        if (action.resourceType == supportedResourceType) {
            if (textSolutionStackCount >= 3) {
                _navigateToNewScreenLiveData.value = SingleEvent(Pair(screen, argument))
            } else {
                _navigateToSameScreenLiveData.value = SingleEvent(Pair(screen, argument))
                if (screen == TextSolutionScreen) {
                    textSolutionStackCount += 1
                }
            }
        } else {
            _navigateToNewScreenLiveData.value = SingleEvent(Pair(screen, argument))
        }
    }

    private fun onSimilarVideoSuccess(similarVideoEntity: SimilarVideoEntity) {
        hashMapSimilar = linkedMapOf()
        compositeDisposable.add(
            Single.fromCallable {
                val similarResult = similarVideoEntity.run {
                    similarVideoMapper.map(similarVideoEntity)
                }
                this.similarVideoEntity = similarVideoEntity

                val similarVideoViewItem = mutableListOf<RecyclerViewItem>()
                hashMapSimilar["All"] = similarVideoViewItem

                val similarVideoFetched: List<SimilarVideoList> =
                    similarResult.matchedQuestions.filterIsInstance<SimilarVideoList>()
                val filter = similarVideoFetched.groupBy { it.targetCourse }
                filter.keys.forEach { filterKeys ->
                    val oneList = mutableListOf<RecyclerViewItem>()
                    if (filterKeys != null && filter[filterKeys] != null) {
                        val meta = filter[filterKeys]?.groupBy { it.meta }
                        meta?.keys?.forEach { metaKey ->
                            val oneMetaList = meta[metaKey].orEmpty()
                            if (!oneMetaList.isNullOrEmpty()) {
                                if (!metaKey.isNullOrBlank()) {
                                    var videoText = oneMetaList.size.toString()
                                    videoText = if (videoText == "0" || videoText == "1") {
                                        "$videoText Video"
                                    } else {
                                        "$videoText Videos"
                                    }
                                    oneList.add(
                                        SimilarHeaderViewItem(
                                            metaKey.orEmpty(),
                                            videoText,
                                            R.layout.item_similar_header
                                        )
                                    )
                                }
                                oneList.addAll(oneMetaList)
                            }
                        }
                        if (!oneList.isNullOrEmpty()) {
                            hashMapSimilar[filterKeys] = oneList
                        }
                    }
                }

                if (similarResult.conceptsVideo != null && similarResult.conceptsVideo.isNotEmpty()) {
                    similarVideoViewItem.add(
                        HorizontalCardViewItem(
                            similarResult.conceptsVideo,
                            R.layout.concept_video_view
                        )
                    )
                }

                val similarListCommon =
                    similarResult.matchedQuestions.filterIsInstance<SimilarVideoList>()
                        .groupBy { it.meta }
                val initialList = mutableListOf<RecyclerViewItem>()
                similarListCommon.keys.forEach {
                    if (it != null) {
                        val list = similarListCommon[it].orEmpty()
                        if (!it.isNullOrBlank()) {
                            var videoTextCount = list.size.toString()
                            videoTextCount = if (videoTextCount == "0" || videoTextCount == "1") {
                                "$videoTextCount Video"
                            } else {
                                "$videoTextCount Videos"
                            }
                            initialList.add(
                                SimilarHeaderViewItem(
                                    it.orEmpty(),
                                    videoTextCount,
                                    R.layout.item_similar_header
                                )
                            )
                        }
                        initialList.addAll(list)
                    }
                }
                similarVideoViewItem.addAll(initialList)
                similarResult.feedbackViewItem?.let {
                    if (it.isShow == 1) {
                        similarVideoViewItem.add(it)
                    }
                }

                val indexOfBanner =
                    similarResult.matchedQuestions.indexOfFirst { it is SimilarPCBannerVideoItem }
                val indexOfWhatsapp =
                    similarResult.matchedQuestions.indexOfFirst { it is SimilarVideoWhatsappViewItem }
                val indexOfIasSearch =
                    similarResult.matchedQuestions.indexOfFirst { it is SimilarTopicSearchViewItem }
                val indexOfSaleCard =
                    similarResult.matchedQuestions.indexOfFirst { it is SaleTimerItem }
                val indexOfScratchCard =
                    similarResult.matchedQuestions.indexOfFirst { it is ScratchCardItem }

                if (indexOfBanner != -1) {
                    if (!similarVideoViewItem.isNullOrEmpty() && similarVideoViewItem.size > indexOfBanner) {
                        similarVideoViewItem.addAll(
                            indexOfBanner,
                            similarResult.matchedQuestions.filterIsInstance<SimilarPCBannerVideoItem>()
                        )
                    } else {
                        similarVideoViewItem.addAll(similarResult.matchedQuestions.filterIsInstance<SimilarPCBannerVideoItem>())
                    }
                }

                if (indexOfWhatsapp != -1) {
                    if (!similarVideoViewItem.isNullOrEmpty() && similarVideoViewItem.size > indexOfWhatsapp) {
                        similarVideoViewItem.addAll(
                            indexOfWhatsapp,
                            similarResult.matchedQuestions.filterIsInstance<SimilarVideoWhatsappViewItem>()
                        )
                    } else {
                        similarVideoViewItem.addAll(similarResult.matchedQuestions.filterIsInstance<SimilarVideoWhatsappViewItem>())
                    }
                }

                var hasNcertItem = false

                similarResult.matchedQuestions.forEachIndexed { index, recyclerViewItem ->
                    if (recyclerViewItem is NcertViewItem || recyclerViewItem is SimilarWidgetViewItem) {
                        if (recyclerViewItem is NcertViewItem) {
                            hasNcertItem = true
                        } else if (recyclerViewItem is SimilarWidgetViewItem) {
                            if (recyclerViewItem.widget.extraParams == null) {
                                recyclerViewItem.widget.extraParams = hashMapOf()
                            }
                            recyclerViewItem.widget.extraParams?.put(
                                EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME,
                                SimilarVideoFragment.TAG
                            )
                        }
                        if (!similarVideoViewItem.isNullOrEmpty() && similarVideoViewItem.size > index) {
                            similarVideoViewItem.add(index, recyclerViewItem)
                        } else {
                            similarVideoViewItem.add(recyclerViewItem)
                        }
                    }
                }

                // Start - Insert topic booster in final similar video list
                val indexOfTopicBooster =
                    similarResult.matchedQuestions.indexOfFirst { it is SimilarTopicBoosterViewItem }
                if (indexOfTopicBooster != -1) {
                    _topicBoosterPosition.postValue(indexOfTopicBooster)
                    if (!similarVideoViewItem.isNullOrEmpty() && similarVideoViewItem.size > indexOfTopicBooster) {
                        similarVideoViewItem.addAll(
                            indexOfTopicBooster,
                            similarResult.matchedQuestions.filterIsInstance<SimilarTopicBoosterViewItem>()
                        )
                    } else {
                        similarVideoViewItem.addAll(similarResult.matchedQuestions.filterIsInstance<SimilarTopicBoosterViewItem>())
                    }
                }
                // End - Insert topic booster in final similar video list

                if (hasNcertItem) {
                    sendApxorEvent(EventConstants.NCERT_RE_ENTRY_SIMILAR, hashMapOf())
                }

                if (indexOfIasSearch != -1) {
                    if (!similarVideoViewItem.isNullOrEmpty() && similarVideoViewItem.size > indexOfIasSearch + 1) {
                        similarVideoViewItem.addAll(
                            indexOfIasSearch + 1,
                            similarResult.matchedQuestions.filterIsInstance<SimilarTopicSearchViewItem>()
                        )
                    } else {
                        similarVideoViewItem.addAll(similarResult.matchedQuestions.filterIsInstance<SimilarTopicSearchViewItem>())
                    }
                }
                //todo test
                similarResult.matchedQuestions.forEach {
                    if (it is SaleTimerItem) {
                        it.responseAtTimeInMillis = System.currentTimeMillis()
                    }
                }
                if (indexOfSaleCard != -1) {
                    if (!similarVideoViewItem.isNullOrEmpty() && similarVideoViewItem.size > indexOfSaleCard + 1) {
                        similarVideoViewItem.addAll(
                            indexOfSaleCard + 1,
                            similarResult.matchedQuestions.filterIsInstance<SaleTimerItem>()
                        )
                    } else {
                        similarVideoViewItem.addAll(similarResult.matchedQuestions.filterIsInstance<SaleTimerItem>())
                    }
                }

                if (indexOfScratchCard != -1) {
                    if (!similarVideoViewItem.isNullOrEmpty() && similarVideoViewItem.size > indexOfScratchCard + 1) {
                        similarVideoViewItem.addAll(
                            indexOfScratchCard + 1,
                            similarResult.matchedQuestions.filterIsInstance<ScratchCardItem>()
                        )
                    } else {
                        similarVideoViewItem.addAll(similarResult.matchedQuestions.filterIsInstance<ScratchCardItem>())
                    }
                }

                hashMapSimilar["All"] = similarVideoViewItem
                val filterList = mutableListOf<SubjectTabViewItem>()
                hashMapSimilar.keys.forEach {
                    filterList.add(SubjectTabViewItem(it, it, true))
                }
                filterList
            }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation()).subscribe({
                    _similarVideoLiveData.value = Outcome.loading(false)
                    _similarVideoLiveData.value = Outcome.success(
                        Pair(
                            hashMapSimilar["All"].orEmpty(),
                            if (it.isNullOrEmpty() || it.size == 1) {
                                null
                            } else {
                                it
                            }
                        )
                    )
                }, {
                    _similarVideoLiveData.value = Outcome.loading(false)
                })
        )
    }

    fun getFilteredList(filterText: String): List<RecyclerViewItem> {
        return hashMapSimilar[filterText].orEmpty()
    }

    private fun onMatchQuestionError(error: Throwable) {
        _similarVideoLiveData.value = Outcome.loading(false)
        _similarVideoLiveData.value = if (error is HttpException) {
            val errorCode = error.response()?.code()
            when (errorCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
        logException(error)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException
                || error is NullPointerException
                || error is ClassCastException
                || error is FormatException
                || error is IllegalArgumentException
            ) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    fun getSimilarResults(
        questionId: String, mcId: String?, playlistId: String?, page: String?,
        parentId: String?, ocr: String?, isFilter: Boolean
    ) {
        addSimilarVideoData()
        _similarVideoLiveData.value = Outcome.loading(true)
        compositeDisposable + getSimilarVideoUseCase
            .execute(
                GetSimilarVideoUseCase.Param(
                    questionId,
                    mcId,
                    playlistId,
                    page,
                    parentId,
                    ocr,
                    isFilter
                )
            )
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                onSimilarVideoSuccess(it)
            }, this::onMatchQuestionError)
    }

    fun clickOnWidget(){
        viewModelScope.launch (Dispatchers.IO){
            try {
                popularCourseWidgetClickUseCase.clickOnWidget()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun getCurrentSimilar() {
        similarVideoEntity?.let {
            onSimilarVideoSuccess(it)
        }
    }

    fun getPreviousVideo() {
        _similarVideoLiveData.value = Outcome.loading(true)
        compositeDisposable.add(
            getPreviousSimilarVideoInteractor
                .execute(GetPreviousSimilarVideoInteractor.None())
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    onSimilarVideoSuccess(it)
                }, this::noVideoOnStack)
        )
    }

    fun getTopicBooster(questionId: String) {
        _similarVideoLiveData.value = Outcome.loading(true)
        compositeDisposable + getTopicBoosterUseCase
            .execute(GetTopicBoosterUseCase.Param(questionId))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                onSimilarVideoSuccess(it)
            }, ::onMatchQuestionError)
    }

    private fun noVideoOnStack(error: Throwable) {
        _similarVideoLiveData.value = Outcome.loading(false)
        if (error == EmptyStackException()) {
            _emptyStackLiveData.value = true
        }
    }

    private fun addSimilarVideoData() {
        similarVideoEntity?.let {
            compositeDisposable.add(
                saveSimilarVideoInteractor
                    .execute(SaveSimilarVideoInteractor.Param(it))
                    .applyIoToMainSchedulerOnCompletable()
                    .subscribe({}, {
                        it.printStackTrace()
                    })
            )
        }

    }

    private fun submitFeedback(action: SubmitFeedBack) {

        when (action.isLiked) {
            FEEDBACK_YES -> _feedbackViewReplacement.value = AskNowViewItem(
                R.string.title_matchQuestion_askQuestion,
                R.string.buttonText_matchQuestion_askNow,
                action.bgColorOfFeedView,
                R.layout.item_similar_video_askquestion
            )

            FEEDBACK_NO -> _feedbackViewReplacement.value = PostCommunityViewItem(
                R.string.title_matchQuestion_postCommunity,
                R.string.buttonText_matchQuestion_postOnCommunity,
                action.bgColorOfFeedView,
                R.layout.item_similar_video_postcommunity
            )
        }

        submitQuestionMatchFeedback.execute(action.isLiked).applyIoToMainSchedulerOnCompletable()
            .subscribe()
    }

    private fun postQuestionToCommunity() {
        _showProgressLiveData.value = true
        compositeDisposable + postQuestionToCommunity.execute(
            PostQuestionToCommunity.Param(
                askedQuestionId,
                ""
            )
        )
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable(this::onPostQuestionSuccess, this::onPostQuestionError)

    }

    private fun onPostQuestionError(error: Throwable) {
        _showProgressLiveData.value = false

        _postQuestionResponse.value = if (error is HttpException) {
            val errorCode = error.response()?.code()
            when (errorCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
        logException(error)
    }

    private fun onPostQuestionSuccess() {
        _showProgressLiveData.value = false
        _postQuestionResponse.value = Outcome.success(Unit)
    }

    private fun sendEvent(eventName: String) {
        _eventLiveData.value = eventName
    }

    fun sendRelatedConceptEvent() {
        homeEventManager.eventWith(EventConstants.EVENT_NAME_RELATED_CONCEPT_CLICK, ignoreSnowplow = true)
    }

    fun sendApxorEvent(
        event: String,
        params: HashMap<String, Any>,
        ignoreSnowplow: Boolean = false
    ) {
        homeEventManager.eventWith(event, params, ignoreSnowplow)
    }

}