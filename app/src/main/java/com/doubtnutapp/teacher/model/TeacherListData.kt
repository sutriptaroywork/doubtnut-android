package com.doubtnutapp.teacher.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

@Keep
data class TeacherListData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>
)