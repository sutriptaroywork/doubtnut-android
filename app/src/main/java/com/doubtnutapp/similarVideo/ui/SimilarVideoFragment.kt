package com.doubtnutapp.similarVideo.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.observer.SingleEventObserver
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.VipStateEvent
import com.doubtnutapp.addtoplaylist.AddToPlaylistFragment
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_ETOOS_VIDEO
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_TEXT
import com.doubtnutapp.eventmanager.CommonEventManager
import com.doubtnutapp.matchquestion.model.SubjectTabViewItem
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.similarVideo.model.SimilarVideoList
import com.doubtnutapp.similarVideo.model.SimilarWidgetViewItem
import com.doubtnutapp.similarVideo.ui.adapter.FilterAdapter
import com.doubtnutapp.similarVideo.ui.adapter.SimilarAnsweredAdapter
import com.doubtnutapp.similarVideo.viewmodel.SimilarVideoFragmentViewModel
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.playlist.AddPlaylistFragment
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.videoPage.viewmodel.VideoPageViewModel
import com.doubtnutapp.widgets.SpaceItemDecoration
import com.doubtnutapp.widgets.itemdecorator.GridDividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.similar_answered_fragment.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

/**
 * Used in [TextSolutionActivity] and [VideoPageActivity]
 */
class SimilarVideoFragment : DaggerFragment(), ActionPerformer {

    companion object {

        const val TAG = "SimilarVideoFragment"

        private const val PARAM_KEY_QUESTION_ID = "question_id"
        private const val PARAM_KEY_MC_ID = "mc_id"
        private const val PARAM_KEY_PLAYLIST_ID = "playlist_id"
        private const val PARAM_KEY_PAGE = "page"
        private const val PARAM_KEY_IS_MICROCONCEPT = "is_microconcept"
        private const val PARAM_KEY_PARENT_ID = "parent_id"
        private const val PARAM_KEY_FROM_BACKPRESSED = "from_backpressed"
        private const val PARAM_KEY_SOURCE_RESOURCE_TYPE = "support_resource_type"
        private const val PARAM_KEY_OCR_TEXT = "mcr_text"
        private const val PARAM_KEY_AUTO_PLAY = "auto_play"
        private const val WAS_MOVED_TO_LIVE_CLASS = "was_moved_to_live_class"
        private const val PARAM_KEY_IS_FILTER = "is_filter"
        private const val PARAM_KEY_CHAPTER = "chapter"

        fun newInstance(
            questionId: String,
            mc_id: String,
            playlistId: String,
            page: String?,
            fromMicroConcept: Boolean,
            parentId: String?,
            fromBackpressed: Boolean,
            supportResourceType: String,
            ocr: String? = null,
            autoPlay: Boolean = true,
            wasMovedToLiveClass: Boolean = false,
            isFilter: Boolean,
            chapter: String?,
        ): SimilarVideoFragment {
            val fragment = SimilarVideoFragment()
            val args = Bundle()
            args.putString(PARAM_KEY_QUESTION_ID, questionId)
            args.putString(PARAM_KEY_MC_ID, mc_id)
            args.putString(PARAM_KEY_PLAYLIST_ID, playlistId)
            args.putString(PARAM_KEY_PAGE, page)
            args.putBoolean(PARAM_KEY_IS_MICROCONCEPT, fromMicroConcept)
            args.putString(PARAM_KEY_PARENT_ID, parentId)
            args.putBoolean(PARAM_KEY_FROM_BACKPRESSED, fromBackpressed)
            args.putString(PARAM_KEY_SOURCE_RESOURCE_TYPE, supportResourceType)
            args.putString(PARAM_KEY_OCR_TEXT, ocr)
            args.putBoolean(PARAM_KEY_AUTO_PLAY, autoPlay)
            args.putBoolean(WAS_MOVED_TO_LIVE_CLASS, wasMovedToLiveClass)
            args.putBoolean(PARAM_KEY_IS_FILTER, isFilter)
            args.putString(PARAM_KEY_CHAPTER, chapter)
            fragment.arguments = args
            return fragment
        }
    }

    private var adapter: SimilarAnsweredAdapter? = null

    private var filterAdapter: FilterAdapter? = null

    private var vipObserver: Disposable? = null

    private var listener: OnFragmentInteractionListener? = null
    private var fromMicroConcept: Boolean = false
    private var questionId: String = ""
    private var mcId: String = ""
    private var playlistId: String = ""
    private var page: String = ""
    private var ocr: String? = null
    private var firstSimilarVideoQuestionId: String = ""
    private var firstEtoosVideoId: String? = ""
    private var firstItemIsEtoos = false
    private var isFilter: Boolean = false
    private var mFirstSimilarVideoData: SimilarVideoList? = null

