package com.doubtnutapp.ui.forum.doubtsugggester.models

import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

data class DoubtSuggesterData(
    @SerializedName("widgets")
    val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("count_suggestions")
    val suggestionCount:Int
)
