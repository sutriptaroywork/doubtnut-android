package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by Sachin Saxena on 18/01/22.
 */
@Keep
data class MatchPageNudgesData(
    @SerializedName("nudges") val nudges: Map<String, WidgetEntityModel<*, *>>?
)