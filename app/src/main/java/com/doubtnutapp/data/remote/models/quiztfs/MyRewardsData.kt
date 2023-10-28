package com.doubtnutapp.data.remote.models.quiztfs

import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 07-09-2021
 */
data class MyRewardsData(
    @SerializedName("page_title") val pageTitle: String,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>
)
