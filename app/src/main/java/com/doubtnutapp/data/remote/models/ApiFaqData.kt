package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class ApiFaqData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("header") val header: String?
)
