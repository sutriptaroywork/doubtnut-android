package com.doubtnutapp.doubtfeed2.reward.data.model

import com.google.gson.annotations.SerializedName

data class RewardTermsModel(
    @SerializedName("heading") val heading: String,
    @SerializedName("rules") val rules: List<String>,
)
