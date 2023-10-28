package com.doubtnutapp.libraryhome.coursev3.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnut.core.utils.IntentUtils
import com.doubtnut.core.utils.RecyclerViewUtils
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.Constants.STUDENT_CLASS
import com.doubtnutapp.EventBus.PauseVideoPlayer
import com.doubtnutapp.EventBus.PlayAudioEvent
import com.doubtnutapp.EventBus.VideoSeekEvent
import com.doubtnutapp.EventBus.VipStateEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.ActivateVipTrial
import com.doubtnutapp.base.FilterSelectAction
import com.doubtnutapp.base.RefreshUI
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.course.widgets.PackageDetailWidgetItem
import com.doubtnutapp.course.widgets.ParentAutoplayWidget
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.databinding.ActivityCourseV3Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.libraryhome.coursev3.adapter.CoursePagerAdapter
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseViewModelV3
import com.doubtnutapp.liveclass.ui.SaleFragment
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showApiErrorToast
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.video.VideoFragmentListener
import com.doubtnutapp.video.VideoPlayerManager
import com.doubtnutapp.videoPage.model.VideoResource
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.FilterSubjectWidget
import com.doubtnutapp.widgets.LockableBottomSheetBehavior
import com.doubtnutapp.widgets.RowTextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.android.HasAndroidInjector
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.acitivity_course_bottom_sheet.*
import kotlinx.android.synthetic.main.dialog_emi.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CourseActivityBottomSheet : BaseBindingActivity<CourseViewModelV3, ActivityCourseV3Binding>(),
    HasAndroidInjector,
    ActionPerformer,
    VideoFragmentListener {

    companion object {
        private const val TAG = "CourseActivityBottomSheet"
        const val TITLE = "title"
        const val ASSORTMENT_ID = "id"
        const val DEFAULT_SUBJECT = "ALL"
        private const val REQUEST_CODE_LIVE_CHAT = 122

        fun startActivity(
            context: Context, start: Boolean = true, assortmentId: String,
            source: String?, studentClass: String? = null,
        ): Intent {
            return Intent(context, CourseActivityBottomSheet::class.java).apply {
                putExtra(ASSORTMENT_ID, assortmentId)
                putExtra(Constants.SOURCE, source.orEmpty())
                putExtra(Constants.STUDENT_CLASS, studentClass)
            }.also {
                if (start) context.startActivity(it)
            }
        }
    }

    private var chatTabIndex = -1
    private var buttonInfo: ButtonInfo? = null
    private var callData: CallData? = null
    private var isTrialActivated: Boolean = false
    private var refreshUI: Boolean = false
    private var isLaunched = false
    private var hasToHandleBottomBarVisibility: Boolean = false
    private var hasToHandleVideoBottomVisibility: Boolean = false

    private var isSheetLocked = false


    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    private lateinit var adapter: WidgetLayoutAdapter
    private lateinit var stickyWidgetsAdapter: WidgetLayoutAdapter
    private lateinit var extraAdapter: WidgetLayoutAdapter
    private var subject: String = DEFAULT_SUBJECT
    private var assortmentId: String = "0"
    private var studentClass: String? = null
    private var rxBusEventObserver: Disposable? = null
    private var shouldShowSaleDialog = false
    private var courseData: ApiCourseDataV3? = null
    var chatDeeplink = ""
    private var isSaleDialogShown = false
    private var nudgeId: Int = 0
    private var nudgeMaxCount: Int = 0

    @Inject
    lateinit var timerDisposable: CompositeDisposable

    private var videoPlayerManager: VideoPlayerManager? = null

    override fun provideViewBinding(): ActivityCourseV3Binding =
        ActivityCourseV3Binding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): CourseViewModelV3 =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        DoubtnutApp.INSTANCE.bus()?.send(PauseVideoPlayer)
        parentLayout.setOnClickListener {
            onBackPressed()
        }

        ivClose.setOnClickListener {
            onBackPressed()
        }


        bottomSheetBehavior = LockableBottomSheetBehavior.from(container)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_SETTLING
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    ivClose.hide()
                    (bottomSheetBehavior as LockableBottomSheetBehavior).setLocked(true)
                } else {
                    ivClose.show()
                }
                if (newState == BottomSheetBehavior.STATE_HIDDEN) finish()
            }
        })


        setUpObserver()
        setupListeners()
        assortmentId = intent.getStringExtra(ASSORTMENT_ID).orEmpty()
        initUi()
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.PAGE_VIEW + TAG,
                hashMapOf(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID to assortmentId)
            )
        )

        tvCall.setOnClickListener {
            if (callData != null) {
                try {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + callData!!.number)))
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
                        this,
                        callData!!.number
                    )
                }
            }
        }
    }

    private fun setupListeners() {
        videoInfo.setOnClickListener {
            initAndPlayVideo()
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

        videoInfo.hide()
        initVideoPlayer()
        var sourcePage = courseData?.demoVideo?.page.orEmpty()
        if (Constants.PAGE_SEARCH_SRP == intent?.getStringExtra(Constants.SOURCE)) {
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
        videoPlayerManager = VideoPlayerManager(supportFragmentManager,
            this, R.id.videoViewContainer, { _, _ -> })
    }

    override fun singleTapOnPlayerView() {
        if (videoPlayerManager?.videoFragment == null) return
        if (videoPlayerManager?.isPlayerControllerVisible == true) {
            videoPlayerManager?.hidePlayerController()
        } else if (videoPlayerManager?.isPlayerControllerVisible != true) {
            videoPlayerManager?.showPlayerController()
        }
    }

    override fun onBackPressed() {
        if (DoubtnutApp.INSTANCE.isOnboardingStarted) {
            DoubtnutApp.INSTANCE.isOnboardingCompleted = true
        }
        if (shouldShowSaleDialog && nudgeId != 0 && !isSaleDialogShown) {
            var dialogShownCount = defaultPrefs().getInt(Constants.NUDGE_COURSE_COUNT, 0)
            if (dialogShownCount < nudgeMaxCount) {
                SaleFragment.newInstance(nudgeId).show(supportFragmentManager, SaleFragment.TAG)
                isSaleDialogShown = true
                dialogShownCount++
                defaultPrefs().edit().putInt(Constants.NUDGE_COURSE_COUNT, dialogShownCount).apply()
            } else {
                if (isTrialActivated) {
                    (applicationContext as DoubtnutApp).bus()?.send(VipStateEvent(true))
                }
                super.onBackPressed()
            }
        } else {
            if (isTrialActivated) {
                (applicationContext as DoubtnutApp).bus()?.send(VipStateEvent(true))
            }
            super.onBackPressed()
        }
    }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.white_20)


    }

    override fun performAction(action: Any) {
        if (action is FilterSelectAction) {
            if (action.type == "subject") {
                subject = action.filterText ?: DEFAULT_SUBJECT
                viewModel.getAllCoursesData(assortmentId, subject, studentClass, 1)
            }
        } else if (action is ActivateVipTrial) {
            viewModel.activateTrial(action.assortmentId)
        } else if (action is RefreshUI) {
            refreshUI = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (refreshUI) {
            initUi()
            refreshUI = false
        }
    }

    private fun setUpObserver() {
        viewModel.courseLiveData.observeK(
            this@CourseActivityBottomSheet,
            this::onWidgetListFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
        viewModel.activateVipLiveData.observeK(
            this@CourseActivityBottomSheet,
            this::onActivateTrialSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        viewModel.widgetsLiveData.observeK(
            this@CourseActivityBottomSheet,
            this::onStoriesSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        viewModel.callData.observe(this, EventObserver {
            callData = it
            if (it == null) {
                tvCall.hide()
            } else {
                tvCall.show()
                tvCall.text = it.title
            }
        })

        rxBusEventObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is VipStateEvent) {
                if (event.state) {
                    if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                    isSheetLocked = true
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
                    val widget = rvExtraWidgets.layoutManager?.findViewByPosition(index)
                    if (widget != null && widget.isOnScreen && widget is ParentAutoplayWidget) {
                        widget.stopVideo()
                    }
                }
                videoPlayerManager?.pauseExoPlayer()
            }
        }
    }

    private fun handleBottomButtonView(buttonInfo: ButtonInfo?) {
        if (buttonInfo == null) {
            bottomBar.hide()
        } else {
            if (!courseData?.extraWidgets.isNullOrEmpty()) {
                hasToHandleBottomBarVisibility = true
            }
            bottomBar.show()
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
                                    EventConstants.ASSORTMENT_ID to assortmentId,
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
                deeplinkAction.performAction(this, buttonInfo.deeplink)
                val event = AnalyticsEvent(
                    EventConstants.COURSE_CLICK_BUY_NOW,
                    hashMapOf(
                        EventConstants.ASSORTMENT_ID to assortmentId,
                        EventConstants.VARIANT_ID to buttonInfo.variantId.orEmpty(),
                        EventConstants.MUlTIPLE_PACKAGE to multiplePackage
                    ), ignoreMoengage = false
                )
                val event2 = AnalyticsEvent(
                    EventConstants.COURSE_CLICK_BUY_NOW + "_v2",
                    hashMapOf(
                        EventConstants.ASSORTMENT_ID to assortmentId,
                        EventConstants.VARIANT_ID to buttonInfo.variantId.orEmpty(),
                        EventConstants.MUlTIPLE_PACKAGE to multiplePackage
                    )
                )
                analyticsPublisher.publishEvent(event)
                val countToSendEvent: Int = Utils.getCountToSend(
                    RemoteConfigUtils.getEventInfo(),
                    EventConstants.COURSE_CLICK_BUY_NOW
                )
                val eventCopy = event.copy()
                val event2Copy = event2.copy()
                repeat((0 until countToSendEvent).count()) {
                    analyticsPublisher.publishBranchIoEvent(eventCopy)
                    analyticsPublisher.publishBranchIoEvent(event2Copy)
                }
            }

            textViewPayInstallment.setOnClickListener {
                deeplinkAction.performAction(this, buttonInfo.installmentDeeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.COURSE_CLICK_EMI,
                        hashMapOf(
                            EventConstants.ASSORTMENT_ID to assortmentId,
                            EventConstants.VARIANT_ID to buttonInfo.variantIdInstallment.orEmpty()
                        )
                    )
                )
            }
        }
    }

    private fun initUi() {
        bottomBar.hide()
        studentClass = intent.getStringExtra(STUDENT_CLASS)
        if (studentClass == "") {
            studentClass = null
        }
        adapter = WidgetLayoutAdapter(this, this, intent.getStringExtra(Constants.SOURCE).orEmpty())
        rvWidgets.adapter = adapter

        extraAdapter =
            WidgetLayoutAdapter(this, this, intent.getStringExtra(Constants.SOURCE).orEmpty())
        rvExtraWidgets.adapter = extraAdapter

        stickyWidgetsAdapter =
            WidgetLayoutAdapter(this, this, intent.getStringExtra(Constants.SOURCE).orEmpty())
        rvStickyWidgets.adapter = stickyWidgetsAdapter

        viewModel.extraParams.apply {
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME, TAG)
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.SUBJECT, subject)
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID, assortmentId)
        }
        viewModel.getAllCoursesData(assortmentId, subject, studentClass, 1)
        //viewModel.getStories()
        setAppBarScrollListener()
    }

    private fun onWidgetListFetched(courseData: ApiCourseDataV3) {
        viewPager.visibility = View.VISIBLE
        videoPlayerManager?.resetVideo()
        videoPlayerManager?.removeVideoFromContainer()
        videoPlayerManager = null
        hasToHandleBottomBarVisibility = false
        hasToHandleVideoBottomVisibility = false
        timerDisposable.clear()
        this.courseData = courseData
        shouldShowSaleDialog = courseData.shouldShowSaleDialog ?: false
        nudgeId = courseData.nudgeId ?: 0
        nudgeMaxCount = courseData.nudgeCount ?: 0
        val savedNudgeId = defaultPrefs().getInt(Constants.NUDGE_ID_COURSE, 0)
        if (savedNudgeId == 0 || savedNudgeId != nudgeId) {
            defaultPrefs().edit().putInt(Constants.NUDGE_ID_COURSE, nudgeId).apply()
            defaultPrefs().edit().putInt(Constants.NUDGE_COURSE_COUNT, 0).apply()
        }
        adapter.addWidgets(courseData.widgets.orEmpty())
        if (courseData.courseList != null && courseData.courseList.size > 1) {
            dropdownCard.visibility = View.VISIBLE
            tvToolbarTitle.visibility = View.GONE
            cardTitle.text = courseData.toolbarTitle.orEmpty()
            dropdownCard.setOnClickListener {
                val menu = CourseSelectDropDownMenu(this, courseData.courseList)
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
                        cardTitle.text = data.display
                        assortmentId = data.id
                        viewPager.visibility = View.GONE
                        initUi()
                    }
                })
            }
        } else {
            dropdownCard.visibility = View.GONE
            tvToolbarTitle.visibility = View.VISIBLE
            tvToolbarTitle.text = courseData.toolbarTitle.orEmpty()
        }

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

        if (!courseData.shareMessage.isNullOrEmpty()) {
            tvShare.visibility = View.VISIBLE
            tvShare.loadImage(courseData.shareImageUrl)
            tvShare.setOnClickListener {
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
                whatsAppSharing.startShare(this)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.SHARE_COURSE_BUTTON_CLICK,
                        hashMapOf(
                            EventConstants.ASSORTMENT_ID to assortmentId,
                            EventConstants.STUDENT_ID to getStudentId()
                        )
                    )
                )
                analyticsPublisher.publishBranchIoEvent(
                    AnalyticsEvent(
                        EventConstants.SHARE_COURSE_BUTTON_CLICK,
                        hashMapOf(
                            EventConstants.ASSORTMENT_ID to assortmentId,
                            EventConstants.STUDENT_ID to getStudentId()
                        )
                    )
                )
            }
        } else {
            tvShare.visibility = View.GONE
        }

        if (courseData.stickyWidgets.isNullOrEmpty()) {
            rvStickyWidgets.hide()
        } else {
            stickyWidgetsAdapter.setWidgets(courseData.stickyWidgets.orEmpty())
            rvStickyWidgets.show()
        }

        if (courseData.extraWidgets.isNullOrEmpty()) {
            rvExtraWidgets.hide()
            videoLayout.hide()
            videoBottomView.hide()
            viewPager.show()
            sendPageTypeView(EventConstants.COURSE_PAGE_POST_PURCHASE)
        } else {
            sendPageTypeView(EventConstants.COURSE_PAGE_PRE_PURCHASE)
            rvExtraWidgets.show()
            buttonTryNowVideo.setVisibleState(!courseData.demoVideo?.bottomSubTitle.isNullOrBlank())
            viewPager.hide()
            extraAdapter.setWidgets(courseData.extraWidgets.orEmpty())
            val delay = courseData.demoVideo?.delay?.toLongOrNull()
            if (delay != null) {
                setVideoAutoPlay(delay)
            }

            if (courseData.demoVideo != null) {
                videoLayout.show()
                videoBottomView.show()
                if (courseData.demoVideo.videoResources.isNullOrEmpty()) {
                    playCourse.hide()
                } else {
                    playCourse.show()
                }
                hasToHandleVideoBottomVisibility = true
                videoInfo.loadBackgroundImage(
                    courseData.demoVideo.imageUrl.orEmpty(),
                    R.color.yellow_f4ac3e
                )
                videoBottomView.loadBackgroundImage(
                    if (courseData.demoVideo.imageUrlTwo.isNullOrBlank()) {
                        courseData.demoVideo.imageUrl.orEmpty()
                    } else {
                        courseData.demoVideo.imageUrlTwo.orEmpty()
                    }, R.color.yellow_f4ac3e
                )
                tvVideoTitle.setTextColor(
                    Utils.parseColor(
                        courseData.demoVideo.textColor.orDefaultValue(
                            "#ffffff"
                        )
                    )
                )
                tvVideoTitleTwo.setTextColor(
                    Utils.parseColor(
                        courseData.demoVideo.textColor.orDefaultValue(
                            "#ffffff"
                        )
                    )
                )
                tvVideoTitle.text = courseData.demoVideo.bottomTitle.orEmpty()
                tvVideoTitleTwo.text = courseData.demoVideo.bottomSubTitle.orEmpty()
                buttonTryNowVideo.text = courseData.demoVideo.buttonText.orEmpty()
                buttonTryNowVideo.setOnClickListener {
                    if (!courseData.demoVideo.buttonDeeplink.isNullOrBlank()) {
                        deeplinkAction.performAction(
                            this,
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
                videoLayout.hide()
                videoBottomView.hide()
            }
        }

        buttonInfo = courseData.buttonInfo
        handleBottomButtonView(courseData.buttonInfo)

        if (courseData.extraWidgets.isNullOrEmpty() && !courseData.tabList.isNullOrEmpty()) {
            viewPager.adapter = CoursePagerAdapter(
                supportFragmentManager,
                courseData.tabList,
                assortmentId,
                intent.getStringExtra(Constants.SOURCE).orEmpty()
            )
            if (DoubtnutApp.INSTANCE.isOnboardingStarted) {
                viewPager.offscreenPageLimit = 4
            }
        }

        if (!courseData.popUpDeeplink.isNullOrBlank()) {
            deeplinkAction.performAction(this, courseData.popUpDeeplink.orEmpty())
        }

        if (courseData.expanded == true) {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            isSheetLocked = true
        }
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
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        isSheetLocked = true
        isTrialActivated = true
        showToast(this, data.message.orEmpty())
        adapter.clearData()
        extraAdapter.clearData()
        viewModel.getAllCoursesData(assortmentId, subject, studentClass, 1)
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
        showApiErrorToast(this)
    }

    private fun unAuthorizeUserError() {
        showApiErrorToast(this)
    }

    private fun updateProgress(state: Boolean) {
        progressBar.setVisibleState(state)
    }

    override fun onDestroy() {
        super.onDestroy()
        rxBusEventObserver?.dispose()
        timerDisposable.dispose()
        compositeDisposable.dispose()
    }

    private fun showEmiDialog(emi: PackageDetailWidgetItem.Emi) {
        Dialog(this).apply {
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
                val textView = RowTextView(this@CourseActivityBottomSheet)
                textView.setViews(it.title.orEmpty(), it.amount.orEmpty(), null)
                layoutInstallment.addView(textView)
            }
        }
    }

    private fun setAppBarScrollListener() {
        compositeDisposable + appBarLayout.onTopReachedListener()
            .debounce(300, TimeUnit.MILLISECONDS)
            .distinctUntilChanged()
            .switchMap {
                Observable.just(it)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (!isSheetLocked && it && bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    (bottomSheetBehavior as LockableBottomSheetBehavior).setLocked(false)
                }
                manageVisibility(it)
            }
    }

    private fun manageVisibility(topReached: Boolean) {
        if (topReached) {
            if (hasToHandleBottomBarVisibility) {
                if (RecyclerViewUtils.isRecyclerScrollable(rvExtraWidgets)) {
                    bottomBar.isVisible = false
                }
            }
            if (hasToHandleVideoBottomVisibility) {
                videoBottomView.isVisible = true
            }
        } else {
            if (hasToHandleBottomBarVisibility) {
                bottomBar.isVisible = true
            }
            if (hasToHandleVideoBottomVisibility) {
                videoBottomView.isVisible = false
            }
        }
    }

    private fun sendPageTypeView(pageType: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.PAGE_VIEW + pageType,
                hashMapOf(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID to assortmentId),
                ignoreBranch = false
            )
        )
    }

}