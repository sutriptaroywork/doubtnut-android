package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-07-29.
 */
@Keep
data class ApiAdvanceSearchData(
    @SerializedName("facetType") val facetType: String,
    @SerializedName("display") val display: String,
    @SerializedName("local") val local: Boolean,
    @SerializedName("isSelected") var isSelected: Boolean,
    @SerializedName("isMultiSelect") val isMultiSelect: Boolean,
    @SerializedName("showDisplayText") val showDisplayText: Boolean,
    @SerializedName("upperFocussed") val upperFocused: Boolean,
    @SerializedName("data") val data: List<ApiAdvanceSearchTopic>
)

@Keep
data class ApiAdvanceSearchTopic(
    @SerializedName("display") val display: String,
    @SerializedName("isSelected") var isSelected: Boolean,
    @SerializedName("isAll") val isAllTopic: Boolean,
    @SerializedName("type") val type: String?,
    @SerializedName("data") val data: JsonArray,
    @SerializedName("selectable") val selectable: Boolean?
)