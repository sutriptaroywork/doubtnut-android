package com.doubtnutapp.shorts

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.os.postDelayed
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.data.BottomNavIconsNotificationDataStore
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.setStatusBarColor
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.AutoPlayVideoCompleted
import com.doubtnutapp.base.AutoPlayVideoStarted
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.bottomnavigation.model.BottomNavigationTabsData
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.databinding.FragmentShortsBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.libraryhome.library.ui.adapter.LibraryTabAdapter
import com.doubtnutapp.matchquestion.ui.activity.NoInternetRetryActivity
import com.doubtnutapp.rvexoplayer.RecyclerViewExoPlayerHelper
import com.doubtnutapp.rvexoplayer.RvPlayStrategy
import com.doubtnutapp.shorts.model.ShortsListData
import com.doubtnutapp.shorts.viewmodel.ShortsViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.mediahelper.ExoPlayerCacheManager
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.ShortsVideoDefaultWidget
import com.doubtnutapp.widgetmanager.widgets.ShortsVideoProgressWidget
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.gson.Gson
import com.uxcam.UXCam
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShortsFragment : BaseBindingFragment<ShortsViewModel, FragmentShortsBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "ShortsFragment"
        const val QID = "qid"
        const val TYPE = "type"
        const val REQUEST_CODE_NO_INTERNET = 199
        const val NAV_SOURCE = "nav_source"
        fun newInstance(qid: String?, type: String, navSource: String?) = ShortsFragment()
            .apply {
                arguments = Bundle().apply {
                    putString(QID, qid)
                    putString(TYPE, type)
                    putString(NAV_SOURCE, navSource)
                }
            }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var lastId: String? = null
    private var lastScrollPosition: Int? = null
    private var lastScrollPositionDownloaded: Int = -1
    private var videoShownCount: Int = 0

    private var rVExoPlayerHelper: RecyclerViewExoPlayerHelper? = null

    private var enterTime: Long? = null

    private val autoPlay: Boolean by lazy {
        true
    }

    private val autoPlayDuration: Long? by lazy {
        10000000000
    }
    private val autoPlayInitiation: Long? by lazy {
        500L
    }

    private var isViewEventSent = false

    private var qid: String? = null

    private var isProgressHandled = false

    private var videoPlayedCount = 0

    private val type: String by lazy {
        arguments?.getString(TYPE) ?: "DEFAULT"
    }

    private lateinit var adapter: WidgetLayoutAdapter

    private val layoutManager by lazy {
        LinearLayoutManager(requireContext())
    }

    private lateinit var exoPlayerCacheManager: ExoPlayerCacheManager

    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    @Inject
    lateinit var defaultDataStore: DefaultDataStore

    @Inject
    lateinit var bottomNavIconsNotificationsDataStore: BottomNavIconsNotificationDataStore

    private var shortsCategoryBottomSheet: ShortsCategoryBottomSheet? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentShortsBinding =
        FragmentShortsBinding.inflate(layoutInflater)

    override fun provideViewModel(): ShortsViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        enterTime = System.currentTimeMillis() / 1000
        exoPlayerCacheManager = ExoPlayerCacheManager.getInstance(requireContext()).also {
            it.clearAllCache()
        }
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        activity?.setStatusBarColor(R.color.black)
        binding.bottomNavigationView.menu.setGroupCheckable(0, false, true)
        UXCam.tagScreenName(TAG)
        qid = arguments?.getString(QID)
        adapter = WidgetLayoutAdapter(requireContext(), this)
        binding.rvWidgets.layoutManager = layoutManager
        binding.rvWidgets.adapter = adapter
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.rvWidgets)
        viewModel.extraParams.apply {
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME, TAG)
            put(NAV_SOURCE, arguments?.getString(NAV_SOURCE) ?: "DEFAULT")
        }
        initiateRecyclerListenerAndFetchInitialData()
        setUpAutoPlay()

        binding.ivCamera.setColorFilter(
            ContextCompat.getColor(
                requireContext(),
                R.color.color_c0c0c0
            ), PorterDuff.Mode.SRC_IN
        )
        binding.askQuestionButton.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SHORTS_BOTTOM_NAV_CAMERA_CLICKED))
            startActivity(CameraActivity.getStartIntent(
                requireContext(),
                "dn_shorts",
                isUserOpened = true
            ).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }
        handleBottomNavItemSelectedListener()
    }

    private fun handleBottomNavItemSelectedListener() {
        bottomNavigationView?.setOnItemSelectedListener { item ->
            var isBottomNavIconsDataAvailable = false
            lifecycleScope.launch {
                val responseJson =
                    defaultDataStore.bottomNavigationIconsData.firstOrNull().orEmpty()
                isBottomNavIconsDataAvailable =
                    Utils.isBottomNavigationIconsApiDataAvailable(responseJson)
                if (isBottomNavIconsDataAvailable) {

                    val responseBottomNavData =
                        Gson().fromJson(responseJson, BottomNavigationTabsData::class.java)

                    when (item.itemId) {
                        R.id.homeFragment -> {
                            launchDeeplinkAndSendEvent(responseBottomNavData.tab1, "1")
                            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SHORTS_BOTTOM_NAV_HOME_CLICKED))
                        }
                        R.id.libraryFragment -> {

                            launchDeeplinkAndSendEvent(responseBottomNavData.tab2, "2")
                            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SHORTS_BOTTOM_NAV_LIBRARY_CLICKED))
                        }
                        R.id.forumFragment -> {

                            launchDeeplinkAndSendEvent(responseBottomNavData.tab3, "3")
                            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SHORTS_BOTTOM_NAV_FEED_CLICKED))
                        }
                        R.id.userProfileFragment -> {
                            launchDeeplinkAndSendEvent(responseBottomNavData.tab4, "4")
                            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SHORTS_BOTTOM_NAV_PROFILE_CLICKED))

                        }
                        else -> {
                            false
                        }
                    }

                } else {
                    when (item.itemId) {
                        R.id.homeFragment -> {
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            intent.action = Constants.NAVIGATE_HOME
                            requireContext().startActivity(intent)
                            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SHORTS_BOTTOM_NAV_HOME_CLICKED))
                        }
                        R.id.libraryFragment -> {
                            val intent = Intent(requireContext(), MainActivity::class.java).also {
                                it.action = Constants.NAVIGATE_LIBRARY
                                it.putExtra(Constants.TAG, LibraryTabAdapter.TAG_MY_COURSES)
                            }
                            requireContext().startActivity(intent)
                            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SHORTS_BOTTOM_NAV_LIBRARY_CLICKED))
                        }
                        R.id.forumFragment -> {
                            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                                action = Constants.NAVIGATE_FEED
                            }
                            requireContext().startActivity(intent)
                            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SHORTS_BOTTOM_NAV_FEED_CLICKED))

                        }
                        R.id.userProfileFragment -> {
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            intent.action = Constants.NAVIGATE_PROFILE
                            requireContext().startActivity(intent)
                            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SHORTS_BOTTOM_NAV_PROFILE_CLICKED))
                        }
                    }
                }
            }
            true
        }
    }

    private fun launchDeeplinkAndSendEvent(
        responseTabData: BottomNavigationTabsData.TabData?,
        position: String
    ) {
        deeplinkAction.performAction(
            requireContext(),
            responseTabData?.deeplink
        )
        Utils.publishBottomNavTabClickEvent(
            analyticsPublisher,
            responseTabData?.name.orEmpty(),
            position,
        )
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {
        adapter.clearData()
        binding.rvWidgets.clearOnScrollListeners()
        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvWidgets.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    context?.run {
                        fetchList()
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = (recyclerView.layoutManager) as LinearLayoutManager
                    val firstItem = layoutManager.findFirstVisibleItemPosition()
                    if (firstItem < 0 || lastScrollPosition == firstItem) {
                        return
                    }
                    lastScrollPosition = firstItem
                    val itemAtPosition = adapter.widgets.getOrNull(firstItem)
                    if (itemAtPosition != null && itemAtPosition is ShortsVideoDefaultWidget.Model) {
                        videoShownCount++
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                itemAtPosition.type + EventConstants.UNDERSCORE + "added_to_screen",
                                itemAtPosition.extraParams ?: hashMapOf()
                            )
                        )
                        val lastScrollPositionToPreDownload = (lastScrollPosition ?: 0) + 1
                        if (lastScrollPositionDownloaded < lastScrollPositionToPreDownload) {
                            val itemAtPositionToPreDownload =
                                adapter.widgets.getOrNull(lastScrollPositionToPreDownload)
                            if (itemAtPositionToPreDownload != null
                                && itemAtPositionToPreDownload is ShortsVideoDefaultWidget.Model
                            ) {
                                lastScrollPositionDownloaded = lastScrollPositionToPreDownload
                                val url =
                                    itemAtPositionToPreDownload._data?.videoResource?.resource.orEmpty() to MEDIA_TYPE_BLOB
                                preLoadVideos(listOf(url))
                            }
                        }
                    }
                }

            }.also {
                it.setStartPage(1)
            }
        binding.rvWidgets.addOnScrollListener(infiniteScrollListener)
        adapter.addWidget(ShortsVideoProgressWidget.Model()
            .apply {
                _type = WidgetTypes.TYPE_WIDGET_SHORTS_VIDEO_PROGRESS
                _data = ShortsVideoProgressWidget.Data()
            })
        fetchList()
    }

    override fun onResume() {
        super.onResume()
        handlePageViewEvent()
    }

    @Synchronized
    private fun handlePageViewEvent() {
        if (!isViewEventSent) {
            isViewEventSent = true
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.SHORTS_FRAGMENT_PAGE_VIEW,
                    hashMapOf<String, Any>().apply {
                        putAll(viewModel.extraParams)
                    })
            )
        }
    }

    private fun fetchList() {
        viewModel.fetchShortsList(lastId, qid, type)
        qid = null
    }

    override fun setupObservers() {
        viewModel.widgetLiveData.observeK(
            viewLifecycleOwner,
            this::onWidgetListFetched,
            this::onApiErrorDefault,
            this::unAuthorizeUserErrorDefault,
            this::ioExceptionHandlerDefault,
            this::updateProgress
        )
    }

    private fun onWidgetListFetched(data: ShortsListData) {
        if (data.categoryPage == true) {
            showCategoryBottomSheet()
            removeProgressWidget()
        }
        lastId = data.lastId
        if (data.widgets.isNullOrEmpty() || data.lastId == null) {
            infiniteScrollListener.isLastPageReached = true
        }
        adapter.addWidgetsToBottom(data.widgets)
        removeProgressWidget()
    }

    @Synchronized
    private fun removeProgressWidget() {
        if (isProgressHandled) return
        if (adapter.widgets.getOrNull(0)?._type == WidgetTypes.TYPE_WIDGET_SHORTS_VIDEO_PROGRESS) {
            isProgressHandled = true
            binding.rvWidgets.smoothScrollToPosition(1)
            lifecycleScope.launchWhenStarted {
                delay(500)
                adapter.removeWidget(WidgetTypes.TYPE_WIDGET_SHORTS_VIDEO_PROGRESS)
            }
        }
    }

    private fun updateProgress(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        binding.progressBar.setVisibleInvisibleState(state)
    }

    private fun onApiErrorDefault(e: Throwable) {
        apiErrorToast(e)
    }

    private fun unAuthorizeUserErrorDefault() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun ioExceptionHandlerDefault() {
        val currentContext = context ?: return
        if (NetworkUtils.isConnected(currentContext)) {
            toast(getString(R.string.somethingWentWrong))
        } else if (adapter.widgets.isNullOrEmpty()) {
            startActivityForResult(
                NoInternetRetryActivity.getStartIntent(
                    requireContext(),
                    Constants.DN_SHORTS_SCREEN
                ),
                REQUEST_CODE_NO_INTERNET
            )
        }
    }

    private fun setUpAutoPlay() {
        /*
          if AutoPlay, create exoplayer helper instance and attach it
          to recyclerview after setting layout manager and adapter.
         */
        mBinding?.apply {
            if (autoPlay) {
                rVExoPlayerHelper = RecyclerViewExoPlayerHelper(
                    mContext = requireContext(),
                    id = R.id.rvPlayer,
                    autoPlay = true,
                    autoPlayInitiation = 100L,
                    playStrategy = RvPlayStrategy.DEFAULT,
                    defaultMute = false,
                    loop = 0,
                    progressRequired = true,
                    defaultMinBufferMs = 5000,
                    defaultMaxBufferMs = 5000,
                    reBufferDuration = 0 // For autoplay no need to buffer extra bytes
                ).apply {
                    getPlayerView()?.apply {
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    }
                    makeLifeCycleAware(this@ShortsFragment)
                }
            }
//            rVExoPlayerHelper?.rvPlayerHelper?.enableLoopCurrentVideo()

            rVExoPlayerHelper?.attachToRecyclerView(binding.rvWidgets)
            rVExoPlayerHelper?.playCurrent(binding.rvWidgets)
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is AutoPlayVideoCompleted -> {
                if (NetworkUtils.isConnected(requireContext())) {
                    if (action.adapterPosition != RecyclerView.NO_POSITION && ((binding.rvWidgets.adapter?.itemCount
                            ?: 0) - 1 > action.adapterPosition)
                    ) {
                        if (layoutManager.findFirstCompletelyVisibleItemPosition() == action.adapterPosition) {
                            Handler(Looper.getMainLooper()).postDelayed(action.delayToMoveToNext) {
                                binding.rvWidgets.smoothScrollToPosition(action.adapterPosition + 1)
                            }
                        }
                    }
                }
            }
            is AutoPlayVideoStarted -> {
                videoPlayedCount++
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                if (requestCode == REQUEST_CODE_NO_INTERNET) {
                    fetchList()
                }
            }
            else -> {
            }
        }
    }

    private fun preLoadVideos(urlList: List<Pair<String, String>>?) {
        val cacheSize = ExoPlayerCacheManager.DEFAULT_CACHE_SIZE
        exoPlayerCacheManager.stopCaching()
        urlList?.forEach {
            if (it.first.isNotBlank()) {
                exoPlayerCacheManager.cacheVideo(
                    it.first,
                    cacheSize, ExoPlayerHelper.getMediaSourceType(it.second)
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        ExoPlayerCacheManager.getInstance(requireContext()).clearAllCache()
    }

    override fun onDestroy() {
        super.onDestroy()
        val exitTime = System.currentTimeMillis() / 1000
        val durationActual = exitTime - (enterTime ?: exitTime)
        val duration = Utils.getTimeSpentForEventFromActualDuration(durationActual)
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.SHORTS_EXIT,
                hashMapOf<String, Any>(
                    EventConstants.DURATION to duration,
                    EventConstants.DURATION_ACTUAL to durationActual.toString(),
                    EventConstants.VIDEO_PLAYED_COUNT to videoPlayedCount.toString(),
                    EventConstants.VIDEO_VIEW_ON_SCREEN_COUNT to videoShownCount.toString()
                ).apply {
                    if (videoShownCount != 0) {
                        put(
                            EventConstants.VIDEO_PERCENT_PLAYED,
                            ((videoPlayedCount.toDouble() / videoShownCount) * 100).toInt()
                                .toString()
                        )
                    }
                }
            ))
    }

    private fun showCategoryBottomSheet() {
        ShortsCategoryBottomSheet.newInstance().apply {
            shortsCategoryBottomSheet = this
            shortsCategoryBottomSheetListener(object : InflatedListener {
                override fun onInflated(inflated: Boolean) {
                    if (!inflated)
                        activity?.let {
                            it.recreate()
                        }
                }
            })
        }.show(childFragmentManager, ShortsCategoryBottomSheet.TAG)
    }
}