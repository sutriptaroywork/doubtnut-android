package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiDelayNotification(
    @SerializedName("title") val title: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("image") val image: String?
)
