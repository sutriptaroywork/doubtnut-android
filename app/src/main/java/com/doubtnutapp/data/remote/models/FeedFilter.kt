package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class FeedFilter(
    @SerializedName("description") val description: String,
    @SerializedName("key") val key: String?,
    var isSelected: Boolean = false
)
