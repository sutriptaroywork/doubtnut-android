package com.doubtnutapp.newglobalsearch.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem
import com.google.gson.annotations.SerializedName

data class ApiSearchAdvanceFilterResponse(
        @SerializedName("title") val title: String,
        @SerializedName("tab_type") val tabType: String?,
        @SerializedName("list") val list : List<ApiSearchAdvanceFilterData>?)

@Keep
data class ApiSearchAdvanceFilterData(
        @SerializedName("key") val key: String,
        @SerializedName("value") val value: String?,
        @SerializedName("display") val display: String?,
        @SerializedName("list") val list : List<SearchFilterItem>?
)