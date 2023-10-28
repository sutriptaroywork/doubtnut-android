package com.doubtnutapp.similarVideo.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnut.core.widgets.entities.WidgetEntityModel

@Keep
data class SimilarWidgetViewItem(
    val widget: WidgetEntityModel<*, *>,
    override val viewType: Int = widget.type.hashCode()
) : RecyclerViewItem {
    companion object {
        const val type: String = "widget"
    }
}