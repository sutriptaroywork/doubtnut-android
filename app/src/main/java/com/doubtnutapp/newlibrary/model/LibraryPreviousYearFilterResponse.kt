package com.doubtnutapp.newlibrary.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 07/12/21
 */

@Keep
data class LibraryPreviousYearFilterResponse(
    @SerializedName("exam_id") val examId: String,
    @SerializedName("tab_id") val tabId: String,
    @SerializedName("filter_id") val filterId: String,
    @SerializedName("filter_data_type") val filterDataType: String,
    @SerializedName("filter_text") val filterText: String?,
    @SerializedName("selected_filter_data") val selectedFilterData: List<SelectedFilterItem>
)

@Keep
data class SelectedFilterItem(
    @SerializedName("text") val text: String,
    @SerializedName("id") val id: String,
    @SerializedName("is_selected") var isSelected: Boolean,
)