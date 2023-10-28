package com.doubtnutapp.dnr.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DnrVideoWatchReward(
    @SerializedName("view_id")
    val viewId: String,

    @SerializedName("question_id")
    val questionId: String,

    @SerializedName("duration")
    val duration: Long,

    @SerializedName("source")
    val source: String,

    @SerializedName("type")
    override val type: String
) : BaseDnrReward
