package com.doubtnutapp.domain.newglobalsearch.entities

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class SearchFilter(
    @SerializedName("key") val key: String,
    @SerializedName("label") val label: String,
    @SerializedName("list") var filters: ArrayList<SearchFilterItem>,
    var isSelected: Boolean = false
) : Parcelable {
    // Used to maintain selection state in all filter screen
    var previousSelectedState: Boolean = false
    var isExpanded: Boolean = false
    var appliedLabel: String = label
}

@Keep
@Parcelize
data class SearchFilterItem(
    @SerializedName("value") val value: String,
    @SerializedName("label") val label: String?,
    @SerializedName("key") val key: String?,
    @SerializedName("order") val order: Int?,
    @SerializedName("is_selected") var isSelected: Boolean,
    @SerializedName("is_disabled") var isDisabled: Boolean? = null
) : Parcelable {
    // Used to maintain selection state in all filter screen
    var previousSelectedState: Boolean = false

    fun isDisabled(): Boolean {
        return isDisabled ?: false
    }

    fun display(): String {
        return label ?: value
    }

    fun toSearchFilterItemApiParam(): SearchFilterItemApiParam {
        return SearchFilterItemApiParam(value, key, order, isSelected)
    }
}

@Keep
@Parcelize
data class SearchFilterItemApiParam(
    @SerializedName("value") val value: String,
    @SerializedName("key") val key: String?,
    @SerializedName("order") val order: Int?,
    @SerializedName("is_selected") var isSelected: Boolean
) : Parcelable
