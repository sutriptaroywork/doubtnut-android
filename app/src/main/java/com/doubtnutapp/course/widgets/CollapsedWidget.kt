package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.WidgetShownEvent
import com.doubtnutapp.databinding.WidgetCollapsedBinding
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.doubtnutapp.widgets.itemdecorator.SimpleDividerItemDecoration
import com.google.gson.annotations.SerializedName
import kotlin.math.min

/**
 * Created by Sachin Kumar on 29/01/21.
 */
class CollapsedWidget(context: Context) : BaseBindingWidget<CollapsedWidget.WidgetHolder,
    CollapsedWidget.Model, WidgetCollapsedBinding>(context) {

    companion object {
        const val TAG = "CollapsedWidget"
    }

    private var viewTrackingBus: ViewTrackingBus? = null

    var source: String? = null

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

    override fun onDetachedFromWindow() {
        viewTrackingBus?.unsubscribe()
        super.onDetachedFromWindow()
    }

    override fun getViewBinding(): WidgetCollapsedBinding {
        return WidgetCollapsedBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(16, 16, 16, 16)
            }
        )
        val data: Data = model.data
        val binding = holder.binding
        data.cardRadius?.let { binding.collapsedCardView.radius = it.dpToPx() }
        data.cardElevation?.let {
            binding.collapsedCardView.cardElevation = it.dpToPx()
            binding.collapsedCardView.invalidateOutline()
        }

        data.cardCompatPadding?.let {
            binding.collapsedCardView.useCompatPadding = it
        }

        data.title?.let {
            binding.textViewTitleMain.show()
            binding.textViewTitleMain.text = it
        } ?: binding.textViewTitleMain.hide()
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(context, layoutManager.orientation)
        if (data.itemDecorator == true) {
            dividerItemDecoration.setDrawable(
                getDrawable(
                    context,
                    R.drawable.recyclerview_divider_layer
                ).forceUnWrap()
            )
            binding.recyclerView.addItemDecoration(dividerItemDecoration)
        } else {
            binding.recyclerView.removeItemDecoration(dividerItemDecoration)
        }

        val adapter = WidgetLayoutAdapter(context, actionPerformer, source).apply {
            viewTrackingBus?.let {
                registerViewTracking(it)
            }
        }
        binding.recyclerView.adapter = adapter

        if (data.showItemDecorator == true) {
            binding.recyclerView.addItemDecoration(
                SimpleDividerItemDecoration(
                    R.color.light_grey,
                    0.5F
                )
            )
        }

        data.items?.mapIndexed { index, widget ->
            if (widget.extraParams == null) {
                widget.extraParams = hashMapOf()
            }
            widget.extraParams?.putAll(model.extraParams ?: HashMap())
            widget.extraParams?.put(EventConstants.ITEM_POSITION, index)
            widget.extraParams?.put(EventConstants.PAGE, source.orEmpty())
            widget.extraParams?.put(EventConstants.ID, data.id.orEmpty())
            widget.extraParams?.put(EventConstants.SOURCE, source.orEmpty())
            widget.extraParams?.put(EventConstants.PARENT_TITLE, data.title.orEmpty())
        }

        val totalItems = data.items.orEmpty().toMutableList()
        data.nudges?.forEach {
            val index = it.key.toIntOrNull() ?: 0
            val widget = it.value
            if (index < totalItems.size) {
                totalItems.add(index, widget)
            } else {
                totalItems.add(widget)
            }
        }

        data.displayedItemCount?.let { displayedCount ->
            val itemsCount = totalItems.size
            val minItemsToShow = min(displayedCount, itemsCount)
            if (totalItems.size >= minItemsToShow) {
                adapter.setWidgets(totalItems.subList(0, minItemsToShow))
            }
            binding.tvShowMore.text = data.showMoreButtonText.orEmpty()
            data.showMoreButtonTextColor?.let {
                binding.tvShowMore.setTextColor(Color.parseColor(it))
            }
            data.showMoreButtonGravity?.let {
                binding.tvShowMore.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    0,
                    0
                )
                binding.tvShowMore.gravity = Gravity.END
                binding.tvShowMore.setMargins(
                    left = 0,
                    top = 12.dpToPx(),
                    right = 16.dpToPx(),
                    bottom = 0
                )
            }
            binding.tvShowMore.isVisible =
                totalItems.size > minItemsToShow
        } ?: adapter.setWidgets(totalItems)

        binding.tvShowMore.setOnClickListener {
            val remainingItems =
                totalItems.subList(data.displayedItemCount!!, totalItems.size)
            adapter.addWidgets(remainingItems)
            binding.tvShowMore.isVisible = false
        }

        DoubtnutApp.INSTANCE.bus()?.send(WidgetShownEvent(model.extraParams))
        trackingViewId = data.id
        return holder
    }

    class WidgetHolder(binding: WidgetCollapsedBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCollapsedBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("title_text_size") val titleTextSize: Float?,
        @SerializedName("is_title_bold") val isTitleBold: Boolean?,
        @SerializedName("displayed_item_count") val displayedItemCount: Int? = 0,
        @SerializedName("card_compat_padding") val cardCompatPadding: Boolean?,
        @SerializedName("card_radius") val cardRadius: Float?,
        @SerializedName("card_elevation") val cardElevation: Float?,
        @SerializedName("item_decorator") val itemDecorator: Boolean? = true,
        @SerializedName("scroll_direction") val scrollDirection: String?,
        @SerializedName("show_more_button_text") val showMoreButtonText: String?,
        @SerializedName("show_more_button_text_color") val showMoreButtonTextColor: String?,
        @SerializedName("show_more_button_gravity") val showMoreButtonGravity: String?,
        @SerializedName("show_item_decorator") val showItemDecorator: Boolean?,
        @SerializedName("items") var items: List<WidgetEntityModel<*, *>>?,
        @SerializedName("nudges") var nudges: Map<String, WidgetEntityModel<*, *>>?
    ) : WidgetData()
}
