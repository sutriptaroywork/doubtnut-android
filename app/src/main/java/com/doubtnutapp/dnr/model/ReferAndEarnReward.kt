package com.doubtnutapp.dnr.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ReferAndEarnReward(
    @SerializedName("type")
    override val type: String
) : BaseDnrReward