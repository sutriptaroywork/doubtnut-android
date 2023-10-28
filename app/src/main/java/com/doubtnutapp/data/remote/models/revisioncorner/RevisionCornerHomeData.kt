package com.doubtnutapp.data.remote.models.revisioncorner

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 10/08/21.
 */

@Keep
data class RevisionCornerHomeData(
    @SerializedName("progress_report_icon") val progressReportIcon: String,
    @SerializedName("carousels") val carousels: List<WidgetEntityModel<*, *>>?,
    @SerializedName("rules") val rules: Map<String, RulesInfo>?
)
