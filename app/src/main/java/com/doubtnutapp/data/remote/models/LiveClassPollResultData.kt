package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class LiveClassPollOptionsData(
    @SerializedName("color")
    val color: String?,
    @SerializedName("key")
    val optionKey: String?,
    @SerializedName("display")
    val progressDisplay: String?,
    @SerializedName("value")
    val progressValue: Double?
)
