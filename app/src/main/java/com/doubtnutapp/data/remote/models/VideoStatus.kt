package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class VideoStatus(
    @SerializedName("state") val state: String?,
    @SerializedName("time_remaining") val timeRemaining: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("thumbnail") val thumbnail: String?
)
