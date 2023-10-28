package com.doubtnutapp.bottomsheet

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.core.view.forEachIndexed
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.Dismiss
import com.doubtnutapp.base.PerformDeeplinkAction
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.DialogBaseWidgetPaginatedBottomSheetBinding
import com.doubtnutapp.databinding.ItemTabFilterBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.adapter.BaseBindingBottomSheetDialogFragment
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgets.data.entities.BaseWidgetData
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.uxcam.UXCam
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class BaseWidgetPaginatedBottomSheetDialogFragment :
    BaseBindingBottomSheetDialogFragment<BaseWidgetPaginatedBottomSheetDialogFragmentVM, DialogBaseWidgetPaginatedBottomSheetBinding>(),
    ActionPerformer {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var actionPerformer: ActionPerformer? = null

    private val id: String by lazy { requireArguments().getString(KEY_ID).orEmpty() }
    private val type: String by lazy { requireArguments().getString(KEY_TYPE).orEmpty() }
    private var tabId: String = ""

    private var appStateObserver: Disposable? = null
    private lateinit var adapter: WidgetLayoutAdapter

//    private val showCloseBtn: Boolean by lazy {
//        requireArguments().getBoolean(
//            KEY_SHOW_CLOSE_BTN,
//            true
//        )
//    }

    private var page = 0
    private var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TransparentBottomSheetStyle)
        UXCam.tagScreenName(TAG)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#94000000")))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog?.setCanceledOnTouchOutside(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogBaseWidgetPaginatedBottomSheetBinding {
        return DialogBaseWidgetPaginatedBottomSheetBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): BaseWidgetPaginatedBottomSheetDialogFragmentVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.apply {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        init()
        initListeners()

        getPaginatedBottomSheetWidgetData()
    }

    fun getPaginatedBottomSheetWidgetData() {
        viewModel.getPaginatedBottomSheetWidgetData(
            id = id,
            type = type,
            tabId = tabId,
            page = page
        )
    }

    override fun setupObservers() {
        super.setupObservers()
        appStateObserver = DoubtnutApp.INSTANCE
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is PerformDeeplinkAction -> {
                        dismiss()
                    }
                }
            }

        viewModel.widgetsLiveData.observeNonNull(this) { outcome ->
            when (outcome) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = outcome.loading
                }
                is Outcome.Success -> {
                    binding.progressBar.isVisible = false

                    if (outcome.data.widgets.isNullOrEmpty()) {
                        infiniteScrollListener?.isLastPageReached = true
                    }

                    outcome.data.widgets?.filterNotNull()?.forEach {
                        if (it.extraParams == null) {
                            it.extraParams = HashMap()
                        }
                        it.extraParams?.put(
                            EventConstants.WIDGET_TITLE,
                            outcome.data.title.orEmpty()
                        )
                        it.extraParams?.put(EventConstants.ID, id)
                        it.extraParams?.put(EventConstants.TYPE, type)
                    }

                    outcome.data.title
                        ?.takeIf { it.isNotEmpty() }
                        ?.let {
                            binding.tvTitle.show()
                            binding.tvTitle.text = it
                        }

                    binding.tvTitle.applyTextColor(outcome.data.titleTextColor)
                    binding.tvTitle.applyTextSize(outcome.data.titleTextSize)

                    binding.tvAction.isVisible = outcome.data.actionText.isNullOrEmpty().not()
                    binding.tvAction.text = outcome.data.actionText
                    binding.tvAction.applyTextColor(outcome.data.actionTextColor)
                    binding.tvAction.applyTextSize(outcome.data.actionTextSize)

                    binding.tvAction.setOnClickListener {
                        deeplinkAction.performAction(requireContext(), outcome.data.actionDeepLink)
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",

                                hashMapOf<String, Any>(
                                    EventConstants.WIDGET to type,
                                    EventConstants.ID to id,
                                    EventConstants.TAB_ID to tabId,
                                    EventConstants.CTA_TEXT to outcome.data.actionText.orEmpty(),
                                    EventConstants.STUDENT_ID to UserUtil.getStudentId()
                                ).apply {
                                    putAll(outcome.data.extraParams.orEmpty())
                                }
                            )
                        )
                    }

                    when (outcome.data.tabData?.style) {
                        1 -> {
                            binding.tabLayout.hide()
                            binding.rvFilters.show()
                            binding.flexBox.hide()

                            if (binding.llHorizontal.isEmpty()) {
                                val data = outcome.data.tabData
                                binding.llHorizontal.removeAllViews()
                                data?.items?.forEach { item ->
                                    val itemBinding =
                                        ItemTabFilterBinding.inflate(LayoutInflater.from(context), binding.llHorizontal, false)
                                    itemBinding.tvFilter.text = item.title
                                    itemBinding.tvFilter.tag = item.isSelected

                                    if (item.isSelected == true) {
                                        itemBinding.tvFilter.background = Utils.getShape(
                                            colorString = "#ea532c",
                                            strokeColor = "#ea532c",
                                            cornerRadius = 4.dpToPxFloat(),
                                            strokeWidth = 1.dpToPx()
                                        )
                                        itemBinding.tvFilter.setTextColor(Utils.parseColor("#ffffff"))
                                    } else {
                                        itemBinding.tvFilter.background = Utils.getShape(
                                            colorString = "#ffffff",
                                            strokeColor = "#b1adad",
                                            cornerRadius = 4.dpToPxFloat(),
                                            strokeWidth = 1.dpToPx()
                                        )
                                        itemBinding.tvFilter.setTextColor(Utils.parseColor("#9a9a9a"))
                                    }

                                    binding.llHorizontal.addView(itemBinding.root)

                                    itemBinding.tvFilter.setOnClickListener {
                                        if (tabId == item.id) {
                                            return@setOnClickListener
                                        }

                                        binding.llHorizontal.forEachIndexed { index, tvFilter ->
                                            (tvFilter as? TextView)?.let {
                                                if ((tvFilter.tag as? Boolean) == true) {
                                                    data.items.getOrNull(index)?.isSelected = false
                                                    tvFilter.tag = false
                                                    tvFilter.background = Utils.getShape(
                                                        colorString = "#ffffff",
                                                        strokeColor = "#b1adad",
                                                        cornerRadius = 4.dpToPxFloat(),
                                                        strokeWidth = 1.dpToPx()
                                                    )
                                                    tvFilter.setTextColor(Utils.parseColor("#9a9a9a"))
                                                }
                                            }
                                        }
                                        itemBinding.tvFilter.tag = true
                                        item.isSelected = true
                                        itemBinding.tvFilter.background = Utils.getShape(
                                            colorString = "#ea532c",
                                            strokeColor = "#ea532c",
                                            cornerRadius = 4.dpToPxFloat(),
                                            strokeWidth = 1.dpToPx()
                                        )
                                        itemBinding.tvFilter.setTextColor(Utils.parseColor("#ffffff"))

                                        tabId =
                                            data.items.firstOrNull { it.isSelected == true }?.id.orEmpty()
                                        updateFilter(outcome.data)
                                    }
                                }
                            }
                        }
                        2 -> {
                            binding.tabLayout.hide()
                            binding.rvFilters.hide()
                            binding.flexBox.show()

                            if (binding.flexBox.isEmpty()) {
                                val data = outcome.data.tabData
                                binding.flexBox.removeAllViews()
                                data?.items?.forEach { item ->
                                    val itemBinding =
                                        ItemTabFilterBinding.inflate(LayoutInflater.from(context), binding.flexBox, false)
                                    itemBinding.tvFilter.text = item.title
                                    itemBinding.tvFilter.tag = item.isSelected

                                    if (item.isSelected == true) {
                                        itemBinding.tvFilter.background = Utils.getShape(
                                            colorString = "#ea532c",
                                            strokeColor = "#ea532c",
                                            cornerRadius = 4.dpToPxFloat(),
                                            strokeWidth = 1.dpToPx()
                                        )
                                        itemBinding.tvFilter.setTextColor(Utils.parseColor("#ffffff"))
                                    } else {
                                        itemBinding.tvFilter.background = Utils.getShape(
                                            colorString = "#ffffff",
                                            strokeColor = "#b1adad",
                                            cornerRadius = 4.dpToPxFloat(),
                                            strokeWidth = 1.dpToPx()
                                        )
                                        itemBinding.tvFilter.setTextColor(Utils.parseColor("#9a9a9a"))
                                    }

                                    binding.flexBox.addView(itemBinding.root)

                                    itemBinding.tvFilter.setOnClickListener {
                                        if (tabId == item.id) {
                                            return@setOnClickListener
                                        }

                                        binding.flexBox.forEachIndexed { index, tvFilter ->
                                            (tvFilter as? TextView)?.let {
                                                if ((tvFilter.tag as? Boolean) == true) {
                                                    data.items.getOrNull(index)?.isSelected = false
                                                    tvFilter.tag = false
                                                    tvFilter.background = Utils.getShape(
                                                        colorString = "#ffffff",
                                                        strokeColor = "#b1adad",
                                                        cornerRadius = 4.dpToPxFloat(),
                                                        strokeWidth = 1.dpToPx()
                                                    )
                                                    tvFilter.setTextColor(Utils.parseColor("#9a9a9a"))
                                                }
                                            }
                                        }
                                        itemBinding.tvFilter.tag = true
                                        item.isSelected = true
                                        itemBinding.tvFilter.background = Utils.getShape(
                                            colorString = "#ea532c",
                                            strokeColor = "#ea532c",
                                            cornerRadius = 4.dpToPxFloat(),
                                            strokeWidth = 1.dpToPx()
                                        )
                                        itemBinding.tvFilter.setTextColor(Utils.parseColor("#ffffff"))

                                        tabId =
                                            data.items.firstOrNull { it.isSelected == true }?.id.orEmpty()
                                        updateFilter(outcome.data)
                                    }
                                }
                            }
                        }
                        else -> {
                            binding.tabLayout.show()
                            binding.rvFilters.hide()
                            binding.flexBox.hide()

                            if (binding.tabLayout.tabCount == 0) {
                                outcome.data.tabData?.let { tabData ->
                                    binding.tabLayout.tabMode =
                                        tabData.mode ?: TabLayout.MODE_SCROLLABLE
                                    tabData.items?.let { tabItems ->

                                        binding.tabLayout.removeAllTabs()
                                        binding.tabLayout.clearOnTabSelectedListeners()

                                        tabItems.forEach { item ->
                                            binding.tabLayout.addTab(
                                                binding.tabLayout.newTab()
                                                    .apply {
                                                        text = item.title
                                                        tag = item.id
                                                    }
                                            )
                                        }

                                        outcome.data.tabData?.items?.indexOfFirst { tabItem -> tabItem.isSelected == true }
                                            ?.takeIf { it != -1 }
                                            ?.let {
                                                binding.tabLayout.getTabAt(it)?.select()
                                            }

                                        binding.tabLayout.addOnTabSelectedListener { tab ->
                                            if (tabId == tab.tag?.toString()) {
                                                return@addOnTabSelectedListener
                                            }

                                            outcome.data.tabData?.items?.forEach { tabItem ->
                                                tabItem.isSelected = false
                                            }
                                            tabId = tab.tag?.toString().orEmpty()

                                            updateFilter(outcome.data)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    outcome.data.paddingBottom?.let {
                        binding.rvMain.setPadding(
                            0,
                            0,
                            0,
                            it.dpToPx()
                        )
                    }
                    adapter.addWidgets(outcome.data.widgets.orEmpty())

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.VIEWED}",
                            hashMapOf<String, Any>(
                                EventConstants.WIDGET to type,
                                EventConstants.ID to id,
                                EventConstants.TAB_ID to tabId,
                                EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                                EventConstants.PAGE to page,
                            ).apply {
                                putAll(outcome.data.extraParams.orEmpty())
                            }
                        )
                    )
                }
                else -> {
                    dismiss()
                }
            }
        }
    }

    private fun updateFilter(data: BaseWidgetData) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                "${EVENT_TAG}_${EventConstants.TAB_CLICKED}",
                hashMapOf<String, Any>(
                    EventConstants.WIDGET to type,
                    EventConstants.ID to id,
                    EventConstants.TAB_ID to tabId,
                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                ).apply {
                    putAll(data.extraParams.orEmpty())
                }
            )
        )

        page = 0
        infiniteScrollListener?.isLastPageReached = false
        infiniteScrollListener?.setStartPage(0)
        adapter.clearData()

        getPaginatedBottomSheetWidgetData()
    }

    fun setActionListener(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }

    private fun init() {
        viewModel = viewModelProvider(viewModelFactory)
        tabId = requireArguments().getString(KEY_TAB_ID).orEmpty()

        adapter = WidgetLayoutAdapter(
            context = requireContext(),
            actionPerformer = this,
            source = TAG,
        )
        binding.rvMain.adapter = adapter
    }

    private fun initListeners() {
        binding.ivClose.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CROSS_CLICKED}",
                    hashMapOf(
                        EventConstants.WIDGET to type,
                        EventConstants.ID to id,
                        EventConstants.TAB_ID to tabId,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    )
                )
            )

            dismiss()
        }

        binding.ivBack.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.BACK_CLICKED2}",
                    hashMapOf(
                        EventConstants.WIDGET to type,
                        EventConstants.ID to id,
                        EventConstants.TAB_ID to tabId,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    )
                )
            )
            dismiss()
        }

        val behaviour = BottomSheetBehavior.from(binding.root.parent as View)
        behaviour.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
