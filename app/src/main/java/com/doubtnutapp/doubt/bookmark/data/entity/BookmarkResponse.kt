package com.doubtnutapp.doubt.bookmark.data.entity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BookmarkResponse(
    @SerializedName("message")
    val message: String?,
    @SerializedName("subtitle")
    val subtitle: String?,
)
