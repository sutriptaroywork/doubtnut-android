package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.util.*

@Keep
data class ApiUserSearchMetaData(
    @SerializedName("resource_reference") val resourceReference: String?,
    @SerializedName("player_type") val playerType: String?,
    @SerializedName("page") val page: String?,
    @SerializedName("live_at") val liveAt: String?,
    @SerializedName("video_time_in_mins") val liveLengthMin: Long?,
    @SerializedName("title") val liveClassTitle: String,
    @SerializedName("current_time") val currentTime: String?
)
