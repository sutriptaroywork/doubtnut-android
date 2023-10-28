package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class CommentFilter(
    @SerializedName("title") val title: String,
    @SerializedName("filter") val filter: String?,
    @SerializedName("is_selected") var isSelected: Boolean?
)
