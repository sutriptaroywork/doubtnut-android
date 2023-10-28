package com.doubtnutapp.shorts.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class ShortsListData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>,
    @SerializedName("last_id", alternate = ["lastId"]) val lastId: String?,
    @SerializedName("categoryPage", alternate = ["category_page"]) val categoryPage: Boolean?
)