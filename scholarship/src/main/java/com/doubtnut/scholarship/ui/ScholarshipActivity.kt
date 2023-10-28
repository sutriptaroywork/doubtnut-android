package com.doubtnut.scholarship.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.sharing.IWhatsAppSharing
import com.doubtnut.core.sharing.entities.BranchShareData
import com.doubtnut.core.sharing.entities.ShareOnApp
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnut.core.ui.base.CoreBindingActivity
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.IWidgetLayoutAdapter
import com.doubtnut.scholarship.R
import com.doubtnut.scholarship.data.entity.ScholarshipBottomData
import com.doubtnut.scholarship.databinding.ActivityScholarshipBinding
import com.doubtnut.scholarship.event.ChangeTest
import com.doubtnut.scholarship.event.RegisterTestEvent
import com.doubtnut.scholarship.event.UpdateBottomData
import com.doubtnut.scholarship.widget.ProgressWidgetModel
import com.doubtnut.scholarship.widget.RegisterTestWidgetModel
import com.doubtnut.scholarship.widget.ScholarshipTabsWidgetModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

class ScholarshipActivity :
    CoreBindingActivity<ScholarshipActivityVM, ActivityScholarshipBinding>(),
    ActionPerformer {

    private val testId: String by lazy { intent?.getStringExtra(TEST_ID).orEmpty() }
    private var changeTest = false

    private var appStateObserver: Disposable? = null
    var restartJob: Job? = null

    @Inject
    lateinit var whatsAppSharing: IWhatsAppSharing

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    override fun provideViewBinding(): ActivityScholarshipBinding {
        return ActivityScholarshipBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): ScholarshipActivityVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.colorSecondaryDark
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.widgetsLiveData.observe(this) { outcome ->
            when (outcome) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = outcome.loading
                    binding.root.isRefreshing = outcome.loading
                }
                is Outcome.Success -> {
                    changeTest = false
                    binding.progressBar.isVisible = false
                    binding.root.isRefreshing = false

                    restartJob?.cancel()
                    restartJob = lifecycleScope.launchWhenResumed {
                        if (outcome.data.startTimeInMillis != null && outcome.data.startTimeInMillis!! > 0L) {
                            delay(outcome.data.startTimeInMillis!!)
                            viewModel.getScholarshipData(testId, changeTest)
                        }
                    }

                    binding.root.applyBackgroundColor(outcome.data.bgColor)
                    binding.appBarLayout.applyBackgroundColor(outcome.data.bgColor)

                    binding.tvToolbarTitle.text = outcome.data.title
                    binding.tvToolbarTitle.applyTextColor(outcome.data.titleTextColor)
                    binding.tvToolbarTitle.applyTextSize(outcome.data.titleTextSize)

                    binding.rvStickyWidgets.isVisible =
                        outcome.data.stickyWidgets.isNullOrEmpty().not()

                    (binding.rvStickyWidgets.adapter as? IWidgetLayoutAdapter)
                        ?.setWidgets(outcome.data.stickyWidgets.orEmpty())

                    listOf(
                        outcome.data.stickyWidgets,
                        outcome.data.widgets,
                        outcome.data.footerWidgets
                    )
                        .forEach { listItem ->
                            listItem?.forEach { widgetEntityModel ->
                                if (widgetEntityModel.extraParams == null) {
                                    widgetEntityModel.extraParams = HashMap()
                                }
                                when (widgetEntityModel) {
                                    is ScholarshipTabsWidgetModel -> {
                                        widgetEntityModel.data.items?.forEach { scholarshipTabsWidgetItem ->
                                            scholarshipTabsWidgetItem.widgets?.filterNotNull()
                                                ?.forEach {
                                                    if (it.extraParams == null) {
                                                        it.extraParams = HashMap()
                                                    }
                                                    it.extraParams?.put(
                                                        EventConstants.SCHOLARSHIP_TEST_ID,
                                                        outcome.data.scholarshipTestId.orEmpty()
                                                    )
                                                    it.extraParams?.put(
                                                        EventConstants.TEST_ID,
                                                        outcome.data.testId.orEmpty()
                                                    )
                                                }
                                        }
                                    }
                                    is RegisterTestWidgetModel -> {
                                        widgetEntityModel.data.items?.forEach { items ->
                                            items.items?.forEach {
                                                if (it.extraParams == null) {
                                                    it.extraParams = HashMap()
                                                }
                                                it.extraParams?.put(
                                                    EventConstants.SCHOLARSHIP_TEST_ID,
                                                    outcome.data.scholarshipTestId.orEmpty()
                                                )
                                                it.extraParams?.put(
                                                    EventConstants.TEST_ID,
                                                    outcome.data.testId.orEmpty()
                                                )
                                            }
                                        }
                                    }
                                }
                                widgetEntityModel.extraParams?.put(
                                    EventConstants.SCHOLARSHIP_TEST_ID,
                                    outcome.data.scholarshipTestId.orEmpty()
                                )
                                widgetEntityModel.extraParams?.put(
                                    EventConstants.TEST_ID,
                                    outcome.data.testId.orEmpty()
                                )
                            }
                        }

                    listOf(outcome.data.stickyWidgets, outcome.data.widgets)
                        .forEach { listItem ->
                            listItem?.forEach { widgetEntityModel ->
                                if (widgetEntityModel is ProgressWidgetModel) {
                                    var isCurrentUpdate = false
                                    widgetEntityModel.data.items?.forEachIndexed { index, progressWidgetItem ->
                                        if (index == 0) progressWidgetItem.isFirst = true
                                        if (index == widgetEntityModel.data.items?.lastIndex ?: 0) progressWidgetItem.isLast =
                                            true
                                        if (!isCurrentUpdate && progressWidgetItem.isSelected == false) {
                                            progressWidgetItem.isCurrent = true
                                            isCurrentUpdate = true
                                        }
                                    }
                                }
                            }
                        }

                    (binding.rvWidgets.adapter as? IWidgetLayoutAdapter)
                        ?.setWidgets(outcome.data.widgets.orEmpty())

                    binding.rvFooterWidgets.isVisible =
                        outcome.data.footerWidgets.isNullOrEmpty().not()

                    (binding.rvFooterWidgets.adapter as? IWidgetLayoutAdapter)
                        ?.setWidgets(outcome.data.footerWidgets.orEmpty())

                    binding.rvWidgetsBottom.isVisible =
                        outcome.data.bottomData.isNullOrEmpty().not()

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.VIEWED}",
                            hashMapOf<String, Any>(
                                EventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                                EventConstants.SCHOLARSHIP_TEST_ID to outcome.data.scholarshipTestId.orEmpty(),
                                EventConstants.TEST_ID to outcome.data.testId.orEmpty(),
                                EventConstants.PROGRESS to outcome.data.progress.orEmpty(),
                                EventConstants.CHANGE_TEST to changeTest,
                            ).apply {
                                putAll(outcome.data.extraParams.orEmpty())
                            }
                        )
                    )

                }
                else -> {
                    // Do nothing
                }
            }
        }

        viewModel.registerTestLiveData.observe(this) { outcome ->
            when (outcome) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = outcome.loading
                }
                is Outcome.Success -> {
                    binding.progressBar.isVisible = false
                    if (outcome.data.message.isNullOrEmpty().not()) {
                        toast(outcome.data.message.orEmpty())
                    }
                    viewModel.getScholarshipData(testId, changeTest)
                }
                else -> {
                }
            }
        }

        appStateObserver = CoreApplication.INSTANCE
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is RegisterTestEvent -> {
                        viewModel.registerScholarshipTest(it.testId)
                    }
                }
            }
    }

    override fun setupView(savedInstanceState: Bundle?) {
        changeTest = intent?.getBooleanExtra(CHANGE_TEST, false) ?: false

        viewModel.getScholarshipData(testId, changeTest)

        binding.ivBack.setOnClickListener { onBackPressed() }
        binding.root.setOnRefreshListener {
            viewModel.getScholarshipData(testId)
        }

        binding.appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            binding.root.isEnabled = verticalOffset == 0
        })

        (binding.rvStickyWidgets.adapter as? IWidgetLayoutAdapter)?.actionPerformer = this
        (binding.rvWidgets.adapter as? IWidgetLayoutAdapter)?.actionPerformer = this
        (binding.rvFooterWidgets.adapter as? IWidgetLayoutAdapter)?.actionPerformer = this
        (binding.rvWidgetsBottom.adapter as? IWidgetLayoutAdapter)?.actionPerformer = this
    }

    companion object {
        private const val TAG = "ScholorshipActivity"
        private const val EVENT_TAG = "scholorship_activity"

        private const val TEST_ID = "test_id"
        const val CHANGE_TEST = "change_test"

        fun getStartIntent(
            context: Context,
            testId: String,
            changeTest: Boolean
        ) =
            Intent(context, ScholarshipActivity::class.java).apply {
                putExtra(TEST_ID, testId)
                putExtra(CHANGE_TEST, changeTest)
            }
    }

    override fun performAction(action: Any) {
        when (action) {
            is UpdateBottomData -> {
                updateBottomData(action.data as? ScholarshipBottomData)
            }
            is ChangeTest -> {
                changeTest = true
                viewModel.getScholarshipData(testId, changeTest)
            }
            is ShareOnApp -> {
                val data = action.data as? BranchShareData ?: return
                whatsAppSharing.shareOnWhatsApp(
                    ShareOnWhatApp(
                        channel = "",
                        featureType = "",
                        campaign = "",
                        imageUrl = data.shareImageUrl,
                        controlParams = hashMapOf(),
                        bgColor = "#00000000",
                        sharingMessage = data.shareMessage.orEmpty(),
                        questionId = "",
                        packageName = data.packageName,
                        appName = data.appName,
                        skipBranch = data.skipBranch,
                    )
                )
                whatsAppSharing.startShare(this)
            }
            else -> {
            }
        }
    }

    private fun updateBottomData(bottomData: ScholarshipBottomData?) {
        bottomData?.let {
            binding.rvWidgetsBottom.applyBackgroundTintList(
                bottomData.background,
                "#541488"
            )
            val behavior = BottomSheetBehavior.from(binding.rvWidgetsBottom)
            behavior.peekHeight = (bottomData.peekHeight ?: 100).dpToPx()
            (binding.rvWidgetsBottom.adapter as? IWidgetLayoutAdapter)
                ?.setWidgets(bottomData.widgets.orEmpty())
        }
        binding.rvWidgetsBottom.isVisible = bottomData != null
    }

    override fun onDestroy() {
        super.onDestroy()
        appStateObserver?.dispose()
        whatsAppSharing.dispose()
    }

}