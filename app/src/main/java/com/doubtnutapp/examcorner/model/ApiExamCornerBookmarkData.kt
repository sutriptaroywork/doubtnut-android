package com.doubtnutapp.examcorner.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class ApiExamCornerBookmarkData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>
)
