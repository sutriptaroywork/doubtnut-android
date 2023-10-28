package com.doubtnutapp.libraryhome.coursev3.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.DocumentsContract
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.StickyHeadersLinearLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.*
import com.doubtnutapp.Constants.MY_COURSE
import com.doubtnutapp.Constants.SOURCE
import com.doubtnutapp.EventBus.PlayAudioEvent
import com.doubtnutapp.EventBus.VideoSeekEvent
import com.doubtnutapp.EventBus.VipStateEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.course.widgets.*
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.databinding.FragmentCoursesBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.homefeed.interactor.ConfigData
import com.doubtnutapp.freeclasses.bottomsheets.FilterListBottomSheetDialogFragment
import com.doubtnutapp.freeclasses.widgets.FilterSortWidget
import com.doubtnutapp.libraryhome.course.ui.ExploreFragment
import com.doubtnutapp.libraryhome.coursev3.adapter.CoursePagerAdapter
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseViewModelV3
import com.doubtnutapp.libraryhome.library.LibraryFragmentHome
import com.doubtnutapp.libraryhome.library.ui.adapter.LibraryTabAdapter
import com.doubtnutapp.liveclass.ui.CourseCategoryActivity
import com.doubtnutapp.liveclass.ui.SaleFragment
import com.doubtnutapp.liveclass.ui.dialog.FilterDialogFragment
import com.doubtnutapp.newglobalsearch.ui.InAppSearchActivity
import com.doubtnutapp.sales.PrePurchaseCallingCard2
import com.doubtnutapp.sales.PrePurchaseCallingCardConstant
import com.doubtnutapp.sales.PrePurchaseCallingCardData2
import com.doubtnutapp.sales.PrePurchaseCallingCardModel2
import com.doubtnutapp.sales.event.PrePurchaseCallingCardDismiss
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.studygroup.viewmodel.SgCreateViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.video.VideoFragmentListener
import com.doubtnutapp.video.VideoPlayerManager
import com.doubtnutapp.videoPage.model.VideoResource
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.FilterSubjectWidget
import com.doubtnutapp.widgets.base.BaseWidgetBottomSheetDialogFragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.uxcam.UXCam
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Pre Purchase and Post purchase course..
 * Free Live class page
 */
