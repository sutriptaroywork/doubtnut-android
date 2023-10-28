package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class ContestRules(
    @SerializedName("id") val id: String,
    @SerializedName("rules") val rules: String,
    @SerializedName("type") val type: String,
    @SerializedName("parameter") val parameter: String

)
