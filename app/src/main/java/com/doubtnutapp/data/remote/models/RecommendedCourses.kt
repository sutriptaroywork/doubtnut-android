package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 21/4/21.
 */

@Keep
data class RecommendedCourses(
    @SerializedName("courses") val courses: List<WidgetEntityModel<WidgetData, WidgetAction>>?,
)
