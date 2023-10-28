package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class ContestDetails(
    @SerializedName("winners") val winners: String,
    @SerializedName("prize_money") val prizeMoney: Int,
    @SerializedName("rules") val rules: List<String>
)
