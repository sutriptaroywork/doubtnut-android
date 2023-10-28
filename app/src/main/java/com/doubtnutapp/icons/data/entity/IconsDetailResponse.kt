package com.doubtnutapp.icons.data.entity

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class IconsDetailResponse(
    @SerializedName("header_title")
    val headerTitle: String?,

    @SerializedName("deeplink")
    val deeplink: String?,
    @SerializedName("finish_activity")
    val finishActivity: Boolean?,

    @SerializedName("widgets")
    val widgets: List<WidgetEntityModel<*, *>>?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>?
)