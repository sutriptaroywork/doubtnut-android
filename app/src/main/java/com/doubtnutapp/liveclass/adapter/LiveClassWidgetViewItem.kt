package com.doubtnutapp.liveclass.adapter

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class LiveClassWidgetViewItem(
    @SerializedName("widget") val widget: WidgetEntityModel<*, *>,
    override val viewType: Int = widget.type.hashCode()
) : RecyclerViewItem {
    companion object {
        const val type: String = "widget"
    }
}