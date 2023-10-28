package com.doubtnutapp.course.widgets

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnut.core.utils.RecyclerViewUtils.addRvRequestDisallowInterceptTouchEventListener
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetShownEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.AutoPlayVideoCompleted
import com.doubtnutapp.base.FeedDNVideoWatched
import com.doubtnutapp.base.FeedPinnedVideoItemVisible
import com.doubtnutapp.base.MuteAutoPlayVideo
import com.doubtnutapp.databinding.ItemCategoryV2Binding
import com.doubtnutapp.databinding.WidgetParentAutoplayBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.feed.viewmodel.FeedViewModel
import com.doubtnutapp.home.recyclerdecorator.HorizontalBannerSpaceItemDecoration
import com.doubtnutapp.matchquestion.ui.fragment.MatchQuestionFragment
import com.doubtnutapp.referral.ReferralVideoWidget
import com.doubtnutapp.rvexoplayer.RecyclerViewExoPlayerHelper
import com.doubtnutapp.rvexoplayer.RvMuteStatus
import com.doubtnutapp.utils.NetworkUtil
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.doubtnutapp.widgetmanager.widgets.FeedPinnedVideoAutoplayChildWidget
import com.doubtnutapp.widgetmanager.widgets.VideoBannerAutoplayChildWidget
import com.doubtnutapp.widgets.SpaceItemDecoration
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.tabs.TabLayout
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Sachin Saxena on 03/01/21.
 *
 * Container view of auto play widget e.g. Live classes on home page.
 * see [AutoPlayChildWidget]
 */
