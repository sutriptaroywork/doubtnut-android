package com.doubtnutapp.common.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
data class FilterListData(
    @SerializedName("title") val title: String?,
    @SerializedName("filter_id") val filterId: String?,
    @SerializedName("list") val list: List<FilterListItem>?,
    @SerializedName("cta") val cta: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("is_multi_select") val isMultiSelect: Boolean = false
) {
    @Parcelize
    data class FilterListItem(
        @SerializedName("title") val title: String?,
        @SerializedName("filter_id") val filterId: String?,
        @SerializedName("is_selected") var isSelected: Boolean = false,
    ) : Parcelable
}