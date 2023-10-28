package com.doubtnutapp.domain.videopageliveclass.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 26/10/20.
 */

@Keep
data class ApiVideoPageSimilarLiveClass(
    @SerializedName("statusTags") val statusTags: List<ApiLiveClassStatus>,
    @SerializedName("questions") val widgets: List<WidgetEntityModel<*, *>>,
    @SerializedName("viewAllLink") val viewAllLink: String,
    @SerializedName("title_text") val titleText: String
)
