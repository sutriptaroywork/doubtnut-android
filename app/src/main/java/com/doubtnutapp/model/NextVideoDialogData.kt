package com.doubtnutapp.model

import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

data class NextVideoDialogData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("title") val title: String?,
    @SerializedName("scroll_position") val scrollPosition: Int?
)
