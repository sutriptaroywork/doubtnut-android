package com.doubtnutapp.home.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 05/01/22
 */

@Keep
data class ExploreMoreWidgetResponse(
    @SerializedName("position") val position: Int,
    @SerializedName("items") val items: List<WidgetEntityModel<*, *>>
)
