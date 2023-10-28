package com.doubtnutapp.domain.similarVideo.models

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class ApiRecommendedClasses(
    @SerializedName("data") val popularCourses: WidgetEntityModel<WidgetData, WidgetAction>,
)
