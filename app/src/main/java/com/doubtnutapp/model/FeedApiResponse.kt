package com.doubtnutapp.model

import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

data class FeedApiResponse(
    @SerializedName("feeddata") val data: List<WidgetEntityModel<WidgetData, WidgetAction>>?,
    @SerializedName("offsetCursor") val offsetCursor: String
)