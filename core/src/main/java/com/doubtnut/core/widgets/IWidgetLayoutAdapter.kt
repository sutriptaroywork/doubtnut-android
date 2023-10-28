package com.doubtnut.core.widgets

import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreWidgetVH

abstract class IWidgetLayoutAdapter(
    var actionPerformer: ActionPerformer? = null,
    var source: String? = null
) : RecyclerView.Adapter<CoreWidgetVH>() {

    abstract fun setWidgets(widgets: List<WidgetEntityModel<*, *>?>)

    abstract fun setWidget(widget: WidgetEntityModel<*, *>?)

    abstract fun addWidgets(widgets: List<WidgetEntityModel<*, *>?>)

}