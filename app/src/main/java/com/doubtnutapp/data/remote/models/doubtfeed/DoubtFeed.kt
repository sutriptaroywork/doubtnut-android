package com.doubtnutapp.data.remote.models.doubtfeed

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 7/5/21.
 */

@Keep
data class DoubtFeed(
    @SerializedName("heading") var heading: String,
    @SerializedName("topics") val topics: List<Topic>?,
    @SerializedName("carousels") val carousels: List<WidgetEntityModel<*, *>>,
    @SerializedName("bottom_sheet_data") val bottomSheetData: List<WidgetEntityModel<*, *>>,
    @SerializedName("back_press_popup_data") val backPressPopupData: DoubtFeedBackPressPopupData,
    @SerializedName("is_previous") val isPrevious: Boolean,
)
