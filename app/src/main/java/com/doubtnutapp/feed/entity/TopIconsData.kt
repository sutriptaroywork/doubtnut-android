package com.doubtnutapp.feed.entity

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class TopIconsData(

    @SerializedName("title")
    val title: String?,

    @SerializedName("widgets")
    val widgets: List<WidgetEntityModel<*, *>>?,
)