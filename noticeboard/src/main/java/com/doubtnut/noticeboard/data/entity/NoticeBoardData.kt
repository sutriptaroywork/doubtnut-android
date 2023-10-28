package com.doubtnut.noticeboard.data.entity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class NoticeBoardData(
    @SerializedName("list")
    val items: List<NoticeBoardItem>?,
    @SerializedName("empty_message")
    val emptyMessage: String? = null,
)