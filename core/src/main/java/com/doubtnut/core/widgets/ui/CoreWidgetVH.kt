package com.doubtnut.core.widgets.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class CoreWidgetVH(itemView: View?) : RecyclerView.ViewHolder(
    itemView!!
) {
    open val viewId: Int = View.NO_ID

    var trackingViewId: String? = null

    var trackingViewParams: HashMap<String, Any>? = null

    open fun bindItemPayload(payload: Any) {}

    open fun onViewAttachedToWindow() {}

    open fun onViewDetachedFromWindow() {}

}

abstract class WidgetBindingVH<VB : ViewBinding>(
    val binding: VB,
    widget: CoreWidget<*, *>
) : CoreWidgetVH(widget)