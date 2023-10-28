package com.doubtnutapp.dnr.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DnrSgMessageReward(
    @SerializedName("room_id")
    val roomId: String,

    @SerializedName("type")
    override val type: String
) : BaseDnrReward
