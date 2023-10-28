package com.doubtnutapp.data.remote.models.quiztfs

import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 08-09-2021
 */

data class HistoryData(
    @SerializedName("title") val pageTitle: String,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>
)
