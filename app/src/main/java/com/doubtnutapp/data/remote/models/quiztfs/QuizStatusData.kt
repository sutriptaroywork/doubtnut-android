package com.doubtnutapp.data.remote.models.quiztfs

import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 16-09-2021
 */

data class QuizStatusData(
    @SerializedName("title") val title: String,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>
)
