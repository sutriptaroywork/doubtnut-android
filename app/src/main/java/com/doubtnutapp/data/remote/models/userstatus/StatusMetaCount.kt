package com.doubtnutapp.data.remote.models.userstatus

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class StatusMetaCount(
    @SerializedName("type")val type: String,
    @SerializedName("count") val count: Int
)
