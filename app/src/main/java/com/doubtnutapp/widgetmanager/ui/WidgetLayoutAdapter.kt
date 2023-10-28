package com.doubtnutapp.widgetmanager.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.StickyHeaders
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.widgets.IWidgetLayoutAdapter
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreWidgetVH
import com.doubtnutapp.course.widgets.AutoPlayChildWidget
import com.doubtnutapp.course.widgets.CourseAutoPlayChildWidget
import com.doubtnutapp.course.widgets.ExplorePromoWidget
import com.doubtnutapp.course.widgets.VideoAutoplayChildWidget2
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.filterValuesNotNull
import com.doubtnutapp.referral.ReferralVideoWidget
import com.doubtnutapp.rvexoplayer.RvMuteStatus
import com.doubtnutapp.widgetmanager.WidgetFactory
import com.doubtnutapp.widgetmanager.widgets.FeedPinnedVideoAutoplayChildWidget
import com.doubtnutapp.widgetmanager.widgets.SgGroupChatWidget
import com.doubtnutapp.widgetmanager.widgets.VideoBannerAutoplayChildWidget
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.concurrent.ConcurrentHashMap

class WidgetLayoutAdapter(
    private val context: Context,
    actionPerformer: ActionPerformer? = null,
    source: String? = null
) : IWidgetLayoutAdapter(actionPerformer, source), StickyHeaders {

    var widgets = ArrayList<WidgetEntityModel<*, *>>()

    private val widgetMap = hashMapOf<Int, String>()
    private var trackingBus: ViewTrackingBus? = null
    private var lifecycleOwner: LifecycleOwner? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoreWidgetVH {
        return WidgetFactory.createViewHolder(
            context = context,
            parent = parent,
            type = widgetMap[viewType]!!,
            actionsPerformerListener = actionPerformer,
            source = source,
            lifecycleOwner = lifecycleOwner
        )!!
    }

    override fun onBindViewHolder(holder: CoreWidgetVH, position: Int) {
        WidgetFactory.bindViewHolder(
            holder = holder,
            widget = widgets[position],
            adapter = this
        )
    }

    override fun getItemCount(): Int {
        return widgets.size
    }

    override fun getItemViewType(position: Int): Int {
        return widgets[position].type.hashCode()
    }

    override fun onBindViewHolder(holder: CoreWidgetVH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            if (holder is VideoBannerAutoplayChildWidget.WidgetHolder
                || holder is ReferralVideoWidget.WidgetHolder
                || holder is AutoPlayChildWidget.WidgetHolder
                || holder is CourseAutoPlayChildWidget.WidgetHolder
                || holder is FeedPinnedVideoAutoplayChildWidget.WidgetHolder
                || holder is VideoAutoplayChildWidget2.WidgetHolder
                || holder is ExplorePromoWidget.WidgetViewHolder
            ) {
                payloads.filterIsInstance<RvMuteStatus>().forEach {
                    holder.bindItemPayload(it)
                }
            } else if (holder is SgGroupChatWidget.WidgetHolder) {
                payloads.filterIsInstance<Boolean>().forEach {
                    holder.bindItemPayload(it)
                }
            }
        }
    }

    fun addWidget(widget: WidgetEntityModel<*, *>) {
        widgets.add(widget)
        widgetMap[widget.type.hashCode()] = widget.type
        notifyItemChanged(widgets.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeWidget(widget: WidgetEntityModel<*, *>, applyNotifyDataSetChanged: Boolean = false) {
        try {
            if (widgets.contains(widget)) {
                val index = widgets.indexOf(widget)
                widgets.removeAt(index)
                if (applyNotifyDataSetChanged) {
                    notifyDataSetChanged()
                } else {
                    notifyItemRemoved(index)
                }
            }
        } catch (e: Exception) {
            // in case the view is computing or scrolling.
        }
    }

    fun addWidgetToPosition(
        widget: WidgetEntityModel<*, *>,
        index: Int,
        checkSize: Boolean = true
    ) {
        if ((checkSize && index < widgets.size) || checkSize.not()) {
            widgets.add(index, widget)
            widgetMap[widget.type.hashCode()] = widget.type
            notifyItemInserted(index)
        }
    }

    override fun addWidgets(widgets: List<WidgetEntityModel<*, *>?>) {
        for (widget in widgets) {
            if (widget != null) {
                this.widgets.add(widget)
                widgetMap[widget.type.hashCode()] = widget.type
            }
        }
        notifyDataSetChanged()
    }

    fun addWidgetsToBottom(widgets: List<WidgetEntityModel<*, *>?>) {
        val positionStart = this.widgets.size
        for (widget in widgets) {
            if (widget != null) {
                this.widgets.add(widget)
                widgetMap[widget.type.hashCode()] = widget.type
            }
        }
        notifyItemRangeInserted(positionStart, this.widgets.size)
    }

    fun addWidgetToPosition0(widget: WidgetEntityModel<*, *>) {
        addWidgetToPosition(widget, 0, false)
    }

    override fun setWidgets(widgets: List<WidgetEntityModel<*, *>?>) {
        this.widgets.clear()
        for (widget in widgets) {
            if (widget != null) {
                this.widgets.add(widget)
                widgetMap[widget.type.hashCode()] = widget.type
            }
        }
        notifyDataSetChanged()
    }

    override fun setWidget(widget: WidgetEntityModel<*, *>?) {
        setWidgets(listOf(widget))
    }

    fun removeWidget(type: String) {
        val indexOfWidget = this.widgets.indexOfFirst { it.type == type }
        if (indexOfWidget == RecyclerView.NO_POSITION) return
        this.widgets.removeAt(indexOfWidget)
        notifyItemRemoved(indexOfWidget)
    }

    fun removeWidgetAt(indexOfWidget: Int) {
        this.widgets.removeAt(indexOfWidget)
        notifyItemRemoved(indexOfWidget)
    }

    fun getWidget(type: String) =
        this.widgets.find {
            it.type == type
        }

    fun clearData() {
        this.widgets.clear()
        widgetMap.clear()
        notifyDataSetChanged()
    }

    override fun isStickyHeader(position: Int): Boolean {
        return widgets.getOrNull(position)?.isSticky == true
    }

    override fun onViewAttachedToWindow(holder: CoreWidgetVH) {
        super.onViewAttachedToWindow(holder)
        holder.onViewAttachedToWindow()
        trackViewAdded(holder)
    }

    override fun onViewDetachedFromWindow(holder: CoreWidgetVH) {
        super.onViewDetachedFromWindow(holder)
        holder.onViewDetachedFromWindow()
        trackViewRemoved(holder)
    }

    fun registerViewTracking(trackingBus: ViewTrackingBus) {
        this.trackingBus = trackingBus
    }

    fun attachLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner
    }

    private fun trackViewAdded(holder: CoreWidgetVH) {
        if (trackingBus == null) return
        val position = holder.bindingAdapterPosition
        if (position >= 0) {
            try {
                widgets[position].let {
                    val paramMap: ConcurrentHashMap<String, Any> = ConcurrentHashMap<String, Any>()
                    paramMap.apply {
                        put(EventConstants.WIDGET_TYPE, it.type)
                        putAll(it.extraParams.filterValuesNotNull())
                    }
                    trackingBus?.trackViewAdded(
                        position = position,
                        id = it.trackingViewId.orEmpty(),
                        params = HashMap(paramMap)
                    )
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().log(e.message.orEmpty())
            }
        }
    }

    fun trackViewRemoved(holder: CoreWidgetVH) {
        if (trackingBus == null) return
        val position = holder.bindingAdapterPosition
        if (position >= 0) {
            try {
                (widgets[position]).let {
                    val paramMap: ConcurrentHashMap<String, Any> = ConcurrentHashMap<String, Any>()
                    paramMap.apply {
                        put(EventConstants.WIDGET_TYPE, it.type)
                        putAll(it.extraParams.filterValuesNotNull())
                    }
                    trackingBus?.trackViewRemoved(
                        position = position,
                        id = it.trackingViewId.orEmpty(),
                        params = HashMap(paramMap)
                    )
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().log(e.message.orEmpty())
            }
        }
    }
}