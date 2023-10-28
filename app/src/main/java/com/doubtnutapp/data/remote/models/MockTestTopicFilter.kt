package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class MockTestTopicFilter(
    @SerializedName("name") val topicName: String,
    @SerializedName("scroll_to") val scrollToPosition: String?,
    var isSelected: Boolean = false
)