//                    binding.ivBack.show()
                    binding.ivClose.hide()
                } else if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED || newState == BottomSheetBehavior.STATE_COLLAPSED) {
//                    binding.ivBack.hide()
                    binding.ivClose.show()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvMain.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    page++
                    getPaginatedBottomSheetWidgetData()
                }
            }
        binding.rvMain.addOnScrollListener(infiniteScrollListener!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        appStateObserver?.dispose()
    }

    companion object {
        const val TAG = "BaseWidgetPaginatedBottomSheetDialogFragment"
        const val EVENT_TAG = "base_widget_paginated_bottom_sheet_dialog_fragment"

        const val KEY_ID = "id"
        const val KEY_TYPE = "type"
        const val KEY_TAB_ID = "tab_id"

        const val KEY_SHOW_CLOSE_BTN = "show_close_btn"

        fun newInstance(
            id: String?,
            type: String,
            tabId: String?,
            showCloseBtn: Boolean = true
        ) =
            BaseWidgetPaginatedBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_ID, id)
                    putString(KEY_TYPE, type)
                    putString(KEY_TAB_ID, tabId)
                    putBoolean(KEY_SHOW_CLOSE_BTN, showCloseBtn)
                }
            }
    }

    override fun performAction(action: Any) {
        when (action) {
            is Dismiss -> {
                dismiss()
            }
            else -> {
            }
        }
    }
}
