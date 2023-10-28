package com.doubtnutapp.leaderboard.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.StickyHeadersLinearLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.Dismiss
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.ActivityLeaderboardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.leaderboard.data.entity.LeaderboardData
import com.doubtnutapp.leaderboard.data.entity.LeaderboardHelp
import com.doubtnutapp.leaderboard.event.OnLeaderboardPersonalWidgetItemClick
import com.doubtnutapp.leaderboard.event.OnTabSelected
import com.doubtnutapp.leaderboard.ui.dialog.LeaderboardHelpBottomSheetDialogFragment
import com.doubtnutapp.leaderboard.widget.*
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.uxcam.UXCam
import dagger.android.AndroidInjection
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import kotlin.math.min

class LeaderboardActivity : BaseActivity(), ActionPerformer {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var binding: ActivityLeaderboardBinding
    private lateinit var viewModel: LeaderboardActivityVM

    private val source by lazy { intent.getStringExtra(Constants.SOURCE).orEmpty() }
    private val type by lazy { intent.getStringExtra(Constants.TYPE).orEmpty() }
    private val assortmentId by lazy {
        intent.getStringExtra(Constants.ASSORTMENT_ID).orEmpty()
    }
    private val testId by lazy { intent.getStringExtra(Constants.TEST_ID).orEmpty() }

    private var selectedTab: String? = null
    private var leaderBoardData: LeaderboardData? = null

    private var indexOfLeaderboardWidgetForCurrentUser = -1

    private var appStateObserver: Disposable? = null

    private val bottomSheetBehaviour by lazy { BottomSheetBehavior.from(binding.rvWidgetsBottom) }

    companion object {
        @Suppress("unused")
        private const val TAG = "LeaderboardActivity"

        fun getStartIntent(
            context: Context,
            source: String,
            assortmentId: String?,
            testId: String?,
            type: String?,
        ) =
            Intent(context, LeaderboardActivity::class.java).apply {
                putExtra(Constants.SOURCE, source)
                putExtra(Constants.ASSORTMENT_ID, assortmentId)
                putExtra(Constants.TEST_ID, testId)
                putExtra(Constants.TYPE, type)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        initListeners()
        initObservers()

        if (type.contains("paid_user_championship")) {
            viewModel.getPaidUserChampionshipLeaderboard(source, assortmentId, testId, type)
        } else {
            viewModel.getTestLeaderboardData(source, assortmentId, testId, type)
        }
    }

    fun init() {
        viewModel = viewModelProvider(viewModelFactory)
        binding.rvWidgets.layoutManager =
            StickyHeadersLinearLayoutManager<WidgetLayoutAdapter>(this)

        (binding.rvWidgetsBottom.adapter as? WidgetLayoutAdapter)?.actionPerformer = this
    }

    private fun initListeners() {
        binding.ivBack.setOnClickListener { onBackPressed() }
    }

    private fun initObservers() {
        appStateObserver = DoubtnutApp.INSTANCE
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is OnTabSelected -> {
                        selectedTab = it.id
                        setUpLeaderboard()
                        updateBottomData()
                    }
                    is OnLeaderboardPersonalWidgetItemClick -> {
                        if (it.item.type == LeaderboardPersonalDataItem.TYPE_MY_RANK) {
                            val adapter =
                                binding.rvWidgets.adapter as? WidgetLayoutAdapter
                                    ?: return@subscribe

                            if (indexOfLeaderboardWidgetForCurrentUser in 0..adapter.widgets.lastIndex)
                                binding.rvWidgets.smoothScrollToPosition(
                                    indexOfLeaderboardWidgetForCurrentUser
                                )
                        }
                    }
                }
            }


