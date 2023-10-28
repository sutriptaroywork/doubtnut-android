package com.doubtnutapp.libraryhome.course.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.core.content.edit
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.StickyHeadersLinearLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.*
import com.doubtnutapp.Constants.IS_COURSE_SELECTION_SHOWN
import com.doubtnutapp.Constants.MY_COURSE
import com.doubtnutapp.Constants.SOURCE
import com.doubtnutapp.EventBus.ScrollToTopEvent
import com.doubtnutapp.EventBus.UpdateExploreEvent
import com.doubtnutapp.EventBus.VipStateEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.course.widgets.PackageDetailWidgetItem
import com.doubtnutapp.course.widgets.PopularCourseWidgetModel
import com.doubtnutapp.course.widgets.RecommendationWidget
import com.doubtnutapp.data.remote.models.ActivateTrialData
import com.doubtnutapp.data.remote.models.ApiCourseData
import com.doubtnutapp.data.remote.models.ButtonInfo
import com.doubtnutapp.data.remote.models.CourseFilterTypeData
import com.doubtnutapp.dnr.model.DnrReward
import com.doubtnutapp.dnr.ui.fragment.DnrRewardBottomSheetFragment
import com.doubtnutapp.dnr.ui.fragment.DnrRewardDialogFragment
import com.doubtnutapp.dnr.viewmodel.DnrRewardViewModel
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.libraryhome.course.viewmodel.CoursesViewModel
import com.doubtnutapp.libraryhome.coursev3.ui.*
import com.doubtnutapp.libraryhome.library.LibraryFragmentHome
import com.doubtnutapp.libraryhome.library.ui.adapter.LibraryTabAdapter
import com.doubtnutapp.liveclass.ui.CourseCategoryActivity
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.sales.*
import com.doubtnutapp.sales.event.PrePurchaseCallingCardDismiss
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.BannerActionUtils.deeplinkAction
import com.doubtnutapp.utils.DateUtils.isToday
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgets.RowTextView
import com.uxcam.UXCam
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_emi.*
import kotlinx.android.synthetic.main.fragment_library_courses_v2.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 16/04/20.
 *
 * Called from MainActivity in Online Class TAB
 * or
 * Called from CourseCategoryActivity e.g. Kota Classes, IIT JEE, NEET etc.
 */
class ExploreFragment : DaggerFragment(), ActionPerformer {

