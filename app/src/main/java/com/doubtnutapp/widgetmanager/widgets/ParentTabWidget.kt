package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.InsertItemsOfATabKey
import com.doubtnutapp.EventBus.WidgetShownEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.InsertChildrenAtNode
import com.doubtnutapp.base.LibraryWidgetClick
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.LayoutPadding
import com.doubtnutapp.databinding.WidgetParentTabBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.newlibrary.ui.LibraryFragment
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.android.material.tabs.TabLayout
import com.google.gson.annotations.SerializedName
import com.uxcam.UXCam
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by devansh on 29/1/21.
 */

class ParentTabWidget(context: Context) :
    BaseBindingWidget<ParentTabWidget.WidgetHolder, ParentTabWidget.Model, WidgetParentTabBinding>(
        context
    ) {

    companion object {
        const val TAG = "ParentTabWidget"
        const val GRAVITY_BOTTOM = "bottom"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    var selectedTabPos = 0

    var source: String? = null

    private var viewTrackingBus: ViewTrackingBus? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
        setUpViewTracking()
    }

    private fun setUpViewTracking() {
        if (viewTrackingBus == null) {
            viewTrackingBus = ViewTrackingBus({}, {})
        } else {
            if (viewTrackingBus?.isPaused() == true) {
                viewTrackingBus?.resume()
            }
        }
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    var adapter: WidgetLayoutAdapter? = null

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 0, 0)
        })
        val data: Data = model.data
        val binding = holder.binding

        data.items?.forEach {
            it.value.forEachIndexed { index, widget ->
                if (widget != null) {
                    if (widget.extraParams == null) {
                        widget.extraParams = hashMapOf()
                    }
                    widget.extraParams?.apply {
                        putAll(model.extraParams ?: hashMapOf())
                        put(EventConstants.ITEM_POSITION, index)
                        source?.let { put(EventConstants.SOURCE, it) }
                        put(EventConstants.PARENT_TITLE, data.title.orEmpty())
                        put(EventConstants.PARENT_TAB_KEY, it.key)
                        put(EventConstants.WIDGET_TYPE, widget.type)
                        put(EventConstants.PARENT_ID, data.id.orEmpty())
                    }
                }
            }
        }

        // pass uxcam event
        data.uxCamEvent?.let { event -> UXCam.logEvent(event) }

        DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { action ->
            when (action) {
                is InsertItemsOfATabKey -> {
                    val value = action.value ?: return@subscribe
                    val (key, position) = Pair(
                        data.tabs?.find { tab ->
                            tab.playListId == action.playlistId
                        }?.key,
                        data.tabs?.indexOfFirst { tab ->
                            tab.playListId == action.playlistId
                        }
                    )
                    key ?: return@subscribe
                    position ?: return@subscribe

                    val items = data.items ?: return@subscribe
                    val newMap = items.toMutableMap()
                    newMap[key] = value
                    data.items = newMap

                    notifyTabs(position, data)
                }
            }
        }?.apply {
            compositeDisposable.add(this)
        }

        widgetViewHolder.binding.apply {

            rootLayout.setBackgroundColor(Color.parseColor(data.backgroundColor ?: "#ffffff"))

            textViewTitleMain.apply {
                text = data.title.orEmpty()
                isVisible = !data.title.isNullOrEmpty()
                if (data.titleTextSize != null) {
                    textSize = data.titleTextSize
                }
                if (data.isTitleBold == true) {
                    setTypeface(typeface, Typeface.BOLD)
                }
            }

            if (data.tabsBackgroundColor.isValidColorCode()) {
                tabLayout.setBackgroundColor(Color.parseColor(data.tabsBackgroundColor))
            } else {
                tabLayout.setBackgroundColor(Color.parseColor("#ffffff"))
            }

            textViewViewAll.apply {
                text = data.linkText.orEmpty()
                isVisible = data.linkText.isNullOrBlank().not()
                setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.TAB_WIDGET_LINK_TEXT_CLICK,
                            hashMapOf(
                                EventConstants.SOURCE to source.orEmpty(),
                                EventConstants.PARENT_TITLE to data.title.orEmpty(),
                                EventConstants.CTA_TITLE to data.linkText.orEmpty()
                            )
                        )
                    )
                    if (source == LibraryFragment.TAG) {
                        actionPerformer?.performAction(
                            LibraryWidgetClick(
                                data.id.orDefaultValue(),
                                data.tabs?.map { it.playListId },
                                widgetViewHolder.absoluteAdapterPosition
                            )
                        )
                    } else if (!data.deeplink.isNullOrBlank()) {
                        deeplinkAction.performAction(context, data.deeplink)
                    } else if (data.linkTextUrl.isNotNullAndNotEmpty()) {
                        apiCallForActionButton(holder.itemView.context)
                    }
                }

                if (data.isLinkTextBold != null) {
                    setTypeface(typeface, Typeface.BOLD)
                } else {
                    setTypeface(typeface, Typeface.BOLD)
                }

                if (data.showArrowIconForLinkText == null || data.showArrowIconForLinkText) {
                    try {

                        val drawableEnd =
                            ContextCompat.getDrawable(context, R.drawable.ic_arrow_forward)
                        binding.textViewViewAll.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            null,
                            null,
                            drawableEnd,
                            null
                        )
                    } catch (ignored: Exception) {
                        // https://console.firebase.google.com/project/doubtnut-e000a/crashlytics/app/android:com.doubtnutapp/issues/4d05f70f8df16ae61cc321882d1d76e5?time=last-twenty-four-hours&sessionEventKey=60B0592103B900013B43609A2C4CE946_1545612299651154538
                    }
                } else {
                    binding.textViewViewAll.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }

            }

            tvSubtitle.apply {
                isVisible = data.subtitle.isNotNullAndNotEmpty()
                text = data.subtitle
                if (data.subtitleRemoveDrawableEnd == true) {
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                } else {
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_forward, 0)
                }
                if (data.subtitleTextColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.subtitleTextColor))
                }
                data.subtitleTextSize?.let { size ->
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
                }
            }

            when (data.scrollDirection) {
                "grid" -> {
                    recyclerView.layoutManager = GridLayoutManager(context, 2)
                }
                "vertical" -> {
                    recyclerView.layoutManager = LinearLayoutManager(
                        context,
                        RecyclerView.VERTICAL, false
                    )

                }
                else -> {
                    recyclerView.layoutManager = LinearLayoutManager(
                        context,
                        RecyclerView.HORIZONTAL, false
                    )
                }
            }
            adapter = WidgetLayoutAdapter(context, actionPerformer, source).apply {
                viewTrackingBus?.let {
                    registerViewTracking(it)
                }
            }
            recyclerView.adapter = adapter

            binding.recyclerView.updatePadding(
                left = (data.layoutPadding?.paddingStart ?: 8).dpToPx(),
                top = (data.layoutPadding?.paddingTop ?: 0).dpToPx(),
                right = (data.layoutPadding?.paddingEnd ?: 8).dpToPx(),
                bottom = (data.layoutPadding?.paddingBottom ?: 0).dpToPx()
            )

            tabLayout.apply {
                // Clearing is necessary to prevent selected tab getting reset on recycler view scroll
                clearOnTabSelectedListeners()
                removeAllTabs()

                var isCustomTab = false
                data.isCustomTab?.let {
                    if (it) {
                        isCustomTab = true
                    }
                }
                data.tabs?.forEachIndexed { index, tabData ->
                    if (isCustomTab) {
                        val view = LayoutInflater.from(context)
                            .inflate(R.layout.parent_tab_widget_custom_tab_layout, null)

                        val image = view.findViewById<ImageView>(R.id.imageView)
                        if (tabData.iconUrl != null) {
                            image.loadImage(tabData.iconUrl)
                        }

                        val textView = view.findViewById<TextView>(R.id.textView)
                        val tab = tabLayout.newTab()
                            .setTag(tabData.key)
                            .setCustomView(view)

                        textView.text = tabData.title
                        textView.apply {
                            if (tabData.isSelected) {
                                textView.setTypeface(typeface, Typeface.BOLD)
                            }
                        }

                        tabLayout.addTab(tab)
                        val paddingStart = 4.dpToPx()
                        val padding = 8.dpToPx()
                        tabLayout.setPadding(paddingStart, padding, padding, 0)
                    } else {
                        tabLayout.addTab(
                            tabLayout.newTab()
                                .setText(tabData.title)
                                .setTag(tabData.key)
                        )
                        val paddingStart = 4.dpToPx()
                        val paddingTop = 8.dpToPx()
                        tabLayout.setPadding(paddingStart, paddingTop, 0, 0)
                    }

                    if (tabData.isSelected) {
                        selectedTabPos = index
                        adapter?.setWidgets(data.items?.get(tabData.key).orEmpty())
                    }
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.HOME_PAGE_CAROUSEL_TAB_VISIBLE,
                            hashMapOf(
                                Constants.TAB_TITLE to tabData.title,
                                Constants.TAB_KEY to tabData.key,
                                EventConstants.ITEM_POSITION to index,
                                EventConstants.SOURCE to source.orEmpty(),
                                EventConstants.PARENT_TITLE to data.title.orEmpty()
                            )
                        )
                    )
                }

                addOnTabSelectedListener { tab ->
                    val selectedTabItems = data.items?.get(tab.tag).orEmpty()
                    if (selectedTabItems.isNotEmpty()) {
                        adapter?.setWidgets(selectedTabItems)
                        widgetViewHolder.binding.recyclerView?.smoothScrollToPosition(0)
                    } else {
                        val selectedTab = data.tabs?.find { it.title == tab.text }
                        selectedTab?.let {
                            loadTabListItems(
                                playlistId = it.playListId,
                                key = it.key,
                                type = it.type,
                                data = data
                            )
                        }
                    }

                    notifyTabs(tab.position, data)
                }
            }

            DoubtnutApp.INSTANCE.bus()?.send(WidgetShownEvent(model.extraParams))
        }

        binding.bottomCta.apply {
            isVisible = data.bottomCta.isNotNullAndNotEmpty()
            text = data.bottomCta
            setOnClickListener {
                if (data.bottomCtaDeeplink.isNullOrEmpty()) return@setOnClickListener
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.TAB_WIDGET_BOTTOM_CTA_CLICK,
                        hashMapOf(
                            Constants.TITLE to data.bottomCta.orEmpty(),
                            EventConstants.SOURCE to source.orEmpty(),
                            EventConstants.PARENT_TITLE to data.title.orEmpty()
                        )
                    )
                )
                deeplinkAction.performAction(context, data.bottomCtaDeeplink)
            }
        }

        data.layoutConfig?.let { layoutConfig ->
            layoutConfig.tabLayoutHeight?.let {
                binding.tabLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    height = it.dpToPx()
                }
                layoutConfig.marginLeft?.let {
                    val topMargin = 10.dpToPx()
                    val startMargin = it.dpToPx()
                    binding.tabLayout.setMargins(startMargin, topMargin, startMargin, 0)
                }
            } ?: run {
                binding.tabLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    height = 36.dpToPx()
                }
            }

            layoutConfig.itemsTopMargin?.let {
                binding.recyclerView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    val topMargin = it.dpToPx()
                    setMargins(0, topMargin, 0, 0)
                }
            }
        } ?: run {
            binding.tabLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = 36.dpToPx()
            }
        }

        trackingViewId = data.id

        if (data.tabLayoutGravity.isNotNullAndNotEmpty() && data.tabLayoutGravity.equals(
                GRAVITY_BOTTOM
            )
        ) {

            binding.recyclerView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToBottom = binding.tvSubtitle.id
            }

            binding.tabLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {

                topToBottom = binding.recyclerView.id

                if (data.layoutConfig?.matchParent != null && data.layoutConfig.matchParent) {
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
                } else {
                    bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                    width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                    endToEnd = ConstraintLayout.LayoutParams.UNSET
                }
            }

            val padding = 10.dpToPx()
            val marginStart = 16.dpToPx()
            binding.tabLayout.setMargins(marginStart, 0, 0, 0)
            binding.tabLayout.setPadding(0, padding, 0, padding)
        } else {

            binding.tabLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToBottom = binding.tvSubtitle.id
                bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                endToEnd = ConstraintLayout.LayoutParams.UNSET
            }

            binding.recyclerView.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToBottom = binding.tabLayout.id
            }

            val paddingStart = 4.dpToPx()
            val paddingTop = 8.dpToPx()
            binding.tabLayout.setPadding(paddingStart, paddingTop, 0, 0)
            binding.tabLayout.setMargins(0, 0, 0, 0)
        }

        return holder
    }

    private fun notifyTabs(position: Int, data: Data) {
        data.tabs?.forEach {
            it.isSelected = false
        }
        if (position != TabLayout.Tab.INVALID_POSITION) {
            selectedTabPos = position
        }
        selectedTabPos = position
        data.tabs?.getOrNull(position)?.let {
            it.isSelected = true
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.HOME_PAGE_CAROUSEL_TAB_CLICKED,
                    hashMapOf(
                        Constants.TAB_TITLE to it.title,
                        Constants.TAB_KEY to it.key,
                        EventConstants.ITEM_POSITION to position,
                        EventConstants.SOURCE to source.orEmpty(),
                        EventConstants.PARENT_TITLE to data.title.orEmpty()
                    ), ignoreSnowplow = true
                )
            )
        }

        afterMeasured {
            widgetViewHolder.binding.tabLayout.getTabAt(selectedTabPos)?.select()
        }
    }

    private fun loadTabListItems(playlistId: String, key: String, type: String, data: Data) {
        widgetViewHolder.binding.progressBar.show()
        compositeDisposable.add(
            DataHandler.INSTANCE.ncertSimilarVideoRepository
                .getNcertVideoAdditionalData(playlistId = playlistId, type = type, questionId = "")
                .applyIoToMainSchedulerOnSingle()
                .subscribe(
                    {
                        widgetViewHolder.binding.progressBar.hide()
                        it.data.ncertSimilar?.let { ncertSimilar ->
                            adapter?.setWidgets(ncertSimilar)
                            data.items?.let { map ->
                                val newMap = map.toMutableMap()
                                newMap[key] = ncertSimilar
                                data.items = newMap
                            }

                            actionPerformer?.performAction(
                                InsertChildrenAtNode(
                                    playlistId,
                                    ncertSimilar
                                )
                            )
                        }
                    },
                    {
                        widgetViewHolder.binding.progressBar.hide()
                        it.printStackTrace()
                    }
                ))
    }

    private fun apiCallForActionButton(context: Context) {
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            val dnrRepository = DataHandler.INSTANCE.dnrRepository
            try {
                val responseReminder = dnrRepository.setReminderForDnrReward()
                responseReminder.data?.let { data ->
                    data.message?.let {
                        showToast(context, it)
                    }
                }
            } catch (e: java.lang.Exception) {
                showToast(context, context.getString(R.string.somethingWentWrong))
            }
        }

    }

    override fun onDetachedFromWindow() {
        compositeDisposable.clear()
        viewTrackingBus?.unsubscribe()
        super.onDetachedFromWindow()
    }

    class WidgetHolder(
        binding: WidgetParentTabBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetParentTabBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_size") val titleTextSize: Float?,
        @SerializedName("is_title_bold") val isTitleBold: Boolean?,
        @SerializedName("link_text") val linkText: String?,
        @SerializedName("link_text_url") val linkTextUrl: String?,
        @SerializedName("is_link_text_bold") val isLinkTextBold: String?,
        @SerializedName("link_text_show_arrow") val showArrowIconForLinkText: Boolean?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_text_size") val subtitleTextSize: Float?,
        @SerializedName("subtitle_text_color") val subtitleTextColor: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("scroll_direction") val scrollDirection: String?,
        @SerializedName("tabs") val tabs: List<TabData>?,
        @SerializedName("is_custom_tabs") val isCustomTab: Boolean?,
        @SerializedName("ux_cam_event") val uxCamEvent: String?,
        @SerializedName("items") var items: Map<String, List<WidgetEntityModel<*, *>>>?,
        @SerializedName("bottom_cta") val bottomCta: String?,
        @SerializedName("bottom_cta_deeplink") val bottomCtaDeeplink: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("tabs_background_color") val tabsBackgroundColor: String?,
        @SerializedName("layout_padding") val layoutPadding: LayoutPadding?,
        @SerializedName("subtitle_remove_drawable_end") val subtitleRemoveDrawableEnd: Boolean?,
        @SerializedName("layout_config") val layoutConfig: LayoutConfig?,
        @SerializedName("tab_layout_gravity") val tabLayoutGravity: String?
    ) : WidgetData()

    @Keep
    data class LayoutConfig(
        @SerializedName("items_top_margin") val itemsTopMargin: Int?,
        @SerializedName("tab_layout_height") val tabLayoutHeight: Int?,
        @SerializedName("match_parent") val matchParent: Boolean?,
        @SerializedName("margin_left") val marginLeft: Int?,
    )

    @Keep
    data class TabData(
        @SerializedName("id") val playListId: String,
        @SerializedName("type") val type: String,
        @SerializedName("title") val title: String,
        @SerializedName("title_text_size") val titleTextSize: String?,
        @SerializedName("icon_url") val iconUrl: String?,
        @SerializedName("key") val key: String,
        @SerializedName("is_selected") var isSelected: Boolean,
    )

    override fun getViewBinding(): WidgetParentTabBinding {
        return WidgetParentTabBinding.inflate(LayoutInflater.from(context), this, true)
    }
}
