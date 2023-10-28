package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.google.gson.annotations.SerializedName

@Keep
data class ApiSearchTab(
    @SerializedName("description")
    val description: String?,
    @SerializedName("key")
    val key: String?,
    @SerializedName("is_vip")
    val isVip: Boolean?,
    @SerializedName("filterList")
    val filterList: ArrayList<SearchFilter>?

)