    companion object {
        const val TAG = "ExploreFragment"
        private const val CATEGORY_ID = "categoryId"
        private const val FILTERS = "filters"
        private const val REQUEST_CODE_COURSE_SELECTION = 134
        private var isCallingCardAdded = false

        fun newInstance(
            categoryId: String? = null,
            filters: String? = null,
            source: String? = null
        ): ExploreFragment {
            val fragment = ExploreFragment()
            val args = Bundle()
            args.putString(CATEGORY_ID, categoryId)
            args.putString(FILTERS, filters)
            args.putString(SOURCE, source)
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: CoursesViewModel
    private lateinit var dnrRewardViewModel: DnrRewardViewModel

    private lateinit var adapter: WidgetLayoutAdapter

    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    private lateinit var recyclerViewListing: RecyclerView

    private var isViewEventSent = false

    private var vipObserver: Disposable? = null

    private var categoryId: String? = null

    private var source: String = ""

    private var refreshUI: Boolean = false

    private var filtersList: List<String> = ArrayList()

    private var isCourseSelectionShown: Boolean = false

    private lateinit var fragment: CourseSelectionFragment

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var appStateObserver: Disposable? = null

    var isCourse = false

    var isRecommendationCourseShowOnce = false

    private var widgets = mutableListOf<WidgetEntityModel<*, *>>()
    private var viewTrackingBus: ViewTrackingBus? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library_courses_v2, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUi()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun onCourseSelected(bundle: Bundle?) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.COURSE_BOTTOM_SHEET_CLICK,
                hashMapOf(EventConstants.SOURCE to EventConstants.BOTTOM_ICON),
                ignoreSnowplow = true
            )
        )
        childFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        categoryId = bundle?.getString(CourseSelectionFragment.CATEGORY_ID).orEmpty()
        val assortmentId = bundle?.getString(CourseSelectionFragment.ASSORTMENT_ID).orEmpty()
        if (categoryId.isNullOrEmpty()) {
            defaultPrefs().edit().putString(Constants.SELECTED_ASSORTMENT_ID, assortmentId)
                .apply()
            defaultPrefs().edit().putString(Constants.SELECTED_CATEGORY_ID, "").apply()
            val parentFragment = parentFragment as LibraryFragmentHome?
            parentFragment?.adapter?.updateTabAtPosition(
                resources.getString(R.string.my_course),
                LibraryTabAdapter.TAG_MY_COURSES,
                CourseFragment.newInstance(
                    assortmentId = assortmentId,
                    source = MY_COURSE
                ),
                0
            )
        } else {
            defaultPrefs().edit().putString(Constants.SELECTED_CATEGORY_ID, categoryId).apply()
            defaultPrefs().edit().putString(Constants.SELECTED_ASSORTMENT_ID, assortmentId).apply()
            val parentFragment = parentFragment as LibraryFragmentHome?
            parentFragment?.adapter?.updateTabAtPosition(
                resources.getString(R.string.my_course),
                LibraryTabAdapter.TAG_MY_COURSES,
                newInstance(
                    categoryId = categoryId,
                    source = MY_COURSE
                ),
                0
            )
        }
    }

    private fun initUi() {
        categoryId = arguments?.getString(CATEGORY_ID)
        source = arguments?.getString(SOURCE).orEmpty()
        viewModel = viewModelProvider(viewModelFactory)
        dnrRewardViewModel = viewModelProvider(viewModelFactory)
        viewModel.extraParams.apply {
            put(
                EventConstants.EVENT_SCREEN_PREFIX + EventConstants.CATEGORY_ID,
                categoryId.orEmpty()
            )
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME, TAG)
            if (activity is MainActivity) {
                put(
                    EventConstants.EVENT_PARENT_SCREEN_PREFIX + EventConstants.NAME,
                    MainActivity.TAG
                )
            } else {
                put(
                    EventConstants.EVENT_PARENT_SCREEN_PREFIX + EventConstants.NAME,
                    CourseCategoryActivity.TAG
                )
            }
        }
        setUpRecyclerView()
        setUpObserver()
        setupViewTracking()
        if (categoryId.isNullOrBlank()) {
            chatBuddyAnimation.show()
            chatBuddyAnimation.setAnimation("lottie_chat_buddy_home.zip")
            chatBuddyAnimation.repeatCount = 3
            chatBuddyAnimation.playAnimation()
            chatBuddyAnimation.setOnClickListener {
                openCourseRecommendationScreen(requireContext(), false)
            }
            handleBackPressForCrPage()
            if (!defaultPrefs().getBoolean(Constants.CR_TOOLTIP_SHOWN_ONCE, false)) {
                defaultPrefs().edit { putBoolean(Constants.CR_TOOLTIP_SHOWN_ONCE, true) }
                groupTooltip.show()
            } else {
                groupTooltip.hide()
            }
        } else {
            chatBuddyAnimation.hide()
        }
        layoutTooltip.setOnClickListener {
            groupTooltip.hide()
        }

        // DNR region start
        checkDnrRewardBottomSheet()
        // DNR region end
    }

    private fun checkDnrRewardBottomSheet() {
        dnrRewardViewModel.markCoursePurchased()
    }

    private fun setupViewTracking() {
        viewTrackingBus = ViewTrackingBus(
            { viewModel.trackView(it) },
            {}
        )

        adapter.registerViewTracking(viewTrackingBus!!)
    }

    private fun openCourseRecommendationScreen(context: Context, isBack: Boolean) {
        groupTooltip.hide()
        CourseRecommendationActivity.getStartIntent(
            context,
            isBack
        ).apply {
            startActivity(this)
        }
    }

    private fun getLastVisitCRPageDate() =
        defaultPrefs().getLong(Constants.CR_LAST_VISITED_TIME, 0).run {
            Date(this)
        }

    private fun handleBackPressForCrPage() {
        if (!getLastVisitCRPageDate().isToday()) {
            activity?.let {
                view?.isFocusableInTouchMode = true
                view?.requestFocus()
                view?.setOnKeyListener(object : View.OnKeyListener {
                    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                        if (keyCode == KeyEvent.KEYCODE_BACK
                            && event.action == KeyEvent.ACTION_UP
                            && !isRecommendationCourseShowOnce
                            && !getLastVisitCRPageDate().isToday()
                        ) {
                            isRecommendationCourseShowOnce = true
                            activity?.let { currentActivity ->
                                openCourseRecommendationScreen(currentActivity, true)
                            }
                            return true
                        }
                        return false
                    }
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (categoryId.equals("free_classes")) {
            UXCam.tagScreenName("ExploreFreeClasses")
        } else {
            UXCam.tagScreenName("Explore")
        }
        handlePageViewEvent()
        if (refreshUI) {
            refreshUI = false
            initiateRecyclerListenerAndFetchInitialData()
        }
    }

    @Synchronized
    private fun handlePageViewEvent() {
        if (!isViewEventSent) {
            isViewEventSent = true
            viewModel.publishEvent(
                AnalyticsEvent(EventConstants.EXPLORE_PAGE_VIEW,
                    hashMapOf<String, Any>().apply {
                        putAll(viewModel.extraParams)
                    }), false
            )
        }
    }

    private fun setUpRecyclerView() {
        val filterString = arguments?.getString(FILTERS)
        if (!filterString.isNullOrBlank()) {
            val list = filterString.split(",")
            if (!list.isNullOrEmpty()) {
                filtersList = list
            }
        }
        recyclerViewListing = rvWidgets
        adapter =
            WidgetLayoutAdapter(
                context = requireActivity(),
                actionPerformer = this,
                source = arguments?.getString(SOURCE)?.ifEmptyThenNull() ?: TAG
            )
        recyclerViewListing.layoutManager =
            StickyHeadersLinearLayoutManager<WidgetLayoutAdapter>(requireActivity())
        recyclerViewListing.adapter = adapter
        initiateRecyclerListenerAndFetchInitialData()
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {
        val startPage = 1
        recyclerViewListing.clearOnScrollListeners()

        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(recyclerViewListing.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    context?.run {
                        fetchList(currentPage, categoryId)
                    }
                }
            }.also {
                it.setStartPage(startPage)
            }

        recyclerViewListing.addOnScrollListener(infiniteScrollListener)
        recyclerViewListing.addOnScrollListener(object : PrePurchaseCallingCardRvScrollListener() {
            override fun scrollBackToTop() {
                super.scrollBackToTop()
                if (categoryId.equals("free_classes")) return

                val source = when (activity) {
                    is MainActivity -> Constants.SOURCE_ALL_COURSES
                    is CourseCategoryActivity -> Constants.SOURCE_COURSE_CATEGORY
                    else -> ""
                }

                val titleSubtitle = if (isCourse) {
                    Pair(
                        defaultPrefs().getString(Constants.TITLE_PROBLEM_PURCHASE, ""),
                        defaultPrefs().getString(Constants.SUBTITLE_PROBLEM_PURCHASE, "")
                    )
                } else {
                    Pair(
                        defaultPrefs().getString(Constants.TITLE_PROBLEM_SEARCH, ""),
                        defaultPrefs().getString(Constants.SUBTITLE_PROBLEM_SEARCH, "")
                    )
                }
                if (titleSubtitle.first.isNullOrEmpty()) {
                    return
                }
                if (source == Constants.SOURCE_ALL_COURSES) {
                    if (PrePurchaseCallingCard2.isShownOnCheckAllCourses) {
                        return
                    }
                    PrePurchaseCallingCard2.isShownOnCheckAllCourses = true
                } else {
                    val prePurchaseCallingCardShownTimestamp =
                        defaultPrefs().getLong(
                            PrePurchaseCallingCardConstant.PRE_PURCHASE_CALLING_CARD_COURSE_LAST_SHOWN_TIMESTAMP,
                            0
                        )
                    if (System.currentTimeMillis() - prePurchaseCallingCardShownTimestamp
                        > TimeUnit.DAYS.toMillis(1)
                    ) {
                        defaultPrefs().edit {
                            putString(
                                PrePurchaseCallingCardConstant.PRE_PURCHASE_CALLING_CARD_COURSE_ASSORTMENT_IDS,
                                ""
                            )
                        }
                    }

                    if (defaultPrefs().getString(
                            PrePurchaseCallingCardConstant.PRE_PURCHASE_CALLING_CARD_COURSE_ASSORTMENT_IDS,
                            ""
                        ).orEmpty().contains(categoryId.orEmpty())
                    ) {
                        return
                    }

                    defaultPrefs().edit {
                        putLong(
                            PrePurchaseCallingCardConstant.PRE_PURCHASE_CALLING_CARD_COURSE_LAST_SHOWN_TIMESTAMP,
                            System.currentTimeMillis()
                        )
                    }
                    defaultPrefs().edit {
                        putString(
                            PrePurchaseCallingCardConstant.PRE_PURCHASE_CALLING_CARD_COURSE_ASSORTMENT_IDS,
                            "${
                                defaultPrefs().getString(
                                    PrePurchaseCallingCardConstant.PRE_PURCHASE_CALLING_CARD_COURSE_ASSORTMENT_IDS,
                                    ""
                                )
                            }-$categoryId"
                        )
                    }
                }

                val position =
                    if (adapter.widgets.any { it.type == WidgetTypes.TYPE_CATEGORY_PAGE_FILTER || it.type == WidgetTypes.TYPE_CATEGORY_PAGE_FILTER_V2 }) {
                        1
                    } else {
                        0
                    }

                adapter.addWidgetToPosition(
                    widget = PrePurchaseCallingCardModel2().apply {
                        _widgetType = WidgetTypes.TYPE_PRE_PURCHASE_CALL_CARD_V2
                        isSticky = true
                        _data = PrePurchaseCallingCardData2(
                            title = titleSubtitle.first,
                            titleTextSize = PrePurchaseCallingCard2.titleTextSize(),
                            titleTextColor = PrePurchaseCallingCard2.titleTextColor(),
                            subtitle = titleSubtitle.second,
                            subtitleTextSize = PrePurchaseCallingCard2.subtitleTextSize(),
                            subtitleTextColor = PrePurchaseCallingCard2.subtitleTextColor(),
                            actionText = PrePurchaseCallingCard2.action(),
                            actionTextSize = PrePurchaseCallingCard2.actionTextSize(),
                            actionTextColor = PrePurchaseCallingCard2.actionTextColor(),
                            actionImageUrl = PrePurchaseCallingCard2.actionImageUrl(),
                            actionDeepLink = defaultPrefs().getString(
                                Constants.CALLBACK_DEEPLINK,
                                ""
                            ),
                            imageUrl = PrePurchaseCallingCard2.imageUrl(),
                            source = source,
                        )
                    },
                    index = position
                )
                isCallingCardAdded = true
            }
        })

        fetchList(startPage, categoryId)
    }

    private fun fetchList(pageNumber: Int, categoryId: String?) {

        viewModel.fetchCourseData(pageNumber, categoryId, filtersList)

    }

    private fun setUpObserver() {
        appStateObserver = DoubtnutApp.INSTANCE
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is PrePurchaseCallingCardDismiss -> {
                        adapter.removeWidget(WidgetTypes.TYPE_PRE_PURCHASE_CALL_CARD)
                        adapter.removeWidget(WidgetTypes.TYPE_PRE_PURCHASE_CALL_CARD_V2)
                    }
                }
            }


        viewModel.exploreLiveData.observeK(
            viewLifecycleOwner,
            this::onWidgetListFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        vipObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is VipStateEvent) {
                if (event.state) {
                    viewModel.itemsAdded = 0
                    initiateRecyclerListenerAndFetchInitialData()
                }
            } else if (event is UpdateExploreEvent) {
                viewModel.itemsAdded = 0
                initiateRecyclerListenerAndFetchInitialData()
            } else if (event is ScrollToTopEvent) {
                rvWidgets.smoothScrollToPosition(0)
            }
        }

        viewModel.activateVipLiveData.observeK(
            this,
            this::onActivateTrialSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        dnrRewardViewModel.dnrRewardLiveData.observeEvent(viewLifecycleOwner) {
            showDnrReward(it)
        }
    }

    private fun showDnrReward(dnrReward: DnrReward) {
        dnrRewardViewModel.checkRewardPopupToBeShown(dnrReward)
        dnrRewardViewModel.dnrRewardPopupLiveData.observeEvent(this) { rewardPopupType ->
            when (rewardPopupType) {
                DnrRewardViewModel.RewardPopupType.NO_POPUP -> {
                    return@observeEvent
                }
                DnrRewardViewModel.RewardPopupType.REWARD_BOTTOM_SHEET -> {
                    DnrRewardBottomSheetFragment.newInstance(dnrReward)
                        .show(childFragmentManager, DnrRewardBottomSheetFragment.TAG)
                }
                DnrRewardViewModel.RewardPopupType.REWARD_DIALOG -> {
                    DnrRewardDialogFragment.newInstance(dnrReward)
                        .show(childFragmentManager, DnrRewardDialogFragment.TAG)
                }
            }
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        context?.let { currentContext ->
            if (NetworkUtils.isConnected(currentContext)) {
                toast(getString(R.string.somethingWentWrong))
            } else {
                toast(getString(R.string.string_noInternetConnection))
            }
        }
    }

    private fun updateProgress(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        progressBar.setVisibleState(state)
    }

    private fun onWidgetListFetched(data: ApiCourseData) {
        updateProgress(false)
        if (data.widgets.isEmpty()) {
            infiniteScrollListener.isLastPageReached = true
        }
        widgets.addAll(data.widgets)
        if (!data.popupDeeplink.isNullOrBlank()) {
            deeplinkAction.performAction(requireContext(), data.popupDeeplink.orEmpty())
        }
        data.widgets.forEach {
            if (it is PopularCourseWidgetModel) {
                it.data.addExtraSpacing = true
            }
        }

        val shouldShowCourseSelection =
            defaultPrefs().getBoolean(Constants.SHOULD_SHOW_COURSE_SELECTION, false)
        isCourseSelectionShown = defaultPrefs().getBoolean(IS_COURSE_SELECTION_SHOWN, false)
        if (infiniteScrollListener.currentPage == 1) {
            setDropDownData(data)
            if (shouldShowCourseSelection && source == MY_COURSE && !isCourseSelectionShown) {
                showCourseSelection()
            } else {
                courseSelection.hide()
            }
            adapter.setWidgets(data.widgets)
        } else {
            adapter.addWidgets(data.widgets)
        }
        handleBottomButtonView(data.buttonInfo)
    }

    private fun showCourseSelection() {
        defaultPrefs().edit().putBoolean(IS_COURSE_SELECTION_SHOWN, true).apply()
        progressBar.hide()
        courseSelection.show()
        overlay.show()
        overlay.setOnClickListener {
            courseSelection.hide()
            overlay.hide()
        }
        fragment = CourseSelectionFragment.newInstance(
            page = "live_class_bottom_icon",
            requestCode = TAG
        )
        childFragmentManager.beginTransaction().replace(R.id.courseSelection, fragment)
            .commitAllowingStateLoss()
        fragment.setFragmentResultListener(TAG) { requestKey, bundle ->
            onCourseSelected(bundle)
        }
    }

    private fun setDropDownData(courseData: ApiCourseData) {
        if (courseData.courseList != null && courseData.courseList.size > 1) {
            dropdownCard.visibility = View.VISIBLE
            cardTitle.text = courseData.title.orEmpty()
            dropdownCard.setOnClickListener {
                val menu = CourseSelectDropDownMenu(requireContext(), courseData.courseList)
                menu.height = WindowManager.LayoutParams.WRAP_CONTENT
                menu.width = Utils.convertDpToPixel(300f).toInt()
                menu.isOutsideTouchable = true
                menu.isFocusable = true
                menu.showAsDropDown(dropdownCard)
                menu.setCategorySelectedListener(object :
                    CourseFilterDropDownAdapter.FilterSelectedListener {
                    override fun onFilterSelected(position: Int, data: CourseFilterTypeData) {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.COURSE_DROPDOWN_SELECT,
                                hashMapOf<String, Any>(
                                    EventConstants.NAME to data.display
                                )
                            )
                        )
                        menu.dismiss()
                        defaultPrefs().edit()
                            .putString(Constants.SELECTED_ASSORTMENT_ID, data.id).apply()
                        if (data.categoryId.isNullOrEmpty()) {
                            defaultPrefs().edit().putString(Constants.SELECTED_CATEGORY_ID, "")
                                .apply()
                            when (activity) {
                                is MainActivity -> {
                                    val parentFragment = parentFragment as LibraryFragmentHome?
                                    parentFragment?.adapter?.updateTabAtPosition(
                                        resources.getString(
                                            R.string.my_course
                                        ),
                                        LibraryTabAdapter.TAG_MY_COURSES,
                                        CourseFragment.newInstance(
                                            assortmentId = data.id,
                                            source = MY_COURSE
                                        ), 0
                                    )
                                }
                                is CourseCategoryActivity -> {
                                    (activity as CourseCategoryActivity).replaceFragment(
                                        CourseFragment.newInstance(
                                            assortmentId = data.id,
                                            source = MY_COURSE
                                        ),
                                        ""
                                    )
                                }
                                is CourseActivityV3 -> {
                                    var pageSource = MY_COURSE
                                    if (source == Constants.PAGE_SEARCH_SRP) {
                                        pageSource = Constants.PAGE_SEARCH_SRP
                                    }
                                    (activity as CourseActivityV3).replaceFragment(
                                        CourseFragment.newInstance(
                                            assortmentId = data.id,
                                            source = pageSource
                                        ), ""
                                    )
                                }
                            }
                        } else {
                            categoryId = data.categoryId
                            cardTitle.text = data.display
                            defaultPrefs().edit()
                                .putString(Constants.SELECTED_CATEGORY_ID, categoryId).apply()
                            initUi()
                        }
                    }
                })
            }
        } else {
            dropdownCard.visibility = View.GONE
        }
    }

    private fun onActivateTrialSuccess(data: ActivateTrialData) {
        showToast(requireContext(), data.message.orEmpty())
        initiateRecyclerListenerAndFetchInitialData()
    }

    override fun onDestroy() {
        super.onDestroy()
        vipObserver?.dispose()
        analyticsPublisher.publishEvent(
            AnalyticsEvent(EventConstants.EXPLORE_BACK,
                hashMapOf<String, Any>().apply {
                    if (::viewModel.isInitialized) {
                        putAll(viewModel.extraParams)
                    }
                })
        )
    }

    override fun performAction(action: Any) {
        if (action is RefreshUI) {
            refreshUI = true
        } else if (action is FetchDetails) {
            filtersList = action.filtersList
            initiateRecyclerListenerAndFetchInitialData()
        } else if (action is ActivateVipTrial) {
            viewModel.activateTrial(action.assortmentId)
        } else if (action is OnNudgeClosed) {
            widgets.filterIsInstance<RecommendationWidget.RecommendationWidgetModel>().apply {
                this.forEach {
                    adapter.removeWidget(it)
                }
            }
        } else if (action is ScrollToPosition) {
            if (isCallingCardAdded) {
                when (action.itemPosition) {
                    2 -> {
                        (rvWidgets.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                            action.position,
                            300
                        )
                    }
                    3 -> {
                        (rvWidgets.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                            action.position,
                            400
                        )
                    }
                    else -> {
                        (rvWidgets.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                            action.position,
                            -600
                        )
                    }
                }
            } else {
                when (action.itemPosition) {
                    2 -> {
                        (rvWidgets.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                            action.position,
                            950
                        )
                    }
                    3 -> {
                        (rvWidgets.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                            action.position,
                            800
                        )
                    }
                    else -> {
                        (rvWidgets.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                            action.position,
                            100
                        )
                    }
                }
            }
        }
    }

    private fun handleBottomButtonView(buttonInfo: ButtonInfo?) {
        isCourse = buttonInfo != null
        if (buttonInfo == null) {
            bottomBarExplore.hide()
        } else {
            bottomBarExplore.show()
            if (buttonInfo.type != null && buttonInfo.type == "emi") {
                textViewEmiTitle.show()
                textViewKnowMore.show()
                textViewEmiTitle.text = buttonInfo.emi?.title.orEmpty()
                if (buttonInfo.payText.isNullOrBlank()
                    || buttonInfo.variantId.isNullOrBlank()
                ) {
                    textViewPay.hide()
                    viewPaymentDivider.hide()
                } else {
                    textViewPay.show()
                    viewPaymentDivider.show()
                }
                textViewKnowMore.setOnClickListener {
                    if (buttonInfo.emi != null) {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.COURSE_CLICK_KNOW_MORE,
                                hashMapOf(
                                    EventConstants.ASSORTMENT_ID to buttonInfo.assortmentId.orEmpty(),
                                    EventConstants.VARIANT_ID to buttonInfo.variantId.orEmpty()
                                )
                            )
                        )
                        showEmiDialog(buttonInfo.emi)
                    }
                }
                if (buttonInfo.multiplePackage == true) {
                    textViewPayInstallment.hide()
                    viewPaymentDivider.hide()
                    tvStartingAt.show()
                }
            } else {
                textViewEmiTitle.show()
                textViewEmiTitle.text = buttonInfo.title.orEmpty()
                textViewKnowMore.hide()
                textViewPayInstallment.hide()
                viewPaymentDivider.hide()
                textViewPay.show()
                if (buttonInfo.multiplePackage == true) {
                    tvStartingAt.show()
                }
            }

            textViewKnowMore.text = buttonInfo.knowMoreText.orEmpty()
            textViewPay.text = buttonInfo.payText.orEmpty()
            textViewPayInstallment.text = buttonInfo.payInstallmentText.orEmpty()
            textViewAmountToPay.text = buttonInfo.amountToPay.orEmpty()
            textViewAmountSaving.text = buttonInfo.amountSaving.orEmpty()
            textViewAmountStrikeThrough.text = buttonInfo.amountStrikeThrough.orEmpty()
            textViewAmountStrikeThrough.paintFlags =
                textViewAmountStrikeThrough.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val multiplePackage: Boolean = buttonInfo.multiplePackage ?: false
            textViewPay.setOnClickListener {
                deeplinkAction.performAction(requireContext(), buttonInfo.deeplink)
                val event = AnalyticsEvent(
                    EventConstants.COURSE_CLICK_BUY_NOW,
                    hashMapOf(
                        EventConstants.ASSORTMENT_ID to buttonInfo.assortmentId.orEmpty(),
                        EventConstants.VARIANT_ID to buttonInfo.variantId.orEmpty(),
                        EventConstants.MUlTIPLE_PACKAGE to multiplePackage
                    ), ignoreMoengage = false
                )
                val event2 = AnalyticsEvent(
                    EventConstants.COURSE_CLICK_BUY_NOW + "_v2",
                    hashMapOf(
                        EventConstants.ASSORTMENT_ID to buttonInfo.assortmentId.orEmpty(),
                        EventConstants.VARIANT_ID to buttonInfo.variantId.orEmpty(),
                        EventConstants.MUlTIPLE_PACKAGE to multiplePackage
                    )
                )
                analyticsPublisher.publishEvent(event)
                val countToSendEvent: Int = Utils.getCountToSend(
                    RemoteConfigUtils.getEventInfo(),
                    EventConstants.COURSE_CLICK_BUY_NOW
                )
                MoEngageUtils.setUserAttribute(requireContext(), "dn_bnb_clicked",true)

                val eventCopy = event.copy()
                val event2Copy = event2.copy()
                repeat((0 until countToSendEvent).count()) {
                    analyticsPublisher.publishBranchIoEvent(eventCopy)
                    analyticsPublisher.publishBranchIoEvent(event2Copy)
                }
            }

            textViewPayInstallment.setOnClickListener {
                deeplinkAction.performAction(requireContext(), buttonInfo.installmentDeeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.COURSE_CLICK_EMI,
                        hashMapOf(
                            EventConstants.ASSORTMENT_ID to buttonInfo.assortmentId.orEmpty(),
                            EventConstants.VARIANT_ID to buttonInfo.variantIdInstallment.orEmpty()
                        )
                    )
                )
            }
        }
    }

    private fun showEmiDialog(emi: PackageDetailWidgetItem.Emi) {
        Dialog(requireContext()).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_emi)
            window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#cc000000")))
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            show()
            setCancelable(true)
            dialogParentView.setOnClickListener {
                dismiss()
            }
            imageViewClose.setOnClickListener {
                dismiss()
            }

            textViewEmiSubTitle.text = emi.subTitle.orEmpty()
            textViewEmiDescription.text = emi.description.orEmpty()
            textViewMonth.text = emi.monthLabel.orEmpty()
            textViewInstallment.text = emi.installmentLabel.orEmpty()
            textViewEmiTotal.text = emi.totalLabel.orEmpty()
            textViewTotalAmount.text = emi.totalAmount.orEmpty()

            layoutInstallment.removeAllViews()
            emi.installments?.forEach {
                val textView = RowTextView(requireContext())
                textView.setViews(it.title.orEmpty(), it.amount.orEmpty(), null)
                layoutInstallment.addView(textView)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appStateObserver?.dispose()
        viewTrackingBus?.unsubscribe()
    }

}