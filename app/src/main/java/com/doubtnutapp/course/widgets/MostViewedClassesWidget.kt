package com.doubtnutapp.course.widgets

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.core.view.isVisible
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
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetShownEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.AutoPlayVideoCompleted
import com.doubtnutapp.base.FeedDNVideoWatched
import com.doubtnutapp.base.FeedPinnedVideoItemVisible
import com.doubtnutapp.base.MuteAutoPlayVideo
import com.doubtnutapp.databinding.WidgetMostViewedClassesBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.matchquestion.ui.fragment.MatchQuestionFragment
import com.doubtnutapp.rvexoplayer.RecyclerViewExoPlayerHelper
import com.doubtnutapp.rvexoplayer.RvMuteStatus
import com.doubtnutapp.utils.NetworkUtil
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MostViewedClassesWidget(context: Context) :
    BaseBindingWidget<MostViewedClassesWidget.WidgetHolder,
        MostViewedClassesWidget.Model,
        WidgetMostViewedClassesBinding>(context) {

    companion object {
        const val TAG = "MostViewedClassesWidget"
        const val EVENT_TAG = "most_viewed_classes_widget"
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
    private var autoScrollChildJob: Job? = null

    var isAutoScrollWidgetInteractionDone = false
    var isAutoScrollChildInteractionDone = false

    private lateinit var data: MostViewedClassesWidgetData
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

    private fun startVideo() {
        rVExoPlayerHelper?.playCurrent(widgetViewHolder.binding.recyclerView)
    }

    private fun stopVideo() {
        rVExoPlayerHelper?.stopCurrent()
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: Model
    ): WidgetHolder {
        super.bindWidget(holder, model)
        data = model.data
        val binding = widgetViewHolder.binding

        binding.root.applyBackgroundColor(data.bgColor)
        binding.viewBackgroundContainer.applyBackgroundColor(data.bgColor2)

        binding.tvTitle.isVisible = data.title?.isNotBlank() == true
        TextViewUtils.setTextFromHtml(binding.tvTitle, data.title.orEmpty())
        binding.tvTitle.applyTextSize(data.titleTextSize)
        binding.tvTitle.applyTextColor(data.titleTextColor)

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
        binding.recyclerView.addRvRequestDisallowInterceptTouchEventListener()

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

                            data.defaultMute = action.isMute

                            val muteStatus = RvMuteStatus(action.isMute)
                            childAdapter?.notifyItemRangeChanged(
                                0,
                                childAdapter?.itemCount
                                    ?: 0,
                                muteStatus
                            )
                            data.items?.filterIsInstance<ExplorePromoWidget.Model>()
                                ?.forEach {
                                    it.data.defaultMute = action.isMute
                                }
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
            this.adapter = childAdapter
            if (onFlingListener == null) {
                val snapHelper = PagerSnapHelper()
                snapHelper.attachToRecyclerView(this)
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

        updateTabData(binding, model)

        DoubtnutApp.INSTANCE.bus()?.send(WidgetShownEvent(model.extraParams))

        trackingViewId = data.id

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
        binding: WidgetMostViewedClassesBinding,
        model: Model
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
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.HOME_PAGE_AUTOPLAY_STOP_REASON_SLOW_INTERNET))
        }

        childAdapter?.setWidgets(items.orEmpty())
        rVExoPlayerHelper?.attachToRecyclerView(binding.recyclerView)

        val tabData = data.getSelectedTabData()
        binding.tvTeacherTitle.isVisible = tabData?.teacherTitle.isNotNullAndNotEmpty()
        binding.tvTeacherTitle.text = tabData?.teacherTitle
        binding.tvTeacherTitle.applyTextSize(tabData?.teacherTitleSize)
        binding.tvTeacherTitle.applyTextColor(tabData?.teacherTitleColor)

        binding.tvTeacherSubtitle.isVisible = tabData?.teacherTitleTwo.isNotNullAndNotEmpty()
        binding.tvTeacherSubtitle.text = tabData?.teacherTitleTwo
        binding.tvTeacherSubtitle.applyTextSize(tabData?.teacherTitleTwoSize)
        binding.tvTeacherSubtitle.applyTextColor(tabData?.teacherTitleTwoColor)

        binding.rvTeacherDetails.isVisible = tabData?.items?.isNotEmpty() ?: false
        binding.rvTeacherDetails.adapter =
            CourseDetailsWidgetItemAdapter(tabData?.items.orEmpty()) { itemCourseDetailsBinding ->
                itemCourseDetailsBinding.tvTitle.updateMargins(start = 4.dpToPx())
                itemCourseDetailsBinding.ivImage.layoutParams?.apply {
                    val buttonAction = tabData?.action
                    width = buttonAction?.textOneSize?.toInt()?.dpToPx() ?: 14.dpToPx()
                    height = buttonAction?.textOneSize?.toInt()?.dpToPx() ?: 14.dpToPx()
                    itemCourseDetailsBinding.ivImage.requestLayout()
                }
            }

        if (binding.rvTeacherDetails.onFlingListener == null) {
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(binding.rvTeacherDetails)
        }

        (context as? AppCompatActivity)?.let { appCompatActivity ->

            binding.rvTeacherDetails.addOnItemTouchListener(object :
                    RecyclerView.SimpleOnItemTouchListener() {
                    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                        if (rv.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
                            isAutoScrollChildInteractionDone = true
                        }
                        return super.onInterceptTouchEvent(rv, e)
                    }
                })

            binding.rvTeacherDetails.smoothScrollToPosition(0)

            autoScrollChildJob?.cancel()
            autoScrollChildJob = appCompatActivity.lifecycleScope.launchWhenResumed {
                autoRotateChild(
                    recyclerView = binding.rvTeacherDetails,
                    delayInSec = 3,
                    lastIndex = (binding.rvTeacherDetails.adapter?.itemCount ?: 0) - 1
                )
            }

            if (tabData?.action == null) {
                binding.btnAction.hide()
            } else {
                appCompatActivity.lifecycleScope.launchWhenResumed {
                    delay(5000)
                    binding.btnAction.show()
                }
            }
        }

        binding.ivTeacher.isVisible = tabData?.teacherImageUrl.isNotNullAndNotEmpty()
        binding.ivTeacher.loadImage(tabData?.teacherImageUrl)

        val buttonAction = tabData?.action
        buttonAction?.let {
            binding.btnAction.text = buttonAction.textOne
            binding.btnAction.applyTextColor(buttonAction.textOneColor)
            binding.btnAction.applyTextSize(buttonAction.textOneSize)
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

    private suspend fun autoRotateChild(
        recyclerView: RecyclerView,
        delayInSec: Long,
        lastIndex: Int
    ) {
        delay(TimeUnit.SECONDS.toMillis(delayInSec))
        if (isAutoScrollChildInteractionDone) {
            isAutoScrollChildInteractionDone = false
        } else {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val visibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            val scrollToPosition =
                if (visibleItemPosition == lastIndex) 0 else visibleItemPosition + 1
            recyclerView.smoothScrollToPosition(scrollToPosition)
        }
        autoRotateChild(recyclerView, delayInSec, lastIndex)
    }

    class WidgetHolder(
        binding: WidgetMostViewedClassesBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetMostViewedClassesBinding>(binding, widget)

    class Model :
        WidgetEntityModel<MostViewedClassesWidgetData, WidgetAction>()

    @Keep
    data class MostViewedClassesWidgetData(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_size") val titleTextSize: String?,
        @SerializedName("title_text_color") val titleTextColor: String?,

        @SerializedName("auto_play") val autoPlay: Boolean?,
        @SerializedName("auto_play_initiation") val autoPlayInitiation: Long?,
        @SerializedName("auto_play_duration") val autoPlayDuration: Long?,
        @SerializedName("buffer_duration") val bufferDuration: Long?,
        @SerializedName("scroll_direction") val scrollDirection: String?,
        @SerializedName("full_width_cards") val fullWidthCards: Boolean = false,
        @SerializedName("default_mute") var defaultMute: Boolean?,
        @SerializedName("play_strategy") val playStrategy: Float?,
        @SerializedName("clip_to_padding") var clipToPadding: Boolean?,

        // Using Nullable to prevent inconsistent data.
        @SerializedName("items") val items: List<WidgetEntityModel<*, *>?>?,
        @SerializedName("auto_scroll_time_in_sec") val autoScrollTimeInSec: Long?,
        @SerializedName("show_item_count") val showItemCount: Boolean?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("bg_color2") val bgColor2: String?,

        @SerializedName("tabs") val tabs: List<TabData>?,
        @SerializedName("actions") val actions: List<ButtonAction>?,

        var selectedPagePosition: Int = 0,
        var flagrId: String?,
        var variantId: String?
    ) : WidgetData() {

        fun getItemByGroupId() = items?.filterNotNull()
            ?.filter { model ->
                model.groupId == getSelectedTabData()?.key
            }

        fun getSelectedTabData() = tabs?.firstOrNull { tabData -> tabData.isSelected == true }
    }

    @Keep
    data class TabData(
        @SerializedName("title") val title: String?,
        @SerializedName("key") val key: String?,
        @SerializedName("is_selected") var isSelected: Boolean?,

        @SerializedName("teacher_title") val teacherTitle: String?,
        @SerializedName("teacher_title_size") val teacherTitleSize: String?,
        @SerializedName("teacher_title_color") val teacherTitleColor: String?,
        @SerializedName("teacher_image_url") val teacherImageUrl: String?,
        @SerializedName("teacher_title_two") val teacherTitleTwo: String?,
        @SerializedName("teacher_title_two_size") val teacherTitleTwoSize: String?,
        @SerializedName("teacher_title_two_color") val teacherTitleTwoColor: String?,
        @SerializedName("items") val items: List<CourseDetailsWidgetDataItem>?,
        @SerializedName("action") val action: ButtonAction?
    )

    @Keep
    data class ButtonAction(
        @SerializedName("text_one") val textOne: String?,
        @SerializedName("text_one_size") val textOneSize: String?,
        @SerializedName("text_one_color") val textOneColor: String?,
        @SerializedName("deep_link", alternate = ["deeplink"]) val deepLink: String?
    )

    override fun getViewBinding(): WidgetMostViewedClassesBinding {
        return WidgetMostViewedClassesBinding.inflate(LayoutInflater.from(context), this, true)
    }
}