class ParentAutoplayWidget(context: Context) : BaseBindingWidget<ParentAutoplayWidget.WidgetHolder,
        ParentAutoplayWidget.ParentAutoplayWidgetModel, WidgetParentAutoplayBinding>(context) {

    companion object {
        const val TAG = "ParentAutoplayWidget"
        const val EVENT_TAG = "parent_autoplay_widget"
        const val TAB_GRAVITY_FILL_FOR_MAX_COUNT = 5
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var networkUtil: NetworkUtil

    var source: String? = null

    var rVExoPlayerHelper: RecyclerViewExoPlayerHelper? = null
    private var autoScrollJob: Job? = null
    var isAutoScrollWidgetInteractionDone = false
    private lateinit var data: ParentAutoplayWidgetData
    private var childAdapter: WidgetLayoutAdapter? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE) {
            startVideo()
        } else {
            stopVideo()
        }
    }

    fun startVideo() {
        rVExoPlayerHelper?.playCurrent(widgetViewHolder.binding.recyclerView)
    }

    fun stopVideo() {
        rVExoPlayerHelper?.stopCurrent()
    }

    override fun bindWidget(holder: WidgetHolder, model: ParentAutoplayWidgetModel): WidgetHolder {
        if (model.layoutConfig == null) {
            if (source == CourseBottomSheetDialogFragment.TAG) {
                // removing spacing as view already has padding.
                model.apply {
                    layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
                }
            } else {
                model.apply {
                    layoutConfig = WidgetLayoutConfig(0, 8, 0, 0)
                }
            }
        }
        super.bindWidget(holder, model)
        data = model.data
        val binding = widgetViewHolder.binding

        if (FeedViewModel.DEFAULT_SOURCE === source) {
            binding.divider.show()
        } else {
            binding.divider.hide()
        }

        binding.ivIcon.apply {
            data.topIcon?.let {
                layoutParams.height = (data.topIconHeight ?: 24).dpToPx()
                layoutParams.width = (data.topIconWidth ?: 24).dpToPx()
                requestLayout()
                show()
                loadImage(data.topIcon)
            } ?: hide()
        }

        binding.root.apply {
            background = Utils.getShape(
                colorString = data.bgColor ?: "#FFFFFF",
                strokeColor = data.borderColor ?: "#FFFFFF",
                cornerRadius = data.cornerRadius ?: 0F,
                strokeWidth = data.borderWidth ?: 0
            )

            binding.tvTitle.isVisible = data.title?.isNotBlank() == true
            TextViewUtils.setTextFromHtml(binding.tvTitle, data.title.orEmpty())
            binding.tvTitle.applyTextSize(data.titleTextSize)
            binding.tvTitle.applyTextColor(data.titleTextColor)

            binding.liveNowAnimation.isVisible =
                data.isLive == true && data.showLiveGraphics == null || data.showLiveGraphics == true
        }

        binding.textViewSubtitle.apply {
            if (data.subtitle.isNotNullAndNotEmpty()) {
                show()
                text = data.subtitle.orEmpty()
                applyTextSize(data.subtitleTextSize)
                applyTextColor(data.subtitleTextColor.orDefaultValue("#000000"))
            } else {
                hide()
            }
        }

        binding.textViewViewAll.apply {
            if (data.linkText.isNullOrBlank()) {
                hide()
            } else {
                show()
                text = data.linkText.orEmpty()
                applyTypeface(data.isActionButtonTitleBold)
            }

            setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${EVENT_TAG}_${EventConstants.MORE_CLICKED}",
                        hashMapOf<String, Any>(
                            EventConstants.PARENT_TITLE to data.title.orEmpty(),
                            EventConstants.WIDGET to ParentWidget.TAG
                        ).apply {
                            putAll(model.extraParams ?: HashMap())
                        }
                    )
                )
                if (!data.deeplink.isNullOrBlank()) {
                    deeplinkAction.performAction(context, data.deeplink)
                }
            }
        }

        binding.tabLayout.isVisible = data.tabs != null
        binding.viewTabDivider.isVisible = data.tabs != null
        binding.tabLayout.removeAllTabs()
        binding.tabLayout.clearOnTabSelectedListeners()

        val layoutManager = when (model.data.scrollDirection) {
            "vertical" -> {
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            }
            else -> {
                LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            }
        }

        binding.recyclerView.layoutManager = layoutManager

        childAdapter = WidgetLayoutAdapter(
            context,
            object : ActionPerformer {

                override fun performAction(action: Any) {
                    when (action) {
                        is AutoPlayVideoCompleted -> {
                            if ((
                                        binding.recyclerView.adapter?.itemCount
                                            ?: 0
                                        ) - 1 > action.adapterPosition
                            ) {
                                if (layoutManager.findFirstCompletelyVisibleItemPosition() == action.adapterPosition) {
                                    Handler(Looper.getMainLooper()).postDelayed(action.delayToMoveToNext) {
                                        binding.recyclerView.smoothScrollToPosition(action.adapterPosition + 1)
                                    }
                                }
                            }
                        }

                        is FeedPinnedVideoItemVisible -> {
                            actionPerformer?.performAction(action)
                        }
                        is FeedDNVideoWatched -> {
                            actionPerformer?.performAction(action)
                        }
                        is MuteAutoPlayVideo -> {
                            data.items?.filterIsInstance<VideoBannerAutoplayChildWidget.Model>()
                                ?.forEach {
                                    it.data.defaultMute = action.isMute
                                }

                            data.items?.filterIsInstance<ReferralVideoWidget.WidgetModel>()
                                ?.forEach {
                                    it.data.defaultMute = action.isMute
                                }

                            data.items?.filterIsInstance<AutoPlayChildWidget.AutoplayChildWidgetModel>()
                                ?.forEach {
                                    it.data.defaultMute = action.isMute
                                }

                            data.items?.filterIsInstance<CourseAutoPlayChildWidget.CourseAutoPlayChildWidgetModel>()
                                ?.forEach {
                                    it.data.defaultMute = action.isMute
                                }

                            data.items?.filterIsInstance<FeedPinnedVideoAutoplayChildWidget.Model>()
                                ?.forEach {
                                    it.data.defaultMute = action.isMute
                                }

                            data.items?.filterIsInstance<VideoAutoplayChildWidget2.Model>()
                                ?.forEach {
                                    it.data.defaultMute = action.isMute
                                }

                            data.items?.filterIsInstance<ExplorePromoWidget.Model>()
                                ?.forEach {
                                    it.data.defaultMute = action.isMute
                                }

                            data.defaultMute = action.isMute

                            val muteStatus = RvMuteStatus(action.isMute)
                            childAdapter?.notifyItemRangeChanged(
                                0,
                                childAdapter?.itemCount
                                    ?: 0,
                                muteStatus
                            )
                        }
                        else -> {
                        }
                    }
                }
            },
            source
        )

        binding.recyclerView.apply {
            clipToPadding = model.data.clipToPadding == true
            addRvRequestDisallowInterceptTouchEventListener()
            this.adapter = childAdapter
            val snapHelper = PagerSnapHelper()
            if (data.fullWidthCards) {
                if (onFlingListener == null) {
                    updatePadding(left = 0, right = 0)
                    snapHelper.attachToRecyclerView(this)

                    removeItemDecorations()
                    val outValue = TypedValue()
                    context.resources.getValue(R.dimen.spacing_circle, outValue, true)
                    if (model.data.showItemCount != true) {
                        val value = outValue.float.dpToPx().toInt()
                        addItemDecoration(
                            HorizontalBannerSpaceItemDecoration(
                                value,
                                snapHelper,
                                false,
                                4f.dpToPx()
                            )
                        )
                    }
                }
            } else {
                updatePadding(left = 8.dpToPx(), right = 8.dpToPx())
                snapHelper.attachToRecyclerView(null)
                removeItemDecorations()
            }
        }

        data.items?.filterNotNull()?.mapIndexed { index, widget ->
            if (widget.extraParams == null) {
                widget.extraParams = hashMapOf()
            }
            widget.extraParams?.putAll(model.extraParams ?: HashMap())
            widget.extraParams?.put(EventConstants.ITEM_POSITION, index)
            source?.let { widget.extraParams?.put(EventConstants.SOURCE, it) }
            widget.extraParams?.put(EventConstants.PARENT_TITLE, data.title.orEmpty())
            widget.extraParams?.put(EventConstants.WIDGET_TYPE, widget.type)
            widget.extraParams?.put(EventConstants.PARENT_ID, data.id.orEmpty())
            widget.extraParams?.put(Constants.COUNT, data.items?.size ?: 0)
        }

        model.data.tabs?.forEach { tab ->
            binding.tabLayout.addTab(
                binding.tabLayout.newTab()
                    .apply {
                        text = tab.title.orEmpty()
                        tag = tab.key.orEmpty()
                    }
            )
        }

        binding.tabLayout.addOnTabSelectedListener { tab ->
            model.data.tabs?.forEach { it.isSelected = false }
            model.data.tabs?.firstOrNull { it.key == tab.tag?.toString() }
                ?.let {
                    it.isSelected = true
                    updateTabData(binding, model)

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.TAB_CLICKED}",
                            hashMapOf<String, Any>(
                                EventConstants.WIDGET to TAG,
                                EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                                EventConstants.TAB_TITLE to tab.tag?.toString().orEmpty(),
                            ).apply {
                                putAll(model.extraParams.orEmpty())
                            }
                        )
                    )
                }
        }

        model.data.tabs?.indexOfFirst { it.isSelected == true }
            ?.takeIf { it != -1 }
            ?.let {
                binding.tabLayout.getTabAt(it)?.select()
            }

        // for gravity fill boolean true, if tab size<5, show fill gravity
        // else show tab mode fixed
        if (model.data.tabGravityFill) {
            model.data.tabs?.let {
                if (it.size < TAB_GRAVITY_FILL_FOR_MAX_COUNT) {
                    binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL
                    binding.tabLayout.tabMode = TabLayout.MODE_FIXED
                } else {
                    binding.tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
                }
            }
        } else {
            binding.tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        }

        updateTabData(binding, model)

        DoubtnutApp.INSTANCE.bus()?.send(WidgetShownEvent(model.extraParams))

        trackingViewId = data.id

        if (!model.data.categoryList.isNullOrEmpty()) {
            binding.bottomDivider.visibility = INVISIBLE
            binding.rvCategory.visibility = VISIBLE
            binding.rvCategory.layoutManager =
                LinearLayoutManager(holder.itemView.context, RecyclerView.HORIZONTAL, false)
            binding.rvCategory.addRvRequestDisallowInterceptTouchEventListener()
            binding.rvCategory.adapter = Adapter(
                model.data.categoryList.orEmpty(),
                analyticsPublisher, deeplinkAction,
                holder.itemView.context,
                model.extraParams ?: HashMap()
            )
            if (binding.rvCategory.itemDecorationCount == 0)
                binding.rvCategory.addItemDecoration(
                    SpaceItemDecoration(
                        ViewUtils.dpToPx(
                            8f,
                            context
                        ).toInt()
                    )
                )
        } else {
            binding.bottomDivider.visibility = GONE
            binding.rvCategory.visibility = GONE
        }

        (context as? AppCompatActivity)?.let { appCompatActivity ->

            binding.recyclerView.addOnItemTouchListener(object :
                RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    if (rv.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        isAutoScrollWidgetInteractionDone = true
                    }
                    return super.onInterceptTouchEvent(rv, e)
                }
            })

            binding.recyclerView.smoothScrollToPosition(model.data.selectedPagePosition)

            autoScrollJob?.cancel()
            model.data.autoScrollTimeInSec?.let { autoScrollTimeInSec ->
                if (autoScrollTimeInSec > 0L) {
                    autoScrollJob = appCompatActivity.lifecycleScope.launchWhenResumed {
                        autoRotate(
                            recyclerView = binding.recyclerView,
                            delayInSec = autoScrollTimeInSec,
                            lastIndex = (binding.recyclerView.adapter?.itemCount ?: 0) - 1
                        )
                    }
                }
            }
        }
        return holder
    }

    private fun updateTabData(
        binding: WidgetParentAutoplayBinding,
        model: ParentAutoplayWidgetModel
    ) {
        data.selectedPagePosition = 0 // reset the selected page position used for auto scroll

        val items = data.getItemByGroupId()
        val defaultBufferDuration: Int? = when {
            data.bufferDuration != null -> data.bufferDuration?.toInt()
            data.autoPlayDuration != null -> data.autoPlayDuration?.toInt()
            else -> null
        }
        if (networkUtil.isConnectionFast() && data.autoPlay == true) {
            rVExoPlayerHelper = RecyclerViewExoPlayerHelper(
                mContext = context,
                id = R.id.rvPlayer,
                autoPlay = data.autoPlay == true,
                autoPause = data.autoPause == true,
                autoPlayInitiation = data.autoPlayInitiation ?: 500L,
                playStrategy = data.playStrategy
                    ?: (if (items?.size ?: 0 > 1) 1.0F else 0.70F),
                defaultMute = data.defaultMute ?: true,
                loop = 0,
                progressRequired = true,
                defaultMinBufferMs = defaultBufferDuration
                    ?: MatchQuestionFragment.DEFAULT_MIN_BUFFER_MS,
                defaultMaxBufferMs = defaultBufferDuration
                    ?: MatchQuestionFragment.DEFAULT_MAX_BUFFER_MS,
                reBufferDuration = 0 // For autoplay no need to buffer extra bytes
            ).apply {
                getPlayerView()?.apply {
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                }
                makeLifeCycleAware(context as LifecycleOwner)
            }
        } else {
            rVExoPlayerHelper = null
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.HOME_PAGE_AUTOPLAY_STOP_REASON_SLOW_INTERNET,
                    ignoreSnowplow = true
                )
            )
        }

        childAdapter?.setWidgets(items.orEmpty())
        rVExoPlayerHelper?.attachToRecyclerView(binding.recyclerView)

        val buttonAction = data.getActionByGroupId()
        binding.btnAction.isVisible = buttonAction != null
        buttonAction?.let {
            binding.btnAction.text = buttonAction.textOne
            binding.btnAction.applyTextColor(buttonAction.textOneColor)
            binding.btnAction.applyTextSize(buttonAction.textOneSize)
            binding.btnAction.applyStrokeColor(buttonAction.bgStrokeColor)
            binding.btnAction.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                        hashMapOf<String, Any>(
                            EventConstants.WIDGET to TAG,
                            EventConstants.CTA_TEXT to buttonAction.textOne.orEmpty(),
                            EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                            EventConstants.SOURCE to source.orEmpty(),
                        ).apply {
                            putAll(model.extraParams.orEmpty())
                        }
                    )
                )
                deeplinkAction.performAction(context, buttonAction.deepLink)
            }
        }
    }

    private suspend fun autoRotate(
        recyclerView: RecyclerView,
        delayInSec: Long,
        lastIndex: Int
    ) {
        delay(TimeUnit.SECONDS.toMillis(delayInSec))
        if (isAutoScrollWidgetInteractionDone) {
            isAutoScrollWidgetInteractionDone = false
        } else {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val visibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            val scrollToPosition =
                if (visibleItemPosition == lastIndex) 0 else visibleItemPosition + 1
            recyclerView.smoothScrollToPosition(scrollToPosition)
            data.selectedPagePosition = scrollToPosition
        }
        autoRotate(recyclerView, delayInSec, lastIndex)
    }

    class Adapter(
        val items: List<CategoryItem>,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val context: Context,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemCategoryV2Binding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.imageView.loadImageEtx(item.imageUrl.orEmpty())
            if (items.isNotEmpty()) {
                if (items.size > 4) {
                    Utils.setWidthBasedOnPercentage(
                        holder.itemView.context, holder.itemView, "4.85", R.dimen.spacing
                    )
                } else {
                    Utils.setWidthBasedOnPercentage(
                        holder.itemView.context, holder.itemView, "4", R.dimen.spacing_zero
                    )
                }
            }
            holder.binding.cardView.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.CATEGORY_WIDGET_CLICKED,
                        hashMapOf(EventConstants.CATEGORY to item.id.orEmpty()),
                        ignoreSnowplow = true
                    )
                )
                deeplinkAction.performAction(context, item.deeplink)
            }
        }

        class ViewHolder(val binding: ItemCategoryV2Binding) : RecyclerView.ViewHolder(binding.root)

        override fun getItemCount(): Int {
            return items.size
        }
    }

    class WidgetHolder(
        binding: WidgetParentAutoplayBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetParentAutoplayBinding>(binding, widget)

    class ParentAutoplayWidgetModel : WidgetEntityModel<ParentAutoplayWidgetData, WidgetAction>()

    @Keep
    data class ParentAutoplayWidgetData(
        @SerializedName("id") val id: String?,
        @SerializedName("top_icon") val topIcon: String?,
        @SerializedName("top_icon_width") val topIconWidth: Int?,
        @SerializedName("top_icon_height") val topIconHeight: Int?,
        @SerializedName("border_color") val borderColor: String?,
        @SerializedName("corner_radius") val cornerRadius: Float?,
        @SerializedName("border_width") val borderWidth: Int?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_size") val titleTextSize: String?,
        @SerializedName("title_text_color") val titleTextColor: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_text_color") val subtitleTextColor: String?,
        @SerializedName("subtitle_text_size") val subtitleTextSize: String?,
        @SerializedName("is_live") val isLive: Boolean?,
        @SerializedName("show_live_graphics") val showLiveGraphics: Boolean?,
        @SerializedName("subject") val subject: String?,
        @SerializedName("live_text") val liveText: String?,
        @SerializedName("live_at") val liveAt: Long?,
        @SerializedName("auto_play") val autoPlay: Boolean?,
        @SerializedName("auto_pause") val autoPause: Boolean?,
        @SerializedName("auto_play_initiation") val autoPlayInitiation: Long?,
        @SerializedName("auto_play_duration") val autoPlayDuration: Long?,
        @SerializedName("buffer_duration") val bufferDuration: Long?,
        @SerializedName("scroll_direction") val scrollDirection: String?,
        @SerializedName("full_width_cards") val fullWidthCards: Boolean = false,
        @SerializedName("default_mute") var defaultMute: Boolean?,
        @SerializedName("play_strategy") val playStrategy: Float?,
        @SerializedName("clip_to_padding") var clipToPadding: Boolean?,
        @SerializedName("category_items") var categoryList: List<CategoryItem>?,
        // Using Nullable to prevent inconsistent data.
        @SerializedName("items") val items: List<WidgetEntityModel<*, *>?>?,
        @SerializedName("auto_scroll_time_in_sec") val autoScrollTimeInSec: Long?,
        @SerializedName("show_item_count") val showItemCount: Boolean?,
        @SerializedName("bg_color") val bgColor: String?,

        @SerializedName("tabs") val tabs: List<TabData>?,
        @SerializedName("actions") val actions: List<ButtonAction>?,
        @SerializedName("tab_gravity_full") val tabGravityFill: Boolean,
        @SerializedName("link_text") val linkText: String?,
        @SerializedName("is_action_button_title_bold") val isActionButtonTitleBold: Boolean?,
        @SerializedName("deeplink") val deeplink: String?,

        var selectedPagePosition: Int = 0,
        var flagrId: String?,
        var variantId: String?
    ) : WidgetData() {

        fun getItemByGroupId() = items?.filterNotNull()
            ?.filter { model ->
                model.groupId ==
                        tabs?.firstOrNull { tabData -> tabData.isSelected == true }?.key
            }

        fun getActionByGroupId() = actions?.firstOrNull {
            it.groupId == tabs?.firstOrNull { tabData -> tabData.isSelected == true }?.key
        }
    }

    @Keep
    data class CategoryItem(
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("id") val id: String?
    )

    @Keep
    data class TabData(
        @SerializedName("title") val title: String?,
        @SerializedName("key") val key: String?,
        @SerializedName("is_selected") var isSelected: Boolean?,
    )

    @Keep
    data class ButtonAction(
        @SerializedName("group_id") var groupId: String?,
        @SerializedName("bg_stroke_color") val bgStrokeColor: String?,
        @SerializedName("text_one") val textOne: String?,
        @SerializedName("text_one_size") val textOneSize: String?,
        @SerializedName("text_one_color") val textOneColor: String?,
        @SerializedName("deep_link", alternate = ["deeplink"]) val deepLink: String?
    )

    override fun getViewBinding(): WidgetParentAutoplayBinding {
        return WidgetParentAutoplayBinding.inflate(LayoutInflater.from(context), this, true)
    }
}
