package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SubmitPost(
    @SerializedName("msg") val msg: String,
    @SerializedName("type") val type: String,
    @SerializedName("topic") val topic: String?,
    @SerializedName("attachment") val attachments: List<String>?,

    // live post data
    @SerializedName("live_status") var liveStatus: Int? = null,
    @SerializedName("is_paid") var isPaid: Boolean? = null,
    @SerializedName("stream_fee") var streamFee: Int? = null,
    @SerializedName("stream_date") var streamDate: String? = null,
    @SerializedName("stream_start_time") var streamStartTime: String? = null,
    @SerializedName("stream_end_time") var streamEndTime: String? = null
)
