package com.doubtnutapp.newlibrary.model

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 07/12/21
 */

@Keep
data class LibraryPreviousYearSelectionResponse(
    @SerializedName("exam_id") val examId: String,
    @SerializedName("tab_id") val tabId: String,
    @SerializedName("filter_id") val filterId: String,
    @SerializedName("filter_data_type") val filterDataType: String,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("exam_widget_data") val examWidgetData: List<WidgetEntityModel<*, *>>
)