        viewModel.widgetsLiveData.observe(this) { outcome ->
            when (outcome) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = outcome.loading
                }
                is Outcome.Success -> {
                    binding.progressBar.isVisible = false

                    outcome.data.stickyWidgets?.forEach {
                        if (it.extraParams == null) {
                            it.extraParams = HashMap()
                        }
                        it.extraParams?.put(EventConstants.TYPE, type)
                    }

                    outcome.data.widgets?.forEach {
                        if (it.extraParams == null) {
                            it.extraParams = HashMap()
                        }
                        it.extraParams?.put(EventConstants.TYPE, type)
                    }

                    outcome.data.bottomData?.widgets?.forEach {
                        if (it.extraParams == null) {
                            it.extraParams = HashMap()
                        }
                        it.extraParams?.put(EventConstants.TYPE, type)
                    }

                    outcome.data.bottomTabsData?.forEach { leaderboardBottomData ->
                        leaderboardBottomData.widgets?.forEach {
                            if (it.extraParams == null) {
                                it.extraParams = HashMap()
                            }
                            it.extraParams?.put(EventConstants.TYPE, type)
                        }
                    }

                    binding.root.applyBackgroundColor(outcome.data.background)

                    binding.ivBack.applyImageTintList(outcome.data.backIconColor, "#273de9")

                    binding.tvToolbarTitle.text = outcome.data.title
                    binding.tvToolbarTitle.applyTextColor(outcome.data.titleTextColor)
                    binding.tvToolbarTitle.applyTextSize(outcome.data.titleTextSize)

                    binding.btnAction.text = outcome.data.buttonCtaText
                    binding.btnAction.isVisible = !outcome.data.buttonCtaText.isNullOrEmpty()
                    binding.btnAction.setOnClickListener {
                        analyticsPublisher.publishEvent(
                            hashMapOf<String, Any>(
                                EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                                EventConstants.ASSORTMENT_ID to assortmentId,
                                EventConstants.TEST_ID to testId
                            ).let {
                                AnalyticsEvent(
                                    EventConstants.TEST_LEADERBOARD_GOTO_TESTS_CLICK,
                                    it,
                                    ignoreSnowplow = true
                                )
                            }
                        )
                        outcome.data.deeplink
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { deepLink ->
                                deeplinkAction.performAction(this, deepLink)
                            }
                    }

                    outcome.data.leaderboardHelp?.let { leaderboardHelp ->
                        binding.ivHelp.loadImage(leaderboardHelp.icon)
                        binding.ivHelp.isVisible = leaderboardHelp.icon.isNullOrEmpty().not()

                        binding.tvHelp.isVisible = leaderboardHelp.title.isNullOrEmpty().not()
                        binding.tvHelp.text = leaderboardHelp.title
                        binding.tvHelp.applyTextColor(leaderboardHelp.titleTextColor)
                        binding.tvHelp.applyTextSize(leaderboardHelp.titleTextSize)

                        listOf(binding.ivHelp, binding.tvHelp).forEach {
                            it.setOnClickListener {
                                leaderboardHelp.titleDeeplink?.let {
                                    deeplinkAction.performAction(
                                        this,
                                        leaderboardHelp.titleDeeplink
                                    )
                                    return@setOnClickListener
                                }
                                showLeaderboardHelpBottomSheetDialogFragment(leaderboardHelp)
                            }
                        }
                    }

                    selectedTab = (
                            outcome.data.widgets
                                ?.firstOrNull { it.type == WidgetTypes.TYPE_WIDGET_LEADERBOARD_TAB }
                                ?.data as? LeaderboardTabData
                            )
                        ?.items
                        ?.firstOrNull()
                        ?.id
                        // If id not found in widgets then find in stickyWidgets.
                        ?: (
                                outcome.data.stickyWidgets
                                    ?.firstOrNull { it.type == WidgetTypes.TYPE_WIDGET_LEADERBOARD_TAB }
                                    ?.data as? LeaderboardTabData
                                )
                            ?.items
                            ?.firstOrNull()
                            ?.id


                    binding.rvStickyWidgets.isVisible =
                        outcome.data.stickyWidgets.isNullOrEmpty().not()

                    (binding.rvStickyWidgets.adapter as? WidgetLayoutAdapter)
                        ?.setWidgets(outcome.data.stickyWidgets.orEmpty())

                    leaderBoardData = outcome.data
                    setUpLeaderboard()
                    updateBottomData()
                }
                else -> {
                    // Do nothing
                }
            }
        }
    }

    private fun updateBottomData() {
        val data = leaderBoardData?.bottomTabsData?.firstOrNull { it.tab == selectedTab }
            ?: leaderBoardData?.bottomData

        data?.let {
            binding.rvWidgetsBottom.applyBackgroundTintList(
                it.background,
                "#541488"
            )
            val behavior = BottomSheetBehavior.from(binding.rvWidgetsBottom)
            behavior.peekHeight = (it.peekHeight ?: 100).dpToPx()
            (binding.rvWidgetsBottom.adapter as? WidgetLayoutAdapter)
                ?.setWidgets(it.widgets.orEmpty())
        }

        binding.rvWidgetsBottom.isVisible = data != null
    }

    private fun showLeaderboardHelpBottomSheetDialogFragment(leaderboardHelp: LeaderboardHelp) {
        analyticsPublisher.publishEvent(
            hashMapOf<String, Any>(
                EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                EventConstants.ASSORTMENT_ID to assortmentId,
                EventConstants.TEST_ID to testId
            ).let {
                AnalyticsEvent(
                    EventConstants.TEST_LEADERBOARD_HELP_CLICK,
                    it,
                    ignoreSnowplow = true
                )
            }
        )
        LeaderboardHelpBottomSheetDialogFragment.newInstance(
            leaderboardHelp,
            assortmentId,
            testId
        )
            .show(
                supportFragmentManager,
                LeaderboardHelpBottomSheetDialogFragment.TAG
            )
    }

    private fun setUpLeaderboard() {
        val adapter =
            binding.rvWidgets.adapter as? WidgetLayoutAdapter ?: return

        val widgets: ArrayList<WidgetEntityModel<*, *>> = arrayListOf()

        var hasTabs = false

        leaderBoardData?.widgets?.forEach { widgetEntityModel ->
            when (widgetEntityModel.type) {
                WidgetTypes.TYPE_WIDGET_LEADERBOARD_TAB -> {
                    hasTabs = true
                    (widgetEntityModel.data as? LeaderboardTabData)
                        ?.let { data ->
                            data.assortmentId = assortmentId
                            data.testId = testId
                            data.items?.forEach { item ->
                                item.isSelected = item.id == selectedTab
                            }
                        }

                    widgets.add(widgetEntityModel)
                }
                WidgetTypes.TYPE_WIDGET_LEADERBOARD_PERSONAL -> {
                    if (widgetEntityModel is LeaderboardPersonalModel && !widgetEntityModel.data.items.isNullOrEmpty()) {
                        val groupByTab = widgetEntityModel.data.items?.groupBy { it.tab }
                        groupByTab?.get(selectedTab)?.let { items ->
                            widgets.add(
                                LeaderboardPersonalModel().apply {
                                    _type =
                                        WidgetTypes.TYPE_WIDGET_LEADERBOARD_PERSONAL
                                    _data = LeaderboardPersonalData(
                                        margin = true,
                                        items = items,
                                        assortmentId = assortmentId,
                                        testId = testId,
                                    )
                                }
                            )
                        }
                    }
                }

                WidgetTypes.TYPE_WIDGET_LEADERBOARD -> {
                    if (widgetEntityModel is LeaderBoardWidgetModel && !widgetEntityModel.data.items.isNullOrEmpty()) {
                        val groupByTab = widgetEntityModel.data.items?.groupBy { it.tab }
                        if (selectedTab == null) {
                            selectedTab = groupByTab?.entries?.firstOrNull()?.key
                        }
                        groupByTab?.get(selectedTab)
                            ?.let { items ->
                                if (items.isNotEmpty()) {
                                    widgets.add(
                                        LeaderboardTopThreeWidgetModel().apply {
                                            _type =
                                                WidgetTypes.TYPE_WIDGET_LEADERBOARD_TOP_THREE
                                            _data = LeaderBoardWidgetData(
                                                margin = true,
                                                item = null,
                                                items = items.subList(0, min(3, items.size)),
                                                assortmentId = assortmentId,
                                                testId = testId
                                            )
                                            extraParams = widgetEntityModel.extraParams
                                        }
                                    )

                                    if (items.size > 3) {
                                        val studentId = UserUtil.getStudentId()
                                        for (index in 3..items.lastIndex) {
                                            val item = items[index]

                                            widgets.add(
                                                LeaderBoardWidgetModel().apply {
                                                    _type =
                                                        WidgetTypes.TYPE_WIDGET_LEADERBOARD
                                                    _data = LeaderBoardWidgetData(
                                                        margin = true,
                                                        item = item,
                                                        items = null,
                                                        assortmentId = assortmentId,
                                                        testId = testId
                                                    )
                                                }
                                            )
                                            if (item.studentId == studentId) {
                                                indexOfLeaderboardWidgetForCurrentUser =
                                                    widgets.lastIndex
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
                else -> widgets.add(widgetEntityModel)
            }
        }
        adapter.clearData()
        adapter.addWidgets(widgets)

        if (!hasTabs) {
            analyticsPublisher.publishEvent(
                hashMapOf<String, Any>(
                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    EventConstants.ASSORTMENT_ID to assortmentId,
                    EventConstants.TEST_ID to testId,
                    EventConstants.TAB_NAME to "NA",
                    EventConstants.SOURCE to EventConstants.SOURCE,
                ).let {
                    AnalyticsEvent(
                        EventConstants.TEST_LEADERBOARD_TAB_VIEW,
                        it,
                        ignoreSnowplow = true
                    )
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (type == "paid_user_championship") {
            UXCam.tagScreenName("paid_user_championship")
        } else {
            UXCam.tagScreenName(TAG)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        appStateObserver?.dispose()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (bottomSheetBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) {
                val outRect = Rect()
                binding.rvWidgetsBottom.getGlobalVisibleRect(outRect)
                if (!outRect.contains(
                        event.rawX.toInt(),
                        event.rawY.toInt()
                    )
                ) {
                    bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun performAction(action: Any) {
        when (action) {
            is Dismiss -> {
                if (action.from == LeaderBoardWidget.TAG
                    && bottomSheetBehaviour.state == BottomSheetBehavior.STATE_COLLAPSED
                ) {
                    bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
                    return
                }
                bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            else -> {
            }
        }
    }
}