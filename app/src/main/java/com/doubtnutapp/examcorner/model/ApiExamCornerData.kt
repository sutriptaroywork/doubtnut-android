package com.doubtnutapp.examcorner.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class ApiExamCornerData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>
)
