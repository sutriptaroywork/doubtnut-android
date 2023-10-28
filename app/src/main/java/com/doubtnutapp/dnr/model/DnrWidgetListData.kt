package com.doubtnutapp.dnr.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class DnrWidgetListData(
    @SerializedName("toolbar_data") val toolbarData: DnrToolbarData?,
    @SerializedName("title") val title: String?,
    @SerializedName("is_widget_available") val isWidgetAvailable: Boolean?,
    @SerializedName(
        "widgets",
        alternate = ["milestones"]
    ) val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("no_widget_container") val noWidgetContainer: DnrNoWidgetContainer?,
    @SerializedName("is_reached_end") val isReachedEnd: Boolean?,
    @SerializedName("page", alternate = ["last_entry"]) val page: String?,
    @SerializedName("source") val source: String?,
)

@Keep
data class DnrNoWidgetContainer(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("image") val image: String?,
)
