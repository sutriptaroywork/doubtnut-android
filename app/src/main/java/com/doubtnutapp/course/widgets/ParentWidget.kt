package com.doubtnutapp.course.widgets

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.IntentUtils
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetClickedEvent
import com.doubtnutapp.EventBus.WidgetShownEvent
import com.doubtnutapp.EventBus.WidgetSwipeEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.data.remote.models.LayoutPadding
import com.doubtnutapp.databinding.WidgetParentCourseBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.feed.viewmodel.FeedViewModel
import com.doubtnutapp.home.recyclerdecorator.HorizontalSpaceItemDecoration
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.doubtnutapp.widgets.itemdecorator.SimpleDividerItemDecoration
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 01/10/20.
 */
class ParentWidget(context: Context) : BaseBindingWidget<ParentWidget.WidgetHolder,
        ParentWidget.WidgetChildModel, WidgetParentCourseBinding>(context) {

    companion object {
        const val TAG = "ParentWidget"
        private const val DEFAULT_GRID_SPAN_COUNT = 2
        const val DNR_REWARD_BANNER = "dnr_reward_banner"

    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher
    var source: String? = null
    var isAutoScrollEnabled = false
    var isAutoScrollWidgetInteractionDone = false

    private var viewTrackingBus: ViewTrackingBus? = null
    private var extraParamsForViewTracking = mutableMapOf<String, Any>()

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
        setupViewTracking()
    }

    override fun getViewBinding(): WidgetParentCourseBinding {
        return WidgetParentCourseBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun onDetachedFromWindow() {
        viewTrackingBus?.unsubscribe()
        super.onDetachedFromWindow()
    }

    private fun setupViewTracking() {
        if (viewTrackingBus == null) {
            viewTrackingBus = ViewTrackingBus(
                { state ->
                    when (state.state) {
                        ViewTrackingBus.VIEW_ADDED -> {
                            eventWith(
                                StructuredEvent(
                                    category = EventConstants.VIEW_INSIDE_PARENT_WIDGET,
                                    action = EventConstants.EVENT_VIEW_ADDED,
                                    label = state.trackId,
                                    property = state.position.toString(),
                                    eventParams = state.trackParams.apply {
                                        putAll(extraParamsForViewTracking)
                                    }
                                )
                            )
                        }
                    }
                },
                {
                }
            )
        } else {
            if (viewTrackingBus?.isPaused() == true) {
                viewTrackingBus?.resume()
            }
        }
    }

    override fun bindWidget(holder: WidgetHolder, model: WidgetChildModel): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(
                    marginTop = 0,
                    marginBottom = 0,
                    marginLeft = 0,
                    marginRight = 0
                )
            }
        )
        val data: WidgetChildData = model.data
        val binding = holder.binding

        // Put extraParams so that we can pass to view tracking
        extraParamsForViewTracking.putAll(model.extraParams ?: hashMapOf())

        binding.textViewTitleMain.apply {
            binding.textViewTitleMain.setVisibleState(!data.title.isNullOrEmpty())
            val title = data.title ?: ""
            val charToReplace = data.titleCharToReplace
            text = if (charToReplace.isNullOrEmpty().not() &&
                title.isEmpty().not() &&
                title.contains(charToReplace!!)
            ) {
                val newSubtitle =
                    title.replace(charToReplace, data.stringTitleToReplaceWith.orEmpty())
                setSpan(
                    newSubtitle,
                    data.stringTitleToReplaceWith.orEmpty(),
                    data.stringToReplaceWithColor
                )
            } else {
                title
            }
            if (data.titleTextSize != null) {
                textSize = data.titleTextSize
            }
            if (data.isTitleBold == true) {
                setTypeface(typeface, Typeface.BOLD)
            }
            maxLines = data.titleTextMaxLine ?: 1
            setTextColor(Utils.parseColor(data.titleTextColor, Color.BLACK))
        }

        binding.tvTitleSecondary.apply {
            isVisible = data.secondaryTitle.isNotNullAndNotEmpty()
            text = data.secondaryTitle
        }

        model.data.imageBanner?.let { image ->
            if (image.isNotEmpty()) {
                binding.imageBanner.visibility = View.VISIBLE
                binding.imageBanner.loadImage(image)
            }
            model.data.imageBannerHeight?.let { imageHeight ->
                binding.imageBanner.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    height = imageHeight.toInt().dpToPx()
                }

            }
            if (model.data.imageDeeplink.isNotNullAndNotEmpty()) {
                binding.imageBanner.setOnClickListener {
                    deeplinkAction.performAction(context, model.data.imageDeeplink)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent("${DNR_REWARD_BANNER}_${EventConstants.EVENT_IMAGE_CLICK}")
                    )
                }
            }
        } ?: run {
            binding.imageBanner.visibility = View.GONE
        }


        binding.textViewSubtitle.apply {
            text = data.subtitle.orEmpty()
            setVisibleState(!data.subtitle.isNullOrEmpty())
            setTextColor(
                Utils.parseColor(
                    data.subtitleTextColor,
                    Color.BLACK
                )
            )
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

        binding.ivIcon2.apply {
            data.topIcon2?.let {
                show()
                loadImage(data.topIcon2)
            } ?: hide()

            setOnClickListener {
                val extraParams = if (model.extraParams == null) hashMapOf() else model.extraParams
                extraParams?.put(Constants.WIDGET_CLICK_SOURCE, Constants.WIDGET_PARENT_TOP_ICON_2)

                DoubtnutApp.INSTANCE.bus()?.send(
                    WidgetClickedEvent(
                        extraParams = extraParams,
                        widgetType = WidgetTypes.TYPE_WIDGET_PARENT
                    )
                )
            }
        }

        binding.tvViewAll.apply {
            text = data.bottomRightText.orEmpty()
            textSize = data.bottomRightTextSize ?: 12f
            setOnClickListener {
                deeplinkAction.performAction(context, data.bottomRightDeeplink.orEmpty())
            }
            setVisibleState(data.bottomRightText.isNotNullAndNotEmpty())
        }

        binding.tvBottomText.apply {
            text = data.bottomText.orEmpty()
            setVisibleState(!data.bottomText.isNullOrEmpty())
            if (data.bottomTextSize != null) {
                textSize = data.bottomTextSize
            }

            data.helplineNumber?.let {
                setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_small_phone, 0, 0, 0)
                compoundDrawables[0]?.setTint(ContextCompat.getColor(context, R.color.dark_two))
                compoundDrawablePadding = 4.dpToPx()
                setOnClickListener {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:" + data.helplineNumber.orEmpty())
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // No Activity found to handle Intent { act=android.intent.action.DIAL dat=tel:xxxxxxxxxxx }
                        IntentUtils.showCallActionNotPerformToast(
                            context,
                            data.helplineNumber.orEmpty()
                        )
                    }
                }
            }
        }

        binding.bottomLine.apply {
            isVisible = data.isBottomTextUnderlined == true
        }

        try {
            val drawableEnd = ContextCompat.getDrawable(context, R.drawable.ic_arrow_forward)
            binding.textViewViewAll.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                drawableEnd,
                null
            )
        } catch (ignored: Exception) {
            // https://console.firebase.google.com/project/doubtnut-e000a/crashlytics/app/android:com.doubtnutapp/issues/4d05f70f8df16ae61cc321882d1d76e5?time=last-twenty-four-hours&sessionEventKey=60B0592103B900013B43609A2C4CE946_1545612299651154538
        }

        if (data.linkText.isNullOrBlank()) {
            binding.textViewViewAll.hide()
        } else {
            binding.textViewViewAll.show()
        }

        binding.textViewViewAll.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.EXPLORE_SEE_MORE +
                            "_" + EventConstants.WIDGET_ITEM_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.PARENT_TITLE to data.title.orEmpty(),
                        EventConstants.WIDGET to TAG,
                        EventConstants.SOURCE to source.orEmpty()
                    ).apply {
                        putAll(model.extraParams ?: HashMap())
                    }
                )
            )
            if (!data.deeplink.isNullOrBlank()) {
                deeplinkAction.performAction(context, data.deeplink)
            }
        }

        binding.textViewViewAll.text = data.linkText.orEmpty()
        binding.textViewViewAll.apply {
            if (data.isActionButtonTitleBold == true) {
                setTypeface(typeface, Typeface.BOLD)
            } else {
                setTypeface(typeface, Typeface.NORMAL)
            }
        }

        binding.recyclerView.updateMargins(
            top = (data.rvMarginTop ?: 12).dpToPx()
        )

        when (model.data.scrollDirection) {
            "grid" -> {
                binding.recyclerView.layoutManager =
                    GridLayoutManager(context, data.gridSpanCount ?: DEFAULT_GRID_SPAN_COUNT)
            }
            "vertical" -> {
                binding.recyclerView.layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.VERTICAL, false
                )
            }
            else -> {
                binding.recyclerView.layoutManager = LinearLayoutManager(
                    context,
                    RecyclerView.HORIZONTAL, false
                )
            }
        }
        val adapter = WidgetLayoutAdapter(context, actionPerformer, source.orEmpty())
        adapter.registerViewTracking(viewTrackingBus!!)
        binding.recyclerView.adapter = adapter
        data.items?.mapIndexed { index, widget ->
            if (widget != null) {
                if (widget.extraParams == null) {
                    widget.extraParams = hashMapOf()
                }
                widget.extraParams?.putAll(model.extraParams ?: HashMap())
                widget.extraParams?.put(EventConstants.ITEM_POSITION, index)
                source?.let { widget.extraParams?.put(EventConstants.SOURCE, it) }
                widget.extraParams?.put(EventConstants.PARENT_TITLE, data.title.orEmpty())
                widget.extraParams?.put(EventConstants.WIDGET_TYPE, widget.type)
                widget.extraParams?.put(EventConstants.PARENT_ID, data.id.orEmpty())
            }
        }
        adapter.setWidgets(data.items.orEmpty())

        // Only for LinearLayoutManager attached with recyclerview
        binding.recyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (data.items == null) return
                    val firstVisibleItem =
                        (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition()
                    val lastVisibleItem =
                        (recyclerView.layoutManager as? LinearLayoutManager)?.findLastCompletelyVisibleItemPosition()
                    if (firstVisibleItem == null || lastVisibleItem == null ||
                        firstVisibleItem == RecyclerView.NO_POSITION ||
                        lastVisibleItem == RecyclerView.NO_POSITION ||
                        firstVisibleItem > data.items.orEmpty().size - 1 ||
                        lastVisibleItem > data.items.orEmpty().size - 1
                    ) return

                    for (i in firstVisibleItem..lastVisibleItem) {
                        val item = data.items[i]
                        val params = hashMapOf<String, Any>()
                        params[EventConstants.ITEM_POSITION] = i + 1
                        params[EventConstants.WIDGET_TYPE] = item?.type.orEmpty()
                        params[EventConstants.SOURCE] = FeedViewModel.SOURCE_HOME
                        DoubtnutApp.INSTANCE.bus()?.send(WidgetSwipeEvent(params))
                    }
                }
            }
        })

        isAutoScrollEnabled = data.autoPlayDuration != null

        binding.recyclerView.onFlingListener = null
        val snapHelper = PagerSnapHelper()

        if (model.data.showIndicator == true || data.autoPlayDuration != null) {
            snapHelper.attachToRecyclerView(binding.recyclerView)
            binding.circleIndicator.visibility = View.VISIBLE
            binding.circleIndicator.attachToRecyclerView(binding.recyclerView)
            val indicatorMargin = data.indicatorMargin
            if (indicatorMargin != null) {
                binding.circleIndicator.setMargins(
                    left = indicatorMargin.marginLeft.dpToPx(),
                    top = indicatorMargin.marginTop.dpToPx(),
                    right = indicatorMargin.marginRight.dpToPx(),
                    bottom = indicatorMargin.marginBottom.dpToPx()
                )
            } else {
                binding.circleIndicator.setMargins(
                    left = 0.dpToPx(),
                    top = 12.dpToPx(),
                    right = 0.dpToPx(),
                    bottom = 0.dpToPx()
                )
            }
        } else {
            binding.circleIndicator.visibility = View.GONE
            snapHelper.attachToRecyclerView(null)
        }

        if (data.autoPlayDuration != null) {
            binding.recyclerView.apply {
                // Remove existing item decorations before adding new, to prevent crashes
                removeItemDecorations()

                // Touch listener to capture user interaction
                addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
                    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                        if (rv.scrollState == RecyclerView.SCROLL_STATE_DRAGGING) {
                            isAutoScrollWidgetInteractionDone = true
                        }
                        return super.onInterceptTouchEvent(rv, e)
                    }
                })
                // Add item decoration, dots indicator
                //val outValue = TypedValue()
                // resources.getValue(R.dimen.spacing_circle, outValue, true)
                //val spacing = outValue.float.dpToPx().toInt()
                // addItemDecoration(HorizontalSpaceItemDecoration(spacing))
            }

            val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
            var visibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            val delayDuration = data.autoPlayDuration

            // Autoscroll logic
            (context as? AppCompatActivity)?.lifecycleScope?.launchWhenResumed {
                while (true) {
                    delay(delayDuration)
                    if (isAutoScrollWidgetInteractionDone) {
                        isAutoScrollWidgetInteractionDone = false
                        // Add autoscroll delay after user interaction is done
                        delay(delayDuration)
                    }
                    if (data.items != null && data.items.isNotEmpty()) {
                        val newAdapterPosition = (visibleItemPosition + 1) % data.items.size
                        visibleItemPosition = newAdapterPosition
                        if (isAutoScrollEnabled)
                            holder.binding.recyclerView.smoothScrollToPosition(newAdapterPosition)
                    }
                }
            }
        } else {
            // Remove item decorations in case the view holder is being reused for non-autoscroll widget
            binding.recyclerView.removeItemDecorations()
        }

        binding.parentLayout.apply {
            background = Utils.getShape(
                colorString = data.backgroundColor ?: "#FFFFFF",
                strokeColor = data.borderColor ?: "#FFFFFF",
                cornerRadius = data.cornerRadius ?: 0F,
                strokeWidth = data.borderWidth ?: 0
            )
        }

        DoubtnutApp.INSTANCE.bus()?.send(WidgetShownEvent(model.extraParams))

        with(binding) {
            if (data.walletData != null) {
                walletCard.visibility = View.VISIBLE
                tvWalletTitle.text = data.walletData.title.orEmpty()
                tvPrice.text = data.walletData.price.orEmpty()
                ivWalletIcon.loadImageEtx(data.walletData.iconUrl.orEmpty())
                walletCard.background = Utils.getShape(
                    data.walletData.bgColor.orEmpty(),
                    data.walletData.bgColor.orEmpty(),
                    4f
                )
            } else {
                walletCard.visibility = View.GONE
            }
        }

        // Add itemDecorator in between parent widget list items
        if (data.showItemDecorator == true) {
            binding.recyclerView.addItemDecoration(
                SimpleDividerItemDecoration(
                    R.color.light_grey,
                    0.5F
                )
            )
        }
        if (data.removePadding == true) {
            binding.recyclerView.updatePadding(
                left = 0.dpToPx(),
                top = 0.dpToPx(),
                right = 0.dpToPx(),
                bottom = 0.dpToPx()
            )
        } else {
            if (data.layoutPadding != null) {
                binding.recyclerView.updatePadding(
                    left = (data.layoutPadding.paddingStart ?: 0).dpToPx(),
                    top = (data.layoutPadding.paddingTop ?: 0).dpToPx(),
                    right = (data.layoutPadding.paddingEnd ?: 0).dpToPx(),
                    bottom = (data.layoutPadding.paddingBottom ?: 0).dpToPx()
                )
            } else {
                binding.recyclerView.updatePadding(
                    left = 8.dpToPx(),
                    top = 0.dpToPx(),
                    right = 8.dpToPx(),
                    bottom = 0.dpToPx()
                )
            }
        }
        trackingViewId = data.id
        return holder
    }

    fun eventWith(snowplowEvent: StructuredEvent) {
        analyticsPublisher.publishEvent(snowplowEvent)
    }

    private fun setSpan(
        subtitle: String,
        stringToReplaceWith: String,
        stringToReplaceWithColor: String?
    ): SpannableStringBuilder {
        val builder = SpannableStringBuilder(subtitle)
        val startIndex = subtitle.indexOf(stringToReplaceWith)
        val lastIndex = startIndex + stringToReplaceWith.length

        val span = object : ClickableSpan() {
            override fun onClick(widget: View) {
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                if (stringToReplaceWithColor.isNullOrEmpty().not()) {
                    ds.color = Color.parseColor(stringToReplaceWithColor)
                }
            }
        }
        builder.setSpan(span, startIndex, lastIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return builder
    }

    class WidgetHolder(binding: WidgetParentCourseBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetParentCourseBinding>(binding, widget)

    class WidgetChildModel : WidgetEntityModel<WidgetChildData, WidgetAction>()

    @Keep
    data class WidgetChildData(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("image_banner") val imageBanner: String?,
        @SerializedName("image_deeplink") val imageDeeplink: String?,
        @SerializedName("image_banner_height") val imageBannerHeight: String?,
        @SerializedName("top_icon") val topIcon: String?,
        @SerializedName("top_icon_width") val topIconWidth: Int?,
        @SerializedName("top_icon_height") val topIconHeight: Int?,
        @SerializedName("title_text_size") val titleTextSize: Float?,
        @SerializedName("title_text_max_line") val titleTextMaxLine: Int?,
        @SerializedName("is_title_bold") val isTitleBold: Boolean?,
        @SerializedName("is_action_button_title_bold") val isActionButtonTitleBold: Boolean?,
        @SerializedName("link_text") val linkText: String?,
        @SerializedName("link_text_url_for_api") val linkTextUrlForApi: String?,
        @SerializedName("link_text_show_arrow") val showArrowIconForLinkText: Boolean?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("scroll_direction") val scrollDirection: String?,
        @SerializedName("rv_margin_top") val rvMarginTop: Int?,
        @SerializedName("bottom_text") val bottomText: String?,
        @SerializedName("bottom_text_size") val bottomTextSize: Float?,
        @SerializedName("is_bottom_text_underlined") val isBottomTextUnderlined: Boolean?,
        @SerializedName("helpline_number") val helplineNumber: String?,
        @SerializedName("items") val items: List<WidgetEntityModel<*, *>?>?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_text_color") val subtitleTextColor: String?,
        @SerializedName("title_text_color") val titleTextColor: String?,
        @SerializedName("autoplay_duration") val autoPlayDuration: Long?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("border_color") val borderColor: String?,
        @SerializedName("corner_radius") val cornerRadius: Float?,
        @SerializedName("border_width") val borderWidth: Int?,
        @SerializedName("wallet_data") val walletData: WalletData?,
        @SerializedName("show_indicator") val showIndicator: Boolean?,
        @SerializedName("indicator_margin") val indicatorMargin: WidgetLayoutConfig?,
        @SerializedName("show_item_decorator") val showItemDecorator: Boolean?,
        @SerializedName("secondary_title") val secondaryTitle: String?,
        @SerializedName("top_icon_2") val topIcon2: String?,
        @SerializedName("remove_padding") val removePadding: Boolean?,
        @SerializedName("layout_padding") val layoutPadding: LayoutPadding?,
        @SerializedName("html_title") val htmlTitle: String?,
        @SerializedName("string_title_to_replace_with") val stringTitleToReplaceWith: String?,
        @SerializedName("title_char_to_replace") val titleCharToReplace: String?,
        @SerializedName("string_title_to_replace_with_color") val stringToReplaceWithColor: String?,
        @SerializedName("grid_span_count") val gridSpanCount: Int?,
        @SerializedName("bottom_right_text") val bottomRightText: String?,
        @SerializedName("bottom_right_text_size") val bottomRightTextSize: Float?,
        @SerializedName("bottom_right_deeplink") val bottomRightDeeplink: String?,
    ) : WidgetData()

    @Keep
    data class WalletData(
        @SerializedName("title") val title: String?,
        @SerializedName("price") val price: String?,
        @SerializedName("bg_color") val bgColor: String?,
        @SerializedName("icon_url") val iconUrl: String?,
        @SerializedName("top_icon") val topIcon: String?
    )
}
