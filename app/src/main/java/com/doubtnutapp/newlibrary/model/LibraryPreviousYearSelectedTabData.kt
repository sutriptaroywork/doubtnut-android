package com.doubtnutapp.newlibrary.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Mehul Bisht on 27/11/21
 */

@Keep
data class LibraryPreviousYearSelectedTabData(
    @SerializedName("selected_text") val selectedText: String,
    @SerializedName("selection_type") val selectionType: String,
    @SerializedName("year_selection_data") val yearSelectionData: List<LibraryByYearDates>,
    @SerializedName("shift_selection_data") val shiftSelectionData: List<WidgetEntityModel<*, *>>
)

@Keep
data class LibraryByYearDates(
    @SerializedName("date") val date: String,
    @SerializedName("is_selected") val isSelected: Boolean,
)

@Keep
@Parcelize
data class LibraryPreviousYearPapersFilter(
    @SerializedName("filter_text") val filterText: String,
    @SerializedName("filter_data_type") val filterDataType: String,
    @SerializedName("is_selected") var isSelected: Boolean,
    @SerializedName("filter_id") val filterId: String
) : Parcelable