    private var askedQuestionId: String = ""

    private var autoPlay = true

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var commonEventManager: CommonEventManager

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

/*    @Inject
    lateinit var convivaVideoAnalytics: Lazy<ConvivaVideoAnalytics>*/

    private lateinit var viewModel: SimilarVideoFragmentViewModel
    private lateinit var videoPageViewModel: VideoPageViewModel
    private var matchResultList: RecyclerView? = null

    private var isFromTopicBooster: Boolean = false

    private var topicBoosterPosition: Int? = null

    private var mSimilarVideosOriginalList: List<RecyclerViewItem> = emptyList()

    val done = AtomicBoolean()

    override fun performAction(action: Any) {
        activity?.let {
            when (action) {
                is FilterSubject -> {
                    filterAdapter?.updateTagSelection(action.position)
                    adapter?.updateData(viewModel.getFilteredList(action.subjectName))
                }
                is SendConceptVideoClickEvent -> {
                    viewModel.sendRelatedConceptEvent()
                    sendEvent(EventConstants.EVENT_NAME_RELATED_CONCEPT_CLICK)
                }
                is ShareOnWhatApp -> {
                    videoPageViewModel.shareOnWhatsApp(action)
                }
                is WatchLaterRequest -> {
                    videoPageViewModel.addToWatchLater(action.id)
                }
                is ShowMoreSimilarVideos -> {
                    updateUi(mSimilarVideosOriginalList.filter {
                        it.viewType == R.layout.item_similar_result
                    })
                    viewModel.sendApxorEvent(
                        EventConstants.SHOW_MORE_SIMILAR_VIDEOS, hashMapOf(
                            Constants.QUESTION_ID to questionId
                        ), ignoreSnowplow = true
                    )
                }
                is ClickOnWidgetAction -> {
                    viewModel.clickOnWidget()
                }
                else -> {
                    viewModel.handleAction(action, playlistId)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.similar_answered_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(SimilarVideoFragmentViewModel::class.java)
        videoPageViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        )[VideoPageViewModel::class.java]
        matchResultList = view?.findViewById(R.id.rvSimilar)
        viewModel.closeConvivaSession.value = false
        getDataIntent()
        setupObserver()
    }

    private fun getDataIntent() {
        autoPlay = if (requireArguments().containsKey(PARAM_KEY_AUTO_PLAY)) {
            arguments?.getBoolean(PARAM_KEY_AUTO_PLAY) ?: true
        } else {
            true
        }
        questionId = arguments?.getString(PARAM_KEY_QUESTION_ID)!!
        if (!requireArguments().getBoolean(PARAM_KEY_FROM_BACKPRESSED)) {
            mcId = arguments?.getString(PARAM_KEY_MC_ID) ?: ""
            playlistId = if (arguments?.getString(PARAM_KEY_PLAYLIST_ID)
                    .isNullOrBlank()
            ) "null" else arguments?.getString(PARAM_KEY_PLAYLIST_ID)
                ?: "null"
            page = arguments?.getString(PARAM_KEY_PAGE) ?: ""
            viewModel.page = page

            fromMicroConcept = arguments?.getBoolean(PARAM_KEY_IS_MICROCONCEPT) ?: false
            askedQuestionId = arguments?.getString(PARAM_KEY_PARENT_ID) ?: "0"
            ocr = arguments?.getString(PARAM_KEY_OCR_TEXT)
            isFilter = arguments?.getBoolean(PARAM_KEY_IS_FILTER) ?: false
            viewModel.askedQuestionId = askedQuestionId
            viewModel.supportedResourceType = arguments?.getString(PARAM_KEY_SOURCE_RESOURCE_TYPE)!!

            getSimilarVideo(questionId, mcId, playlistId, page, askedQuestionId, ocr, isFilter)
        } else if (requireArguments().getBoolean(WAS_MOVED_TO_LIVE_CLASS)) {
            viewModel.getCurrentSimilar()
        } else {
            viewModel.getPreviousVideo()
            page = arguments?.getString(PARAM_KEY_PAGE) ?: ""
            viewModel.page = page
        }
    }

    private fun getSimilarVideo(
        questionId: String, mcId: String?, playlistId: String?, page: String?,
        parentId: String?, ocr: String?, isFilter: Boolean
    ) {
        viewModel.getSimilarResults(questionId, mcId, playlistId, page, parentId, ocr, isFilter)
    }

    private fun setUpFilterView(tab: List<SubjectTabViewItem>?) {
        val tabInitialValue = tab?.map {
            SubjectTabViewItem(it.subject, it.display, it.subject.equals("All", false))
        }
        tabInitialValue?.let {
            recyclerViewFilter.show()
            filterAdapter = FilterAdapter(it, this, rvSimilar)
            recyclerViewFilter?.adapter = filterAdapter
            recyclerViewFilter?.addItemDecoration(
                SpaceItemDecoration(
                    ViewUtils.dpToPx(
                        8f,
                        requireContext()
                    ).toInt()
                )
            )
        } ?: recyclerViewFilter.hide()
    }

    private fun updateUi(similarVideoItem: List<RecyclerViewItem>) {
        adapter = SimilarAnsweredAdapter(this, commonEventManager, TAG, deeplinkAction)
        rvSimilar.layoutManager = LinearLayoutManager(requireActivity())
        rvSimilar.adapter = adapter
        rvSimilar.addItemDecoration(
            GridDividerItemDecoration(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.grey_light
                )
            )
        )

        adapter?.updateData(similarVideoItem)

        if (page == "WATCH-HISTORY" && (adapter?.itemCount ?: 0) > 0) {
            txtListTitle.visibility = View.VISIBLE
        }

        if (similarVideoItem.isNotEmpty() && autoPlay) {
            val similarVideoFilterPredicate = { item: RecyclerViewItem ->
                item is SimilarVideoList && item.resourceType != SOLUTION_RESOURCE_TYPE_TEXT
                        && if ((item.resourceType == SOLUTION_RESOURCE_TYPE_ETOOS_VIDEO)) {
                    item.isVip
                } else {
                    true
                }
            }

            similarVideoItem.firstOrNull(similarVideoFilterPredicate)?.let { item ->
                if (item is SimilarVideoList) {
                    firstSimilarVideoQuestionId = item.questionIdSimilar
                    mFirstSimilarVideoData = item
                    if (item.resourceType == SOLUTION_RESOURCE_TYPE_ETOOS_VIDEO) {
                        firstItemIsEtoos = true
                        firstEtoosVideoId = item.questionIdSimilar
                    }
                }
            }
            videoPageViewModel.firstSimilarPipPlayableVideo.value = similarVideoItem.firstOrNull {
                it is SimilarVideoList && similarVideoFilterPredicate(it) && it.isPlayableInPip
            } as? SimilarVideoList
        }

        rvSimilar?.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val lastVisibleItem =
                        (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                    val lastCompletelyVisibleItem =
                        (recyclerView.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()
                    topicBoosterPosition?.let {
                        if (it != -1 && it < lastVisibleItem) {
                            if (done.get()) return
                            if (done.compareAndSet(false, true)) {
                                sendEvent(EventConstants.TOPIC_BOOSTER_VISIBLE)
                                viewModel.sendApxorEvent(
                                    EventConstants.TOPIC_BOOSTER_VISIBLE,
                                    hashMapOf()
                                )
                            }
                        }
                    }

                    viewModel.sendApxorEvent(
                        EventConstants.SIMILAR_LIST_SCROLLED,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.LAST_VISIBLE_ITEM, lastCompletelyVisibleItem)
                        }, ignoreSnowplow = true
                    )
                }
            }
        })
    }

    private fun addRecommendedClasses(apiRecommendedClasses: SimilarWidgetViewItem) {
        adapter?.addWidgetItemAtPosition0(apiRecommendedClasses)
    }

    private fun setupObserver() {

        viewModel.similarVideoLiveData.observeK(
            viewLifecycleOwner,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.showProgressLiveData.observe(viewLifecycleOwner, Observer(this::updateProgress))

        viewModel.navigateToSameScreenLiveData.observe(viewLifecycleOwner) { screenNavigationArgument ->
            val param = screenNavigationArgument.getContentIfNotHandled()
            if (param != null) {
                listener?.onFragmentInteraction(
                    param.second.getValue(Constants.QUESTION_ID) as String,
                    param.second.getValue(Constants.IS_FROM_TOPIC_BOOSTER_SOLUTION) as Boolean
                )
            }
        }

        viewModel.navigateToNewScreenLiveData.observe(viewLifecycleOwner) { screenNavigation ->
            val param = screenNavigation.getContentIfNotHandled()
            if (param != null) {
                screenNavigator.startActivityForResultFromFragment(
                    this,
                    param.first,
                    param.second.toBundle(),
                    0
                )
            }
        }

        viewModel.navigateScreenLiveData.observe(viewLifecycleOwner, SingleEventObserver {
            activity?.let { context ->
                screenNavigator.startActivityFromActivity(context, it.first, it.second?.toBundle())
            }
        })

        viewModel.navigateLiveData.observe(viewLifecycleOwner) {
            val navigationData = it.getContentIfNotHandled()
            if (navigationData != null) {
                val args: Bundle? = navigationData.hashMap?.toBundle()
                context?.let { activityContext ->
                    screenNavigator.startActivityFromActivity(
                        activityContext,
                        navigationData.screen,
                        args
                    )
                }
            }
        }

        viewModel.feedbackViewReplacement.observe(
            viewLifecycleOwner,
            Observer(this::replaceFeedbackView)
        )

        viewModel.postQuestionResponse.observeK(
            this,
            this::postQuestionSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        viewModel.eventLiveData.observe(viewLifecycleOwner) {
            sendEvent(it)
        }

        viewModel.addToPlayListLiveData.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                addToPlayList(it)
            }
        }

        videoPageViewModel.onAddToWatchLater.observe(viewLifecycleOwner, SingleEventObserver {
            onWatchLaterSubmit(it)
        })

        viewModel.topicBoosterPosition.observe(viewLifecycleOwner) {
            topicBoosterPosition = it
        }

        vipObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is VipStateEvent) {
                if (event.state) {
                    adapter?.clearList()
                    getSimilarVideo(
                        questionId,
                        mcId,
                        playlistId,
                        page,
                        askedQuestionId,
                        ocr,
                        isFilter
                    )
                }
            }
        }


        /*viewModel.closeConvivaSession.observe(viewLifecycleOwner) {
            if (it) {
                ThreadUtils.runOnAnalyticsThread {
                    convivaVideoAnalytics.get().reportPlaybackEnded()
                }
            }
        }*/

        videoPageViewModel.recommendedClassesData.observe(viewLifecycleOwner) {
            if (page != Constants.PAGE_SRP) return@observe
            addRecommendedClasses(it)
        }
    }

    private fun onWatchLaterSubmit(id: String) {
        activity?.showSnackbar(
            R.string.video_saved_to_watch_later, R.string.change,
            Snackbar.LENGTH_LONG, id
        ) { idToPost ->
            videoPageViewModel.removeFromPlaylist(idToPost, "1")
            AddToPlaylistFragment.newInstance(idToPost)
                .show(childFragmentManager, AddToPlaylistFragment.TAG)
        }
    }

    private fun addToPlayList(videoId: String) {
        AddPlaylistFragment.newInstance(videoId).show(childFragmentManager, "AddPlaylist")
    }

    private fun onSuccess(data: Pair<List<RecyclerViewItem>, List<SubjectTabViewItem>?>) {
        setUpFilterView(data.second)
        updateUi(getSimilarVideosListToShow(data.first))

        // Observe this data in SimilarVideoFragment and add at position o in case of SRP page
        if (page == Constants.PAGE_SRP) {
            videoPageViewModel.getRecommendedClasses(questionId)
        }
    }

    private fun getSimilarVideosListToShow(originalList: List<RecyclerViewItem>): List<RecyclerViewItem> {
        mSimilarVideosOriginalList = originalList
        return originalList
    }

    private fun replaceFeedbackView(similarVideoViewItem: RecyclerViewItem) {
        (matchResultList?.adapter as? SimilarAnsweredAdapter)?.replaceFeedbackView(
            similarVideoViewItem
        )
    }

    private fun postQuestionSuccess(unit: Unit) {}

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(
            qid: String,
            isFromTopicBooster: Boolean,
            nextSimilarVideoData: SimilarVideoList? = null
        )

        fun onNextCourseVideoPlay() {}
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(parentFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgress(state: Boolean) {
        pbSimilarQuestions.setVisibleState(state)
    }

    private fun updateProgressBarState(state: Boolean) {
        pbSimilarQuestions.setVisibleState(state)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(requireActivity()).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    fun onVideoComplete() {
        if (firstSimilarVideoQuestionId.isNotBlank()) {
            listener?.onFragmentInteraction(
                firstSimilarVideoQuestionId,
                isFromTopicBooster,
                mFirstSimilarVideoData
            )
        }
    }

    /**
     * @return true if next PiP playable video is available else false
     */
    fun onNextVideoRequestedInPip(): Boolean {
        val nextPipPlayableVideo =
            videoPageViewModel.firstSimilarPipPlayableVideo.value ?: return false
        listener?.onFragmentInteraction(nextPipPlayableVideo.questionIdSimilar, false)
        return true
    }

    fun sendEvent(eventName: String) {
        activity?.apply {
            (activity?.applicationContext as DoubtnutApp).getEventTracker().addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(
                    defaultPrefs(requireActivity())
                        .getString(Constants.STUDENT_ID, "").orDefaultValue()
                )
                .addScreenName(EventConstants.SCREEN_SIMILAR_VIDEO_FRAGMENT)
                .track()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vipObserver?.dispose()
    }

}
