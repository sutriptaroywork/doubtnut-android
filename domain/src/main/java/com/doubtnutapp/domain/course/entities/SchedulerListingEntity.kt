package com.doubtnutapp.domain.course.entities

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 23/12/21
 */

@Keep
data class SchedulerListingEntity(
    @SerializedName("title") val title: String,
    @SerializedName("filter_widgets") val filterWidgets: List<WidgetEntityModel<*, *>>,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>
)