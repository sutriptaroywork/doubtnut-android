package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SearchTabsEntity(
    val description: String,
    val key: String,
    val isVip: Boolean,
    @SerializedName("filterList")
    val filterList: ArrayList<SearchFilter>?
)
