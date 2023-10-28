package com.doubtnutapp.data.remote.models.reward

import com.google.gson.annotations.SerializedName

data class RewardTermsModel(
    @SerializedName("heading") val heading: String,
    @SerializedName("rules") val rules: List<String>,
)