class CourseFragment : BaseBindingFragment<CourseViewModelV3, FragmentCoursesBinding>(),
    ActionPerformer,
    VideoFragmentListener {

    companion object {
        const val TAG = "CourseFragment"
        const val TITLE = "title"
        const val ASSORTMENT_ID = "id"
        const val DEFAULT_SUBJECT = "ALL"
        private const val REQUEST_CODE_COURSE_SELECTION = 133

        fun newInstance(
            assortmentId: String? = null,
            source: String?,
            studentClass: String? = null,
        ): CourseFragment {
            val fragment = CourseFragment()
            val args = Bundle()
            args.putString(ASSORTMENT_ID, assortmentId)
            args.putString(SOURCE, source.orEmpty())
            args.putString(Constants.STUDENT_CLASS, studentClass)
            fragment.arguments = args
            return fragment
        }
    }

    private var buttonInfo: ButtonInfo? = null
    private var callData: CallData? = null
    private var title = ""
    private var isTrialActivated: Boolean = false
    private var refreshUI: Boolean = false
    var hasToHandleBottomBarVisibility: Boolean = false
    private var hasToHandleVideoBottomVisibility: Boolean = false
    private lateinit var fragment: CourseSelectionFragment
    private var freeLiveClassTabSelectedId: String? = null
    private val freeLiveClassFiltersSelected: HashMap<String, MutableList<String>> = hashMapOf()

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    private lateinit var widgetPlanButtonVM: WidgetPlanButtonVM

    private lateinit var createStudyGroupViewModel: SgCreateViewModel

    // temporary saving the state so the view plan button of MainActivity does not
    // comes on top of course selection fragment.
    private var tempWidgetViewPlanButtonVisibility = false

    private lateinit var adapter: WidgetLayoutAdapter
    private lateinit var stickyWidgetsAdapter: WidgetLayoutAdapter
    private lateinit var extraAdapter: WidgetLayoutAdapter
    private var subject: String = DEFAULT_SUBJECT
    private var widgets: List<WidgetEntityModel<*, *>>? = null
    private var assortmentId: String = ""
    private var source: String = ""
    private var studentClass: String? = null
    private var rxBusEventObserver: Disposable? = null
    private var shouldShowSaleDialog = false
    private var courseData: ApiCourseDataV3? = null
    var chatDeeplink = ""
    private var isSaleDialogShown = false
    private var nudgeId: Int = 0
    private var nudgeMaxCount: Int = 0
    private var supportOptions: List<SupportData>? = null
    var isVip: Boolean = false
    var callingCardChatDeeplink: String? = null
    private var isCourseSelectionShown = false
    private var filePath = ""
    private var backDeeplink = ""

    @Inject
    lateinit var timerDisposable: CompositeDisposable

    private var enterTime = System.currentTimeMillis()

    private var videoPlayerManager: VideoPlayerManager? = null

    private var appStateObserver: Disposable? = null

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCoursesBinding =
        FragmentCoursesBinding.inflate(layoutInflater)

    override fun provideViewModel(): CourseViewModelV3 {
        widgetPlanButtonVM = activityViewModelProvider(viewModelFactory)
        createStudyGroupViewModel = viewModelProvider(viewModelFactory)
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setupListeners()

        assortmentId = arguments?.getString(ASSORTMENT_ID).orEmpty()
        source = arguments?.getString(SOURCE).orEmpty()

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            viewModel.getSnackBar(assortmentId)
        }

        val shouldShowCourseSelection =
            defaultPrefs().getBoolean(Constants.SHOULD_SHOW_COURSE_SELECTION, false)
        isCourseSelectionShown =
            defaultPrefs().getBoolean(Constants.IS_COURSE_SELECTION_SHOWN, false)
        if (assortmentId.isNotEmpty()) {
            initUi()
            if (source == MY_COURSE && shouldShowCourseSelection && !isCourseSelectionShown) {
                showCourseSelection()
            }
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.PAGE_VIEW + TAG,
                    hashMapOf(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID to assortmentId)
                )
            )
        } else if (assortmentId.isEmpty() && shouldShowCourseSelection) {
            showCourseSelection()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListeners() {
        binding.videoInfo.setOnClickListener {
            initAndPlayVideo()
        }

        binding.tvCall.setOnClickListener {
            if (callData != null) {
                binding.tvCall.setOnClickListener {
                    if (!supportOptions.isNullOrEmpty()) {
                        binding.tvCall.setCompoundDrawables(null, null, null, null)
                        showSupportOptionsMenu()
                    } else if (callData != null) {
                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:" + callData!!.number)
                                )
                            )
                            val event = AnalyticsEvent(
                                EventConstants.COURSE_CALL_CLICK,
                                hashMapOf(
                                    EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID to assortmentId,
                                    EventConstants.PHONE_NUMBER to callData!!.number
                                )
                            )
                            analyticsPublisher.publishEvent(event)
                            val countToSendEvent: Int = Utils.getCountToSend(
                                RemoteConfigUtils.getEventInfo(),
                                EventConstants.COURSE_CALL_CLICK
                            )
                            val eventCopy = event.copy()
                            repeat((0 until countToSendEvent).count()) {
                                analyticsPublisher.publishBranchIoEvent(eventCopy)
                            }
                        } catch (e: Exception) {
                            // No Activity found to handle Intent { act=android.intent.action.DIAL dat=tel:xxxxxxxxxxx }
                            IntentUtils.showCallActionNotPerformToast(
                                requireContext(),
                                callData!!.number
                            )
                        }
                    }
                }
            }
        }

        binding.tvChat.setOnClickListener {
            handleChatSupport()
        }

        binding.ivOverflow.setOnClickListener {
            showSupportOptionsMenu()
        }

        binding.ivBack.setOnClickListener { onBackPressed() }

        binding.globalSearch.setOnClickListener {
        }

        binding.globalSearch.setOnTouchListener { v, event ->
            val drawableRightIndex = 2
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableRight = binding.globalSearch.compoundDrawables[drawableRightIndex]
                if (event.rawX >= (binding.globalSearch.right - (drawableRight?.bounds?.width()
                        ?: 0))
                ) {
                    openSearchActivity(true)
                } else if (event.rawX < (binding.globalSearch.right - (drawableRight?.bounds?.width()
                        ?: 0))
                ) {
                    openSearchActivity(false)
                }
            }
            return@setOnTouchListener false
        }

    }

    private fun handleChatSupport() {
        if (FeaturesManager.isFeatureEnabled(
                requireContext(),
                Features.STUDY_GROUP_AS_FRESH_CHAT
            )
        ) {
            createStudyGroupViewModel.createGroup(
                groupName = null,
                groupImage = null,
                isSupport = true
            )
        } else {
            ChatUtil.setUser(
                requireContext(), assortmentId, "",
                this.title, "", EventConstants.COURSE
            )
            ChatUtil.startConversation(requireContext())
            defaultPrefs().edit()
                .putLong(Constants.PREF_CHAT_START_TIME, System.currentTimeMillis()).apply()
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CHAT_CLICK,
                    hashMapOf(
                        EventConstants.SOURCE to if (isVip)
                            EventConstants.POST_PURCHASE
                        else
                            EventConstants.PRE_PURCHASE,
                        EventConstants.FEATURE to EventConstants.FEATURE_FRESHWORKS
                    ), ignoreBranch = false
                )
            )
        }
    }

    private fun openSearchActivity(startVoiceSearch: Boolean) {
        Utils.executeIfContextNotNull(context) { context: Context ->
            InAppSearchActivity.startActivity(
                context = context,
                source = TAG,
                startVoiceSearch = startVoiceSearch,
                selectedClass = null
            )
        }
    }

    private fun initAndPlayVideo(startPosition: Long = 0) {
        timerDisposable.clear()
        val videoResources = courseData?.demoVideo?.videoResources ?: return
        val resources = videoResources.map {
            VideoResource(
                resource = it.resource,
                drmScheme = it.drmScheme,
                drmLicenseUrl = it.drmLicenseUrl,
                mediaType = it.mediaType,
                isPlayed = false,
                dropDownList = null,
                timeShiftResource = null,
                offset = it.offset
            )
        }

        binding.videoInfo.hide()
        initVideoPlayer()
        var sourcePage = courseData?.demoVideo?.page.orEmpty()
        if (Constants.PAGE_SEARCH_SRP == arguments?.getString(Constants.SOURCE)) {
            sourcePage = Constants.PAGE_SEARCH_SRP
        }
        videoPlayerManager?.setAndInitPlayFromResource(
            courseData?.demoVideo?.qid.orEmpty(),
            resources,
            courseData?.demoVideo?.viewId.orEmpty(),
            startPosition,
            false,
            sourcePage,
            VideoFragment.DEFAULT_ASPECT_RATIO,
            null, null,
            false
        )
    }

    private fun initVideoPlayer() {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.COURSE_INTRO_VIDEO_PLAY,
                hashMapOf(
                    EventConstants.ASSORTMENT_ID to assortmentId,
                    EventConstants.SOURCE to TAG
                ), ignoreSnowplow = true
            )
        )
        videoPlayerManager = VideoPlayerManager(
            requireFragmentManager(),
            this, R.id.videoViewContainer, { _, _ -> }
        )
    }

    override fun singleTapOnPlayerView() {
        if (videoPlayerManager?.videoFragment == null) return
        if (videoPlayerManager?.isPlayerControllerVisible == true) {
            videoPlayerManager?.hidePlayerController()
        } else if (videoPlayerManager?.isPlayerControllerVisible != true) {
            videoPlayerManager?.showPlayerController()
        }
    }

    fun onBackPressed() {
        if (shouldShowSaleDialog && nudgeId != 0 && !isSaleDialogShown) {
            var dialogShownCount = defaultPrefs().getInt(Constants.NUDGE_COURSE_COUNT, 0)
            if (dialogShownCount < nudgeMaxCount) {
                SaleFragment.newInstance(nudgeId).show(requireFragmentManager(), SaleFragment.TAG)
                isSaleDialogShown = true
                dialogShownCount++
                defaultPrefs().edit().putInt(Constants.NUDGE_COURSE_COUNT, dialogShownCount).apply()
            } else {
                if (isTrialActivated) {
                    (activity?.applicationContext as? DoubtnutApp)?.bus()?.send(VipStateEvent(true))
                }
                activity?.onBackPressed()
            }
        } else if (backDeeplink.isNotEmpty()) {
            deeplinkAction.performAction(requireContext(), backDeeplink)
            backDeeplink = ""
        } else {
            if (isTrialActivated) {
                (activity?.applicationContext as? DoubtnutApp)?.bus()?.send(VipStateEvent(true))
            }
            activity?.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1211 && data?.data != null) {
            alterFile(data.data!!)
        }
    }

    private fun alterFile(uri: Uri) {
        try {
            val pdfUri =
                FileProvider.getUriForFile(requireContext(), BuildConfig.AUTHORITY, File(filePath))
            val inputStream = requireContext().contentResolver.openInputStream(pdfUri)
            requireContext().contentResolver.openFileDescriptor(uri, "w")?.use {
                FileOutputStream(it.fileDescriptor).use { outStream ->
                    inputStream!!.copyTo(outStream)
                    Snackbar.make(
                        requireActivity().findViewById(android.R.id.content),
                        "Your PDF has been downloaded.\n" +
                                "You can also find it under My PDFs",
                        Snackbar.LENGTH_LONG
                    )
                        .setAction("View") {
                            openFile(uri)
                        }.show()
                }
            }
        } catch (e: Exception) {
            DocumentsContract.deleteDocument(requireContext().contentResolver, uri)
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                "Unable to download file",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun openFile(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(Intent.createChooser(intent, "Open PDF"))
        } catch (e: Exception) {
            Log.e(e)
            toast("No pdf reader found")
        }
    }

    private fun onCourseSelected(data: Bundle?) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.COURSE_BOTTOM_SHEET_CLICK,
                hashMapOf(EventConstants.SOURCE to EventConstants.BOTTOM_ICON)
            )
        )

        binding.courseSelectContainer.hide()
        binding.overlayCourse.hide()
        binding.appBarLayout.show()
        widgetPlanButtonVM.updateWidgetViewPlanButtonVisibility(tempWidgetViewPlanButtonVisibility)

        assortmentId = data?.getString(CourseSelectionFragment.ASSORTMENT_ID).orEmpty()
        val categoryId = data?.getString(CourseSelectionFragment.CATEGORY_ID).orEmpty()
        childFragmentManager?.beginTransaction()?.remove(fragment)?.commitAllowingStateLoss()

        if (categoryId.isEmpty()) {
            defaultPrefs().edit().putString(Constants.SELECTED_ASSORTMENT_ID, assortmentId)
                .apply()
            defaultPrefs().edit().putString(Constants.SELECTED_CATEGORY_ID, "").apply()
            initUi()
        } else {
            defaultPrefs().edit().putString(Constants.SELECTED_CATEGORY_ID, categoryId).apply()
            defaultPrefs().edit().putString(Constants.SELECTED_ASSORTMENT_ID, assortmentId).apply()
            val parentFragment = parentFragment as LibraryFragmentHome?
            parentFragment?.adapter?.updateTabAtPosition(
                resources.getString(R.string.my_course),
                LibraryTabAdapter.TAG_MY_COURSES,
                fragment = ExploreFragment.newInstance(categoryId = categoryId),
                position = 0
            )
        }
    }

    private fun showCourseSelection() {
        tempWidgetViewPlanButtonVisibility =
            widgetPlanButtonVM.widgetViewPlanButtonVisibility.value ?: false
        widgetPlanButtonVM.updateWidgetViewPlanButtonVisibility(false)

        defaultPrefs().edit().putBoolean(Constants.IS_COURSE_SELECTION_SHOWN, true).apply()
        binding.progressBar.hide()
        binding.overlayCourse.show()
        binding.courseSelectContainer.show()
        binding.overlayCourse.setOnClickListener {
            binding.courseSelectContainer.hide()
            binding.overlayCourse.hide()
            binding.appBarLayout.show()
            widgetPlanButtonVM.updateWidgetViewPlanButtonVisibility(
                tempWidgetViewPlanButtonVisibility
            )
        }
        fragment = CourseSelectionFragment.newInstance("live_class_bottom_icon", TAG)
        childFragmentManager.beginTransaction().replace(R.id.courseSelectContainer, fragment)
            .commitAllowingStateLoss()
        fragment.setFragmentResultListener(TAG) { requestKey, bundle ->
            onCourseSelected(bundle)
        }
        binding.appBarLayout.hide()
    }

    private fun showSupportOptionsMenu() {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.HELP_ICON_CLICK,
                hashMapOf(
                    EventConstants.ASSORTMENT_ID to assortmentId
                ), ignoreSnowplow = true
            )
        )

        val menu = SupportDropDownMenu(requireContext(), supportOptions ?: emptyList())
        menu.height = WindowManager.LayoutParams.WRAP_CONTENT
        menu.width = Utils.convertDpToPixel(150f).toInt()
        menu.isOutsideTouchable = true
        menu.isFocusable = true
        menu.showAsDropDown(binding.ivOverflow)
        menu.setCategorySelectedListener(object :
            SupportDropDownAdapter.SupportOptionSelectedListener {
            override fun onOptionSelected(position: Int, option: SupportData) {
                menu.dismiss()
                when (option.id) {
                    "chat" -> {
                        handleChatSupport()
                    }
                    "call" -> {
                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:" + option.data)
                                )
                            )
                            val event = AnalyticsEvent(
                                EventConstants.COURSE_CALL_CLICK,
                                hashMapOf(
                                    EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID to assortmentId,
                                    EventConstants.PHONE_NUMBER to callData?.number.orEmpty(),
                                    EventConstants.SOURCE to (if (isVip) EventConstants.POST_PURCHASE
                                    else EventConstants.PRE_PURCHASE)
                                )
                            )
                            analyticsPublisher.publishEvent(event)
                            val countToSendEvent: Int = Utils.getCountToSend(
                                RemoteConfigUtils.getEventInfo(),
                                EventConstants.COURSE_CALL_CLICK
                            )
                            val eventCopy = event.copy()
                            repeat((0 until countToSendEvent).count()) {
                                analyticsPublisher.publishBranchIoEvent(eventCopy)
                            }
                        } catch (e: java.lang.Exception) {
                            // No Activity found to handle Intent { act=android.intent.action.DIAL dat=tel:xxxxxxxxxxx }
                            IntentUtils.showCallActionNotPerformToast(
                                requireContext(),
                                callData?.number.orEmpty()
                            )
                        }
                    }
                    else -> {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.COURSE_MENU_CLICK,
                                hashMapOf<String, Any>(EventConstants.ASSORTMENT_ID to assortmentId).apply {
                                    putAll(option.eventParams.orEmpty())
                                }, ignoreSnowplow = true

                            )
                        )
                        deeplinkAction.performAction(requireContext(), option.deeplink.orEmpty())
                    }
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(TAG) { requestKey, bundle ->
            performAction(
                OnMediumSelected(
                    bundle.getString(BaseWidgetBottomSheetDialogFragment.KEY_ASSORTMENT_ID)
                        .orEmpty()
                )
            )
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is FilterSelectAction -> {
                if (action.type == "subject") {
                    subject = action.filterText ?: DEFAULT_SUBJECT
                    viewModel.getAllCoursesData(
                        assortmentId = assortmentId,
                        subject = subject,
                        studentClass = studentClass,
                        page = 1,
                        source = source
                    )
                }
            }
            is ActivateVipTrial -> {
                viewModel.activateTrial(action.assortmentId)
            }
            is RefreshUI -> {
                refreshUI = true
            }
            is OnNudgeClosed -> {
                courseData?.widgets?.filterIsInstance<NudgeWidget.NudgeWidgetModel>().apply {
                    this?.forEach {
                        if (it.data.widgetId == action.nudgeId) {
                            adapter.removeWidget(it)
                        }
                    }

                }
            }
            is OnMediumSelected -> {
                if (!action.selectedAssortmentId.isNullOrEmpty()) {
                    assortmentId = action.selectedAssortmentId.orEmpty()
                }
                initUi()
            }
            is OnPdfDownloadOrShareClick -> {
                viewModel.getPdfFilePath(action.pdfUrl, action.type)
            }
            is TwoTextsVerticalTabWidgetTabChanged -> {
                freeLiveClassTabSelectedId = action.tabSelected
                viewModel.getAllCoursesData(
                    assortmentId = assortmentId,
                    subject = subject,
                    studentClass = studentClass,
                    page = 1,
                    source = source,
                    tabId = freeLiveClassTabSelectedId,
                    filtersMap = freeLiveClassFiltersSelected
                )
            }
            is OnCategoryFilterApplied -> {
                val filters = action.map
                filters.forEach { (s, list) -> freeLiveClassFiltersSelected[s] = list }
                viewModel.getAllCoursesData(
                    assortmentId = assortmentId,
                    subject = subject,
                    studentClass = studentClass,
                    page = 1,
                    source = source,
                    tabId = freeLiveClassTabSelectedId,
                    filtersMap = freeLiveClassFiltersSelected
                )
            }
            is ApplyFilters -> {
                val bottomSheetType = action.bottomSheetType
                val filterName = action.filterName
                val filters = action.selectedFilters
                freeLiveClassFiltersSelected[filterName] = filters as MutableList<String>

                when (bottomSheetType) {
                    FilterSortWidget.FilterType.SUBJECT -> {
                        //for cases like: subject changed from phy -> bio but teachers,chapters are still shown for phy
                        freeLiveClassFiltersSelected["teacher"]?.clear()
                        freeLiveClassFiltersSelected["chapter"]?.clear()
                    }
                }

                viewModel.getAllCoursesData(
                    assortmentId = assortmentId,
                    subject = subject,
                    studentClass = studentClass,
                    page = 1,
                    source = source,
                    tabId = freeLiveClassTabSelectedId,
                    filtersMap = freeLiveClassFiltersSelected
                )
            }
            is OnFreeLiveClassFilterClicked -> {
                when (val type = FilterSortWidget.FilterType.toFilterType(action.type)) {
                    FilterSortWidget.FilterType.FILTER -> {
                        FilterDialogFragment.newInstance(
                            filtersMap = freeLiveClassFiltersSelected,
                            source = FilterSortWidget.EVENT_TAG,
                            assortmentId = assortmentId
                        ).apply {
                            setActionPerformer(this@CourseFragment)
                        }.also {
                            it.show(
                                requireActivity().supportFragmentManager,
                                FilterDialogFragment.TAG
                            )
                        }
                    }
                    FilterSortWidget.FilterType.CHAPTER, FilterSortWidget.FilterType.SORT,
                    FilterSortWidget.FilterType.SUBJECT -> {
                        FilterListBottomSheetDialogFragment.newInstance(
                            type,
                            assortmentId,
                            freeLiveClassFiltersSelected
                        ).apply {
                            setActionPerformer(this@CourseFragment)
                        }.also {
                            it.show(
                                requireActivity().supportFragmentManager,
                                FilterListBottomSheetDialogFragment.TAG
                            )
                        }
                    }
                    FilterSortWidget.FilterType.NONE -> {
                        deeplinkAction.performAction(requireContext(), action.deeplink)
                    }
                }
            }
            is CourseFragmentCloseClicked -> {
                onBackPressed()
            }
            else -> {
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (refreshUI) {
            initUi()
            refreshUI = false
        }
    }

    override fun setupObservers() {
        appStateObserver = DoubtnutApp.INSTANCE
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is PrePurchaseCallingCardDismiss -> {
                        binding.prePurchaseCallingCard.hide()
                    }
                }
            }

        viewModel.courseLiveData.observeK(
            this,
            this::onWidgetListFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
        viewModel.activateVipLiveData.observeK(
            this,
            this::onActivateTrialSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        viewModel.widgetsLiveData.observeK(
            this,
            this::onStoriesSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        viewModel.configLiveData.observeK(this,
            ::onConfigDataSuccess, ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            {})

        viewModel.callData.observe(viewLifecycleOwner, EventObserver {
            callData = it
            if (it == null) {
                binding.tvCall.visibility = View.INVISIBLE
            } else {
                binding.tvCall.show()
                binding.tvCall.text = it.title
            }
        })

        rxBusEventObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is VipStateEvent) {
                if (event.state) {
                    initUi()
                }
            } else if (event is VideoSeekEvent) {
                if (videoPlayerManager == null) {
                    initAndPlayVideo(event.position)
                } else {
                    videoPlayerManager?.setExoCurrentPosition(TimeUnit.SECONDS.toMillis(event.position))
                }
            } else if (event is PlayAudioEvent && event.state) {
                val index = courseData?.extraWidgets?.indexOfFirst {
                    it is ParentAutoplayWidget.ParentAutoplayWidgetModel
                }
                if (index != null && index != -1) {
                    val widget = binding.rvExtraWidgets.layoutManager?.findViewByPosition(index)
                    if (widget != null && widget.isOnScreen && widget is ParentAutoplayWidget) {
                        widget.stopVideo()
                    }
                }
                videoPlayerManager?.pauseExoPlayer()
            }
        }

        viewModel.pdfUriLiveData.observe(viewLifecycleOwner) { pair ->
            if (pair?.first != null) {
                filePath = pair.first.absolutePath
                if (pair.second == Constants.TYPE_SHARE) {
                    sharePdf(filePath)
                } else {
                    saveFile(pair.third)
                }
            } else {
                ToastUtils.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.snackBarData.observeNonNull(viewLifecycleOwner) { data ->
            val snackbar = Snackbar.make(
                requireActivity().findViewById(android.R.id.content), "",
                BaseTransientBottomBar.LENGTH_INDEFINITE
            )
            snackbar.duration = 3000
            val customView = View.inflate(activity, R.layout.item_custom_snackbar, null)

            snackbar.view.setBackgroundColor(Color.TRANSPARENT)
            val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
            snackbarLayout.removeAllViews()
            snackbarLayout.setPadding(0, 0, 0, 0)

            customView.rootView.applyBackgroundColor(data.bgColor)

            val imageView = customView.findViewById<ImageView>(R.id.iv_snackbar_icon)
            imageView.isVisible = data.iconUrl.isNotNullAndNotEmpty()
            imageView.loadImage(data.iconUrl.ifEmptyThenNull())

            val textView = customView.findViewById<TextView>(R.id.tv_snackbar_title)
            textView.text = data.title.orEmpty()
            textView.applyTextSize(data.titleSize)
            textView.applyTextColor(data.titleColor)

            val action = customView.findViewById<TextView>(R.id.tv_snackbar_action)
            action.isVisible = data.deeplinkText.isNotNullAndNotEmpty()
            action.text = data.deeplinkText.orEmpty()
            action.applyTextSize(data.deeplinkTextSize)
            action.applyTextColor(data.deeplinkTextColor)
            action.setOnClickListener {
                deeplinkAction.performAction(requireContext(), data.deeplink)
                snackbar.dismiss()
            }

            snackbarLayout.addView(customView, 0)
            snackbar.show()

            viewLifecycleOwner.lifecycleScope.launchWhenResumed {
                if (data.nextInterval != null) {
                    delay(data.nextInterval.toLong())
                    viewModel.getSnackBar(assortmentId)
                }
            }
        }

        createStudyGroupViewModel.groupCreatedLiveData.observeEvent(this) {
            val deeplink = it.groupChatDeeplink ?: return@observeEvent
            deeplinkAction.performAction(requireContext(), deeplink)
            val eventMap = hashMapOf<String, Any>()
            eventMap[EventConstants.SOURCE] = if (isVip)
                EventConstants.POST_PURCHASE
            else
                EventConstants.PRE_PURCHASE
            eventMap[EventConstants.FEATURE] = EventConstants.FEATURE_STUDY_GROUP
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CHAT_STARTED,
                    eventMap
                )
            )
        }
    }

    private fun onConfigDataSuccess(data: ConfigData) {
        ConfigUtils.saveToPref(data)
    }

    private fun handleBottomButtonView(buttonInfo: WidgetViewPlanButtonData?) {
        if (buttonInfo == null) {
            binding.viewPlanButton.hide()
            widgetPlanButtonVM.updateWidgetViewPlanButtonModel(null)
        } else {
            val model = WidgetViewPlanButtonModel().apply {
                extraParams = buttonInfo.extraParams
                _data = buttonInfo
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
            if (source == MY_COURSE) {
                binding.viewPlanButton.hide()
                widgetPlanButtonVM.updateWidgetViewPlanButtonModel(model)
            } else {
                binding.viewPlanButton.show()
                binding.viewPlanButton.bindWidget(binding.viewPlanButton.widgetViewHolder, model)
                widgetPlanButtonVM.updateWidgetViewPlanButtonModel(null)
            }
        }
    }

    private var trialTimer: CountDownTimer? = null

    private fun showToolbarData(courseData: ApiCourseDataV3, showToolbar: Boolean = true) {
        if (!showToolbar || courseData.isVip != true) {
            binding.toolbar.hide()
            return
        }

        binding.cvCardMain.isVisible = courseData.isVip == true
        binding.ivExpand.isVisible = courseData.courseList != null && courseData.courseList.size > 1

        binding.cardTitle.text = courseData.toolbarTitle.orEmpty()

        binding.tvCardSubtitle.isVisible = courseData.toolbarSubtitle.isNotNullAndNotEmpty()
        binding.tvCardSubtitle.text = courseData.toolbarSubtitle.orEmpty()
        binding.cvCardMain.setOnClickListener {
            if (courseData.courseList == null || courseData.courseList.size <= 1) {
                return@setOnClickListener
            }
            val menu = CourseSelectDropDownMenu(
                requireContext(),
                courseData.courseList ?: return@setOnClickListener
            )
            menu.height = WindowManager.LayoutParams.WRAP_CONTENT
            menu.width = Utils.convertDpToPixel(300f).toInt()
            menu.isOutsideTouchable = true
            menu.isFocusable = true
            menu.showAsDropDown(binding.cvCardMain)
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
                    assortmentId = data.id
                    defaultPrefs().edit()
                        .putString(Constants.SELECTED_ASSORTMENT_ID, assortmentId)
                        .apply()
                    when {
                        data.categoryId.isNullOrEmpty() -> {
                            binding.cardTitle.text = data.display
                            binding.scheduleContainer.visibility = View.GONE
                            defaultPrefs().edit().putString(Constants.SELECTED_CATEGORY_ID, "")
                                .apply()
                            initUi()
                        }
                        activity is CourseActivityV3 -> {
                            defaultPrefs().edit()
                                .putString(Constants.SELECTED_CATEGORY_ID, data.categoryId)
                                .apply()
                            (activity as CourseActivityV3).replaceFragment(
                                ExploreFragment.newInstance(
                                    categoryId = data.categoryId,
                                    source = source
                                ), ""
                            )
                        }
                        activity is CourseCategoryActivity -> {
                            defaultPrefs().edit()
                                .putString(Constants.SELECTED_CATEGORY_ID, data.categoryId)
                                .apply()
                            (activity as CourseCategoryActivity).replaceFragment(
                                ExploreFragment.newInstance(
                                    categoryId = data.categoryId,
                                    source = source
                                ), ""
                            )
                        }
                        activity is MainActivity -> {
                            defaultPrefs().edit()
                                .putString(Constants.SELECTED_CATEGORY_ID, data.categoryId)
                                .apply()
                            val parentFragment = parentFragment as LibraryFragmentHome?
                            parentFragment?.adapter?.updateTabAtPosition(
                                resources.getString(R.string.my_course),
                                LibraryTabAdapter.TAG_MY_COURSES,
                                ExploreFragment.newInstance(
                                    categoryId = data.categoryId,
                                    source = source
                                ),
                                0
                            )
                        }
                    }
                }
            })
        }

        binding.containerTrialInfo.isVisible = courseData.trialTitle.isNotNullAndNotEmpty()
                || (courseData.time != null && courseData.time > 0L)

        binding.containerTrialInfo.background = GradientUtils.getGradientBackground(
            courseData.bgColorOne,
            courseData.bgColorTwo,
            GradientDrawable.Orientation.LEFT_RIGHT
        )

        if (courseData.trialTitle.isNullOrEmpty()) {
            binding.tvTrialInfo.visibility = View.GONE
        } else {
            binding.tvTrialInfo.visibility = View.VISIBLE
            binding.tvTrialInfo.text = courseData.trialTitle
            binding.tvTrialInfo.applyTextSize(courseData.trialTitleSize)
            binding.tvTrialInfo.applyTextColor(courseData.trialTitleColor)
        }

        if (courseData.imageUrl.isNullOrEmpty()) {
            binding.ivGif.visibility = View.GONE
        } else {
            binding.ivGif.visibility = View.VISIBLE
            Glide.with(requireContext()).load(courseData.imageUrl).into(binding.ivGif)
        }

        trialTimer?.cancel()

        if (courseData.time == null || courseData.time <= 0L) {
            binding.tvTimer.visibility = View.GONE
            binding.ivGif.visibility = View.GONE
        } else {
            val actualTimeLeft =
                ((courseData.time.or(0)).minus(System.currentTimeMillis()))

            if (actualTimeLeft > 0) {
                binding.tvTimer.visibility = View.VISIBLE
                binding.tvTimer.applyTextColor(courseData.timeTextColor)
                binding.tvTimer.applyTextSize(courseData.timeTextSize)

                trialTimer = object : CountDownTimer(
                    actualTimeLeft, 1000
                ) {
                    override fun onTick(millisUntilFinished: Long) {
                        mBinding ?: return
                        binding.tvTimer.text =
                            DateTimeUtils.formatMilliSecondsToTime(millisUntilFinished)

                    }

                    override fun onFinish() {
                        mBinding ?: return
                        binding.ivGif.visibility = View.GONE
                        binding.tvTimer.visibility = View.GONE

                        binding.containerTrialInfo.background = GradientUtils.getGradientBackground(
                            courseData.bgColorOneExpired,
                            courseData.bgColorTwoExpired,
                            GradientDrawable.Orientation.LEFT_RIGHT
                        )
                        binding.tvTrialInfo.text = courseData.trialTitleExpired

                        courseData.trialTitle = courseData.trialTitleExpired
                        courseData.bgColorOne = courseData.bgColorOneExpired
                        courseData.bgColorTwo = courseData.bgColorTwoExpired
                    }
                }
                trialTimer?.start()

            } else {
                binding.tvTimer.visibility = View.GONE
                binding.ivGif.visibility = View.GONE

                binding.containerTrialInfo.background = GradientUtils.getGradientBackground(
                    courseData.bgColorOneExpired,
                    courseData.bgColorTwoExpired,
                    GradientDrawable.Orientation.LEFT_RIGHT
                )
                binding.tvTrialInfo.text = courseData.trialTitleExpired

                courseData.trialTitle = courseData.trialTitleExpired
                courseData.bgColorOne = courseData.bgColorOneExpired
                courseData.bgColorTwo = courseData.bgColorTwoExpired
            }
        }

    }

    private fun initUi() {
        binding.bottomBar.hide()
        binding.viewPlanButton.hide()

        binding.containerTrialInfo.hide()
        binding.ivExpand.hide()

        //binding.parentLayout.setPassMode(PASS_MODE_PARENT_FIRST)
        studentClass = arguments?.getString(Constants.STUDENT_CLASS)
        if (studentClass == "") {
            studentClass = null
        }
        adapter = WidgetLayoutAdapter(
            requireContext(),
            this,
            arguments?.getString(Constants.SOURCE).orEmpty()
        ).apply {
            attachLifecycleOwner(viewLifecycleOwner)
        }
        binding.rvWidgets.adapter = adapter

        extraAdapter = WidgetLayoutAdapter(
            requireContext(),
            this,
            arguments?.getString(Constants.SOURCE).orEmpty()
        ).apply {
            attachLifecycleOwner(viewLifecycleOwner)
        }
        binding.rvExtraWidgets.layoutManager =
            StickyHeadersLinearLayoutManager<WidgetLayoutAdapter>(context)
        binding.rvExtraWidgets.adapter = extraAdapter

        stickyWidgetsAdapter = WidgetLayoutAdapter(
            requireContext(),
            this,
            arguments?.getString(Constants.SOURCE).orEmpty()
        )
        binding.rvStickyWidgets.adapter = stickyWidgetsAdapter

        viewModel.extraParams.apply {
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME, TAG)
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.SUBJECT, subject)
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID, assortmentId)
        }
        viewModel.getAllCoursesData(
            assortmentId = assortmentId,
            subject = subject,
            studentClass = studentClass,
            page = 1,
            source = source
        )
        //viewModel.getStories()
        setAppBarScrollListener()
        view?.isFocusableInTouchMode = true
        view?.requestFocus()
        view?.setOnKeyListener { view: View, keyCode: Int, keyEvent: KeyEvent ->
            if (keyEvent.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPressed()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

    }

    private fun onWidgetListFetched(courseData: ApiCourseDataV3) {
        assortmentId = courseData.assortmentId.orEmpty()
        if (courseData.isVip == true) {
            sendPageTypeView(EventConstants.COURSE_PAGE_POST_PURCHASE, courseData.assortmentId)
            UXCam.tagScreenName("PostPurchase")
        } else {
            sendPageTypeView(EventConstants.COURSE_PAGE_PRE_PURCHASE, courseData.assortmentId)
            UXCam.tagScreenName("PrePurchase")
        }

        binding.ivBack.isVisible = courseData.showInAppSearch ?: false
        binding.clSearchView.isVisible = courseData.showInAppSearch ?: false
        binding.globalSearch.text = courseData.inAppSearchTitle.orEmpty()

        binding.scheduleContainer.visibility = View.VISIBLE
        videoPlayerManager?.resetVideo()
        videoPlayerManager?.removeVideoFromContainer()
        videoPlayerManager = null
        hasToHandleBottomBarVisibility = false
        hasToHandleVideoBottomVisibility = false
        timerDisposable.clear()
        this.courseData = courseData
        supportOptions = courseData.supportData
        isVip = courseData.isVip ?: false
        callingCardChatDeeplink = courseData.callingCardChatDeeplink
        backDeeplink = courseData.backDeeplink.orEmpty()

        if (!supportOptions.isNullOrEmpty()) {
            binding.tvCall.setCompoundDrawables(null, null, null, null)
        }
        this.title = courseData.title.orEmpty()
        shouldShowSaleDialog = courseData.shouldShowSaleDialog ?: false
        nudgeId = courseData.nudgeId ?: 0
        nudgeMaxCount = courseData.nudgeCount ?: 0
        val savedNudgeId = defaultPrefs().getInt(Constants.NUDGE_ID_COURSE, 0)
        if (savedNudgeId == 0 || savedNudgeId != nudgeId) {
            defaultPrefs().edit().putInt(Constants.NUDGE_ID_COURSE, nudgeId).apply()
            defaultPrefs().edit().putInt(Constants.NUDGE_COURSE_COUNT, 0).apply()
        }
        widgets = courseData.widgets
        adapter.addWidgets(courseData.widgets.orEmpty())
        showToolbarData(courseData, true)
        courseData.widgets?.find { it is FilterSubjectWidget.FilterTabsWidgetModel }.apply {
            if (this != null) {
                val model = (this as FilterSubjectWidget.FilterTabsWidgetModel)
                if (!DoubtnutApp.INSTANCE.isOnboardingCompleted && DoubtnutApp.INSTANCE.isOnboardingStarted) {
                    model.isOnboardingEnabled = true
                }
                if (subject == null) {
                    model.data.selectedSubject = model.data.items.getOrNull(0)?.filterId ?: ""
                } else {
                    val selectedFilter = model.data.items.firstOrNull { it.filterId == subject }
                    if (selectedFilter == null) {
                        model.data.selectedSubject = model.data.items.getOrNull(0)?.filterId
                            ?: ""
                    } else {
                        model.data.selectedSubject = selectedFilter.filterId
                    }
                }
            }
        }
        courseData.widgets?.find { it is IncreaseValidityWidgetModel }.apply {
            if (this != null) {
                val model = (this as IncreaseValidityWidgetModel)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.INCREASE_VALIDITY_VIEW,
                        hashMapOf<String, Any>().apply {
                            putAll(model.extraParams ?: hashMapOf())
                        }, ignoreSnowplow = true
                    )
                )
            }
        }

        courseData.widgets?.find { it is NudgeWidget.NudgeWidgetModel }.apply {
            if (this != null) {
                val model = (this as NudgeWidget.NudgeWidgetModel)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_NUDGE_VIEW,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.NUDGE_ID, model.data.widgetId.orEmpty())
                            put(EventConstants.NUDGE_TYPE, model.data.nudgeType.orEmpty())
                            putAll(model.extraParams ?: hashMapOf())
                        }
                    ))
            }
        }

        if (!courseData.shareMessage.isNullOrEmpty()) {
            binding.tvShare.visibility = View.VISIBLE
            binding.tvShare.loadImage(courseData.shareImageUrl)
            binding.tvShare.setOnClickListener {
                whatsAppSharing.shareOnWhatsApp(
                    ShareOnWhatApp(
                        courseData.channel.orEmpty(),
                        featureType = courseData.featureName,
                        campaign = courseData.campaigniId.orEmpty(),
                        imageUrl = "",
                        controlParams = courseData.controlParms ?: hashMapOf(),
                        bgColor = "#000000",
                        sharingMessage = courseData.shareMessage.orEmpty(),
                        questionId = ""
                    )
                )
                whatsAppSharing.startShare(requireContext())
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.SHARE_COURSE_BUTTON_CLICK,
                        hashMapOf(
                            EventConstants.ASSORTMENT_ID to assortmentId,
                            EventConstants.STUDENT_ID to UserUtil.getStudentId()
                        )
                    )
                )
                analyticsPublisher.publishBranchIoEvent(
                    AnalyticsEvent(
                        EventConstants.SHARE_COURSE_BUTTON_CLICK,
                        hashMapOf(
                            EventConstants.ASSORTMENT_ID to assortmentId,
                            EventConstants.STUDENT_ID to UserUtil.getStudentId()
                        )
                    )
                )
            }
        } else {
            binding.tvShare.visibility = View.GONE
        }

        if (courseData.stickyWidgets.isNullOrEmpty()) {
            binding.rvStickyWidgets.hide()
        } else {
            stickyWidgetsAdapter.setWidgets(courseData.stickyWidgets.orEmpty())
            binding.rvStickyWidgets.show()
        }

        if (courseData.extraWidgets.isNullOrEmpty()) {
            binding.prePurchaseCallingCard.hide()
            binding.rvExtraWidgets.hide()
            binding.videoLayout.hide()
            binding.videoBottomView.hide()
            binding.scheduleContainer.show()
        } else {
            binding.rvExtraWidgets.show()
            binding.buttonTryNowVideo.setVisibleState(!courseData.demoVideo?.bottomSubTitle.isNullOrBlank())
            binding.scheduleContainer.hide()
            extraAdapter.setWidgets(courseData.extraWidgets.orEmpty())
            val delay = courseData.demoVideo?.delay?.toLongOrNull()
            if (delay != null) {
                setVideoAutoPlay(delay)
            }

            if (courseData.demoVideo != null) {
                binding.videoLayout.show()
                binding.videoBottomView.show()
                if (courseData.demoVideo.videoResources.isNullOrEmpty()) {
                    binding.playCourse.hide()
                } else {
                    binding.playCourse.show()
                }
                hasToHandleVideoBottomVisibility = true
                binding.videoInfo.loadBackgroundImage(
                    courseData.demoVideo.imageUrl.orEmpty(),
                    R.color.yellow_f4ac3e
                )
                binding.videoBottomView.loadBackgroundImage(
                    if (courseData.demoVideo.imageUrlTwo.isNullOrBlank()) {
                        courseData.demoVideo.imageUrl.orEmpty()
                    } else {
                        courseData.demoVideo.imageUrlTwo.orEmpty()
                    }, R.color.yellow_f4ac3e
                )
                binding.tvVideoTitle.setTextColor(
                    Utils.parseColor(
                        courseData.demoVideo.textColor.orDefaultValue(
                            "#ffffff"
                        )
                    )
                )
                binding.tvVideoTitleTwo.setTextColor(
                    Utils.parseColor(
                        courseData.demoVideo.textColor.orDefaultValue(
                            "#ffffff"
                        )
                    )
                )
                binding.tvVideoTitle.text = courseData.demoVideo.bottomTitle.orEmpty()
                binding.tvVideoTitleTwo.text = courseData.demoVideo.bottomSubTitle.orEmpty()
                binding.buttonTryNowVideo.text = courseData.demoVideo.buttonText.orEmpty()
                binding.buttonTryNowVideo.setOnClickListener {
                    if (!courseData.demoVideo.buttonDeeplink.isNullOrBlank()) {
                        deeplinkAction.performAction(
                            requireContext(),
                            courseData.demoVideo.buttonDeeplink.orEmpty()
                        )
                        val event = AnalyticsEvent(
                            EventConstants.BUY_NOW_CLICK,
                            hashMapOf(
                                EventConstants.ASSORTMENT_ID to assortmentId,
                                EventConstants.SOURCE to TAG
                            )
                        )
                        val event2 = AnalyticsEvent(
                            EventConstants.BUY_NOW_CLICK + "_v2",
                            hashMapOf(
                                EventConstants.ASSORTMENT_ID to assortmentId,
                                EventConstants.SOURCE to TAG
                            )
                        )
                        analyticsPublisher.publishEvent(event)
                        analyticsPublisher.publishMoEngageEvent(event)
                        val countToSendEvent: Int = Utils.getCountToSend(
                            RemoteConfigUtils.getEventInfo(),
                            EventConstants.BUY_NOW_CLICK
                        )
                        val eventCopy = event.copy()
                        val event2Copy = event2.copy()
                        repeat((0 until countToSendEvent).count()) {
                            analyticsPublisher.publishBranchIoEvent(eventCopy)
                            analyticsPublisher.publishBranchIoEvent(event2Copy)
                        }
                    } else {
                        viewModel.activateTrial(assortmentId)
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.COURSE_TRIAL_CLICK,
                                hashMapOf(
                                    EventConstants.ASSORTMENT_ID to assortmentId,
                                    EventConstants.SOURCE to TAG
                                ), ignoreBranch = false
                            )
                        )
                    }
                }
            } else {
                binding.videoLayout.hide()
                binding.videoBottomView.hide()
            }
        }

        buttonInfo = courseData.buttonInfo
        handleBottomButtonView(courseData.viewPlanBtnData)

        if (courseData.isVip == true && (courseData.isTrial == null || courseData.isTrial == false)
            && courseData.extraWidgets.isNullOrEmpty()
        ) {
            childFragmentManager.beginTransaction().replace(
                R.id.scheduleContainer,
                CourseScheduleFragment.newInstance(
                    assortmentId = assortmentId,
                    source = source
                )
            ).commitNowAllowingStateLoss()
            binding.viewpager.visibility = View.GONE
            binding.tabLayout.visibility = View.GONE
            binding.scheduleContainer.visibility = View.VISIBLE
        } else if (!courseData.tabList.isNullOrEmpty()) {
            binding.viewpager.visibility = View.VISIBLE
            binding.tabLayout.visibility = View.VISIBLE
            binding.scheduleContainer.visibility = View.GONE
            binding.viewpager.adapter =
                CoursePagerAdapter(childFragmentManager, courseData.tabList, assortmentId, "")
            binding.tabLayout.setupWithViewPager(binding.viewpager)
            binding.tabLayout.addOnTabSelectedListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.NCP_TAB_TAPPED,
                        hashMapOf(
                            EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID to assortmentId,
                            EventConstants.TAB_NAME to it.text.toString()
                        ), ignoreBranch = false
                    )
                )
            }
        } else {
            binding.viewpager.visibility = View.GONE
            binding.tabLayout.visibility = View.GONE
            binding.scheduleContainer.visibility = View.GONE
        }

        if (courseData.fabData?.deeplink.isNullOrBlank()) {
            binding.chatBuddyAnimation.hide()
        } else {
            if (!courseData.fabData?.imageUrl.isNullOrEmpty()) {
                binding.chatBuddyAnimation.loadImageEtx(
                    url = courseData.fabData?.imageUrl.orEmpty(),
                    format = DecodeFormat.PREFER_ARGB_8888
                )
            }
            binding.chatBuddyAnimation.show()
            binding.chatBuddyAnimation.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.NCP_HELP_CLICKED,
                        hashMapOf(
                            EventConstants.ASSORTMENT_ID to assortmentId,
                            EventConstants.VIP_USER to isVip
                        )
                    )
                )
                deeplinkAction.performAction(
                    requireContext(),
                    courseData.fabData?.deeplink.orEmpty()
                )
            }
        }

        if (!courseData.popUpDeeplink.isNullOrBlank()) {
            deeplinkAction.performAction(requireContext(), courseData.popUpDeeplink.orEmpty())
        }

        if (courseData.chatText.isNullOrEmpty()) {
            binding.tvChat.hide()
        } else {
            binding.tvChat.show()
            binding.tvChat.text = courseData.chatText
        }

        if (courseData.courseHelpData != null) {
            CourseHelpDialogFragment.newInstance(courseData.courseHelpData)
                .show(childFragmentManager, TAG)
        }

        binding.ivOverflow.setVisibleState(!courseData.supportData.isNullOrEmpty())
    }

    private fun setVideoAutoPlay(delay: Long) {
        timerDisposable.add(
            Observable.timer(delay, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Long>() {
                    override fun onComplete() {
                        timerDisposable.clear()
                        initAndPlayVideo()
                    }

                    override fun onNext(t: Long) {

                    }

                    override fun onError(e: Throwable) {
                        timerDisposable.clear()
                    }
                })
        )
    }

    private fun onActivateTrialSuccess(data: ActivateTrialData) {
        isTrialActivated = true
        showToast(requireContext(), data.message.orEmpty())
        adapter.clearData()
        extraAdapter.clearData()
        viewModel.getAllCoursesData(assortmentId, subject, studentClass, 1, source)
        getConfigData()
    }

    private fun getConfigData() {
        val postPurchaseSessionCount =
            defaultPrefs().getInt(Constants.POST_PURCHASE_SESSION_COUNT, 0)
        val sessionCount = defaultPrefs().getInt(Constants.SESSION_COUNT, 1)
        viewModel.getConfigData(sessionCount, postPurchaseSessionCount)
    }

    private fun onStoriesSuccess(data: Widgets) {
        if (!data.widgets.isNullOrEmpty()) {
            if (adapter.itemCount > 0) {
                adapter.addWidgetToPosition(data.widgets[0], 0)
            } else {
                adapter.setWidgets(mutableListOf(data.widgets[0]))
            }
        }
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        showApiErrorToast(requireContext())
    }

    private fun unAuthorizeUserError() {
        showApiErrorToast(requireContext())
    }

    private fun updateProgress(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    override fun onDestroy() {
        super.onDestroy()
        rxBusEventObserver?.dispose()
        timerDisposable.dispose()
        compositeDisposable.dispose()
    }

    private fun setAppBarScrollListener() {
        compositeDisposable + binding.appBarLayout.onTopReachedListener()
            .debounce(300, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .switchMap {
                Observable.just(it)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                manageVisibility(it)
            }

        compositeDisposable + binding.appBarLayout.onTopReachedAfterScrollDownListener()
            .debounce(300, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .switchMap {
                Observable.just(it)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it) {
                    if (binding.bottomBar.isVisible && !isVip) {
                        showPrePurchaseCallingCard()
                    }
                }
            }
    }

    private fun manageVisibility(topReached: Boolean) {
        if (topReached) {
            if (hasToHandleBottomBarVisibility) {
                if (RecyclerViewUtils.isRecyclerScrollable(binding.rvExtraWidgets)) {
                    binding.bottomBar.isVisible = false
                }
            }
            if (hasToHandleVideoBottomVisibility) {
                binding.videoBottomView.isVisible = true
            }
        } else {
            if (hasToHandleBottomBarVisibility) {
                binding.bottomBar.isVisible = true
            }
            if (hasToHandleVideoBottomVisibility) {
                binding.videoBottomView.isVisible = false
            }
        }
    }

    private fun showPrePurchaseCallingCard() {
        if (defaultPrefs().getString(Constants.TITLE_PROBLEM_PURCHASE, "").isNullOrEmpty()) {
            return
        }
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
            ).orEmpty().contains(assortmentId)
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
                }-$assortmentId"
            )
        }
        val chatDeepLink = if (callingCardChatDeeplink.isNullOrEmpty()) {
            defaultPrefs().getString(
                Constants.CHAT_DEEPLINK,
                ""
            )
        } else {
            callingCardChatDeeplink
        }
        binding.prePurchaseCallingCard.show()
        binding.prePurchaseCallingCard.bindWidget(
            holder = binding.prePurchaseCallingCard.widgetViewHolder,
            model = PrePurchaseCallingCardModel2().apply {
                _data = PrePurchaseCallingCardData2(
                    title = defaultPrefs().getString(
                        Constants.TITLE_PROBLEM_PURCHASE,
                        ""
                    ),
                    titleTextSize = PrePurchaseCallingCard2.titleTextSize(),
                    titleTextColor = PrePurchaseCallingCard2.titleTextColor(),
                    subtitle = defaultPrefs().getString(
                        Constants.SUBTITLE_PROBLEM_PURCHASE,
                        ""
                    ),
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
                    source = Constants.SOURCE_COURSE_DETAIL,
                )
            }
        )
    }

    private fun sharePdf(filePath: String) {
        val currentContext = context ?: return
        if (FileUtils.isFilePresent(filePath)) {
            val pdfUri =
                FileProvider.getUriForFile(currentContext, BuildConfig.AUTHORITY, File(filePath))
            Intent(Intent.ACTION_SEND).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = "application/pdf"
                `package` = "com.whatsapp"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
            }.also {
                if (AppUtils.isCallable(currentContext, it)) {
                    startActivity(it)
                } else {
                    ToastUtils.makeText(
                        currentContext,
                        R.string.string_install_whatsApp,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        } else {
            toast(getString(R.string.pdf_file_not_present))
        }
    }

    private fun saveFile(pdfDownloadUrl: String) {
        val name = FileUtils.fileNameFromUrl(pdfDownloadUrl) + ".pdf"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, name)
        }
        try {
            startActivityForResult(intent, 1211)
        } catch (e: Exception) {
            Log.e(e)
            showApiErrorToast(requireContext())
        }
    }

    private fun sendPageTypeView(pageType: String, assortmentId: String?) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.PAGE_VIEW + pageType,
                hashMapOf(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID to assortmentId.orEmpty()),
                ignoreBranch = false, ignoreMoengage = false
            )
        )
    }

    override fun onStart() {
        super.onStart()
        enterTime = System.currentTimeMillis()
    }

    override fun onStop() {
        super.onStop()
        if (!isVip && activity is CourseActivityV3) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COURSE_PAGE_EXIT,
                    hashMapOf(
                        EventConstants.ENGAGEMENT_TIME to System.currentTimeMillis() - enterTime,
                        EventConstants.ASSORTMENT_ID to assortmentId
                    )
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appStateObserver?.dispose()
    }
}