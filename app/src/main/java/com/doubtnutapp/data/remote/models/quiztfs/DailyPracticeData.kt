package com.doubtnutapp.data.remote.models.quiztfs

import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 02-09-2021
 */

data class DailyPracticeData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>,
    @SerializedName("toolbar_title") val toolbarTitle: String?,
    @SerializedName("toolbar_deeplink") val toolbarDeeplink: String?,
    @SerializedName("page_title") val pageTitle: String?,
    @SerializedName("bg_color") val bgColor: String?,
)