package com.doubtnutapp.data.store.model

import com.google.gson.annotations.SerializedName

data class ApiConvertCoins(
    @SerializedName("is_converted") val isConverted: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("converted_xp") val convertedXp: Int
)
